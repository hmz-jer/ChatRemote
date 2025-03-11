
{
  "path": "/token/enrollcardtoken",
  "type": "emetteur",
  "notification": {
    "RequestType": {
      "mandatory": true,
      "format": "EnrollCardToken"
    },
    "PANRefId": {
      "mandatory": true,
      "format": "[0-9a-zA-Z]{32}"
    },
    "Tag1": {
      "mandatory": false,
      "format": "[0-9a-zA-Z]+"
    },
    "Tag2": {
      "mandatory": false,
      "format": "[0-9a-zA-Z]+"
    },
    "Tag3": {
      "mandatory": false,
      "format": "[0-9a-zA-Z]+"
    }
  },
  "csvConfig": {
    "enabled": true,
    "filePath": "config/enrollcardtoken_scenarios.csv",
    "defaultScenarioId": "00000",
    "scenarioIdentifier": "PANRefId",
    "tempoColumn": "Tempo"
  }
}
