  // Fonction pour valider un timestamp dans une plage de temps
def validateTimestamp(long timestamp, long seuil) {
    long now = System.currentTimeMillis()
    long minTime = now - seuil
    long maxTime = now + seuil
    
    if (timestamp > minTime && timestamp < maxTime) {
        return "Ok"
    } else {
        return "Ko"
    }
}

// Version alternative avec Date pour plus de lisibilité
def validateTimestampWithDate(long timestamp, long seuil) {
    Date now = new Date()
    Date minTime = new Date(now.time - seuil)
    Date maxTime = new Date(now.time + seuil)
    Date timestampDate = new Date(timestamp)
    
    println "Timestamp à vérifier: ${timestampDate}"
    println "Plage acceptée: ${minTime} à ${maxTime}"
    
    if (timestamp > minTime.time && timestamp < maxTime.time) {
        return "Ok"
    } else {
        return "Ko"
    }
}

// Exemples d'utilisation
long seuil = 5000 // 5 secondes en millisecondes
long maintenant = System.currentTimeMillis()

// Test avec un timestamp valide (dans la plage)
long timestampValide = maintenant + 2000 // +2 secondes
println "Test timestamp valide: ${validateTimestamp(timestampValide, seuil)}"

// Test avec un timestamp invalide (hors plage)
long timestampInvalide = maintenant + 10000 // +10 secondes
println "Test timestamp invalide: ${validateTimestamp(timestampInvalide, seuil)}"

// Test avec timestamp passé invalide
long timestampPasse = maintenant - 10000 // -10 secondes
println "Test timestamp passé: ${validateTimestamp(timestampPasse, seuil)}"

// Version avec affichage détaillé
println "\n--- Test détaillé ---"
println validateTimestampWithDate(timestampValide, seuil)
println validateTimestampWithDate(timestampInvalide, seuil)
