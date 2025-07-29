  Voici les expressions régulières pour valider les UUID dans OpenAPI :Regex UUID standard (toutes versions)components:
  schemas:
    UUID:
      type: string
      format: uuid
      pattern: '^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$'Regex UUID version 4 spécifiquementcomponents:
  schemas:
    UUIDv4:
      type: string
      format: uuid
      pattern: '^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$'Regex UUID insensible à la cassecomponents:
  schemas:
    UUID:
      type: string
      format: uuid
      pattern: '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$'Regex UUID avec tirets optionnelscomponents:
  schemas:
    UUIDFlexible:
      type: string
      pattern: '^[0-9a-f]{8}-?[0-9a-f]{4}-?[1-5][0-9a-f]{3}-?[89ab][0-9a-f]{3}-?[0-9a-f]{12}$'Exemple complet dans OpenAPIopenapi: 3.0.0
info:
  title: API avec UUID validé
  version: 1.0.0

components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: string
          format: uuid
          pattern: '^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$'
          example: "550e8400-e29b-41d4-a716-446655440000"
          description: "UUID v1-v5 en minuscules"

paths:
  /users/{userId}:
    get:
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: string
            format: uuid
            pattern: '^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$'Décomposition de la regex^[0-9a-f]{8}     # 8 caractères hexa (partie 1)
-                # tiret obligatoire
[0-9a-f]{4}      # 4 caractères hexa (partie 2)
-                # tiret obligatoire
[1-5]            # version UUID (1 à 5)
[0-9a-f]{3}      # 3 caractères hexa
-                # tiret obligatoire
[89ab]           # variante UUID (8, 9, a, ou b)
[0-9a-f]{3}      # 3 caractères hexa
-                # tiret obligatoire
[0-9a-f]{12}$    # 12 caractères hexa (partie finale)Variantes selon le besoinUUID v4 uniquement :^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$UUID sans contrainte de version :^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$UUID avec majuscules autorisées :^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$La regex recommandée pour OpenAPI est celle qui valide les versions 1-5 avec la variante standard, en minuscules.
