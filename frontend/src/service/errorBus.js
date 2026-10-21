// src/service/errorBus.js
// Bus d'événements minimaliste pour propager les erreurs API vers les composants React.
//
// Principe (Event Bus pattern) :
//   - L'intercepteur Axios appelle emit() quand une erreur HTTP survient
//   - Le composant GlobalErrorToast s'abonne via subscribeToApiErrors()
//   - Aucun Redux, contexte React, ni dépendance externe n'est nécessaire

/** @type {Function|null} - Référence vers l'unique abonné actif */
let subscriber = null;

/**
 * Publie une erreur vers l'abonné actif.
 * Appelé depuis l'intercepteur Axios (api.js).
 *
 * @param {string} message - Message d'erreur lisible
 * @param {string} [level='error'] - Niveau : 'error' | 'warning' | 'info'
 */
export const emitApiError = (message, level = 'error') => {
  if (subscriber) {
    subscriber(message, level);
  }
};

/**
 * Abonne un composant React aux notifications d'erreurs API.
 * Retourne une fonction de désabonnement à appeler dans useEffect() cleanup.
 *
 * @param {Function} callback - Fonction (message, level) => void
 * @returns {Function} Fonction de désabonnement
 */
export const subscribeToApiErrors = (callback) => {
  subscriber = callback;
  return () => {
    // Nettoyage : retirer l'abonnement
    subscriber = null;
  };
};
