import { Router } from 'express';
import { requireInternalKey } from '../middleware/auth.js';
import { keycloakAdmin } from '../services/keycloakAdmin.js';

const router = Router();

// All internal routes are authenticated by the shared service secret, not a user token.
router.use(requireInternalKey);

const wrap = (fn) => (req, res, next) => Promise.resolve(fn(req, res, next)).catch(next);

/**
 * POST /api/internal/users
 * Provision a Keycloak login on behalf of a trusted backend (MSPatientMedcin).
 * This is THE single place where Keycloak users get created — other services
 * no longer talk to Keycloak's admin API directly.
 *
 * Body: { username, email, firstName, lastName, password, role }
 * Returns: the created user, or 200 with {alreadyExists:true} if the username
 * is taken (so patient/doctor creation is idempotent and never fails on a retry).
 */
router.post('/users', wrap(async (req, res) => {
  const { username, email, firstName, lastName, password, role } = req.body;
  if (!username || !email) {
    return res.status(400).json({ error: 'username and email are required' });
  }

  // If the user already exists in Keycloak, don't fail — just return it.
  const existing = await keycloakAdmin.listUsers({ search: username });
  const match = existing.find((u) => u.username === username || u.email === email);
  if (match) {
    return res.status(200).json({ ...match, alreadyExists: true });
  }

  const created = await keycloakAdmin.createUser({ username, email, firstName, lastName, password, role });
  res.status(201).json(created);
}));

export default router;
