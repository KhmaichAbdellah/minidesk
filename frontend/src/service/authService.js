// src/service/authService.js
// Service d'authentification : login, logout, récupération de l'utilisateur courant.
// Le JWT retourné par le backend est décodé pour extraire les informations de l'utilisateur
// (email, rôle, id) sans appel supplémentaire au serveur.

import api from './api';

/**
 * Décode manuellement le payload d'un token JWT (sans vérifier la signature côté client).
 * Utile côté frontend pour extraire l'email, le rôle et l'id de l'utilisateur.
 *
 * @param {string} token - Le JWT au format "header.payload.signature"
 * @returns {Object|null} Le payload décodé ou null en cas d'erreur
 */
const decodeJWT = (token) => {
  try {
    // Le payload est la 2ème partie du JWT, encodée en Base64URL
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    // Décodage Base64 → JSON avec support UTF-8
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    // En cas d'erreur de décodage, retourner null
    return null;
  }
};

/**
 * Connexion de l'utilisateur via POST /api/auth/login.
 * Stocke le JWT et les infos utilisateur dans localStorage.
 *
 * @param {string} email    - Email de l'utilisateur
 * @param {string} password - Mot de passe
 * @returns {Promise<Object>} Objet { token, user } où user = payload décodé du JWT
 */
export const login = async (email, password) => {
  // Appel vers POST /api/auth/login avec les identifiants
  const response = await api.post('/api/auth/login', { email, password });
  const { token } = response.data;

  // Décoder le JWT pour extraire les informations utilisateur
  const decoded = decodeJWT(token);

  // Construire l'objet user à partir des claims du JWT (incluant name et userId)
  const user = {
    email: decoded?.sub || email,
    role: decoded?.role || decoded?.roles?.[0] || 'USER',
    name: decoded?.name || (decoded?.sub ? decoded.sub.split('@')[0] : 'Utilisateur'),
    id: decoded?.userId || decoded?.id || null,
  };

  // Stocker le token et les infos utilisateur dans localStorage pour maintenir la session
  localStorage.setItem('token', token);
  localStorage.setItem('user', JSON.stringify(user));

  return { token, user };
};

/**
 * Déconnexion : supprime le token et l'utilisateur du localStorage.
 */
export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
};

/**
 * Récupère l'utilisateur actuellement connecté depuis localStorage.
 *
 * @returns {Object|null} L'objet user ou null si personne n'est connecté
 */
export const getCurrentUser = () => {
  const userStr = localStorage.getItem('user');
  try {
    return userStr ? JSON.parse(userStr) : null;
  } catch {
    return null;
  }
};

/**
 * Vérifie si le token JWT en localStorage est encore valide (non expiré).
 *
 * @returns {boolean} true si le token est présent et valide, false sinon
 */
export const isTokenValid = () => {
  const token = localStorage.getItem('token');
  if (!token) return false;
  const decoded = decodeJWT(token);
  if (!decoded?.exp) return false;
  // Comparer la date d'expiration (en secondes) avec la date actuelle
  return decoded.exp * 1000 > Date.now();
};

export default { login, logout, getCurrentUser, isTokenValid };
