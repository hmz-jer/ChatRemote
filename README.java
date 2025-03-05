 <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 500">
  <!-- Background -->
  <rect width="800" height="500" fill="white"/>
  
  <!-- Title -->
  <text x="30" y="50" font-family="Arial" font-size="24" font-weight="bold" fill="#333333">Flux de requête initié par WS-Simulator</text>
  
  <!-- Actors -->
  <rect x="100" y="100" width="140" height="60" rx="5" fill="#6666CC" stroke="#333333" stroke-width="2"/>
  <text x="170" y="137" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="white">WS-Simulator</text>
  
  <rect x="350" y="100" width="140" height="60" rx="5" fill="#4488AA" stroke="#333333" stroke-width="2"/>
  <text x="420" y="137" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="white">TEH</text>
  
  <rect x="600" y="100" width="140" height="60" rx="5" fill="#44AA88" stroke="#333333" stroke-width="2"/>
  <text x="670" y="137" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="white">TSP</text>
  
  <!-- Lifelines -->
  <line x1="170" y1="160" x2="170" y2="450" stroke="#333333" stroke-width="2"/>
  <line x1="420" y1="160" x2="420" y2="450" stroke="#333333" stroke-width="2"/>
  <line x1="670" y1="160" x2="670" y2="450" stroke="#333333" stroke-width="2"/>
  
  <!-- Main box -->
  <rect x="50" y="180" width="700" height="250" rx="20" ry="20" fill="none" stroke="#6666CC" stroke-width="2" stroke-dasharray="5,5"/>
  
  <!-- "Initiated by WS-Simulator" note -->
  <rect x="80" y="200" width="100" height="60" fill="#EEEEEE" stroke="#333333" stroke-width="1"/>
  <text x="130" y="230" font-family="Arial" font-size="13" text-anchor="middle" fill="#333333">Initié par</text>
  <text x="130" y="250" font-family="Arial" font-size="13" text-anchor="middle" fill="#333333">WS-Simulator</text>
  
  <!-- 1. CreateToken Request to TEH -->
  <line x1="170" y1="220" x2="410" y2="220" stroke="#333333" stroke-width="1.5"/>
  <polygon points="410,220 400,215 400,225" fill="#333333"/>
  <text x="295" y="210" font-family="Arial" font-size="12" text-anchor="middle" fill="#333333">1. CreateToken</text>
  <text x="295" y="225" font-family="Arial" font-size="10" text-anchor="middle" fill="#333333">{PAN, ExpiryDate, CVV}</text>
  
  <!-- 2. TokenizeRequest from TEH to TSP -->
  <line x1="420" y1="270" x2="660" y2="270" stroke="#333333" stroke-width="1.5"/>
  <polygon points="660,270 650,265 650,275" fill="#333333"/>
  <text x="545" y="260" font-family="Arial" font-size="12" text-anchor="middle" fill="#333333">2. TokenizeRequest</text>
  <text x="545" y="275" font-family="Arial" font-size="10" text-anchor="middle" fill="#333333">{PAN, ExpiryDate, CVV}</text>
  
  <!-- 3. TokenizeResponse from TSP to TEH -->
  <line x1="670" y1="320" x2="430" y2="320" stroke="#333333" stroke-width="1.5"/>
  <polygon points="430,320 440,315 440,325" fill="#333333"/>
  <text x="545" y="310" font-family="Arial" font-size="12" text-anchor="middle" fill="#333333">3. TokenizeResponse</text>
  <text x="545" y="325" font-family="Arial" font-size="10" text-anchor="middle" fill="#333333">{TokenID, TokenExpiry}</text>
  
  <!-- 4. CreateTokenResponse from TEH to WS-Simulator -->
  <line x1="420" y1="370" x2="180" y2="370" stroke="#333333" stroke-width="1.5"/>
  <polygon points="180,370 190,365 190,375" fill="#333333"/>
  <text x="295" y="360" font-family="Arial" font-size="12" text-anchor="middle" fill="#333333">4. CreateTokenResponse</text>
  <text x="295" y="375" font-family="Arial" font-size="10" text-anchor="middle" fill="#333333">{TokenID, TokenExpiry, ResultCode}</text>
  
  <!-- CSV Example Callout -->
  <rect x="50" y="400" width="240" height="70" rx="5" fill="#EEEEEE" stroke="#333333" stroke-width="2"/>
  <text x="60" y="420" font-family="Arial" font-size="12" font-weight="bold" fill="#333333">Exemple CSV WS-Simulator:</text>
  <text x="60" y="440" font-family="Arial" font-size="11" fill="#333333">ScenarioID,1</text>
  <text x="60" y="455" font-family="Arial" font-size="11" fill="#333333">RequestType,CreateToken</text>
  
  <!-- Processing Note -->
  <rect x="400" y="400" width="350" height="70" rx="5" fill="#EEEEEE" stroke="#333333" stroke-width="2"/>
  <text x="410" y="420" font-family="Arial" font-size="12" font-weight="bold" fill="#333333">Traitement WS-Simulator:</text>
  <text x="410" y="440" font-family="Arial" font-size="11" fill="#333333">1. Lit le fichier CSV et prépare la requête</text>
  <text x="410" y="455" font-family="Arial" font-size="11" fill="#333333">2. Envoie la requête et attend la réponse</text>
  <text x="410" y="470" font-family="Arial" font-size="11" fill="#333333">3. Vérifie la réponse selon les critères attendus</text>
</svg>
