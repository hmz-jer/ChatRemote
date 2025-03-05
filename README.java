 <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 500">
  <rect width="800" height="500" fill="white"/>
  <text x="400" y="50" font-family="Arial" font-size="28" font-weight="bold" text-anchor="middle" fill="#333">Commandes utiles du WS-Simulator</text>
  
  <!-- Command 1 -->
  <rect x="50" y="90" width="700" height="60" rx="5" fill="#e8f5e9"/>
  <text x="70" y="120" font-family="Arial" font-size="18" font-weight="bold" fill="#333">Simulation standard:</text>
  <text x="70" y="145" font-family="Courier New" font-size="14" fill="#333">curl -X POST http://localhost:8080/api-simulator/batch/csv -F "file=@01-createTokenVISA.csv"</text>
  
  <!-- Command 2 -->
  <rect x="50" y="170" width="700" height="60" rx="5" fill="#e8f5e9"/>
  <text x="70" y="200" font-family="Arial" font-size="18" font-weight="bold" fill="#333">Vérification du statut:</text>
  <text x="70" y="225" font-family="Courier New" font-size="14" fill="#333">curl -X GET http://localhost:8080/api-simulator/simulation/{simulation_id}</text>
  
  <!-- Command 3 -->
  <rect x="50" y="250" width="700" height="60" rx="5" fill="#e8f5e9"/>
  <text x="70" y="280" font-family="Arial" font-size="18" font-weight="bold" fill="#333">Récupération des réponses:</text>
  <text x="70" y="305" font-family="Courier New" font-size="14" fill="#333">curl -X GET http://localhost:8080/api-simulator/simulation/{id}/get-response-logs/0/10</text>
  
  <!-- Command 4 -->
  <rect x="50" y="330" width="700" height="60" rx="5" fill="#e8f5e9"/>
  <text x="70" y="360" font-family="Arial" font-size="18" font-weight="bold" fill="#333">Simulation planifiée:</text>
  <text x="70" y="385" font-family="Courier New" font-size="14" fill="#333">curl -X POST http://localhost:8080/api-simulator/batch/csv-scheduled -F "csvInitFile=@init.csv"</text>
  
  <!-- Command 5 -->
  <rect x="50" y="410" width="700" height="60" rx="5" fill="#e8f5e9"/>
  <text x="70" y="440" font-family="Arial" font-size="18" font-weight="bold" fill="#333">Arrêt de simulation planifiée:</text>
  <text x="70" y="465" font-family="Courier New" font-size="14" fill="#333">curl -X POST http://localhost:8080/api-simulator/batch/stop-csv-scheduled</text>
</svg>
