 import java.text.SimpleDateFormat
import java.util.TimeZone

// Solution ultra-simple : extraction + comparaison (format déjà validé)
// timestampStr : timestamp au format ISO8601 (String) - format déjà validé
// seuilStr : marge de temps en millisecondes (String)
def validateTimestamp(String timestampStr, String seuilStr) {
    try {
        long seuilMs = seuilStr.toLong()
        
        // Extraire jusqu'aux secondes : YYYY-MM-DDTHH:MM:SS
        String timestampBase = timestampStr.substring(0, 19) // Les 19 premiers caractères
        
        // Parser vers UTC
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        
        Date parsedDate = sdf.parse(timestampBase)
        long timestampUtc = parsedDate.getTime()
        
        // Comparaison UTC
        long nowUtc = System.currentTimeMillis()
        long minTimeUtc = nowUtc - seuilMs
        long maxTimeUtc = nowUtc + seuilMs
        
        if (timestampUtc > minTimeUtc && timestampUtc < maxTimeUtc) {
            return "Ok"
        } else {
            return "Ko"
        }
    } catch (NumberFormatException e) {
        return "Erreur: Seuil invalide"
    } catch (Exception e) {
        return "Erreur: Parsing impossible"
    }
}

// Version avec debug (optionnelle)
def validateTimestampDebug(String timestampStr, String seuilStr) {
    try {
        long seuilMs = seuilStr.toLong()
        String timestampBase = timestampStr.substring(0, 19)
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        Date parsedDate = sdf.parse(timestampBase)
        long timestampUtc = parsedDate.getTime()
        
        long nowUtc = System.currentTimeMillis()
        
        println "Original: '${timestampStr}'"
        println "Extrait:  '${timestampBase}'"
        println "Maintenant: ${nowUtc}"
        println "Timestamp:  ${timestampUtc}"
        println "Différence: ${Math.abs(nowUtc - timestampUtc)} ms"
        
        if (timestampUtc > (nowUtc - seuilMs) && timestampUtc < (nowUtc + seuilMs)) {
            return "Ok"
        } else {
            return "Ko"
        }
    } catch (Exception e) {
        return "Erreur: ${e.message}"
    }
}

// Tests
String seuil = "5000"

println "Test 1: ${validateTimestamp('2025-07-30T14:30:45Z', seuil)}"
println "Test 2: ${validateTimestamp('2025-07-30T14:30:45.123Z', seuil)}"
println "Test 3: ${validateTimestamp('2025-07-30T14:30:45.999+02:00', seuil)}"

println "\nDebug:"
println validateTimestampDebug('2025-07-30T14:30:45.123+02:00', seuil)
