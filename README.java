 <svg viewBox="0 0 900 600" xmlns="http://www.w3.org/2000/svg">
  <!-- Fond -->
  <rect width="900" height="600" fill="#ffffff" />
  
  <!-- Titre -->
  <text x="450" y="30" font-family="Arial" font-size="20" text-anchor="middle" font-weight="bold">Diagramme de séquence - Workflow IBANCHECK</text>
  
  <!-- Acteurs et lignes verticales -->
  <!-- PSP Externe VOP -->
  <rect x="70" y="50" width="120" height="40" rx="5" fill="#4a86e8" stroke="#2a5db0" stroke-width="2" />
  <text x="130" y="75" font-family="Arial" font-size="14" text-anchor="middle" fill="white">PSP Externe VOP</text>
  <line x1="130" y1="90" x2="130" y2="550" stroke="#aaaaaa" stroke-width="1" stroke-dasharray="5,5" />
  
  <!-- Reverse Proxy -->
  <rect x="240" y="50" width="120" height="40" rx="5" fill="#4a86e8" stroke="#2a5db0" stroke-width="2" />
  <text x="300" y="75" font-family="Arial" font-size="14" text-anchor="middle" fill="white">Reverse Proxy</text>
  <line x1="300" y1="90" x2="300" y2="550" stroke="#aaaaaa" stroke-width="1" stroke-dasharray="5,5" />
  
  <!-- IBC API/API GW-MGR -->
  <rect x="410" y="50" width="120" height="40" rx="5" fill="#4a86e8" stroke="#2a5db0" stroke-width="2" />
  <text x="470" y="75" font-family="Arial" font-size="14" text-anchor="middle" fill="white">IBC API/API GW-MGR</text>
  <line x1="470" y1="90" x2="470" y2="550" stroke="#aaaaaa" stroke-width="1" stroke-dasharray="5,5" />
  
  <!-- Proxy Java -->
  <rect x="580" y="50" width="120" height="40" rx="5" fill="#4a86e8" stroke="#2a5db0" stroke-width="2" />
  <text x="640" y="75" font-family="Arial" font-size="14" text-anchor="middle" fill="white">Proxy Java</text>
  <line x1="640" y1="90" x2="640" y2="550" stroke="#aaaaaa" stroke-width="1" stroke-dasharray="5,5" />
  
  <!-- IBC KVM -->
  <rect x="750" y="50" width="120" height="40" rx="5" fill="#4a86e8" stroke="#2a5db0" stroke-width="2" />
  <text x="810" y="75" font-family="Arial" font-size="14" text-anchor="middle" fill="white">IBC KVM</text>
  <line x1="810" y1="90" x2="810" y2="550" stroke="#aaaaaa" stroke-width="1" stroke-dasharray="5,5" />
  
  <!-- Acteurs répétés en bas -->
  <rect x="70" y="520" width="120" height="40" rx="5" fill="#4a86e8" stroke="#2a5db0" stroke-width="2" />
  <text x="130" y="545" font-family="Arial" font-size="14" text-anchor="middle" fill="white">PSP Externe VOP</text>
  
  <rect x="240" y="520" width="120" height="40" rx="5" fill="#4a86e8" stroke="#2a5db0" stroke-width="2" />
  <text x="300" y="545" font-family="Arial" font-size="14" text-anchor="middle" fill="white">Reverse Proxy</text>
  
  <rect x="410" y="520" width="120" height="40" rx="5" fill="#4a86e8" stroke="#2a5db0" stroke-width="2" />
  <text x="470" y="545" font-family="Arial" font-size="14" text-anchor="middle" fill="white">IBC API/API GW-MGR</text>
  
  <rect x="580" y="520" width="120" height="40" rx="5" fill="#4a86e8" stroke="#2a5db0" stroke-width="2" />
  <text x="640" y="545" font-family="Arial" font-size="14" text-anchor="middle" fill="white">Proxy Java</text>
  
  <rect x="750" y="520" width="120" height="40" rx="5" fill="#4a86e8" stroke="#2a5db0" stroke-width="2" />
  <text x="810" y="545" font-family="Arial" font-size="14" text-anchor="middle" fill="white">IBC KVM</text>
  
  <!-- Flèches et messages -->
  <!-- 1. Appel pour l'appel vérification -->
  <line x1="130" y1="120" x2="300" y2="120" stroke="#000000" stroke-width="1.5" />
  <polygon points="295,115 305,120 295,125" fill="#000000" />
  <text x="215" y="110" font-family="Arial" font-size="12" text-anchor="middle">Appel pour l'appel vérification</text>
  
  <!-- 2. Appel pour l'appel vérification (Reverse Proxy vers API) -->
  <line x1="300" y1="150" x2="470" y2="150" stroke="#000000" stroke-width="1.5" />
  <polygon points="465,145 475,150 465,155" fill="#000000" />
  <text x="385" y="140" font-family="Arial" font-size="12" text-anchor="middle">Appel pour l'appel vérification</text>
  
  <!-- 3. HTTP Header X-SSL-CLIENT -->
  <rect x="240" y="170" width="230" height="40" rx="0" fill="#f5f5f5" stroke="#cccccc" stroke-width="1" />
  <text x="355" y="195" font-family="Arial" font-size="12" text-anchor="middle">HTTP Header X-SSL-CLIENT: Certificat en base64</text>
  
  <!-- Les étapes "Extraire NUH", "Convertir DVOP" et "Enrichir le header" ont été supprimées -->
  
  
  <!-- 4. Header JSON avec messageIdentifier -->
  <rect x="410" y="220" width="230" height="60" rx="0" fill="#f5f5f5" stroke="#cccccc" stroke-width="1" />
  <text x="525" y="240" font-family="Arial" font-size="12" text-anchor="middle">Header JSON avec</text>
  <text x="525" y="260" font-family="Arial" font-size="12" text-anchor="middle">messageIdentifier, originalMessageIdentifier,</text>
  <text x="525" y="280" font-family="Arial" font-size="12" text-anchor="middle">correlationIdentifier, transactionId, requesterId</text>
  
  <!-- 5. Appel Ibancheck_inbound -->
  <line x1="470" y1="310" x2="640" y2="310" stroke="#000000" stroke-width="1.5" />
  <polygon points="635,305 645,310 635,315" fill="#000000" />
  <text x="555" y="300" font-family="Arial" font-size="12" text-anchor="middle">Appel /ibancheck_inbound</text>
  
  <!-- 6. Envoyer message Kafka (messageIdentifier) -->
  <line x1="640" y1="340" x2="810" y2="340" stroke="#000000" stroke-width="1.5" />
  <polygon points="805,335 815,340 805,345" fill="#000000" />
  <text x="725" y="330" font-family="Arial" font-size="12" text-anchor="middle">Envoyer message Kafka (messageIdentifier)</text>
  
  <!-- 7. Retourne la réponse KAPPA -->
  <line x1="810" y1="370" x2="640" y2="370" stroke="#000000" stroke-width="1.5" />
  <polygon points="645,375 635,370 645,365" fill="#000000" />
  <text x="725" y="360" font-family="Arial" font-size="12" text-anchor="middle">Retourne la réponse KAPPA</text>
  
  <!-- 8. Envoyer la réponse RDDV -->
  <line x1="640" y1="400" x2="470" y2="400" stroke="#000000" stroke-width="1.5" />
  <polygon points="475,405 465,400 475,395" fill="#000000" />
  <text x="555" y="390" font-family="Arial" font-size="12" text-anchor="middle">Envoyer la réponse RDDV</text>
  
  <!-- 9. Convertir RDDV <-> RDVOP -->
  <line x1="470" y1="430" x2="300" y2="430" stroke="#000000" stroke-width="1.5" stroke-dasharray="5,3" />
  <polygon points="305,435 295,430 305,425" fill="#000000" />
  <text x="385" y="420" font-family="Arial" font-size="12" text-anchor="middle">Convertir RDDV <-> RDVOP</text>
  
  <!-- 10. Envoyer la réponse au client -->
  <line x1="300" y1="460" x2="130" y2="460" stroke="#000000" stroke-width="1.5" />
  <polygon points="135,465 125,460 135,455" fill="#000000" />
  <text x="215" y="450" font-family="Arial" font-size="12" text-anchor="middle">Envoyer la réponse au client</text>
  
  <!-- 11. Appeler flow_supervision (après avoir envoyé la réponse) -->
  <line x1="470" y1="490" x2="640" y2="490" stroke="#000000" stroke-width="1.5" />
  <polygon points="635,485 645,490 635,495" fill="#000000" />
  <text x="555" y="480" font-family="Arial" font-size="12" text-anchor="middle">Appeler flow_supervision</text>
  
  <!-- 12. Envoyer message Kafka de reporting -->
  <line x1="640" y1="520" x2="810" y2="520" stroke="#000000" stroke-width="1.5" />
  <polygon points="805,515 815,520 805,525" fill="#000000" />
  <text x="725" y="510" font-family="Arial" font-size="12" text-anchor="middle">Envoyer message Kafka de reporting</text>
</svg>
