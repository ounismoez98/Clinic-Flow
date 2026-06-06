import { createRemoteJWKSet, jwtVerify } from 'jose';
import { config, urls } from '../config.js';

/**
 * Keycloak token validation.
 *
 * We fetch Keycloak's public keys ONCE (jose caches the JWKS) and then verify
 * every incoming token locally — no network call to Keycloak per request.
 * This is exactly what the Spring gateway does, re-implemented in Node.
 */
const JWKS = createRemoteJWKSet(new URL(urls.jwks));

/** Express middleware: require a valid Keycloak access token. */
export async function requireAuth(req, res, next) {
  const header = req.headers.authorization || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;

  if (!token) {
    return res.status(401).json({ error: 'Missing Bearer token' });
  }

  try {
    const { payload } = await jwtVerify(token, JWKS, {
      issuer: config.keycloak.issuer, // token must be issued by our realm
    });

    // Attach a small, clean user object for the route handlers.
    req.user = {
      id: payload.sub,
      username: payload.preferred_username,
      email: payload.email,
      name: payload.name,
      roles: payload.realm_access?.roles ?? [],
    };
    next();
  } catch (err) {
    return res.status(401).json({ error: 'Invalid token', detail: err.message });
  }
}

/**
 * Service-to-service auth: a trusted backend (e.g. MSPatientMedcin) calls us
 * with a shared secret in the X-Internal-Api-Key header instead of a user token.
 * Used only for /api/internal/* endpoints.
 */
export function requireInternalKey(req, res, next) {
  const key = req.headers['x-internal-api-key'];
  if (!key || key !== config.internalApiKey) {
    return res.status(401).json({ error: 'Invalid internal API key' });
  }
  next();
}

/**
 * Express middleware factory: require the user to have one of the given roles.
 * Usage: router.get('/users', requireAuth, requireRole('ADMIN'), handler)
 */
export function requireRole(...allowed) {
  return (req, res, next) => {
    const roles = req.user?.roles ?? [];
    if (!allowed.some((r) => roles.includes(r))) {
      return res.status(403).json({ error: `Requires role: ${allowed.join(' or ')}` });
    }
    next();
  };
}
