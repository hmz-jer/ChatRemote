<svg viewBox="0 0 900 600" xmlns="http://www.w3.org/2000/svg">
  <!-- Fond -->
  <rect width="900" height="600" fill="#ffffff" />
  <rect x="0" y="0" width="900" height="80" fill="#e6f7ed" />
  <rect x="0" y="80" width="100" height="520" fill="#d1f0de" />
  
  <!-- Titre -->
  <text x="450" y="50" font-family="Arial" font-size="36" text-anchor="middle" font-weight="bold" fill="#2c3e50">IBC API/Proxy Java</text>
  
  <!-- Séparateur -->
  <line x1="50" y1="90" x2="850" y2="90" stroke="#4caf50" stroke-width="3" />
  
  <!-- Contenu principal -->
  <g transform="translate(130, 130)">
    <!-- Point 1 -->
    <rect x="-15" y="0" width="25" height="25" fill="#2c3e50" />
    <text x="40" y="20" font-family="Arial" font-size="22" font-weight="bold" fill="#2c3e50">Exposition d'une API Rest</text>
    <text x="40" y="50" font-family="Arial" font-size="20" fill="#34495e">/ibancheck_inbound pour les flux entrants</text>
    
    <!-- Point 2 -->
    <rect x="-15" y="80" width="25" height="25" fill="#2c3e50" />
    <text x="40" y="100" font-family="Arial" font-size="20" fill="#34495e">Réalisation d'appels asynchrones avec l'IBC RVM via KAFKA</text>
    
    <!-- Point 3 -->
    <rect x="-15" y="130" width="25" height="25" fill="#2c3e50" />
    <text x="40" y="150" font-family="Arial" font-size="20" fill="#34495e">Le Proxy Java communique avec Kafka en asynchrone via une</text>
    <text x="40" y="180" font-family="Arial" font-size="20" fill="#34495e">Topic pour les "Request" et une Topic pour les "Response"</text>
    
    <!-- Point 4 -->
    <rect x="-15" y="210" width="25" height="25" fill="#2c3e50" />
    <text x="40" y="230" font-family="Arial" font-size="20" fill="#34495e">Le Proxy Java crée et publie un message à destination de la</text>
    <text x="40" y="260" font-family="Arial" font-size="20" fill="#34495e">Topic "Request" avec le messageIdentifier comme élément</text>
    <text x="40" y="290" font-family="Arial" font-size="20" fill="#34495e">d'appairage</text>
    
    <!-- Point 5 -->
    <rect x="-15" y="320" width="25" height="25" fill="#2c3e50" />
    <text x="40" y="340" font-family="Arial" font-size="22" font-weight="bold" fill="#2c3e50">Exposition d'une API Rest pour la supervision</text>
    
    <!-- Point 6 -->
    <rect x="-15" y="370" width="25" height="25" fill="#2c3e50" />
    <text x="40" y="390" font-family="Arial" font-size="20" fill="#34495e">Création des messages dans le topic KAFKA de supervision</text>
    <text x="40" y="420" font-family="Arial" font-size="20" fill="#34495e">/ibancheck_supervision</text>
  </g>
  
  <!-- Éléments graphiques décoratifs -->
  <g transform="translate(720, 200)">
    <!-- Icône Java -->
    <rect x="0" y="0" width="120" height="120" rx="15" fill="#e6f7ed" stroke="#4caf50" stroke-width="2" />
    <text x="60" y="70" font-family="Arial" font-size="24" text-anchor="middle" font-weight="bold" fill="#4caf50">JAVA</text>
    <path d="M30,35 C30,25 45,15 60,15 C75,15 90,25 90,35 C90,45 75,50 60,50 C45,50 30,45 30,35 Z" fill="#4caf50" fill-opacity="0.3" />
    <path d="M40,85 C40,65 50,55 60,55 C70,55 80,65 80,85 Z" fill="#4caf50" fill-opacity="0.3" />
  </g>
  
  <g transform="translate(740, 350)">
    <!-- Icône Kafka -->
    <rect x="0" y="0" width="80" height="80" rx="10" fill="#e6f7ed" stroke="#4caf50" stroke-width="2" />
    <text x="40" y="30" font-family="Arial" font-size="16" text-anchor="middle" fill="#4caf50">KAFKA</text>
    <line x1="15" y1="40" x2="65" y2="40" stroke="#4caf50" stroke-width="2" />
    <line x1="15" y1="50" x2="65" y2="50" stroke="#4caf50" stroke-width="2" />
    <line x1="15" y1="60" x2="65" y2="60" stroke="#4caf50" stroke-width="2" />
  </g>
  
  <g transform="translate(740, 450)">
    <!-- Icône message -->
    <rect x="0" y="0" width="80" height="60" rx="10" fill="#e6f7ed" stroke="#4caf50" stroke-width="2" />
    <text x="40" y="25" font-family="Arial" font-size="12" text-anchor="middle" fill="#4caf50">message</text>
    <text x="40" y="45" font-family="Arial" font-size="12" text-anchor="middle" fill="#4caf50">Identifier</text>
  </g>
  
  <!-- Soulignements pour les termes importants -->
  <line x1="414" y1="180" x2="607" y2="180" stroke="#4caf50" stroke-width="2" />
  <line x1="170" y1="420" x2="380" y2="420" stroke="#4caf50" stroke-width="2" />
  <line x1="490" y1="260" x2="682" y2="260" stroke="#4caf50" stroke-width="2" />
  <line x1="495" y1="420" x2="670" y2="420" stroke="#4caf50" stroke-width="2" />
  
  <!-- Pied de page -->
  <rect x="0" y="570" width="900" height="30" fill="#e6f7ed" />
  <text x="450" y="590" font-family="Arial" font-size="12" text-anchor="middle" fill="#7f8c8d">Service IBANCHECK - Présentation Proxy Java</text>
</svg>
