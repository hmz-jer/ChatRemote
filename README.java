 // build.gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.1'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'com.vaadin' version '24.5.11'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

ext {
    set('vaadinVersion', "24.5.11")
}

dependencies {
    implementation 'com.vaadin:vaadin-spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

dependencyManagement {
    imports {
        mavenBom "com.vaadin:vaadin-bom:${vaadinVersion}"
    }
}

tasks.named('test') {
    useJUnitPlatform()
}

vaadin {
    optimizeBundle = false
}

// src/main/java/com/example/Application.java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// src/main/java/com/example/data/CreditCard.java
package com.example.data;

public record CreditCard(
    Long id,
    String cardType,
    String bankCode,
    String imageUrl
) {}

// src/main/java/com/example/service/CreditCardService.java
package com.example.service;

import com.example.data.CreditCard;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CreditCardService {
    public List<CreditCard> getAllCards() {
        return List.of(
            new CreditCard(1L, "Visa Premium", "BNP42", "/images/visa-premium.png"),
            new CreditCard(2L, "Mastercard Gold", "SG123", "/images/mastercard-gold.png"),
            new CreditCard(3L, "American Express", "CA789", "/images/amex.png")
        );
    }
}

// src/main/java/com/example/views/MainLayout.java
package com.example.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout implements RouterLayout {

    public MainLayout() {
        createHeader();
        getElement().getStyle().set("--lumo-primary-color", "#2196F3");
    }

    private void createHeader() {
        var logo = new H1("Gestion des Cartes");
        logo.addClassNames(
            LumoUtility.Margin.MEDIUM,
            LumoUtility.TextColor.PRIMARY,
            LumoUtility.FontSize.LARGE
        );

        var header = new Header(logo);
        header.addClassNames(
            LumoUtility.Background.PRIMARY,
            LumoUtility.BoxShadow.SMALL,
            LumoUtility.Display.FLEX,
            LumoUtility.FlexDirection.ROW,
            LumoUtility.Width.FULL,
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Padding.MEDIUM
        );
        header.getStyle().set("color", "white");

        addToNavbar(header);
    }
}

// src/main/java/com/example/views/MainView.java
package com.example.views;

import com.example.data.CreditCard;
import com.example.service.CreditCardService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "cards", layout = MainLayout.class)
@PageTitle("Cartes Bancaires | Gestion")
public class MainView extends VerticalLayout {
    private final Grid<CreditCard> grid;

    public MainView(CreditCardService service) {
        this.grid = new Grid<>(CreditCard.class, false);

        addClassName("cards-view");
        setPadding(true);
        setSpacing(true);

        configureGrid(service);
        add(grid);
    }

    private void configureGrid(CreditCardService service) {
        grid.addClassNames(
            LumoUtility.Border.ALL,
            LumoUtility.BorderColor.CONTRAST_10,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.BoxShadow.SMALL,
            LumoUtility.Background.CONTRAST_5
        );

        grid.addThemeVariants(
            GridVariant.LUMO_NO_BORDER,
            GridVariant.LUMO_ROW_STRIPES,
            GridVariant.LUMO_COLUMN_BORDERS
        );

        // Configuration des colonnes en utilisant la syntaxe moderne
        grid.addColumn(CreditCard::id)
            .setHeader("ID")
            .setAutoWidth(true)
            .setFlexGrow(0);

        grid.addColumn(CreditCard::cardType)
            .setHeader("Type de Carte")
            .setAutoWidth(true);

        grid.addColumn(CreditCard::bankCode)
            .setHeader("Code Banque")
            .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(card -> {
            var image = new Image(card.imageUrl(), "Card image");
            image.setHeight("40px");
            image.addClassNames(LumoUtility.BorderRadius.MEDIUM);
            return image;
        }))
            .setHeader("Image")
            .setAutoWidth(true);

        grid.setItems(service.getAllCards());
        grid.setAllRowsVisible(true);
        grid.setWidth("100%");
    }
}

// src/main/resources/application.properties
server.port=8080
vaadin.whitelisted-packages=com.example
