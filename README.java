 // Fonction pour valider un timestamp dans une plage de temps
// timestamp : timestamp en millisecondes (String)
// seuilMs : marge de temps en millisecondes (String)
def validateTimestamp(String timestampStr, String seuilStr) {
    try {
        long timestamp = timestampStr.toLong()
        long seuilMs = seuilStr.toLong()
        
        long now = System.currentTimeMillis()
        long minTime = now - seuilMs
        long maxTime = now + seuilMs
        
        // Condition: (now - seuil) < timestamp < (now + seuil)
        if (timestamp > minTime && timestamp < maxTime) {
            return "Ok"
        } else {
            return "Ko"
        }
    } catch (NumberFormatException e) {
        return "Erreur: Paramètres invalides (${e.message})"
    }
}

// Version avec debug pour voir les valeurs
def validateTimestampDebug(String timestampStr, String seuilStr) {
    try {
        long timestamp = timestampStr.toLong()
        long seuilMs = seuilStr.toLong()
        
        long now = System.currentTimeMillis()
        long minTime = now - seuilMs
        long maxTime = now + seuilMs
        
        println "Now: ${now} (${new Date(now)})"
        println "Timestamp reçu: '${timestampStr}' -> ${timestamp} (${new Date(timestamp)})"
        println "Seuil reçu: '${seuilStr}' -> ${seuilMs} ms"
        println "Plage valide: [${minTime}, ${maxTime}]"
        
        if (timestamp > minTime && timestamp < maxTime) {
            return "Ok"
        } else {
            return "Ko"
        }
    } catch (NumberFormatException e) {
        return "Erreur: Paramètres invalides (${e.message})"
    }
}

// Exemples d'utilisation
long seuilMs = 5000 // 5000 millisecondes = 5 secondes
// Exemples d'utilisation avec des String
long maintenant = System.currentTimeMillis()

// Test avec un timestamp valide (dans la plage de ±5 secondes)
String timestampValide = String.valueOf(maintenant + 2000) // +2000 ms = +2 secondes
String seuil = "5000" // 5000 ms = 5 secondes
println "Test timestamp valide: ${validateTimestamp(timestampValide, seuil)}"

// Test avec un timestamp invalide (hors plage)
String timestampInvalide = String.valueOf(maintenant + 10000) // +10000 ms = +10 secondes
println "Test timestamp invalide: ${validateTimestamp(timestampInvalide, seuil)}"

// Test avec timestamp passé invalide
String timestampPasse = String.valueOf(maintenant - 8000) // -8000 ms = -8 secondes
println "Test timestamp passé: ${validateTimestamp(timestampPasse, seuil)}"

// Test avec des paramètres invalides
println "Test paramètre invalide: ${validateTimestamp('abc', seuil)}"
println "Test seuil invalide: ${validateTimestamp(timestampValide, 'xyz')}"

// Test avec debug pour voir les détails
println "\n--- Test avec debug ---"
println "Résultat: ${validateTimestampDebug(timestampValide, seuil)}"
