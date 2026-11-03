import api from './api';

/**
 * Wrapper frontend pour les endpoints AI côté backend.
 * Utilise l'instance `api` (axios) qui ajoute le token JWT automatiquement.
 */
export const chatWithAi = (payload) =>
  api.post('/api/ai/chat', payload).then((res) => res.data);

export const createTicketFromSession = (sessionId, body) =>
  api.post(`/api/ai/${sessionId}/create-ticket`, body).then((res) => res.data);

export const getAiSessionById = (sessionId) =>
  api.get(`/api/ai/sessions/${sessionId}`).then((res) => res.data);

export const getAiSessionByTicketId = (ticketId) =>
  api.get(`/api/ai/tickets/${ticketId}/session`).then((res) => res.data);

export const getMyAiSessions = () =>
  api.get('/api/ai/my-sessions').then((res) => res.data);

export default {
  chatWithAi,
  createTicketFromSession,
  getAiSessionById,
  getAiSessionByTicketId,
  getMyAiSessions,
};
