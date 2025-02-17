
simulation:
  log_level: "DEBUG"

scenarios:
  K1:
    request_file: "requests/K1_request.json"
    request_endpoint: "http://localhost:8081/src/K9"
    expected_response: "responses/K1_response.json"
    
