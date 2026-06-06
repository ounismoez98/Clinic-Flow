import { Router } from 'express';
import { requireAuth, requireRole } from '../middleware/auth.js';
import { keycloakAdmin } from '../services/keycloakAdmin.js';

const router = Router();

// Every route here requires a valid token AND the ADMIN role.
router.use(requireAuth, requireRole('ADMIN'));

// Wrap async handlers so thrown errors reach the error middleware.
const wrap = (fn) => (req, res, next) => Promise.resolve(fn(req, res, next)).catch(next);

/** GET /api/users?search=  -> list users (optionally filtered) */
router.get('/', wrap(async (req, res) => {
  const users = await keycloakAdmin.listUsers({ search: req.query.search || '' });
  res.json(users);
}));

/** GET /api/users/:id -> one user + their roles */
router.get('/:id', wrap(async (req, res) => {
  const user = await keycloakAdmin.getUser(req.params.id);
  if (!user) return res.status(404).json({ error: 'User not found' });
  user.roles = await keycloakAdmin.getUserRoles(req.params.id);
  res.json(user);
}));

/** POST /api/users -> create a user (optionally with a role) */
router.post('/', wrap(async (req, res) => {
  const { username, email, firstName, lastName, password, role } = req.body;
  if (!username || !email) {
    return res.status(400).json({ error: 'username and email are required' });
  }
  const created = await keycloakAdmin.createUser({ username, email, firstName, lastName, password, role });
  res.status(201).json(created);
}));

/** PUT /api/users/:id -> update basic fields (firstName/lastName/email/enabled) */
router.put('/:id', wrap(async (req, res) => {
  const { firstName, lastName, email, enabled } = req.body;
  const updated = await keycloakAdmin.updateUser(req.params.id, { firstName, lastName, email, enabled });
  if (!updated) return res.status(404).json({ error: 'User not found' });
  res.json(updated);
}));

/** DELETE /api/users/:id */
router.delete('/:id', wrap(async (req, res) => {
  const ok = await keycloakAdmin.deleteUser(req.params.id);
  if (!ok) return res.status(404).json({ error: 'User not found' });
  res.status(204).end();
}));

/** POST /api/users/:id/roles/:role -> assign a realm role */
router.post('/:id/roles/:role', wrap(async (req, res) => {
  await keycloakAdmin.assignRole(req.params.id, req.params.role);
  res.json({ message: `Role ${req.params.role} assigned` });
}));

/** DELETE /api/users/:id/roles/:role -> remove a realm role */
router.delete('/:id/roles/:role', wrap(async (req, res) => {
  await keycloakAdmin.removeRole(req.params.id, req.params.role);
  res.json({ message: `Role ${req.params.role} removed` });
}));

export default router;
