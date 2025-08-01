 Voici comment obtenir la date actuelle en supprimant les zéros à la fin des millisecondes :Dans Postman (Pre-request Script)// Obtenir la date actuelle
const now = new Date();

// Convertir en ISO string et supprimer les zéros trailing des millisecondes
let isoString = now.toISOString();

// Supprimer les zéros à la fin des millisecondes
// Exemple: 2025-08-01T10:30:45.120Z → 2025-08-01T10:30:45.12Z
isoString = isoString.replace(/(\.\d*?)0+Z$/, '$1Z');

// Si toutes les millisecondes sont à zéro, supprimer complètement
// Exemple: 2025-08-01T10:30:45.000Z → 2025-08-01T10:30:45Z
isoString = isoString.replace(/\.0+Z$/, 'Z');

// Stocker dans une variable d'environnement
pm.environment.set("current_timestamp", isoString);

console.log("Timestamp généré:", isoString);Alternative plus simpleconst now = new Date();
let timestamp = now.toISOString();

// Supprimer les zéros trailing avec une regex plus simple
timestamp = timestamp.replace(/0+Z$/, 'Z').replace(/\.Z$/, 'Z');

pm.environment.set("current_timestamp", timestamp);Fonction réutilisablefunction formatTimestampWithoutTrailingZeros() {
    const now = new Date();
    let iso = now.toISOString();
    
    // Supprimer les zéros à la fin des millisecondes
    return iso.replace(/(\.\d*?)0+Z$/, '$1Z').replace(/\.Z$/, 'Z');
}

// Utilisation
pm.environment.set("current_timestamp", formatTimestampWithoutTrailingZeros());Dans le corps de la requêteUtilisez ensuite la variable dans votre JSON :{
  "timestamp": "{{current_timestamp}}",
  "data": "some data"
}Exemples de résultats2025-08-01T10:30:45.123Z → 2025-08-01T10:30:45.123Z (pas de changement)2025-08-01T10:30:45.120Z → 2025-08-01T10:30:45.12Z (zéro supprimé)2025-08-01T10:30:45.100Z → 2025-08-01T10:30:45.1Z (deux zéros supprimés)2025-08-01T10:30:45.000Z → 2025-08-01T10:30:45Z (toutes les millisecondes supprimées)Cette approche garantit que votre timestamp respectera le pattern de validation qui rejette les zéros trailing.
