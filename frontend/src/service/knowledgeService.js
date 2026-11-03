// src/service/knowledgeService.js
// Service pour la base de connaissances (FAQ / Articles d'aide)

import api from './api';

const ARTICLES_URL = '/api/articles';

/**
 * Récupérer tous les articles avec filtres optionnels.
 * @param {string} [search] - Recherche textuelle
 * @param {string} [category] - Catégorie (HARDWARE, SOFTWARE, NETWORK, ACCESS, OTHER)
 * @returns {Promise<Array>} Liste des articles
 */
export const getAllArticles = (search, category) => {
  const params = {};
  if (search && search.trim()) params.search = search.trim();
  if (category && category !== 'ALL') params.category = category;
  return api.get(ARTICLES_URL, { params }).then((res) => res.data);
};

/**
 * Obtenir un article par son ID.
 * @param {number} id - ID de l'article
 * @returns {Promise<Object>} L'article
 */
export const getArticleById = (id) =>
  api.get(`${ARTICLES_URL}/${id}`).then((res) => res.data);

/**
 * Créer un nouvel article (ADMIN ou TECHNICIAN).
 * @param {Object} articleData - { title, content, category, keywords }
 * @returns {Promise<Object>} L'article créé
 */
export const createArticle = (articleData) =>
  api.post(ARTICLES_URL, articleData).then((res) => res.data);

/**
 * Mettre à jour un article existant (ADMIN ou TECHNICIAN).
 * @param {number} id - ID de l'article
 * @param {Object} articleData - Nouvelles données
 * @returns {Promise<Object>} L'article mis à jour
 */
export const updateArticle = (id, articleData) =>
  api.put(`${ARTICLES_URL}/${id}`, articleData).then((res) => res.data);

/**
 * Supprimer un article (ADMIN uniquement).
 * @param {number} id - ID de l'article
 * @returns {Promise<void>}
 */
export const deleteArticle = (id) =>
  api.delete(`${ARTICLES_URL}/${id}`);
