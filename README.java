  // Crée une date UTC + 60 minutes
let now = new Date();
now.setMinutes(now.getMinutes() + 60);
let futureDate = now.toISOString(); // Format ISO 8601

// Enregistrer dans une variable d'environnement ou globale
pm.environment.set("utcPlus60", futureDate);
