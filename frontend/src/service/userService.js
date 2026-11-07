// src/service/userService.js
// Service de gestion des utilisateurs (CRUD) via l'API /api/admin/users.
// Toutes ces fonctions nécessitent un rôle ADMIN côté backend.

import api from './api';

/** URL de base des endpoints admin utilisateurs */
const USERS_URL = '/api/admin/users';

/**
 * Récupérer la liste complète de tous les utilisateurs.
 * GET /api/admin/users
 * @returns {Promise<Array>} Liste des utilisateurs
 */
export const getAllUsers = () =>
  api.get(USERS_URL).then((res) => res.data);

/**
 * Récupérer un utilisateur spécifique par son identifiant.
 * GET /api/admin/users/{id}
 * @param {number} id - Identifiant de l'utilisateur
 * @returns {Promise<Object>} L'utilisateur correspondant
 */
export const getUserById = (id) =>
  api.get(`${USERS_URL}/${id}`).then((res) => res.data);

/**
 * Créer un nouvel utilisateur.
 * POST /api/admin/users
 * Le mot de passe est généré automatiquement par le backend et envoyé par email.
 * @param {{ name: string, email: string, role: string }} userData - Données du nouvel utilisateur
 * @returns {Promise<Object>} L'utilisateur créé avec son ID
 */
export const createUser = (userData) =>
  api.post(USERS_URL, userData).then((res) => res.data);

/**
 * Modifier un utilisateur existant.
 * PUT /api/admin/users/{id}
 * @param {number} id - Identifiant de l'utilisateur à modifier
 * @param {{ name: string, email: string, role: string }} userData - Nouvelles données
 * @returns {Promise<Object>} L'utilisateur mis à jour
 */
export const updateUser = (id, userData) =>
  api.put(`${USERS_URL}/${id}`, userData).then((res) => res.data);

/**
 * Supprimer un utilisateur.
 * DELETE /api/admin/users/{id}
 * @param {number} id - Identifiant de l'utilisateur à supprimer
 * @returns {Promise<void>}
 */
export const deleteUser = (id) =>
  api.delete(`${USERS_URL}/${id}`);

/**
 * Basculer le statut actif/inactif d'un utilisateur.
 * PATCH /api/admin/users/{id}/toggle
 * @param {number} id - Identifiant de l'utilisateur
 * @returns {Promise<string>} Message de confirmation
 */
export const toggleUserStatus = (id) =>
  api.patch(`${USERS_URL}/${id}/toggle`).then((res) => res.data);

/**
 * Réinitialiser le mot de passe d'un utilisateur.
 * POST /api/admin/users/{id}/reset-password
 * @param {number} id - Identifiant de l'utilisateur
 * @returns {Promise<{ temporaryPassword: string }>}
 */
export const resetUserPassword = (id) =>
  api.post(`${USERS_URL}/${id}/reset-password`).then((res) => res.data);

/**
 * Récupérer le profil de l'utilisateur connecté.
 * GET /api/profile
 * @returns {Promise<Object>} Le profil de l'utilisateur courant
 */
export const getMyProfile = () =>
  api.get('/api/profile').then((res) => res.data);

/**
 * Mettre à jour son propre profil (nom et email).
 * PUT /api/profile
 * @param {{ name: string, email: string }} profileData - Nouvelles données de profil
 * @returns {Promise<Object>} Le profil mis à jour
 */
export const updateMyProfile = (profileData) =>
  api.put('/api/profile', profileData).then((res) => res.data);

/**
 * Modifier son propre mot de passe.
 * PUT /api/profile/password
 * @param {{ currentPassword: string, newPassword: string }} passwordData
 * @returns {Promise<string>}
 */
export const changePassword = (passwordData) =>
  api.put('/api/profile/password', passwordData).then((res) => res.data);

