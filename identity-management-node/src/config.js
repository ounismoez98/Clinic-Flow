import 'dotenv/config';

export const config = {
  port: process.env.PORT || 5000,
  corsOrigin: process.env.CORS_ORIGIN || 'http://localhost:4200',

  // Shared secret for trusted service-to-service calls (e.g. MSPatientMedcin
  // asking us to provision a Keycloak login). Not a user token.
  internalApiKey: process.env.INTERNAL_API_KEY || 'clinicflow-internal-secret',

  keycloak: {
    baseUrl: process.env.KEYCLOAK_BASE_URL || 'http://localhost:8180',
    realm: process.env.KEYCLOAK_REALM || 'clinic-flow',
    issuer: process.env.KEYCLOAK_ISSUER || 'http://localhost:8180/realms/clinic-flow',
    adminUsername: process.env.KEYCLOAK_ADMIN_USERNAME || 'admin',
    adminPassword: process.env.KEYCLOAK_ADMIN_PASSWORD || 'admin',
  },

  eureka: {
    host: process.env.EUREKA_HOST || 'localhost',
    port: Number(process.env.EUREKA_PORT || 8761),
    serviceName: process.env.SERVICE_NAME || 'IDENTITY-SERVICE',
    serviceHostname: process.env.SERVICE_HOSTNAME || 'localhost',
  },
};

// Convenience URLs derived from the config above.
export const urls = {
  // Where Keycloak publishes its public keys (to verify token signatures).
  jwks: `${config.keycloak.baseUrl}/realms/${config.keycloak.realm}/protocol/openid-connect/certs`,
  // Admin REST API base for managing users/roles in our realm.
  adminRealm: `${config.keycloak.baseUrl}/admin/realms/${config.keycloak.realm}`,
  // Token endpoint on the master realm (to get an admin token).
  masterToken: `${config.keycloak.baseUrl}/realms/master/protocol/openid-connect/token`,
};
