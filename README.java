 const schema = {
  type: "object",
  properties: {
    id: { type: "number" },
    name: { type: "string" },
    email: { type: "string", format: "email" }
  },
  required: ["id", "name", "email"]
};

pm.test("Le corps de la réponse correspond au schéma JSON", function () {
  pm.response.to.have.jsonSchema(schema);
});
