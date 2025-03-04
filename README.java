
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 700">
  <!-- Background -->
  <rect width="800" height="700" fill="#f8f9fa" />
  
  <!-- Title -->
  <text x="400" y="30" font-family="Arial" font-size="22" font-weight="bold" text-anchor="middle" fill="#212529">Architecture WS-Simulator</text>
  
  <!-- Access Protocols -->
  <rect x="50" y="50" width="700" height="40" rx="5" fill="#e7f5ff" stroke="#339af0" stroke-width="1" />
  <text x="400" y="75" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="#1864ab">Protocoles d'accès: HTTP | HTTPS | CURL | REST</text>
  
  <!-- Main Container -->
  <rect x="50" y="100" width="700" height="500" rx="10" fill="#e7f5ff" stroke="#1864ab" stroke-width="2" />
  <text x="400" y="125" font-family="Arial" font-size="18" font-weight="bold" text-anchor="middle" fill="#1864ab">WS-Simulator</text>

  <!-- Web Interface Layer -->
  <rect x="100" y="140" width="600" height="90" rx="5" fill="#4dabf7" stroke="#1971c2" stroke-width="2" />
  <text x="400" y="160" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="#f8f9fa">Interface Web</text>
  
  <rect x="115" y="170" width="140" height="50" rx="3" fill="#e7f5ff" stroke="#74c0fc" stroke-width="1" />
  <text x="185" y="190" font-family="Arial" font-size="10" font-weight="bold" text-anchor="middle" fill="#1864ab">Simulator Request</text>
  <text x="185" y="205" font-family="Arial" font-size="8" text-anchor="middle" fill="#495057">Création et gestion des requêtes</text>
  
  <rect x="265" y="170" width="140" height="50" rx="3" fill="#e7f5ff" stroke="#74c0fc" stroke-width="1" />
  <text x="335" y="190" font-family="Arial" font-size="10" font-weight="bold" text-anchor="middle" fill="#1864ab">Test Results</text>
  <text x="335" y="205" font-family="Arial" font-size="8" text-anchor="middle" fill="#495057">Visualisation des résultats</text>
  
  <rect x="415" y="170" width="140" height="50" rx="3" fill="#e7f5ff" stroke="#74c0fc" stroke-width="1" />
  <text x="485" y="190" font-family="Arial" font-size="10" font-weight="bold" text-anchor="middle" fill="#1864ab">Notifications List</text>
  <text x="485" y="205" font-family="Arial" font-size="8" text-anchor="middle" fill="#495057">Suivi des notifications</text>
  
  <rect x="565" y="170" width="120" height="50" rx="3" fill="#e7f5ff" stroke="#74c0fc" stroke-width="1" />
  <text x="625" y="190" font-family="Arial" font-size="10" font-weight="bold" text-anchor="middle" fill="#1864ab">Config UI</text>
  <text x="625" y="205" font-family="Arial" font-size="8" text-anchor="middle" fill="#495057">Paramétrage</text>
  
  <!-- Controller Layer -->
  <rect x="100" y="240" width="600" height="70" rx="5" fill="#1c7ed6" stroke="#1864ab" stroke-width="2" />
  <text x="400" y="265" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="#f8f9fa">Contrôleurs REST</text>
  <text x="160" y="290" font-family="Arial" font-size="10" fill="#f8f9fa">WelcomeController</text>
  <text x="280" y="290" font-family="Arial" font-size="10" fill="#f8f9fa">NotificationController</text>
  <text x="420" y="290" font-family="Arial" font-size="10" fill="#f8f9fa">SimulationController</text>
  <text x="540" y="290" font-family="Arial" font-size="10" fill="#f8f9fa">FrontController</text>
  <text x="650" y="290" font-family="Arial" font-size="10" fill="#f8f9fa">APIController</text>
  
  <!-- Service Layer -->
  <rect x="100" y="320" width="600" height="70" rx="5" fill="#339af0" stroke="#1864ab" stroke-width="2" />
  <text x="400" y="345" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="#f8f9fa">Services</text>
  <text x="190" y="365" font-family="Arial" font-size="10" fill="#f8f9fa">NotificationSimulationService</text>
  <text x="380" y="365" font-family="Arial" font-size="10" fill="#f8f9fa">SimulationService</text>
  <text x="550" y="365" font-family="Arial" font-size="10" fill="#f8f9fa">SchedulerService</text>
  
  <!-- Data Components -->
  <rect x="100" y="400" width="380" height="70" rx="5" fill="#74c0fc" stroke="#339af0" stroke-width="2" />
  <text x="290" y="425" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="#1864ab">Composants de Données</text>
  <text x="190" y="445" font-family="Arial" font-size="10" fill="#1864ab">Formats: JWSE / Plain Text</text>
  <text x="380" y="445" font-family="Arial" font-size="10" fill="#1864ab">Modes: ASYNC / SYNC</text>
  
  <!-- Scheduler -->
  <rect x="490" y="400" width="210" height="70" rx="5" fill="#74c0fc" stroke="#339af0" stroke-width="2" />
  <text x="595" y="425" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="#1864ab">Scheduler</text>
  <text x="595" y="445" font-family="Arial" font-size="10" fill="#1864ab">Tâches planifiées / Cron Expression</text>
  
  <!-- Database -->
  <rect x="100" y="480" width="380" height="70" rx="5" fill="#4dabf7" stroke="#1971c2" stroke-width="2" />
  <text x="290" y="505" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="#f8f9fa">Base de Données H2</text>
  <text x="290" y="525" font-family="Arial" font-size="10" fill="#f8f9fa">Base de données en mémoire pour stockage temporaire</text>
  
  <!-- Specifications -->
  <rect x="490" y="480" width="210" height="70" rx="5" fill="#4dabf7" stroke="#1971c2" stroke-width="2" />
  <text x="595" y="505" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="#f8f9fa">Spécifications</text>
  <text x="595" y="525" font-family="Arial" font-size="10" fill="#f8f9fa">Définitions JSON / Validations</text>
  
  <!-- File System -->
  <rect x="50" y="610" width="700" height="40" rx="5" fill="#1864ab" stroke="#1971c2" stroke-width="2" />
  <text x="400" y="635" font-family="Arial" font-size="14" font-weight="bold" text-anchor="middle" fill="#f8f9fa">Système de fichiers</text>
  
  <!-- Arrows -->
  <line x1="400" y1="230" x2="400" y2="240" stroke="#1864ab" stroke-width="2" />
  <polygon points="400,240 395,230 405,230" fill="#1864ab" />
  
  <line x1="400" y1="310" x2="400" y2="320" stroke="#1864ab" stroke-width="2" />
  <polygon points="400,320 395,310 405,310" fill="#1864ab" />
  
  <line x1="350" y1="390" x2="350" y2="400" stroke="#1864ab" stroke-width="2" />
  <polygon points="350,400 345,390 355,390" fill="#1864ab" />
  
  <line x1="595" y1="390" x2="595" y2="400" stroke="#1864ab" stroke-width="2" />
  <polygon points="595,400 590,390 600,390" fill="#1864ab" />
  
  <line x1="290" y1="470" x2="290" y2="480" stroke="#1864ab" stroke-width="2" />
  <polygon points="290,480 285,470 295,470" fill="#1864ab" />
  
  <line x1="595" y1="470" x2="595" y2="480" stroke="#1864ab" stroke-width="2" />
  <polygon points="595,480 590,470 600,470" fill="#1864ab" />
  
  <line x1="290" y1="550" x2="290" y2="610" stroke="#1864ab" stroke-width="2" />
  <polygon points="290,610 285,600 295,600" fill="#1864ab" />
  
  <line x1="595" y1="550" x2="595" y2="610" stroke="#1864ab" stroke-width="2" />
  <polygon points="595,610 590,600 600,600" fill="#1864ab" />
  
  <!-- Connecting lines -->
  <line x1="480" y1="435" x2="490" y2="435" stroke="#1864ab" stroke-width="2" />
  <polygon points="490,435 480,430 480,440" fill="#1864ab" />
  
  <line x1="480" y1="515" x2="490" y2="515" stroke="#1864ab" stroke-width="2" />
  <polygon points="490,515 480,510 480,520" fill="#1864ab" />
</svg>
