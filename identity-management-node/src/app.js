import express from 'express';
import cors from 'cors';
import { config } from './config.js';

import profileRoutes from './routes/profile.js';
import usersRoutes from './routes/users.js';
import rolesRoutes from './routes/roles.js';
import internalRoutes from './routes/internal.js';

export function createApp() {
  const app = express();

  app.use(cors({ origin: config.corsOrigin, credentials: true }));
  app.use(express.json());

  // Public health check (no auth).
  app.get('/health', (_req, res) => res.json({ status: 'up', service: 'identity-management-node' }));

  // Identity endpoints.
  app.use('/api/profile', profileRoutes);
  app.use('/api/users', usersRoutes);
  app.use('/api/roles', rolesRoutes);
  app.use('/api/internal', internalRoutes);

  // 404 for anything else.
  app.use((_req, res) => res.status(404).json({ error: 'Not found' }));

  // Central error handler.
  app.use((err, _req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'Internal error', detail: err.message });
  });

  return app;
}
