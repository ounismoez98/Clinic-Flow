import { Eureka } from 'eureka-js-client';
import { config } from './config.js';

/**
 * Registers this Node service with Eureka so the Spring Cloud Gateway can
 * discover it (route: lb://IDENTITY-SERVICE) — exactly like a Spring service.
 * Eureka health/heartbeat is handled by the client.
 */
export function startEureka() {
  const client = new Eureka({
    instance: {
      app: config.eureka.serviceName,
      instanceId: `${config.eureka.serviceHostname}:${config.eureka.serviceName}:${config.port}`,
      hostName: config.eureka.serviceHostname,
      // Use the same advertised hostname for ipAddr so the gateway (in Docker)
      // resolves it via host.docker.internal rather than a host-only 127.0.0.1.
      ipAddr: config.eureka.serviceHostname,
      statusPageUrl: `http://${config.eureka.serviceHostname}:${config.port}/health`,
      healthCheckUrl: `http://${config.eureka.serviceHostname}:${config.port}/health`,
      port: { $: config.port, '@enabled': true },
      vipAddress: config.eureka.serviceName,
      dataCenterInfo: {
        '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
        name: 'MyOwn',
      },
    },
    eureka: {
      host: config.eureka.host,
      port: config.eureka.port,
      servicePath: '/eureka/apps/',
      maxRetries: 10,
      requestRetryDelay: 2000,
    },
  });

  client.logger.level('warn');
  client.start((err) => {
    if (err) console.error('[eureka] registration failed:', err.message);
    else console.log(`[eureka] registered as ${config.eureka.serviceName}`);
  });

  // De-register cleanly on shutdown.
  const stop = () => client.stop(() => process.exit(0));
  process.on('SIGINT', stop);
  process.on('SIGTERM', stop);

  return client;
}
