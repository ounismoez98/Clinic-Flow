/**
 * Keycloak connection settings for the Angular app.
 * url     -> where Keycloak is reachable from the browser (NOT the docker name)
 * realm   -> the realm we imported (clinic-flow-realm.json)
 * clientId-> the public client defined in that realm file
 */
export const keycloakConfig = {
  url: 'http://localhost:8180',
  realm: 'clinic-flow',
  clientId: 'clinic-flow-frontend',
};
