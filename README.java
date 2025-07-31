 import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.regex.Pattern

// Pattern regex pour valider le format ISO8601 complet
def ISO8601_PATTERN = /^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\.[0-9]{1-9})?(([\+\-][0-9]{2}:[0-9]{2})|Z)$/

// Fonction pour valider un timestamp dans une plage de temps (tout en UTC)
// timestampStr : timestamp au format ISO8601 complet (String)
// seuilStr : marge de temps en millisecondes (String)
def validateTimestamp(String timestampStr, String seuilStr) {
    try {
        long seuilMs = seuilStr.toLong()
        
        // Vérifier que le format correspond au pattern ISO8601
        if (!timestampStr.matches(ISO8601_PATTERN)) {
            return "Erreur: Format timestamp ne correspond pas au pattern ISO8601"
        }
        
        // Parser le timestamp ISO8601 avec gestion multiple formats
        long timestampUtc = parseISO8601ToUTC(timestampStr)
        
        // Temps actuel en UTC (System.currentTimeMillis() est déjà en UTC)
        long nowUtc = System.currentTimeMillis()
        long minTimeUtc = nowUtc - seuilMs
        long maxTimeUtc = nowUtc + seuilMs
        
        // Condition: (nowUtc - seuil) < timestampUtc < (nowUtc + seuil)
        if (timestampUtc > minTimeUtc && timestampUtc < maxTimeUtc) {
            return "Ok"
        } else {
            return "Ko"
        }
    } catch (NumberFormatException e) {
        return "Erreur: Seuil invalide (${e.message})"
    } catch (Exception e) {
        return "Erreur: ${e.message}"
    }
}

// Fonction pour parser tous les formats ISO8601 vers UTC
def parseISO8601ToUTC(String timestampStr) {
    try {
        // Formats possibles à tester dans l'ordre
        def formats = [
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",  // 2025-07-30T14:30:45.123+02:00
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",   // 2025-07-30T14:30:45.123Z
            "yyyy-MM-dd'T'HH:mm:ssXXX",       // 2025-07-30T14:30:45+02:00
            "yyyy-MM-dd'T'HH:mm:ss'Z'",       // 2025-07-30T14:30:45Z
            "yyyy-MM-dd'T'HH:mm:ss.S'Z'",     // 2025-07-30T14:30:45.1Z
            "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",    // 2025-07-30T14:30:45.12Z
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'" // 2025-07-30T14:30:45.1234567Z
        ]
        
        Exception lastException = null
        
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format)
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
                Date parsedDate = sdf.parse(timestampStr)
                return parsedDate.getTime()
            } catch (Exception e) {
                lastException = e
                continue
            }
        }
        
        throw new Exception("Impossible de parser le timestamp ISO8601: ${timestampStr}")
        
    } catch (Exception e) {
        throw new Exception("Erreur parsing ISO8601: ${e.message}")
    }
}

// Version avec debug pour voir les valeurs (tout en UTC)
def validateTimestampDebug(String timestampStr, String seuilStr) {
    try {
        long seuilMs = seuilStr.toLong()
        
        // Vérifier le pattern
        boolean matchesPattern = timestampStr.matches(ISO8601_PATTERN)
        println "=== Validation Pattern ISO8601 ==="
        println "Timestamp: '${timestampStr}'"
        println "Correspond au pattern: ${matchesPattern}"
        
        if (!matchesPattern) {
            return "Erreur: Format timestamp ne correspond pas au pattern ISO8601"
        }
        
        // Parser vers UTC
        long timestampUtc = parseISO8601ToUTC(timestampStr)
        
        long nowUtc = System.currentTimeMillis()
        long minTimeUtc = nowUtc - seuilMs
        long maxTimeUtc = nowUtc + seuilMs
        
        // Formatter pour affichage UTC
        SimpleDateFormat displaySdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        displaySdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        
        println "=== Comparaison en UTC ==="
        println "Maintenant UTC: ${nowUtc} (${displaySdf.format(new Date(nowUtc))})"
        println "Timestamp parsé UTC: ${timestampUtc} (${displaySdf.format(new Date(timestampUtc))})"
        println "Seuil: ${seuilMs} ms"
        println "Plage valide UTC: [${minTimeUtc}, ${maxTimeUtc}]"
        println "Différence: ${Math.abs(nowUtc - timestampUtc)} ms"
        println "Dans la plage? ${timestampUtc > minTimeUtc && timestampUtc < maxTimeUtc}"
        
        if (timestampUtc > minTimeUtc && timestampUtc < maxTimeUtc) {
            return "Ok"
        } else {
            return "Ko"
        }
    } catch (NumberFormatException e) {
        return "Erreur: Seuil invalide (${e.message})"
    } catch (Exception e) {
        return "Erreur: ${e.message}"
    }
}

// Exemples d'utilisation
long seuilMs = 5000 // 5000 millisecondes = 5 secondes
// Exemples d'utilisation avec tous les formats ISO8601 supportés
import java.text.SimpleDateFormat

String seuil = "5000" // 5000 ms = 5 secondes

// Test avec différents formats ISO8601 valides
println "=== Tests avec différents formats ISO8601 ==="

// Format avec timezone UTC (Z)
String timestamp1 = "2025-07-30T14:30:45Z"
println "Format Z: ${validateTimestamp(timestamp1, seuil)}"

// Format avec millisecondes et UTC
String timestamp2 = "2025-07-30T14:30:45.123Z"
println "Format avec ms Z: ${validateTimestamp(timestamp2, seuil)}"

// Format avec timezone offset
String timestamp3 = "2025-07-30T14:30:45+02:00"
println "Format avec timezone +02:00: ${validateTimestamp(timestamp3, seuil)}"

// Format avec millisecondes et timezone offset
String timestamp4 = "2025-07-30T14:30:45.123+02:00"
println "Format avec ms et timezone: ${validateTimestamp(timestamp4, seuil)}"

// Format avec timezone négative
String timestamp5 = "2025-07-30T14:30:45.456-05:00"
println "Format timezone négative: ${validateTimestamp(timestamp5, seuil)}"

// Formats avec différentes précisions de millisecondes
String timestamp6 = "2025-07-30T14:30:45.1Z"      // 1 chiffre
String timestamp7 = "2025-07-30T14:30:45.12Z"     // 2 chiffres
String timestamp8 = "2025-07-30T14:30:45.123Z"    // 3 chiffres

println "1 chiffre ms: ${validateTimestamp(timestamp6, seuil)}"
println "2 chiffres ms: ${validateTimestamp(timestamp7, seuil)}"
println "3 chiffres ms: ${validateTimestamp(timestamp8, seuil)}"

// Tests avec des formats invalides
println "\n=== Tests avec formats invalides ==="
println "Format invalide 1: ${validateTimestamp('2025/07/30T14:30:45Z', seuil)}"
println "Format invalide 2: ${validateTimestamp('2025-07-30 14:30:45', seuil)}"
println "Format invalide 3: ${validateTimestamp('2025-07-30T14:30:45.0Z', seuil)}" // .0 invalide

// Test avec debug complet
println "\n=== Test avec debug complet ==="
println "Résultat: ${validateTimestampDebug(timestamp4, seuil)}"
