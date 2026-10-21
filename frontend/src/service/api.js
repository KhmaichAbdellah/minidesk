// src/service/api.js
// Configuration centralisée d'Axios pour toutes les requêtes vers l'API Spring Boot.
// Gère automatiquement :
//   - L'ajout du header Authorization avec le JWT stocké en localStorage
//   - La redirection vers /login si le backend répond 401 (session expirée)
//   - La notification globale en cas d'erreur HTTP (via errorBus)

import axios from 'axios';
import { emitApiError } from './errorBus';

// URL de base de l'API backend Spring Boot
const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081';

// Création de l'instance Axios avec la baseURL configurée
const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ─── Intercepteur de requête ─────────────────────────────────────────────────
// Avant chaque requête, on récupère le JWT depuis localStorage et on l'injecte
// dans le header Authorization sous le format "Bearer <token>"
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token'); // JWT stocké après login
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── Table des messages d'erreur par code HTTP ───────────────────────────────
// Permet d'afficher un message convivial et compréhensible pour chaque erreur
const HTTP_ERROR_MESSAGES = {
  400: 'Requête invalide. Vérifiez les données saisies.',
  401: 'Session expirée. Reconnectez-vous.',
  403: 'Accès refusé. Vous n\'avez pas les droits nécessaires.',
  404: 'Ressource introuvable.',
  409: 'Conflit : cette ressource existe déjà.',
  422: 'Données invalides envoyées au serveur.',
  500: 'Erreur interne du serveur. Réessayez plus tard.',
  502: 'Serveur temporairement indisponible (502 Bad Gateway).',
  503: 'Service indisponible. Le serveur est peut-être en maintenance.',
};

// ─── Intercepteur de réponse ─────────────────────────────────────────────────
// Gère les erreurs HTTP globalement :
//   - 401 → suppression du token + redirection vers /login
//   - Autres erreurs → notification toast via errorBus (GlobalErrorToast)
api.interceptors.response.use(
  (response) => response, // Succès : laisser passer la réponse normalement

  (error) => {
    const status = error.response?.status;

    if (status === 401) {
      // ── Token expiré ou invalide ──────────────────────────────────────────
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
      return Promise.reject(error);
    }

    // ── Construire le message d'erreur ────────────────────────────────────────
    // Priorité : message du serveur > message du tableau > message générique
    const serverMessage = error.response?.data?.message || error.response?.data;
    const defaultMessage = HTTP_ERROR_MESSAGES[status] || `Erreur réseau (${status || 'pas de réponse'})`;
    const finalMessage = (typeof serverMessage === 'string' && serverMessage.length < 200)
      ? serverMessage
      : defaultMessage;

    // ── Niveau de criticité selon le code HTTP ────────────────────────────────
    const level = status >= 500 ? 'error' : status === 403 ? 'warning' : 'error';

    // ── Publier l'erreur dans le bus global → GlobalErrorToast l'affichera ───
    emitApiError(finalMessage, level);

    return Promise.reject(error);
  }
);

export default api;
