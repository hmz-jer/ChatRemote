{
	"info": {
		"_postman_id": "a123456-7890-1234-abcd-1234567890ab",
		"name": "API Test Collection",
		"description": "Collection pour tester l'API avec 4 scénarios POST sur le même endpoint",
		"schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
	},
	"item": [
		{
			"name": "Scénario 1 - Création d'utilisateur",
			"item": [
				{
					"name": "Créer Utilisateur",
					"event": [
						{
							"listen": "test",
							"script": {
								"exec": [
									"pm.test(\"Statut est 200 ou 201\", function () {",
									"    pm.expect(pm.response.code).to.be.oneOf([200, 201]);",
									"});",
									"",
									"var jsonData = pm.response.json();",
									"",
									"pm.test(\"La réponse contient un ID utilisateur\", function () {",
									"    pm.expect(jsonData).to.have.property('userId');",
									"});",
									"",
									"pm.test(\"Le message de confirmation est correct\", function () {",
									"    pm.expect(jsonData).to.have.property('message');",
									"    pm.expect(jsonData.message).to.include('utilisateur créé');",
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
							"raw": "{\n    \"action\": \"createUser\",\n    \"name\": \"{{userName}}\",\n    \"email\": \"{{userEmail}}\",\n    \"role\": \"user\"\n}"
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
						"description": "Créer un nouvel utilisateur"
					},
					"response": []
				}
			]
		},
		{
			"name": "Scénario 2 - Mise à jour de produit",
			"item": [
				{
					"name": "Mettre à jour produit",
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
