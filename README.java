<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 500">
  <rect width="800" height="500" fill="white"/>
  <text x="400" y="50" font-family="Arial" font-size="28" font-weight="bold" text-anchor="middle" fill="#333">Structure des fichiers CSV dans WS-Simulator</text>
  
  <!-- CSV Example Header -->
  <rect x="50" y="80" width="700" height="40" rx="5" fill="#e8f5e9"/>
  <text x="400" y="105" font-family="Arial" font-size="18" font-weight="bold" text-anchor="middle" fill="#333">Exemple de fichier CSV pour tests automatisés</text>
  
  <!-- CSV Header Row -->
  <rect x="50" y="130" width="150" height="40" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="200" y="130" width="150" height="40" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="350" y="130" width="200" height="40" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="550" y="130" width="200" height="40" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <text x="125" y="155" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="#333">ScenarioId</text>
  <text x="275" y="155" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="#333">RequestType</text>
  <text x="450" y="155" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="#333">fields (requête)</text>
  <text x="650" y="155" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="#333">expected (réponse)</text>
  
  <!-- CSV Row 1 -->
  <rect x="50" y="170" width="150" height="60" fill="white" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="200" y="170" width="150" height="60" fill="white" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="350" y="170" width="200" height="60" fill="white" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="550" y="170" width="200" height="60" fill="white" stroke="#e0e0e0" stroke-width="1"/>
  <text x="125" y="205" font-family="Arial" font-size="16" text-anchor="middle" fill="#333">1</text>
  <text x="275" y="205" font-family="Arial" font-size="16" text-anchor="middle" fill="#333">createToken</text>
  <text x="450" y="195" font-family="Courier New" font-size="14" text-anchor="middle" fill="#333">PAN=5412345678901234</text>
  <text x="450" y="215" font-family="Courier New" font-size="14" text-anchor="middle" fill="#333">ExpiryDate=1225</text>
  <text x="650" y="195" font-family="Courier New" font-size="14" text-anchor="middle" fill="#333">RspnCode=00</text>
  <text x="650" y="215" font-family="Courier New" font-size="14" text-anchor="middle" fill="#333">TokenId=?</text>
  
  <!-- CSV Row 2 -->
  <rect x="50" y="230" width="150" height="60" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="200" y="230" width="150" height="60" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="350" y="230" width="200" height="60" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="550" y="230" width="200" height="60" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <text x="125" y="265" font-family="Arial" font-size="16" text-anchor="middle" fill="#333">2</text>
  <text x="275" y="265" font-family="Arial" font-size="16" text-anchor="middle" fill="#333">updateToken</text>
  <text x="450" y="255" font-family="Courier New" font-size="14" text-anchor="middle" fill="#333">TokenId=1234567890</text>
  <text x="450" y="275" font-family="Courier New" font-size="14" text-anchor="middle" fill="#333">Status=A</text>
  <text x="650" y="255" font-family="Courier New" font-size="14" text-anchor="middle" fill="#333">RspnCode=00</text>
  <text x="650" y="275" font-family="Courier New" font-size="14" text-anchor="middle" fill="#333">Status=NULL</text>
  
  <!-- Legend -->
  <rect x="50" y="310" width="700" height="170" rx="5" fill="#e8f5e9"/>
  <text x="70" y="335" font-family="Arial" font-size="18" font-weight="bold" fill="#333">Légende des valeurs attendues:</text>
  
  <!-- Legend items -->
  <text x="90" y="365" font-family="Arial" font-size="16" fill="#333">• Valeur exacte (ex: "RspnCode=00") :</text>
  <text x="470" y="365" font-family="Arial" font-size="16" fill="#333">Valeur attendue précise</text>
  
  <text x="90" y="395" font-family="Arial" font-size="16" fill="#333">• Symbole "?" (ex: "TokenId=?") :</text>
  <text x="470" y="395" font-family="Arial" font-size="16" fill="#333">Valeur imprévisible, vérification du format uniquement</text>
  
  <text x="90" y="425" font-family="Arial" font-size="16" fill="#333">• Valeur "NULL" (ex: "Status=NULL") :</text>
  <text x="470" y="425" font-family="Arial" font-size="16" fill="#333">Pas de vérification de cette valeur</text>
  
  <text x="90" y="455" font-family="Arial" font-size="16" fill="#333">• Format de traitement:</text>
  <text x="470" y="455" font-family="Arial" font-size="16" fill="#333">Classical = ASYNC, [filename]_seq.csv = SYNC</text>
</svg>
