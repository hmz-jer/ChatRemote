
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 700">
  <!-- Fond principal -->
  <rect width="900" height="700" fill="#ffffff" rx="0" ry="0"/>
  
  <!-- Titre principal -->
  <text x="450" y="40" font-family="Arial" font-size="26" text-anchor="middle" fill="#333333" font-weight="bold">Structure JSON pour Notifications Dynamiques</text>
  
  <!-- Section structure actuelle -->
  <rect x="50" y="70" width="800" height="280" fill="#e8eaf6" rx="5" ry="5" stroke="#3949ab" stroke-width="2"/>
  <text x="80" y="100" font-family="Arial" font-size="20" fill="#283593" font-weight="bold">Structure actuelle</text>
  
  <rect x="80" y="120" width="740" height="210" fill="#ffffff" rx="5" ry="5" stroke="#7986cb" stroke-width="1.5"/>
  <text x="100" y="150" font-family="Consolas, monospace" font-size="16" fill="#212121">
    <tspan x="100" y="150">{</tspan>
    <tspan x="120" y="180">  "path": "/token/status-notification",</tspan>
    <tspan x="120" y="210">  "notification": {</tspan>
    <tspan x="140" y="240">    "CustMsgId": { "mandatory": true, "format": "[0-9a-zA-Z ]{16}" },</tspan>
    <tspan x="140" y="270">    "CustCnxId": { "mandatory": true, "format": "[0-9a-zA-Z ]{12}" },</tspan>
    <tspan x="140" y="300">    ...</tspan>
    <tspan x="120" y="330">  },</tspan>
    <tspan x="120" y="360">  "ack": {</tspan>
    <tspan x="140" y="390">    "CustMsgId": "?",</tspan>
    <tspan x="140" y="420">    "CustCnxId": "?",</tspan>
    <tspan x="140" y="450">    "RspnCode": "00",</tspan>
    <tspan x="140" y="480">    "RspnRsn": "0000"</tspan>
    <tspan x="120" y="510">  }</tspan>
    <tspan x="100" y="540">}</tspan>
  </text>
  
  <!-- Flèche de transition -->
  <path d="M450,370 L450,430" stroke="#333333" stroke-width="3" fill="none" marker-end="url(#arrow)"/>
  <defs>
    <marker id="arrow" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
      <polygon points="0 0, 10 3.5, 0 7" fill="#333333"/>
    </marker>
  </defs>
  
  <!-- Section structure nouvelle -->
  <rect x="50" y="430" width="800" height="400" fill="#e0f2f1" rx="5" ry="5" stroke="#00796b" stroke-width="2"/>
  <text x="80" y="460" font-family="Arial" font-size="20" fill="#00695c" font-weight="bold">Structure proposée</text>
  
  <rect x="80" y="480" width="740" height="330" fill="#ffffff" rx="5" ry="5" stroke="#4db6ac" stroke-width="1.5"/>
  <text x="100" y="510" font-family="Consolas, monospace" font-size="16" fill="#212121">
    <tspan x="100" y="510">{</tspan>
    <tspan x="120" y="540">  "path": "/token/status-notification",</tspan>
    <tspan x="120" y="570">  "notification": { ... },</tspan>
    <tspan x="120" y="600">  "ack": {</tspan>
    <tspan x="140" y="630">    "type": "dynamic",</tspan>
    <tspan x="140" y="660">    "csvConfig": {</tspan>
    <tspan x="160" y="690">      "discriminantFields": ["RequestTypeExpected", "PANRefIdExpected"],</tspan>
    <tspan x="160" y="720">      "additionalDiscriminants": ["Tag1Expected", "Tag2Expected"],</tspan>
    <tspan x="160" y="750">      "defaultScenario": {</tspan>
    <tspan x="180" y="780">        "RspnCode": "00", "RspnRsn": "0000"</tspan>
    <tspan x="160" y="810">      }</tspan>
    <tspan x="140" y="840">    }</tspan>
    <tspan x="120" y="870">  }</tspan>
    <tspan x="100" y="900">}</tspan>
  </text>
  
  <!-- Annotations -->
  <rect x="700" y="630" width="20" height="20" fill="#4caf50" rx="3" ry="3"/>
  <text x="730" y="645" font-family="Arial" font-size="16" fill="#333333" text-anchor="start">Nouveau type</text>
  
  <rect x="700" y="690" width="20" height="20" fill="#2196f3" rx="3" ry="3"/>
  <text x="730" y="705" font-family="Arial" font-size="16" fill="#333333" text-anchor="start">Clés primaires</text>
  
  <rect x="700" y="720" width="20" height="20" fill="#9c27b0" rx="3" ry="3"/>
  <text x="730" y="735" font-family="Arial" font-size="16" fill="#333333" text-anchor="start">Clés secondaires</text>
  
  <rect x="700" y="780" width="20" height="20" fill="#ff9800" rx="3" ry="3"/>
  <text x="730" y="795" font-family="Arial" font-size="16" fill="#333333" text-anchor="start">Scénario par défaut</text>
</svg>
