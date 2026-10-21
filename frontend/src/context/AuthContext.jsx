// src/context/AuthContext.jsx
// Contexte React global d'authentification.
// Fournit à toute l'application : l'utilisateur connecté, les fonctions login/logout,
// et un indicateur de chargement pour éviter un flash de redirection au démarrage.

import { createContext, useContext, useState, useEffect } from 'react';
import { login as loginService, logout as logoutService, getCurrentUser, isTokenValid } from '../service/authService';

// ── Création du contexte ──────────────────────────────────────────────────────
const AuthContext = createContext(null);

/**
 * Provider AuthContext : à placer au plus haut niveau de l'arbre React (dans main.jsx).
 * Wraps toute l'application pour que n'importe quel composant puisse appeler useAuth().
 */
export const AuthProvider = ({ children }) => {
  // Utilisateur courant : objet { email, role, name } ou null si non connecté
  const [user, setUser] = useState(null);

  // Indicateur de chargement initial : vrai pendant la vérification du token au démarrage
  const [loading, setLoading] = useState(true);

  // ── Vérification du token au montage ──────────────────────────────────────
  // Au premier rendu, on vérifie si un JWT valide existe déjà en localStorage.
  // Si oui, on restaure la session sans redemander les credentials.
  useEffect(() => {
    const savedUser = getCurrentUser(); // Récupère { email, role, name } depuis localStorage
    if (savedUser && isTokenValid()) {
      // Token encore valide → restaurer la session
      setUser(savedUser);
    } else {
      // Token absent ou expiré → nettoyer le localStorage
      logoutService();
    }
    // Indiquer que la vérification initiale est terminée
    setLoading(false);
  }, []);

  // ── Fonction de connexion ──────────────────────────────────────────────────
  // Appelle le service d'auth, stocke le JWT, met à jour le state React.
  const login = async (email, password) => {
    const { user: loggedUser } = await loginService(email, password);
    setUser(loggedUser);
    return loggedUser;
  };

  // ── Fonction de déconnexion ────────────────────────────────────────────────
  // Supprime le JWT du localStorage et réinitialise le state utilisateur.
  const logout = () => {
    logoutService(); // Nettoie localStorage
    setUser(null);   // Réinitialise le state → déclenche la redirection vers /login
  };

  // Valeur exposée par le contexte à tous les composants enfants
  const value = { user, loading, login, logout };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * Hook personnalisé pour consommer le contexte d'authentification.
 * Utilisation : const { user, login, logout, loading } = useAuth();
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    // Erreur de développement : useAuth() appelé hors du AuthProvider
    throw new Error('useAuth doit être utilisé à l\'intérieur d\'un AuthProvider');
  }
  return context;
};
