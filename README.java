 <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 620">
  <!-- Background -->
  <rect width="900" height="620" fill="white"/>
  
  <!-- Title -->
  <text x="30" y="40" font-family="Arial" font-size="24" font-weight="bold" fill="#333333">Structure des fichiers de spécification de notification</text>
  
  <!-- File Icon and Name -->
  <rect x="30" y="60" width="840" height="40" rx="5" fill="#f1f8e9" stroke="#558b2f" stroke-width="2"/>
  <text x="50" y="85" font-family="Arial" font-size="16" font-weight="bold" fill="#558b2f">tokenStatusNotification.json - Spécification de notification reçue par WS-Simulator</text>
  
  <!-- JSON File Structure -->
  <rect x="30" y="110" width="420" height="480" rx="5" fill="#fafafa" stroke="#bdbdbd" stroke-width="2"/>
  
  <!-- JSON Content -->
  <text x="50" y="135" font-family="Courier, monospace" font-size="13" fill="#333333">{</text>
  <text x="60" y="155" font-family="Courier, monospace" font-size="13" fill="#a31515">"path"</text>
  <text x="105" y="155" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="115" y="155" font-family="Courier, monospace" font-size="13" fill="#a31515">"/token/status-notification"</text>
  <text x="320" y="155" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  
  <text x="60" y="175" font-family="Courier, monospace" font-size="13" fill="#a31515">"notification"</text>
  <text x="145" y="175" font-family="Courier, monospace" font-size="13" fill="#333333">: {</text>
  
  <text x="80" y="195" font-family="Courier, monospace" font-size="13" fill="#a31515">"CustMsgId"</text>
  <text x="155" y="195" font-family="Courier, monospace" font-size="13" fill="#333333">: {</text>
  <text x="100" y="215" font-family="Courier, monospace" font-size="13" fill="#a31515">"mandatory"</text>
  <text x="170" y="215" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="180" y="215" font-family="Courier, monospace" font-size="13" fill="#0000ff">true</text>
  <text x="210" y="215" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  <text x="100" y="235" font-family="Courier, monospace" font-size="13" fill="#a31515">"format"</text>
  <text x="155" y="235" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="165" y="235" font-family="Courier, monospace" font-size="13" fill="#a31515">"[0-9a-zA-Z ]{16}"</text>
  <text x="290" y="235" font-family="Courier, monospace" font-size="13" fill="#333333"></text>
  <text x="80" y="255" font-family="Courier, monospace" font-size="13" fill="#333333">},</text>
  
  <text x="80" y="275" font-family="Courier, monospace" font-size="13" fill="#a31515">"CustCnxId"</text>
  <text x="150" y="275" font-family="Courier, monospace" font-size="13" fill="#333333">: {</text>
  <text x="100" y="295" font-family="Courier, monospace" font-size="13" fill="#a31515">"mandatory"</text>
  <text x="170" y="295" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="180" y="295" font-family="Courier, monospace" font-size="13" fill="#0000ff">true</text>
  <text x="210" y="295" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  <text x="100" y="315" font-family="Courier, monospace" font-size="13" fill="#a31515">"format"</text>
  <text x="155" y="315" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="165" y="315" font-family="Courier, monospace" font-size="13" fill="#a31515">"[0-9a-zA-Z ]{12}"</text>
  <text x="290" y="315" font-family="Courier, monospace" font-size="13" fill="#333333"></text>
  <text x="80" y="335" font-family="Courier, monospace" font-size="13" fill="#333333">},</text>
  
  <text x="80" y="355" font-family="Courier, monospace" font-size="13" fill="#a31515">"TokStatus"</text>
  <text x="150" y="355" font-family="Courier, monospace" font-size="13" fill="#333333">: {</text>
  <text x="100" y="375" font-family="Courier, monospace" font-size="13" fill="#a31515">"mandatory"</text>
  <text x="170" y="375" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="180" y="375" font-family="Courier, monospace" font-size="13" fill="#0000ff">true</text>
  <text x="210" y="375" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  <text x="100" y="395" font-family="Courier, monospace" font-size="13" fill="#a31515">"format"</text>
  <text x="155" y="395" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="165" y="395" font-family="Courier, monospace" font-size="13" fill="#a31515">"[0-9a-zA-Z ]{1}"</text>
  <text x="290" y="395" font-family="Courier, monospace" font-size="13" fill="#333333"></text>
  <text x="80" y="415" font-family="Courier, monospace" font-size="13" fill="#333333">},</text>
  
  <text x="80" y="435" font-family="Courier, monospace" font-size="13" fill="#a31515">"TokXpryDate"</text>
  <text x="170" y="435" font-family="Courier, monospace" font-size="13" fill="#333333">: {</text>
  <text x="100" y="455" font-family="Courier, monospace" font-size="13" fill="#a31515">"mandatory"</text>
  <text x="170" y="455" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="180" y="455" font-family="Courier, monospace" font-size="13" fill="#0000ff">true</text>
  <text x="210" y="455" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  <text x="100" y="475" font-family="Courier, monospace" font-size="13" fill="#a31515">"format"</text>
  <text x="155" y="475" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="165" y="475" font-family="Courier, monospace" font-size="13" fill="#a31515">"[0-9]{4}"</text>
  <text x="240" y="475" font-family="Courier, monospace" font-size="13" fill="#333333"></text>
  <text x="80" y="495" font-family="Courier, monospace" font-size="13" fill="#333333">},</text>
  
  <text x="80" y="515" font-family="Courier, monospace" font-size="13" fill="#a31515">"NotifReason"</text>
  <text x="170" y="515" font-family="Courier, monospace" font-size="13" fill="#333333">: {</text>
  <text x="100" y="535" font-family="Courier, monospace" font-size="13" fill="#a31515">"mandatory"</text>
  <text x="170" y="535" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="180" y="535" font-family="Courier, monospace" font-size="13" fill="#0000ff">true</text>
  <text x="210" y="535" font-family="Courier, monospace" font-size="13" fill="#333333">,</text>
  <text x="100" y="555" font-family="Courier, monospace" font-size="13" fill="#a31515">"format"</text>
  <text x="155" y="555" font-family="Courier, monospace" font-size="13" fill="#333333">: </text>
  <text x="165" y="555" font-family="Courier, monospace" font-size="13" fill="#a31515">"[0-9]{2}"</text>
  <text x="240" y="555" font-family="Courier, monospace" font-size="13" fill="#333333"></text>
  <text x="80" y="575" font-family="Courier, monospace" font-size="13" fill="#333333">}</text>
  
  <text x="60" y="595" font-family="Courier, monospace" font-size="13" fill="#333333">  },</text>

  <!-- ACK Section at bottom left -->
  <text x="60" y="615" font-family="Courier, monospace" font-size="12" fill="#a31515">"ack"</text>
  <text x="95" y="615" font-family="Courier, monospace" font-size="12" fill="#333333">: {</text>
  <text x="80" y="635" font-family="Courier, monospace" font-size="12" fill="#a31515">"CustMsgId"</text>
  <text x="145" y="635" font-family="Courier, monospace" font-size="12" fill="#333333">: </text>
  <text x="155" y="635" font-family="Courier, monospace" font-size="12" fill="#a31515">"?"</text>
  <text x="170" y="635" font-family="Courier, monospace" font-size="12" fill="#333333">,</text>
  <text x="175" y="635" font-family="Courier, monospace" font-size="12" fill="#a31515">"CustCnxId"</text>
  <text x="240" y="635" font-family="Courier, monospace" font-size="12" fill="#333333">: </text>
  <text x="250" y="635" font-family="Courier, monospace" font-size="12" fill="#a31515">"?"</text>
  <text x="265" y="635" font-family="Courier, monospace" font-size="12" fill="#333333">,</text>
  <text x="80" y="655" font-family="Courier, monospace" font-size="12" fill="#a31515">"RspnCode"</text>
  <text x="145" y="655" font-family="Courier, monospace" font-size="12" fill="#333333">: </text>
  <text x="155" y="655" font-family="Courier, monospace" font-size="12" fill="#a31515">"00"</text>
  <text x="175" y="655" font-family="Courier, monospace" font-size="12" fill="#333333">,</text>
  <text x="185" y="655" font-family="Courier, monospace" font-size="12" fill="#a31515">"RspnRsn"</text>
  <text x="245" y="655" font-family="Courier, monospace" font-size="12" fill="#333333">: </text>
  <text x="255" y="655" font-family="Courier, monospace" font-size="12" fill="#a31515">"0000"</text>
  <text x="60" y="675" font-family="Courier, monospace" font-size="12" fill="#333333">}</text>
  <text x="50" y="695" font-family="Courier, monospace" font-size="12" fill="#333333">}</text>
  
  <!-- Explanation Box -->
  <rect x="470" y="110" width="400" height="480" rx="5" fill="#e8f5e9" stroke="#4caf50" stroke-width="2"/>
  
  <!-- Explanation Title -->
  <rect x="470" y="110" width="400" height="40" rx="5" fill="#4caf50" stroke="#4caf50" stroke-width="2"/>
  <text x="670" y="135" font-family="Arial" font-size="16" font-weight="bold" fill="white" text-anchor="middle">Explication de la spécification de notification</text>
  
  <!-- Explanation Content -->
  <text x="490" y="175" font-family="Arial" font-size="14" font-weight="bold" fill="#2e7d32">1. Path</text>
  <text x="490" y="195" font-family="Arial" font-size="13" fill="#333333">• "/token/status-notification" - URL à laquelle</text>
  <text x="490" y="215" font-family="Arial" font-size="13" fill="#333333">  le WS-Simulator écoute pour cette notification</text>
  <text x="490" y="235" font-family="Arial" font-size="13" fill="#333333">• Cette URL est ajoutée au chemin de base:</text>
  <text x="490" y="255" font-family="Arial" font-size="13" fill="#333333">  http://[host]:8080/notif-simulator/token/status-notification</text>
  
  <text x="490" y="290" font-family="Arial" font-size="14" font-weight="bold" fill="#2e7d32">2. Notification</text>
  <text x="490" y="310" font-family="Arial" font-size="13" fill="#333333">• Définit les champs attendus et leurs formats</text>
  <text x="490" y="330" font-family="Arial" font-size="13" fill="#333333">• mandatory: true = champ obligatoire</text>
  <text x="490" y="350" font-family="Arial" font-size="13" fill="#333333">• format: expression régulière de validation</text>
  <text x="490" y="370" font-family="Arial" font-size="13" fill="#333333">• Exemples:</text>
  <text x="510" y="390" font-family="Arial" font-size="13" fill="#333333">- "[0-9a-zA-Z ]{16}" = 16 caractères alphanumériques</text>
  <text x="510" y="410" font-family="Arial" font-size="13" fill="#333333">- "[0-9]{4}" = 4 chiffres (format date d'expiration)</text>
  <text x="510" y="430" font-family="Arial" font-size="13" fill="#333333">- "[0-9]{2}" = 2 chiffres (code de raison)</text>
  
  <text x="490" y="465" font-family="Arial" font-size="14" font-weight="bold" fill="#2e7d32">3. Ack (Accusé de réception)</text>
  <text x="490" y="485" font-family="Arial" font-size="13" fill="#333333">• Définit la structure de réponse du simulateur</text>
  <text x="490" y="505" font-family="Arial" font-size="13" fill="#333333">• Symboles et valeurs spéciales:</text>
  <text x="510" y="525" font-family="Arial" font-size="13" fill="#333333">- "?" = valeur reprise de la requête entrante</text>
  <text x="510" y="545" font-family="Arial" font-size="13" fill="#333333">- Valeurs codées en dur ("00", "0000") = codes</text>
  <text x="530" y="565" font-family="Arial" font-size="13" fill="#333333">de réponse prédéfinis (succès)</text>
  
  <text x="490" y="595" font-family="Arial" font-size="14" font-weight="bold" fill="#2e7d32">4. Traitement automatique</text>
  <text x="490" y="615" font-family="Arial" font-size="13" fill="#333333">• Le simulateur valide et répond automatiquement</text>
  <text x="490" y="635" font-family="Arial" font-size="13" fill="#333333">  selon cette configuration, sans programmation</text>
  <text x="490" y="655" font-family="Arial" font-size="13" fill="#333333">• Toute notification respectant ce format sera</text>
  <text x="490" y="675" font-family="Arial" font-size="13" fill="#333333">  traitée et recevra une réponse formatée</text>
</svg>
