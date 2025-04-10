
					"name": "Mettre à jour produit",
					"event": [
						{
							"listen": "test",
							"script": {
								"exec": [
									"pm.test(\"Statut est 200\", function () {",
									"    pm.response.to.have.status(200);",
									"});",
			Voici le mail avec une seule commande Newman simplifiée :

---

**Objet :** Tests API - Collection Postman avec commande Newman

Bonjour,

Je vous transmets notre collection Postman pour les tests de l'API, comprenant 4 scénarios de test. Vous trouverez en pièces jointes :

1. `APITestCollection.json` - La collection avec les 4 scénarios de test
2. `APITestEnvironment.json` - Le fichier d'environnement contenant les variables (URL de l'API et X-SSL-Cert)

Pour exécuter ces tests en ligne de commande, voici la commande Newman à utiliser :

```bash
newman run APITestCollection.json --environment APITestEnvironment.json
```

Les scénarios inclus sont tous des requêtes POST sur le même endpoint, mais avec des corps (JSON) différents pour tester les différentes fonctionnalités de l'API.

N'hésitez pas à me contacter si vous avez des questions.

Cordialement,
[Votre nom]

---						"",
									"var jsonData = pm.response.json();",
									"",
									"pm.test(\"La mise à jour est confirmée\", function () {",
									"    pm.expect(jsonData).to.have.property('success');",
									"    pm.expect(jsonData.success).to.be.true;",
									"});",
									"",
									"pm.test(\"Le produit a été mis à jour\", function () {",
									"    pm.expect(jsonData).to.have.property('data');",
									"    pm.expect(jsonData.data).to.have.property('productId');",
									"    pm.expect(jsonData.data).to.have.property('updatedFields');",
									"});"
								],
								"type": "text/javascript"
							}
						}
					],
					"request": {
						"method": "POST",
						"header": [
							{
								"key": "Content-Type",
								"value": "application/json"
							}
						],
						"body": {
							"mode": "raw",
							"raw": "{\n    \"action\": \"updateProduct\",\n    \"productId\": \"{{productId}}\",\n    \"name\": \"{{productName}}\",\n    \"price\": {{productPrice}},\n    \"category\": \"{{productCategory}}\"\n}"
						},
						"url": {
							"raw": "{{baseUrl}}/api/endpoint",
							"host": [
								"{{baseUrl}}"
							],
							"path": [
								"api",
								"endpoint"
							]
						},
						"description": "Mettre à jour un produit existant"
					},
					"response": []
				}
			]
		},
		{
			"name": "Scénario 3 - Traitement de commande",
			"item": [
				{
					"name": "Traiter commande",
					"event": [
						{
							"listen": "test",
							"script": {
								"exec": [
									"pm.test(\"Statut est 200\", function () {",
									"    pm.response.to.have.status(200);",
									"});",
									"",
									"var jsonData = pm.response.json();",
									"",
									"pm.test(\"La commande est validée\", function () {",
									"    pm.expect(jsonData).to.have.property('orderStatus');",
									"    pm.expect(jsonData.orderStatus).to.equal('processed');",
									"});",
									"",
									"pm.test(\"Le numéro de référence est généré\", function () {",
									"    pm.expect(jsonData).to.have.property('referenceNumber');",
									"    pm.expect(jsonData.referenceNumber).to.be.a('string');",
									"});"
								],
								"type": "text/javascript"
							}
						}
					],
					"request": {
						"method": "POST",
						"header": [
							{
								"key": "Content-Type",
								"value": "application/json"
							}
						],
						"body": {
							"mode": "raw",
							"raw": "{\n    \"action\": \"processOrder\",\n    \"orderId\": \"{{orderId}}\",\n    \"items\": [\n        {\n            \"productId\": \"{{productId}}\",\n            \"quantity\": {{orderQuantity}}\n        }\n    ],\n    \"shippingAddress\": \"{{shippingAddress}}\",\n    \"paymentMethod\": \"{{paymentMethod}}\"\n}"
						},
						"url": {
							"raw": "{{baseUrl}}/api/endpoint",
							"host": [
								"{{baseUrl}}"
							],
							"path": [
								"api",
								"endpoint"
							]
						},
						"description": "Traiter une nouvelle commande"
					},
					"response": []
				}
			]
		},
		{
			"name": "Scénario 4 - Génération de rapport",
			"item": [
				{
					"name": "Générer rapport",
					"event": [
						{
							"listen": "test",
							"script": {
								"exec": [
									"pm.test(\"Statut est 200\", function () {",
									"    pm.response.to.have.status(200);",
									"});",
									"",
									"var jsonData = pm.response.json();",
									"",
									"pm.test(\"Le rapport est généré\", function () {",
									"    pm.expect(jsonData).to.have.property('reportGenerated');",
									"    pm.expect(jsonData.reportGenerated).to.be.true;",
									"});",
									"",
									"pm.test(\"Le rapport contient des données\", function () {",
									"    pm.expect(jsonData).to.have.property('reportData');",
									"    pm.expect(jsonData.reportData).to.be.an('object');",
									"});",
									"",
									"pm.test(\"La période du rapport est correcte\", function () {",
									"    pm.expect(jsonData.reportData).to.have.property('period');",
									"    pm.expect(jsonData.reportData.period).to.equal(pm.environment.get('reportPeriod'));",
									"});"
								],
								"type": "text/javascript"
							}
						}
					],
					"request": {
						"method": "POST",
						"header": [
							{
								"key": "Content-Type",
								"value": "application/json"
							}
						],
						"body": {
							"mode": "raw",
							"raw": "{\n    \"action\": \"generateReport\",\n    \"reportType\": \"{{reportType}}\",\n    \"period\": \"{{reportPeriod}}\",\n    \"format\": \"{{reportFormat}}\",\n    \"filters\": {\n        \"category\": \"{{filterCategory}}\",\n        \"minValue\": {{filterMinValue}}\n    }\n}"
						},
						"url": {
							"raw": "{{baseUrl}}/api/endpoint",
							"host": [
								"{{baseUrl}}"
							],
							"path": [
								"api",
								"endpoint"
							]
						},
						"description": "Générer un rapport personnalisé"
					},
					"response": []
				}
			]
		}
	],
	"event": [
		{
			"listen": "prerequest",
			"script": {
				"type": "text/javascript",
				"exec": [
					""
				]
			}
		},
		{
			"listen": "test",
			"script": {
				"type": "text/javascript",
				"exec": [
					""
				]
			}
		}
	]
}
