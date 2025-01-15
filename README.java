 
/ Configuration OpenAPI
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Kafka Request-Reply")
                        .version("1.0.0")
                        .description("API de traitement asynchrone des requêtes via Kafka")
                        .contact(new Contact()
                                .name("Votre Nom")
                                .email("contact@example.com")));
    }
