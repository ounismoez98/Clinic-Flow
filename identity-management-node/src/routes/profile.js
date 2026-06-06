import { Router } from 'express';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

/**
 * GET /api/profile
 * Returns the currently logged-in user's identity, taken straight from the
 * validated Keycloak token. Any authenticated user can call this.
 */
router.get('/', requireAuth, (req, res) => {
  res.json(req.user);
});

export default router;
