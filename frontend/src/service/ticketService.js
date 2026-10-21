// src/service/ticketService.js
// Service de gestion des tickets (CRUD, Commentaires, Historique et Dashboard) via l'API backend.

import api from './api';

const TICKETS_URL = '/api/tickets';

/**
 * Récupérer tous les tickets (filtrés par le backend selon le rôle).
 * @returns {Promise<Array>} Liste des tickets
 */
export const getAllTickets = () =>
  api.get(TICKETS_URL).then((res) => res.data);

/**
 * Récupérer un ticket par son ID.
 * @param {number} id - ID du ticket
 * @returns {Promise<Object>} Le ticket
 */
export const getTicketById = (id) =>
  api.get(`${TICKETS_URL}/${id}`).then((res) => res.data);

/**
 * Créer un nouveau ticket.
 * @param {Object} ticketData - { title, description, category, priority, assignedToId }
 * @returns {Promise<Object>} Le ticket créé
 */
export const createTicket = (ticketData) =>
  api.post(TICKETS_URL, ticketData).then((res) => res.data);

/**
 * Mettre à jour un ticket existant.
 * @param {number} id - ID du ticket
 * @param {Object} ticketData - Nouvelles données du ticket
 * @returns {Promise<Object>} Le ticket mis à jour
 */
export const updateTicket = (id, ticketData) =>
  api.put(`${TICKETS_URL}/${id}`, ticketData).then((res) => res.data);

/**
 * Supprimer un ticket (Admin uniquement).
 * @param {number} id - ID du ticket
 * @returns {Promise<void>}
 */
export const deleteTicket = (id) =>
  api.delete(`${TICKETS_URL}/${id}`);

/**
 * Récupérer les statistiques des tickets pour le dashboard.
 * @returns {Promise<Object>} Les statistiques { total, open, progress, resolved, closed }
 */
export const getDashboardStats = () =>
  api.get(`${TICKETS_URL}/stats`).then((res) => res.data);

/**
 * Récupérer la liste des commentaires déposés sur un ticket.
 * @param {number} ticketId - ID du ticket
 * @returns {Promise<Array>} Liste des commentaires ordonnés chronologiquement
 */
export const getTicketComments = (ticketId) =>
  api.get(`${TICKETS_URL}/${ticketId}/comments`).then((res) => res.data);

/**
 * Ajouter un commentaire sur un ticket.
 * @param {number} ticketId - ID du ticket
 * @param {string} content - Texte du commentaire
 * @returns {Promise<Object>} Le commentaire créé
 */
export const addTicketComment = (ticketId, content) =>
  api.post(`${TICKETS_URL}/${ticketId}/comments`, { content }).then((res) => res.data);

/**
 * Récupérer l'historique chronologique des changements de statut d'un ticket.
 * @param {number} ticketId - ID du ticket
 * @returns {Promise<Array>} Liste des entrées d'audit StatutLog
 */
export const getTicketHistory = (ticketId) =>
  api.get(`${TICKETS_URL}/${ticketId}/history`).then((res) => res.data);
