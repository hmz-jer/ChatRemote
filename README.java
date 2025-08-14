openapi: 3.0.3
info:
  title: Healthcheck API
  description: API de vérification de santé avec redirection vers le service backend
  version: 1.0.0
  contact:
    name: Support API
    email: support@exemple.com

servers:
  - url: https://localhost:8443
    description: Serveur API Gateway Axway

paths:
  /healthcheck:
    get:
      summary: Vérification de santé du service
      description: Endpoint de healthcheck qui redirige vers le service backend
      operationId: getHealthcheck
      tags:
        - Health
      responses:
        '200':
          description: Service en bonne santé
          content:
            application/json:
              schema:
                type: object
                properties:
                  status:
                    type: string
                    example: "OK"
                  timestamp:
                    type: string
                    format: date-time
                    example: "2024-03-14T10:30:00Z"
                  service:
                    type: string
                    example: "application-backend"
              examples:
                success:
                  summary: Réponse de succès
                  value:
                    status: "OK"
                    timestamp: "2024-03-14T10:30:00Z"
                    service: "application-backend"
        '503':
          description: Service indisponible
          content:
            application/json:
              schema:
                type: object
                properties:
                  status:
                    type: string
                    example: "ERROR"
                  message:
                    type: string
                    example: "Service backend indisponible"
                  timestamp:
                    type: string
                    format: date-time
                    example: "2024-03-14T10:30:00Z"
        '500':
          description: Erreur interne du serveur
          content:
            application/json:
              schema:
                type: object
                properties:
                  status:
                    type: string
                    example: "ERROR"
                  message:
                    type: string
                    example: "Erreur interne"

# Configuration spécifique pour Axway API Gateway
x-axway-config:
  backend:
    url: "http://localhost:8080"
    timeout: 5000
    retries: 2
  routing:
    - path: "/healthcheck"
      method: "GET"
      backend_path: "/"
      backend_method: "GET"
  policies:
    - name: "routing"
      configuration:
        target_url: "http://localhost:8080"
        target_path: "/healthcheck"
        preserve_host: false
        timeout: 5000
    - name: "cors"
      configuration:
        allow_origins: ["*"]
        allow_methods: ["GET"]
        allow_headers: ["Content-Type", "Authorization"]

tags:
  - name: Health
    description: Endpoints de vérification de santé

components:
  schemas:
    HealthResponse:
      type: object
      properties:
        status:
          type: string
          enum: ["OK", "ERROR"]
        timestamp:
          type: string
          format: date-time
        service:
          type: string
        message:
          type: string
      required:
        - status
        - timestamp
