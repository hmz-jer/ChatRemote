 // ========================================
// build.gradle (racine du projet)
// ========================================

plugins {
    id 'java'
    id 'application'
    id 'jacoco' // pour la couverture de code
}

group = 'com.example'
version = '1.0.0'
description = 'Mon projet Gradle complet'

// Configuration Java
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// Repositories
repositories {
    mavenCentral()
}

// Dépendances
dependencies {
    // Dépendances principales
    implementation 'org.apache.commons:commons-lang3:3.12.0'
    implementation 'com.google.guava:guava:31.1-jre'
    implementation 'org.slf4j:slf4j-api:1.7.36'
    implementation 'ch.qos.logback:logback-classic:1.2.12'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.14.2'
    
    // Dépendances de test
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:4.6.1'
    testImplementation 'org.assertj:assertj-core:3.23.1'
}

// Configuration de l'application
application {
    mainClass = 'com.example.Main'
}

// Tâches personnalisées
task hello {
    doLast {
        println 'Hello, World!'
    }
}

// Configuration des tests
test {
    useJUnit()
    testLogging {
        events "passed", "skipped", "failed"
    }
}

// Configuration Jacoco pour la couverture
jacoco {
    toolVersion = "0.8.7"
}

jacocoTestReport {
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir('jacocoHtml')
    }
}

// ========================================
// settings.gradle
// ========================================

rootProject.name = 'mon-projet-gradle'

// Pour un projet multi-modules (optionnel)
// include 'module-core'
// include 'module-web'

// ========================================
// gradle.properties
// ========================================

# Configuration du projet
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true

# Propriétés personnalisées
app.name=MonApplication
app.version=1.0.0

// ========================================
// src/main/java/com/example/Main.java
// ========================================

package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.service.CalculatorService;
import com.example.model.Person;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("Démarrage de l'application");
        
        // Utilisation du service
        CalculatorService calculator = new CalculatorService();
        int result = calculator.add(5, 3);
        
        // Création d'une personne
        Person person = new Person("Jean", "Dupont", 30);
        
        System.out.println("Résultat du calcul: " + result);
        System.out.println("Personne créée: " + person);
        
        logger.info("Application terminée avec succès");
    }
}

// ========================================
// src/main/java/com/example/model/Person.java
// ========================================

package com.example.model;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class Person {
    private String firstName;
    private String lastName;
    private int age;
    
    public Person(String firstName, String lastName, int age) {
        this.firstName = StringUtils.capitalize(firstName);
        this.lastName = StringUtils.upperCase(lastName);
        this.age = age;
    }
    
    // Getters et Setters
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isAdult() {
        return age >= 18;
    }
    
    @Override
    public String toString() {
        return String.format("Person{firstName='%s', lastName='%s', age=%d}", 
                           firstName, lastName, age);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return age == person.age &&
               Objects.equals(firstName, person.firstName) &&
               Objects.equals(lastName, person.lastName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, age);
    }
}

// ========================================
// src/main/java/com/example/service/CalculatorService.java
// ========================================

package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalculatorService {
    private static final Logger logger = LoggerFactory.getLogger(CalculatorService.class);
    
    public int add(int a, int b) {
        logger.debug("Addition: {} + {}", a, b);
        return a + b;
    }
    
    public int subtract(int a, int b) {
        logger.debug("Soustraction: {} - {}", a, b);
        return a - b;
    }
    
    public int multiply(int a, int b) {
        logger.debug("Multiplication: {} * {}", a, b);
        return a * b;
    }
    
    public double divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Division par zéro impossible");
        }
        logger.debug("Division: {} / {}", a, b);
        return (double) a / b;
    }
}

// ========================================
// src/main/java/com/example/util/StringUtils.java
// ========================================

package com.example.util;

import com.google.common.base.Strings;

public class StringUtils {
    
    public static boolean isEmpty(String str) {
        return Strings.isNullOrEmpty(str);
    }
    
    public static String reverse(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return new StringBuilder(str).reverse().toString();
    }
    
    public static boolean isPalindrome(String str) {
        if (isEmpty(str)) {
            return false;
        }
        String cleaned = str.toLowerCase().replaceAll("\\s+", "");
        return cleaned.equals(reverse(cleaned));
    }
}

// ========================================
// src/test/java/com/example/service/CalculatorServiceTest.java
// ========================================

