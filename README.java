 <svg viewBox="0 0 900 600" xmlns="http://www.w3.org/2000/svg">
  <!-- Fond blanc avec bande verte sur le côté -->
  <rect width="900" height="600" fill="#ffffff" />
  <rect x="0" y="0" width="80" height="600" fill="#4caf50" />
  
  <!-- Titre -->
  <text x="130" y="80" font-family="Arial" font-size="38" font-weight="bold" fill="#2c3e50">Contexte générale du service</text>
  
  <!-- Séparateur sous le titre -->
  <line x1="130" y1="100" x2="850" y2="100" stroke="#4caf50" stroke-width="2" />
  
  <!-- Objectif principal -->
  <text x="130" y="150" font-family="Arial" font-size="28" font-weight="bold" fill="#2c3e50">Objectif principal:</text>
  <text x="130" y="190" font-family="Arial" font-size="22" fill="#34495e">Service de vérification des IBAN et d'échange de messages liés à cette vérification</text>
  
  <!-- Composants clés -->
  <text x="130" y="260" font-family="Arial" font-size="28" font-weight="bold" fill="#2c3e50">Composants clés:</text>
  
  <!-- Liste des composants avec diamants -->
  <path d="M140,300 L150,310 L140,320 L130,310 Z" fill="#4caf50" />
  <text x="170" y="315" font-family="Arial" font-size="22" fill="#34495e">API GATEWAY</text>
  
  <path d="M140,350 L150,360 L140,370 L130,360 Z" fill="#4caf50" />
  <text x="170" y="365" font-family="Arial" font-size="22" fill="#34495e">API MANAGER</text>
  
  <path d="M140,400 L150,410 L140,420 L130,410 Z" fill="#4caf50" />
  <text x="170" y="415" font-family="Arial" font-size="22" fill="#34495e">PROXY JAVA</text>
  
  <path d="M140,450 L150,460 L140,470 L130,460 Z" fill="#4caf50" />
  <text x="170" y="465" font-family="Arial" font-size="22" fill="#34495e">Communication via Kafka avec le backend</text>
  
  <!-- Informations supplémentaires -->
  <path d="M500,300 L510,310 L500,320 L490,310 Z" fill="#4caf50" />
  <text x="530" y="315" font-family="Arial" font-size="22" fill="#34495e">Utilisation par les partenaires VOP</text>
  
  <path d="M500,350 L510,360 L500,370 L490,360 Z" fill="#4caf50" />
  <text x="530" y="365" font-family="Arial" font-size="22" fill="#34495e">Sécurisation par certificats</text>
  
  <path d="M500,400 L510,410 L500,420 L490,410 Z" fill="#4caf50" />
  <text x="530" y="415" font-family="Arial" font-size="22" fill="#34495e">Conversion formats DVOP/DDV</text>
  
  <path d="M500,450 L510,460 L500,470 L490,460 Z" fill="#4caf50" />
  <text x="530" y="465" font-family="Arial" font-size="22" fill="#34495e">Traçabilité via messageIdentifier</text>
  
  <!-- Élément graphique décoratif -->
  <g transform="translate(750, 520)">
    <rect x="0" y="0" width="100" height="40" rx="5" fill="#e6f7ed" stroke="#4caf50" stroke-width="2" />
    <text x="50" y="25" font-family="Arial" font-size="16" text-anchor="middle" fill="#4caf50">IBANCHECK</text>
  </g>
  
  <!-- Bordure décorative pour l'ensemble -->
  <rect x="110" y="120" width="760" height="400" rx="10" fill="none" stroke="#e0e0e0" stroke-width="1" />
</svg>
