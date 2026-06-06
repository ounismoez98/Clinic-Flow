import { config, urls } from '../config.js';

/**
 * Thin client over Keycloak's Admin REST API.
 * Caches the admin token until it is about to expire.
 */

let cachedToken = null;
let tokenExpiresAt = 0;

async function getAdminToken() {
  // Reuse the token if it still has > 10s of life.
  if (cachedToken && Date.now() < tokenExpiresAt - 10_000) {
    return cachedToken;
  }

  const body = new URLSearchParams({
    grant_type: 'password',
    client_id: 'admin-cli',
    username: config.keycloak.adminUsername,
    password: config.keycloak.adminPassword,
  });

  const res = await fetch(urls.masterToken, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  });
  if (!res.ok) {
    throw new Error(`Failed to get admin token (${res.status})`);
  }
  const data = await res.json();
  cachedToken = data.access_token;
  tokenExpiresAt = Date.now() + data.expires_in * 1000;
  return cachedToken;
}

/** Helper: call the admin API with the admin token attached. */
async function adminFetch(path, options = {}) {
  const token = await getAdminToken();
  const res = await fetch(`${urls.adminRealm}${path}`, {
    ...options,
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
  });
  return res;
}

/** Map Keycloak's verbose user object to a clean shape for the UI. */
function toUser(u) {
  return {
    id: u.id,
    username: u.username,
    email: u.email,
    firstName: u.firstName ?? '',
    lastName: u.lastName ?? '',
    enabled: u.enabled,
    emailVerified: u.emailVerified,
    createdTimestamp: u.createdTimestamp,
  };
}

export const keycloakAdmin = {
  // ---------- Users ----------
  async listUsers({ search = '', max = 100 } = {}) {
    const q = new URLSearchParams({ max: String(max) });
    if (search) q.set('search', search);
    const res = await adminFetch(`/users?${q.toString()}`);
    if (!res.ok) throw new Error(`listUsers failed (${res.status})`);
    const users = await res.json();
    return users.map(toUser);
  },

  async getUser(id) {
    const res = await adminFetch(`/users/${id}`);
    if (res.status === 404) return null;
    if (!res.ok) throw new Error(`getUser failed (${res.status})`);
    return toUser(await res.json());
  },

  async createUser({ username, email, firstName, lastName, password, role }) {
    const res = await adminFetch('/users', {
      method: 'POST',
      body: JSON.stringify({
        username,
        email,
        firstName: firstName || 'User',
        lastName: lastName || 'Account',
        enabled: true,
        emailVerified: true,
        requiredActions: [],
        credentials: password
          ? [{ type: 'password', value: password, temporary: false }]
          : [],
      }),
    });
    if (res.status !== 201) {
      const text = await res.text();
      throw new Error(`createUser failed (${res.status}): ${text}`);
    }
    // Keycloak returns the new user's URL in the Location header.
    const location = res.headers.get('location') || '';
    const id = location.split('/').pop();
    if (role) await this.assignRole(id, role);
    return this.getUser(id);
  },

  async updateUser(id, fields) {
    const res = await adminFetch(`/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(fields),
    });
    if (res.status === 404) return null;
    if (res.status !== 204) throw new Error(`updateUser failed (${res.status})`);
    return this.getUser(id);
  },

  async deleteUser(id) {
    const res = await adminFetch(`/users/${id}`, { method: 'DELETE' });
    if (res.status === 404) return false;
    if (res.status !== 204) throw new Error(`deleteUser failed (${res.status})`);
    return true;
  },

  // ---------- Roles ----------
  async listRealmRoles() {
    const res = await adminFetch('/roles');
    if (!res.ok) throw new Error(`listRealmRoles failed (${res.status})`);
    const roles = await res.json();
    return roles.map((r) => ({ id: r.id, name: r.name, description: r.description ?? '' }));
  },

  async getUserRoles(id) {
    const res = await adminFetch(`/users/${id}/role-mappings/realm`);
    if (!res.ok) throw new Error(`getUserRoles failed (${res.status})`);
    const roles = await res.json();
    return roles.map((r) => r.name);
  },

  async assignRole(userId, roleName) {
    const roleRes = await adminFetch(`/roles/${roleName}`);
    if (!roleRes.ok) throw new Error(`role '${roleName}' not found`);
    const role = await roleRes.json();
    const res = await adminFetch(`/users/${userId}/role-mappings/realm`, {
      method: 'POST',
      body: JSON.stringify([role]),
    });
    if (res.status !== 204) throw new Error(`assignRole failed (${res.status})`);
    return true;
  },

  async removeRole(userId, roleName) {
    const roleRes = await adminFetch(`/roles/${roleName}`);
    if (!roleRes.ok) throw new Error(`role '${roleName}' not found`);
    const role = await roleRes.json();
    const res = await adminFetch(`/users/${userId}/role-mappings/realm`, {
      method: 'DELETE',
      body: JSON.stringify([role]),
    });
    if (res.status !== 204) throw new Error(`removeRole failed (${res.status})`);
    return true;
  },
};
