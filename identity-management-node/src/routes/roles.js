import { Router } from 'express';
import { requireAuth, requireRole } from '../middleware/auth.js';
import { keycloakAdmin } from '../services/keycloakAdmin.js';

const router = Router();

const wrap = (fn) => (req, res, next) => Promise.resolve(fn(req, res, next)).catch(next);

/** GET /api/roles -> list all realm roles. Any authenticated user may read. */
router.get('/', requireAuth, wrap(async (_req, res) => {
  const roles = await keycloakAdmin.listRealmRoles();
  res.json(roles);
}));

export default router;
