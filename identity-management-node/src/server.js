import { createApp } from './app.js';
import { config, urls } from './config.js';
import { startEureka } from './eureka.js';

const app = createApp();

app.listen(config.port, () => {
  console.log(`\n  Identity Management API (Node.js + Express)`);
  console.log(`  -------------------------------------------`);
  console.log(`  Listening on   http://localhost:${config.port}`);
  console.log(`  Keycloak realm ${config.keycloak.realm}`);
  console.log(`  Validating against JWKS: ${urls.jwks}\n`);

  // Register with Eureka so the Spring gateway can route to us.
  startEureka();
});