package com.example.service;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CalculatorServiceTest {
    private CalculatorService calculator;
    
    @Before
    public void setUp() {
        calculator = new CalculatorService();
    }
    
    @Test
    public void testAdd() {
        assertEquals(8, calculator.add(5, 3));
        assertEquals(0, calculator.add(-5, 5));
        assertEquals(-8, calculator.add(-3, -5));
    }
    
    @Test
    public void testSubtract() {
        assertEquals(2, calculator.subtract(5, 3));
        assertEquals(-10, calculator.subtract(-5, 5));
        assertEquals(2, calculator.subtract(-3, -5));
    }
    
    @Test
    public void testMultiply() {
        assertEquals(15, calculator.multiply(5, 3));
        assertEquals(-25, calculator.multiply(-5, 5));
        assertEquals(15, calculator.multiply(-3, -5));
    }
    
    @Test
    public void testDivide() {
        assertEquals(1.666, calculator.divide(5, 3), 0.001);
        assertEquals(-1.0, calculator.divide(-5, 5), 0.001);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDivideByZero() {
        calculator.divide(5, 0);
    }
}

// ========================================
// src/test/java/com/example/model/PersonTest.java
// ========================================

package com.example.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class PersonTest {
    
    @Test
    public void testPersonCreation() {
        Person person = new Person("jean", "dupont", 25);
        assertEquals("Jean", person.getFirstName());
        assertEquals("DUPONT", person.getLastName());
        assertEquals(25, person.getAge());
    }
    
    @Test
    public void testFullName() {
        Person person = new Person("Marie", "Martin", 30);
        assertEquals("Marie MARTIN", person.getFullName());
    }
    
    @Test
    public void testIsAdult() {
        Person adult = new Person("Pierre", "Durand", 25);
        Person minor = new Person("Sophie", "Petit", 16);
        
        assertTrue(adult.isAdult());
        assertFalse(minor.isAdult());
    }
    
    @Test
    public void testEquals() {
        Person person1 = new Person("Jean", "Dupont", 25);
        Person person2 = new Person("Jean", "Dupont", 25);
        Person person3 = new Person("Marie", "Dupont", 25);
        
        assertEquals(person1, person2);
        assertNotEquals(person1, person3);
    }
}

// ========================================
// src/test/java/com/example/util/StringUtilsTest.java
// ========================================

package com.example.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringUtilsTest {
    
    @Test
    public void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty("hello"));
    }
    
    @Test
    public void testReverse() {
        assertEquals("olleh", StringUtils.reverse("hello"));
        assertEquals("", StringUtils.reverse(""));
        assertNull(StringUtils.reverse(null));
    }
    
    @Test
    public void testIsPalindrome() {
        assertTrue(StringUtils.isPalindrome("radar"));
        assertTrue(StringUtils.isPalindrome("A man a plan a canal Panama"));
        assertFalse(StringUtils.isPalindrome("hello"));
        assertFalse(StringUtils.isPalindrome(""));
    }
}

// ========================================
// src/main/resources/logback.xml
// ========================================

<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="FILE" />
    </root>
    
    <logger name="com.example" level="DEBUG" />
</configuration>

// ========================================
// src/main/resources/application.properties
// ========================================

# Configuration de l'application
app.name=Mon Application Gradle
app.version=1.0.0
app.description=Application d'exemple avec Gradle

# Configuration de base de données (exemple)
db.url=jdbc:h2:mem:testdb
db.username=sa
db.password=

# Configuration de logging
logging.level.root=INFO
logging.level.com.example=DEBUG

// ========================================
// gradlew (script Unix)
// ========================================

