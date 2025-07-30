  Voici comment écrire un test Postman pour vérifier ces conditions :Dans l'onglet Tests de votre requête Postman, ajoutez ce code JavaScript :// Test pour vérifier que le code de statut est 400
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

// Test pour vérifier le contenu du corps de la réponse
pm.test("Response contains type and code format error", function () {
    // Parse le JSON de la réponse
    const responseJson = pm.response.json();
    
    // Vérifie que la propriété 'type' existe
    pm.expect(responseJson).to.have.property('type');
    
    // Vérifie que la propriété 'code' existe et a la valeur "format error"
    pm.expect(responseJson).to.have.property('code');
    pm.expect(responseJson.code).to.eql("format error");
});Ou si vous voulez un test plus compact :pm.test("Status code is 400 and response format is correct", function () {
    // Vérification du statut
    pm.response.to.have.status(400);
    
    // Parse et vérification du JSON
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('type');
    pm.expect(responseJson.code).to.eql("format error");
});Explications :pm.test() : Crée un test nommépm.response.to.have.status(400) : Vérifie le code de statut HTTPpm.response.json() : Parse la réponse JSONpm.expect().to.have.property() : Vérifie qu'une propriété existepm.expect().to.eql() : Vérifie qu'une valeur est égale à une valeur attendueCes tests apparaîtront dans l'onglet Test Results après l'exécution de votre requête, avec un indicateur vert (✓) si ils passent ou rouge (✗) s'ils échouent.
