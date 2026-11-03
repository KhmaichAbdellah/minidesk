// src/pages/KnowledgeBase.jsx
// Page de la Base de Connaissances (FAQ & Solutions types)
// Permet la recherche par mots-clés, le filtrage par catégorie et la gestion (CRUD) par ADMIN/TECHNICIAN.

import { useState, useEffect, useCallback } from 'react';
import {
  Box, Typography, Paper, TextField, Button, Chip, Stack,
  CircularProgress, IconButton, Dialog, DialogTitle, DialogContent,
  DialogActions, MenuItem, Accordion, AccordionSummary, AccordionDetails,
  InputAdornment, Tooltip
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import LocalOfferOutlinedIcon from '@mui/icons-material/LocalOfferOutlined';
import { useAuth } from '../context/AuthContext';
import { getAllArticles, createArticle, updateArticle, deleteArticle } from '../service/knowledgeService';

const CATEGORIES = [
  { value: 'ALL', label: 'Toutes les catégories' },
  { value: 'SOFTWARE', label: 'Logiciel (SOFTWARE)' },
  { value: 'HARDWARE', label: 'Matériel (HARDWARE)' },
  { value: 'NETWORK', label: 'Réseau (NETWORK)' },
  { value: 'ACCESS', label: 'Accès (ACCESS)' },
  { value: 'OTHER', label: 'Autre (OTHER)' },
];

const CATEGORY_STYLES = {
  SOFTWARE: { bg: 'rgba(72, 144, 255, 0.1)', color: '#2563eb' },
  HARDWARE: { bg: 'rgba(254, 179, 43, 0.12)', color: '#d97706' },
  NETWORK: { bg: 'rgba(147, 51, 234, 0.1)', color: '#7e22ce' },
  ACCESS: { bg: 'rgba(242, 83, 83, 0.1)', color: '#dc2626' },
  OTHER: { bg: 'rgba(107, 107, 138, 0.12)', color: '#6b6b8a' },
};

const EMPTY_ARTICLE_FORM = {
  title: '',
  content: '',
  category: 'SOFTWARE',
  keywordsText: '',
};

const KnowledgeBase = () => {
  const { user } = useAuth();
  const [articles, setArticles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');

  // Modale Création / Édition
  const [openModal, setOpenModal] = useState(false);
  const [editingArticle, setEditingArticle] = useState(null);
  const [formData, setFormData] = useState(EMPTY_ARTICLE_FORM);
  const [formLoading, setFormLoading] = useState(false);

  const canManage = user?.role === 'ADMIN' || user?.role === 'TECHNICIAN';

  const fetchArticles = useCallback(() => {
    setLoading(true);
    getAllArticles(searchTerm, selectedCategory)
      .then((data) => setArticles(data || []))
      .catch((err) => console.error('Erreur chargement articles:', err))
      .finally(() => setLoading(false));
  }, [searchTerm, selectedCategory]);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchArticles();
    }, 250);
    return () => clearTimeout(timer);
  }, [fetchArticles]);

  const handleOpenCreate = () => {
    setEditingArticle(null);
    setFormData(EMPTY_ARTICLE_FORM);
    setOpenModal(true);
  };

  const handleOpenEdit = (article) => {
    setEditingArticle(article);
    setFormData({
      title: article.title,
      content: article.content,
      category: article.category,
      keywordsText: (article.keywords || []).join(', '),
    });
    setOpenModal(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Voulez-vous vraiment supprimer cet article ?')) return;
    try {
      await deleteArticle(id);
      setArticles((prev) => prev.filter((a) => a.id !== id));
    } catch (err) {
      console.error('Erreur suppression article:', err);
    }
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title.trim() || !formData.content.trim()) return;

    setFormLoading(true);
    const keywords = formData.keywordsText
      ? formData.keywordsText.split(',').map((k) => k.trim()).filter(Boolean)
      : [];

    const payload = {
      title: formData.title.trim(),
      content: formData.content.trim(),
      category: formData.category,
      keywords,
    };

    try {
      if (editingArticle) {
        const updated = await updateArticle(editingArticle.id, payload);
        setArticles((prev) => prev.map((a) => (a.id === updated.id ? updated : a)));
      } else {
        const created = await createArticle(payload);
        setArticles((prev) => [created, ...prev]);
      }
      setOpenModal(false);
    } catch (err) {
      console.error('Erreur sauvegarde article:', err);
    } finally {
      setFormLoading(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', p: { xs: 2, md: 3 } }}>
      {/* Header */}
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        alignItems={{ xs: 'flex-start', sm: 'center' }}
        justifyContent="space-between"
        spacing={2}
        sx={{ mb: 3 }}
      >
        <Box>
          <Stack direction="row" spacing={1.5} alignItems="center">
            <Box
              sx={{
                width: 44,
                height: 44,
                borderRadius: '12px',
                bgcolor: 'rgba(255, 6, 126, 0.1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#FF067E',
              }}
            >
              <MenuBookIcon />
            </Box>
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 900, color: '#303167' }}>
                Base de Connaissances
              </Typography>
              <Typography variant="body2" sx={{ color: '#6b6b8a' }}>
                Solutions types, guides pratiques et procédures du support
              </Typography>
            </Box>
          </Stack>
        </Box>

        {canManage && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenCreate}
            sx={{
              background: 'linear-gradient(135deg, #FF067E 0%, #d4005f 100%)',
              boxShadow: '0 8px 24px rgba(255, 6, 126, 0.35)',
              borderRadius: '12px',
              fontWeight: 800,
              px: 3,
            }}
          >
            Nouvel article
          </Button>
        )}
      </Stack>

      {/* Barre de recherche & Filtres */}
      <Paper
        elevation={0}
        sx={{
          p: 2.5,
          mb: 3,
          borderRadius: '16px',
          bgcolor: '#ffffff',
          border: '1px solid rgba(48, 49, 103, 0.08)',
        }}
      >
        <TextField
          fullWidth
          placeholder="Rechercher par titre, problème ou mot-clé (ex: VPN, mot de passe, imprimante)..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon sx={{ color: '#FF067E' }} />
              </InputAdornment>
            ),
          }}
          sx={{
            mb: 2,
            '& .MuiOutlinedInput-root': {
              bgcolor: '#f8f9fe',
              borderRadius: '12px',
            },
          }}
        />

        <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
          {CATEGORIES.map((cat) => {
            const isSelected = selectedCategory === cat.value;
            return (
              <Chip
                key={cat.value}
                label={cat.label}
                clickable
                onClick={() => setSelectedCategory(cat.value)}
                sx={{
                  fontWeight: 700,
                  fontSize: '0.8rem',
                  py: 1,
                  px: 0.5,
                  borderRadius: '10px',
                  bgcolor: isSelected ? '#303167' : '#f1f3fa',
                  color: isSelected ? '#ffffff' : '#303167',
                  '&:hover': {
                    bgcolor: isSelected ? '#202044' : '#e4e7f4',
                  },
                }}
              />
            );
          })}
        </Stack>
      </Paper>

      {/* Liste des articles */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress size={40} sx={{ color: '#FF067E' }} />
        </Box>
      ) : articles.length === 0 ? (
        <Paper
          elevation={0}
          sx={{
            p: 6,
            textAlign: 'center',
            borderRadius: '16px',
            bgcolor: '#ffffff',
            border: '1px solid rgba(48, 49, 103, 0.08)',
          }}
        >
          <MenuBookIcon sx={{ fontSize: 60, color: '#9899b5', opacity: 0.5, mb: 2 }} />
          <Typography variant="h6" sx={{ fontWeight: 800, color: '#303167' }}>
            Aucun article trouvé
          </Typography>
          <Typography variant="body2" sx={{ color: '#6b6b8a', mt: 0.5 }}>
            Essayez de modifier votre recherche ou sélectionnez une autre catégorie.
          </Typography>
        </Paper>
      ) : (
        <Stack spacing={2}>
          {articles.map((article) => {
            const catStyle = CATEGORY_STYLES[article.category] || CATEGORY_STYLES.OTHER;
            return (
              <Accordion
                key={article.id}
                elevation={0}
                sx={{
                  borderRadius: '16px !important',
                  border: '1px solid rgba(48, 49, 103, 0.08)',
                  bgcolor: '#ffffff',
                  '&:before': { display: 'none' },
                  overflow: 'hidden',
                  transition: 'box-shadow 0.2s',
                  '&:hover': {
                    boxShadow: '0 8px 24px rgba(48, 49, 103, 0.06)',
                  },
                }}
              >
                <AccordionSummary expandIcon={<ExpandMoreIcon sx={{ color: '#FF067E' }} />}>
                  <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%', pr: 2 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      <Chip
                        size="small"
                        label={article.category}
                        sx={{
                          fontWeight: 800,
                          fontSize: '0.72rem',
                          bgcolor: catStyle.bg,
                          color: catStyle.color,
                          borderRadius: '8px',
                        }}
                      />
                      <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#303167' }}>
                        {article.title}
                      </Typography>
                    </Box>

                    {canManage && (
                      <Stack direction="row" spacing={0.5} onClick={(e) => e.stopPropagation()}>
                        <Tooltip title="Modifier">
                          <IconButton size="small" onClick={() => handleOpenEdit(article)} sx={{ color: '#303167' }}>
                            <EditOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        {user?.role === 'ADMIN' && (
                          <Tooltip title="Supprimer">
                            <IconButton size="small" onClick={() => handleDelete(article.id)} sx={{ color: '#dc2626' }}>
                              <DeleteOutlineIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                      </Stack>
                    )}
                  </Box>
                </AccordionSummary>

                <AccordionDetails sx={{ pt: 0, pb: 3, px: 3, borderTop: '1px solid rgba(48, 49, 103, 0.06)' }}>
                  <Typography
                    variant="body2"
                    sx={{
                      color: '#1a1a3a',
                      lineHeight: 1.8,
                      whiteSpace: 'pre-line',
                      mt: 1.5,
                      fontSize: '0.92rem',
                    }}
                  >
                    {article.content}
                  </Typography>

                  {/* Mots-clés */}
                  {article.keywords && article.keywords.length > 0 && (
                    <Stack direction="row" spacing={1} alignItems="center" sx={{ mt: 2.5, pt: 1.5, borderTop: '1px dashed rgba(48, 49, 103, 0.08)' }}>
                      <LocalOfferOutlinedIcon sx={{ fontSize: 16, color: '#9899b5' }} />
                      <Typography variant="caption" sx={{ color: '#9899b5', fontWeight: 700 }}>Mots-clés :</Typography>
                      {article.keywords.map((kw, i) => (
                        <Chip
                          key={i}
                          size="small"
                          label={kw}
                          sx={{
                            height: 22,
                            fontSize: '0.7rem',
                            bgcolor: '#f1f3fa',
                            color: '#6b6b8a',
                            borderRadius: '6px',
                          }}
                        />
                      ))}
                    </Stack>
                  )}

                  <Typography variant="caption" sx={{ display: 'block', color: '#9899b5', mt: 1 }}>
                    Rédigé par {article.authorName || 'Support MiniDesk'} le {article.createdAt ? new Date(article.createdAt).toLocaleDateString('fr-FR') : ''}
                  </Typography>
                </AccordionDetails>
              </Accordion>
            );
          })}
        </Stack>
      )}

      {/* Modale Création / Édition Article */}
      <Dialog
        open={openModal}
        onClose={() => setOpenModal(false)}
        maxWidth="md"
        fullWidth
        PaperProps={{ sx: { borderRadius: '20px' } }}
      >
        <form onSubmit={handleFormSubmit}>
          <DialogTitle sx={{ fontWeight: 900, color: '#303167' }}>
            {editingArticle ? "Modifier l'article" : 'Créer un nouvel article de FAQ'}
          </DialogTitle>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, pt: 2 }}>
            <TextField
              fullWidth
              required
              label="Titre de l'article"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: '12px' } }}
            />

            <TextField
              select
              fullWidth
              label="Catégorie"
              value={formData.category}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: '12px' } }}
            >
              {CATEGORIES.filter((c) => c.value !== 'ALL').map((c) => (
                <MenuItem key={c.value} value={c.value}>{c.label}</MenuItem>
              ))}
            </TextField>

            <TextField
              fullWidth
              required
              multiline
              rows={6}
              label="Contenu / Solution détaillée"
              value={formData.content}
              onChange={(e) => setFormData({ ...formData, content: e.target.value })}
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: '12px' } }}
            />

            <TextField
              fullWidth
              label="Mots-clés (séparés par des virgules)"
              placeholder="ex: vpn, connexion, cisco, télétravail"
              value={formData.keywordsText}
              onChange={(e) => setFormData({ ...formData, keywordsText: e.target.value })}
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: '12px' } }}
            />
          </DialogContent>
          <DialogActions sx={{ p: 2.5 }}>
            <Button onClick={() => setOpenModal(false)} color="inherit" sx={{ fontWeight: 700 }}>
              Annuler
            </Button>
            <Button
              type="submit"
              variant="contained"
              disabled={formLoading}
              sx={{
                background: 'linear-gradient(135deg, #FF067E 0%, #d4005f 100%)',
                borderRadius: '12px',
                fontWeight: 800,
                px: 3,
              }}
            >
              {formLoading ? 'Enregistrement...' : (editingArticle ? 'Mettre à jour' : 'Enregistrer')}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default KnowledgeBase;
