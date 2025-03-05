 <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 500">
  <rect width="800" height="500" fill="white"/>
  <text x="400" y="50" font-family="Arial" font-size="28" font-weight="bold" text-anchor="middle" fill="#333">Commandes pour les notifications</text>
  
  <!-- Header for first section -->
  <rect x="50" y="80" width="700" height="40" rx="5" fill="#e8f5e9"/>
  <text x="400" y="105" font-family="Arial" font-size="18" font-weight="bold" text-anchor="middle" fill="#333">Envoi de notifications vers le WS-Simulator</text>
  
  <!-- Command 1 -->
  <rect x="50" y="130" width="700" height="70" rx="5" fill="#f5f5f5"/>
  <text x="70" y="155" font-family="Arial" font-size="16" font-weight="bold" fill="#333">Envoi d'une notification de statut:</text>
  <text x="70" y="185" font-family="Courier New" font-size="14" fill="#333">curl -X POST http://localhost:8080/notif-simulator/token/status-notification \</text>
  <text x="70" y="205" font-family="Courier New" font-size="14" fill="#333">  -d '{"CustMsgId":"1234567890123456", "TokStatus":"A", "NotifReason":"01"}'</text>
  
  <!-- Command 2 -->
  <rect x="50" y="210" width="700" height="70" rx="5" fill="#e8f5e9"/>
  <text x="70" y="235" font-family="Arial" font-size="16" font-weight="bold" fill="#333">Vérification des notifications reçues:</text>
  <text x="70" y="265" font-family="Courier New" font-size="14" fill="#333">curl -X GET http://localhost:8080/api-simulator/notification/logs</text>
  
  <!-- Header for second section -->
  <rect x="50" y="290" width="700" height="40" rx="5" fill="#e8f5e9"/>
  <text x="400" y="315" font-family="Arial" font-size="18" font-weight="bold" text-anchor="middle" fill="#333">Codes de réponse pour les notifications</text>
  
  <!-- Response codes table -->
  <rect x="50" y="340" width="200" height="40" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="250" y="340" width="500" height="40" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <text x="150" y="365" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="#333">Code</text>
  <text x="500" y="365" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="#333">Description</text>
  
  <!-- Response code 1 -->
  <rect x="50" y="380" width="200" height="40" fill="white" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="250" y="380" width="500" height="40" fill="white" stroke="#e0e0e0" stroke-width="1"/>
  <text x="150" y="405" font-family="Arial" font-size="16" text-anchor="middle" fill="#333">200 / OK</text>
  <text x="500" y="405" font-family="Arial" font-size="16" text-anchor="middle" fill="#333">Notification bien traitée - accusé "valid"</text>
  
  <!-- Response code 2 -->
  <rect x="50" y="420" width="200" height="40" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <rect x="250" y="420" width="500" height="40" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="1"/>
  <text x="150" y="445" font-family="Arial" font-size="16" text-anchor="middle" fill="#333">422</text>
  <text x="500" y="445" font-family="Arial" font-size="16" text-anchor="middle" fill="#333">Notification invalide (ne respecte pas les spécifications)</text>
</svg>
