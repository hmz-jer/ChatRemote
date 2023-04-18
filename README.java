European Payment Initiative (EPI) API Gateway

Ce projet de API Gateway est destiné à supporter l'initiative européenne de paiement (EPI), une initiative visant à créer un système de paiement unifié en Europe. Cette API Gateway est construite en utilisant la plate-forme Axway.
Getting Started

Ces instructions vous aideront à démarrer le projet sur votre machine locale pour des fins de développement et de test.
Prerequisites

Avant de démarrer, vous devez installer les prérequis suivants :

    Node.js version 10 ou supérieure
    Axway API Gateway version 7.6.2 ou supérieure
Installing

    Clonez le repository sur votre machine locale :

L'API Gateway sera lancée sur le port 3000.
Usage

Cette API Gateway fournit un point d'entrée pour accéder à l'API EPI. Pour utiliser cette API Gateway, il suffit de faire une requête HTTP sur le port configuré avec le chemin d'accès à l'API EPI. Par exemple :

bash

http://localhost:3000/v1/payments

Cette requête sera transmise à l'API EPI à l'URL configurée dans la variable API_URL.
