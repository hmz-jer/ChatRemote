import groovy.json.JsonSlurper

// Fonction pour splitter la chaîne en tableau
def splitStringIntoArray(String inputString) {
    return inputString.split(",")
}

// Fonction pour récupérer la valeur de l'attribut JSON
def getJsonValue(String jsonInput, String attributeName) {
    def jsonSlurper = new JsonSlurper()
    def object = jsonSlurper.parseText(jsonInput)
    
    return object[attributeName]
}

// Exemple d'utilisation
def inputString = "attribut1,attribut2,attribut3" // Liste des attributs séparés par des virgules
def jsonInput = '{"attribut1": "valeur1", "attribut2": "valeur2", "attribut3": "valeur3"}'

def attributesArray = splitStringIntoArray(inputString)

attributesArray.each { attributeName ->
    def value = getJsonValue(jsonInput, attributeName)
    println("Valeur de ${attributeName}: ${value}")
}
