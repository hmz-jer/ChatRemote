
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 500">
  <!-- Background -->
  <rect width="900" height="500" fill="white"/>
  
  <!-- Title -->
  <text x="30" y="40" font-family="Arial" font-size="24" font-weight="bold" fill="#333333">Flux de notification avec WS-Simulator</text>
  
  <!-- Actors -->
  <rect x="100" y="100" width="140" height="60" rx="5" fill="#6f42c1" stroke="#5a32a3" stroke-width="2"/>
  <text x="170" y="137" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="white">WS-Simulator</text>
  
  <rect x="400" y="100" width="140" height="60" rx="5" fill="#2e86c1" stroke="#2874a6" stroke-width="2"/>
  <text x="470" y="137" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="white">TEH</text>
  
  <rect x="700" y="100" width="140" height="60" rx="5" fill="#20c997" stroke="#17a37f" stroke-width="2"/>
  <text x="770" y="137" font-family="Arial" font-size="16" font-weight="bold" text-anchor="middle" fill="white">TSP</text>
  
  <!-- Lifelines -->
  <line x1="170" y1="160" x2="170" y2="460" stroke="#333" stroke-width="2"/>
  <line x1="470" y1="160" x2="470" y2="460" stroke="#333" stroke-width="2"/>
  <line x1="770" y1="160" x2="770" y2="460" stroke="#333" stroke-width="2"/>
  
  <!-- Main box -->
  <rect x="50" y="180" width="800" height="260" rx="20" ry="20" fill="none" stroke="#20c997" stroke-width="2" stroke-dasharray="5,5"/>
  
  <!-- "Initiated by TSP" note -->
  <rect x="720" y="200" width="100" height="60" fill="#f8f9fa" stroke="#dee2e6" stroke-width="1"/>
  <text x="770" y="230" font-family="Arial" font-size="13" text-anchor="middle" fill="#333333">Initié par</text>
  <text x="770" y="250" font-family="Arial" font-size="13" text-anchor="middle" fill="#333333">TSP</text>
  
  <!-- 1. TokenStatusNotification from TSP to TEH -->
  <line x1="770" y1="230" x2="470" y2="230" stroke="#333" stroke-width="1.5" marker-end="url(#arrow)"/>
  <text x="620" y="220" font-family="Arial" font-size="12" text-anchor="middle" fill="#333333">1. TokenStatusNotification</text>
  <text x="620" y="235" font-family="Arial" font-size="10" text-anchor="middle" fill="#333333">{TokRefId, TokStatus, NotifyReason...}</text>
  
  <!-- 2. Acknowledge from TEH to TSP -->
  <line x1="470" y1="280" x2="770" y2="280" stroke="#333" stroke-width="1.5" marker-end="url(#arrow)"/>
  <text x="620" y="275" font-family="Arial" font-size="12" text-anchor="middle" fill="#333333">2. Acknowledge</text>
  <text x="620" y="290" font-family="Arial" font-size="10" text-anchor="middle" fill="#333333">{RspnCode, RspnRsn}</text>
  
  <!-- 3. TokenStatusNotification from TEH to WS-Simulator -->
  <line x1="470" y1="330" x2="170" y2="330" stroke="#333" stroke-width="1.5" marker-end="url(#arrow)"/>
  <text x="320" y="320" font-family="Arial" font-size="12" text-anchor="middle" fill="#333333">3. TokenStatusNotification</text>
  <text x="320" y="335" font-family="Arial" font-size="10" text-anchor="middle" fill="#333333">{TokRefId, TokStatus, NotifyReason...}</text>
  
  <!-- Verification Process by WS-Simulator -->
  <rect x="30" y="345" width="280" height="100" rx="5" fill="#f8f9fa" stroke="#6f42c1" stroke-width="2"/>
  <text x="40" y="365" font-family="Arial" font-size="12" font-weight="bold" fill="#333333">Traitement par WS-Simulator:</text>
  <text x="40" y="385" font-family="Arial" font-size="11" fill="#333333">1. Réception sur "/token/status-notification"</text>
  <text x="40" y="405" font-family="Arial" font-size="11" fill="#333333">2. Validation selon spécification JSON</text>
  <text x="40" y="425" font-family="Arial" font-size="11" fill="#333333">3. Préparation de l'accusé de réception</text>
  <text x="40" y="445" font-family="Arial" font-size="11" fill="#333333">4. Journalisation de la notification</text>
  
  <!-- 4. Acknowledge from WS-Simulator to TEH -->
  <line x1="170" y1="400" x2="470" y2="400" stroke="#333" stroke-width="1.5" marker-end="url(#arrow)"/>
  <text x="320" y="390" font-family="Arial" font-size="12" text-anchor="middle" fill="#333333">4. Acknowledge</text>
  <text x="320" y="415" font-family="Arial" font-size="10" text-anchor="middle" fill="#333333">{CustMsgId, CustCnxId, RspnCode:"00"}</text>
  
  <!-- Arrow marker definition -->
  <defs>
    <marker id="arrow" markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto" markerUnits="strokeWidth">
      <path d="M0,0 L0,6 L9,3 z" fill="#333" />
    </marker>
  </defs>
</svg>
