 <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">
  <!-- Background -->
  <rect width="900" height="600" fill="white"/>
  
  <!-- Title -->
  <text x="30" y="40" font-family="Arial" font-size="24" font-weight="bold" fill="#333333">Structure des fichiers de spécification WS-Simulator</text>
  
  <!-- File Icon and Name -->
  <rect x="30" y="60" width="840" height="40" rx="5" fill="#f1f8e9" stroke="#558b2f" stroke-width="2"/>
  <text x="50" y="85" font-family="Arial" font-size="16" font-weight="bold" fill="#558b2f">tokenRequest.json - Spécification de requête vers un service TSP</text>
  
  <!-- JSON File Structure -->
  <rect x="30" y="110" width="420" height="460" rx="5" fill="#fafafa" stroke="#bdbdbd" stroke-width="2"/>
  
  <!-- JSON Content -->
  <text x="50" y="135" font-family="Courier, monospace" font-size="13" fill="#333333">{</text>
  <text x="60" y="155" font-family="Courier, monospace" font-size="13" fill="#a31515">"type"</text>
  <text x="105" y="155" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="115" y="155" font-family="Courier, monospace" font-size="13" fill="#a31515">"token-request"</text>
  <text x="230" y="155" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  
  <text x="60" y="175" font-family="Courier, monospace" font-size="13" fill="#a31515">"url"</text>
  <text x="95" y="175" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="105" y="175" font-family="Courier, monospace" font-size="13" fill="#a31515">"https://10.56.5.76:443/tsp/api-tsp/v2.2/token/request"</text>
  <text x="430" y="175" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  
  <text x="60" y="195" font-family="Courier, monospace" font-size="13" fill="#a31515">"response"</text>
  <text x="125" y="195" font-family="Courier, monospace" font-size="13" fill="#333333">: {</text>
  
  <text x="80" y="215" font-family="Courier, monospace" font-size="13" fill="#a31515">"CustMsgId"</text>
  <text x="155" y="215" font-family="Courier, monospace" font-size="13" fill="#333333">: {</text>
  <text x="100" y="235" font-family="Courier, monospace" font-size="13" fill="#a31515">"mandatory"</text>
  <text x="170" y="235" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="180" y="235" font-family="Courier, monospace" font-size="13" fill="#0000ff">true</text>
  <text x="210" y="235" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  <text x="100" y="255" font-family="Courier, monospace" font-size="13" fill="#a31515">"format"</text>
  <text x="155" y="255" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="165" y="255" font-family="Courier, monospace" font-size="13" fill="#a31515">"[0-9a-zA-Z ]{16}"</text>
  <text x="290" y="255" font-family="Courier, monospace" font-size="13" fill="#333333"></text>
  <text x="80" y="275" font-family="Courier, monospace" font-size="13" fill="#333333">},</text>
  
  <text x="80" y="295" font-family="Courier, monospace" font-size="13" fill="#a31515">"RspnCode"</text>
  <text x="150" y="295" font-family="Courier, monospace" font-size="13" fill="#333333">: {</text>
  <text x="100" y="315" font-family="Courier, monospace" font-size="13" fill="#a31515">"mandatory"</text>
  <text x="170" y="315" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="180" y="315" font-family="Courier, monospace" font-size="13" fill="#0000ff">true</text>
  <text x="210" y="315" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  <text x="100" y="335" font-family="Courier, monospace" font-size="13" fill="#a31515">"format"</text>
  <text x="155" y="335" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="165" y="335" font-family="Courier, monospace" font-size="13" fill="#a31515">"[0-9]{2}"</text>
  <text x="250" y="335" font-family="Courier, monospace" font-size="13" fill="#333333"></text>
  <text x="80" y="355" font-family="Courier, monospace" font-size="13" fill="#333333">},</text>
  
  <text x="80" y="375" font-family="Courier, monospace" font-size="13" fill="#a31515">"TokRefId"</text>
  <text x="150" y="375" font-family="Courier, monospace" font-size="13" fill="#333333">: {</text>
  <text x="100" y="395" font-family="Courier, monospace" font-size="13" fill="#a31515">"mandatory"</text>
  <text x="170" y="395" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="180" y="395" font-family="Courier, monospace" font-size="13" fill="#0000ff">false</text>
  <text x="215" y="395" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  <text x="100" y="415" font-family="Courier, monospace" font-size="13" fill="#a31515">"format"</text>
  <text x="155" y="415" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="165" y="415" font-family="Courier, monospace" font-size="13" fill="#a31515">"[0-9a-zA-Z ]{32}"</text>
  <text x="290" y="415" font-family="Courier, monospace" font-size="13" fill="#333333"></text>
  <text x="80" y="435" font-family="Courier, monospace" font-size="13" fill="#333333">},</text>
  
  <text x="80" y="455" font-family="Courier, monospace" font-size="13" fill="#a31515">"TokStatus"</text>
  <text x="150" y="455" font-family="Courier, monospace" font-size="13" fill="#333333">: {</text>
  <text x="100" y="475" font-family="Courier, monospace" font-size="13" fill="#a31515">"mandatory"</text>
  <text x="170" y="475" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="180" y="475" font-family="Courier, monospace" font-size="13" fill="#0000ff">false</text>
  <text x="215" y="475" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  <text x="100" y="495" font-family="Courier, monospace" font-size="13" fill="#a31515">"format"</text>
  <text x="155" y="495" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="165" y="495" font-family="Courier, monospace" font-size="13" fill="#a31515">"[0-9a-zA-Z ]{10}"</text>
  <text x="290" y="495" font-family="Courier, monospace" font-size="13" fill="#333333"></text>
  <text x="80" y="515" font-family="Courier, monospace" font-size="13" fill="#333333">}</text>
  
  <text x="60" y="535" font-family="Courier, monospace" font-size="13" fill="#333333">  }</text>
  <text x="50" y="555" font-family="Courier, monospace" font-size="13" fill="#333333">}</text>
  
  <!-- Explanation Box -->
  <rect x="470" y="110" width="400" height="460" rx="5" fill="#e8f5e9" stroke="#4caf50" stroke-width="2"/>
  
  <!-- Explanation Title -->
  <rect x="470" y="110" width="400" height="40" rx="5" fill="#4caf50" stroke="#4caf50" stroke-width="2"/>
  <text x="670" y="135" font-family="Arial" font-size="16" font-weight="bold" fill="white" text-anchor="middle">Explication des éléments de spécification</text>
  
  <!-- Explanation Content -->
  <text x="490" y="180" font-family="Arial" font-size="14" font-weight="bold" fill="#2e7d32">1. Identification</text>
  <text x="490" y="200" font-family="Arial" font-size="13" fill="#333333">• "type": Identifie le type de requête</text>
  <text x="490" y="220" font-family="Arial" font-size="13" fill="#333333">• "url": Point de terminaison de l'API cible</text>
  
  <text x="490" y="250" font-family="Arial" font-size="14" font-weight="bold" fill="#2e7d32">2. Response</text>
  <text x="490" y="270" font-family="Arial" font-size="13" fill="#333333">Définit les champs attendus dans la réponse:</text>
  
  <text x="490" y="300" font-family="Arial" font-size="14" font-weight="bold" fill="#2e7d32">3. Champs de réponse</text>
  <text x="490" y="320" font-family="Arial" font-size="13" fill="#333333">Pour chaque champ:</text>
  <text x="510" y="340" font-family="Arial" font-size="13" fill="#333333">• "mandatory": Indique si le champ est obligatoire</text>
  <text x="510" y="360" font-family="Arial" font-size="13" fill="#333333">• "format": Expression régulière pour valider</text>
  <text x="510" y="380" font-family="Arial" font-size="13" fill="#333333">  le format de la valeur</text>
  
  <text x="490" y="410" font-family="Arial" font-size="14" font-weight="bold" fill="#2e7d32">4. Formats d'expression régulière</text>
  <text x="510" y="430" font-family="Arial" font-size="13" fill="#333333">• "[0-9]{2}" - Deux chiffres</text>
  <text x="510" y="450" font-family="Arial" font-size="13" fill="#333333">• "[0-9a-zA-Z ]{16}" - 16 caractères alphanumériques</text>
  <text x="510" y="470" font-family="Arial" font-size="13" fill="#333333">• "[0-9a-zA-Z ]{32}" - 32 caractères alphanumériques</text>
  
  <text x="490" y="500" font-family="Arial" font-size="14" font-weight="bold" fill="#2e7d32">5. Utilisation des spécifications</text>
  <text x="510" y="520" font-family="Arial" font-size="13" fill="#333333">• WS-Simulator utilise ces spécifications pour</text>
  <text x="510" y="540" font-family="Arial" font-size="13" fill="#333333">  valider les réponses reçues des services</text>
  <text x="510" y="560" font-family="Arial" font-size="13" fill="#333333">• Détecte automatiquement les écarts de format</text>
</svg>