#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if [ "$darwin" = "true" ]; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# For Cygwin or MSYS, switch paths to Windows format before running java
if [ "$cygwin" = "true" -o "$msys" = "true" ] ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`
    JAVACMD=`cygpath --unix "$JAVACMD"`

    # We build the pattern for arguments to be converted via cygpath
    ROOTDIRSRAW=`find -L / -maxdepth 1 -mindepth 1 -type d 2>/dev/null`
    SEP=""
    for dir in $ROOTDIRSRAW ; do
        ROOTDIRS="$ROOTDIRS$SEP$dir"
        SEP="|"
    done
    OURCYGPATTERN="(^($ROOTDIRS))"
    # Add a user-defined pattern to the cygpath arguments
    if [ "$GRADLE_CYGPATTERN" != "" ] ; then
        OURCYGPATTERN="$OURCYGPATTERN|($GRADLE_CYGPATTERN)"
    fi
    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    i=0
    for arg in "$@" ; do
        CHECK=`echo "$arg"|egrep -c "$OURCYGPATTERN" -`
        CHECK2=`echo "$arg"|egrep -c "^-"`                                 ### Determine if an option

        if [ $CHECK -ne 0 ] && [ $CHECK2 -eq 0 ] ; then                    ### Added a condition
            eval `echo args$i`=`cygpath --path --ignore --mixed "$arg"`
        else
            eval `echo args$i`="\"$arg\""
        fi
        i=$((i+1))
    done
    case $i in
        (0) set -- ;;
        (1) set -- "$args0" ;;
        (2) set -- "$args0" "$args1" ;;
        (3) set -- "$args0" "$args1" "$args2" ;;
        (4) set -- "$args0" "$args1" "$args2" "$args3" ;;
        (5) set -- "$args0" "$args1" "$args2" "$args3" "$args4" ;;
        (6) set -- "$args0" "$args1" "$args2" "$args3" "$args4" "$args5" ;;
        (7) set -- "$args0" "$args1" "$args2" "$args3" "$args4" "$args5" "$args6" ;;
        (8) set -- "$args0" "$args1" "$args2" "$args3" "$args4" "$args5" "$args6" "$args7" ;;
        (9) set -- "$args0" "$args1" "$args2" "$args3" "$args4" "$args5" "$args6" "$args7" "$args8" ;;
    esac
fi

# Escape application args
save () {
    for i do printf %s\\n "$i" | sed "s/'/'\\\\''/g;1s/^/'/;\$s/\$/' \\\\/" ; done
    echo " "
}
APP_ARGS=$(save "$@")

# Collect all arguments for the java command
set -- $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \"-Dorg.gradle.appname=$APP_BASE_NAME\" -classpath \"$CLASSPATH\" org.gradle.wrapper.GradleWrapperMain "$APP_ARGS"

exec "$JAVACMD" "$@"

// ========================================
// README.md
// ========================================

# Mon Projet Gradle

Un projet Java complet utilisant Gradle comme système de build.

## Structure du projet

```
mon-projet-gradle/
├── build.gradle                 # Configuration Gradle principale
├── settings.gradle              # Configuration des modules
├── gradle.properties            # Propriétés du projet
├── gradlew                      # Script Gradle Wrapper (Unix)
├── gradlew.bat                  # Script Gradle Wrapper (Windows)
├── gradle/
│   └── wrapper/                 # Fichiers du Gradle Wrapper
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       ├── Main.java
│   │   │       ├── model/
│   │   │       ├── service/
│   │   │       └── util/
│   │   └── resources/
│   │       ├── logback.xml
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/example/
└── README.md
```

## Commandes Gradle

### Commandes de base
```bash
./gradlew build          # Compile, teste et package le projet
./gradlew clean          # Nettoie les fichiers générés
./gradlew run            # Exécute l'application
./gradlew test           # Lance les tests
./gradlew check          # Vérifie le code (tests + qualité)
```

### Commandes avancées
```bash
./gradlew dependencies   # Affiche les dépendances
./gradlew tasks          # Liste toutes les tâches disponibles
./gradlew jacocoTestReport  # Génère le rapport de couverture
./gradlew hello          # Tâche personnalisée
```

## Fonctionnalités

- **Java 11** : Version moderne de Java
- **Logging** : SLF4J + Logback pour les logs
- **Tests** : JUnit 4 avec Mockito et AssertJ
- **Couverture de code** : Jacoco
- **Utilitaires** : Apache Commons Lang et Google Guava
- **JSON** : Jackson pour la sérialisation

## Utilisation

1. Cloner le projet
2. Exécuter `./gradlew build` pour construire
3. Exécuter `./gradlew run` pour lancer l'application

## Tests

Les tests sont organisés par package et utilisent :
- JUnit 4 pour les tests unitaires
- Mockito pour les mocks
- AssertJ pour des assertions plus lisibles

Lancer les tests : `./gradlew test`

## Configuration

- `gradle.properties` : Configuration générale
- `logback.xml` : Configuration des logs
- `application.properties` : Propriétés de l'application
