// src/pages/Dashboard.jsx
// Page du Tableau de bord (Dashboard) principal de l'application MiniDesk.
// Affiche les métriques clés, statut des tickets et permet la création directe d'un nouveau ticket.

import { useEffect, useState, useCallback } from 'react';
import { 
  Box, Typography, Grid, Paper, CircularProgress, Alert, Chip, Stack, Button,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem, Snackbar
} from '@mui/material';
import { getDashboardStats, createTicket } from '../service/ticketService';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import ConfirmationNumberIcon from '@mui/icons-material/ConfirmationNumber';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined';
import ErrorOutlinedIcon from '@mui/icons-material/ErrorOutlined';
import BoltIcon from '@mui/icons-material/Bolt';
import AddIcon from '@mui/icons-material/Add';
import SmartToyOutlinedIcon from '@mui/icons-material/SmartToyOutlined';

const CATEGORIES = [
  { value: 'SOFTWARE', label: 'Logiciel (SOFTWARE)' },
  { value: 'HARDWARE', label: 'Matériel (HARDWARE)' },
  { value: 'NETWORK',  label: 'Réseau (NETWORK)' },
  { value: 'ACCESS',   label: 'Accès (ACCESS)' },
  { value: 'OTHER',    label: 'Autre (OTHER)' },
];

const PRIORITIES = [
  { value: 'BASSE',    label: 'Basse' },
  { value: 'NORMALE',  label: 'Normale' },
  { value: 'HAUTE',    label: 'Haute' },
  { value: 'CRITIQUE', label: 'Critique' },
];

/**
 * Carte d'indicateur (StatCard / KPI) avec design moderne.
 */
const StatCard = ({ title, value, icon, color, bgGradient, subtitle }) => (
  <Paper
    elevation={0}
    sx={{
      p: 3,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      borderRadius: '20px',
      background: 'rgba(255, 255, 255, 0.95)',
      backdropFilter: 'blur(20px)',
      border: '1px solid rgba(48, 49, 103, 0.08)',
      boxShadow: '0 8px 30px rgba(0, 0, 0, 0.04)',
      position: 'relative',
      overflow: 'hidden',
      transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
      height: '100%',
      '&:hover': {
        transform: 'translateY(-4px)',
        boxShadow: '0 14px 40px rgba(0, 0, 0, 0.08)',
        borderColor: color,
      },
    }}
  >
    <Box
      sx={{
        position: 'absolute',
        right: '-20px',
        bottom: '-20px',
        width: '100px',
        height: '100px',
        borderRadius: '50%',
        background: bgGradient,
        filter: 'blur(30px)',
        opacity: 0.25,
        pointerEvents: 'none',
      }}
    />

    <Box sx={{ zIndex: 1 }}>
      <Typography
        variant="body2"
        sx={{
          fontWeight: 800,
          mb: 0.8,
          textTransform: 'uppercase',
          letterSpacing: '0.06em',
          color: '#6b6b8a',
          fontSize: '0.78rem',
        }}
      >
        {title}
      </Typography>
      <Typography variant="h3" sx={{ fontWeight: 900, color: '#1a1a3a', lineHeight: 1.1, mb: 0.5 }}>
        {value}
      </Typography>
      {subtitle && (
        <Typography variant="caption" sx={{ color: '#9899b5', fontWeight: 600 }}>
          {subtitle}
        </Typography>
      )}
    </Box>

    <Box
      sx={{
        width: 58,
        height: 58,
        borderRadius: '16px',
        background: bgGradient,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: '#ffffff',
        boxShadow: `0 6px 18px ${color}40`,
        zIndex: 1,
      }}
    >
      {icon}
    </Box>
  </Paper>
);

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // ── States pour la modale de création directe de ticket depuis le Dashboard ──
  const [openCreateModal, setOpenCreateModal] = useState(false);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: 'SOFTWARE',
    priority: 'NORMALE',
  });
  const [formError, setFormError] = useState('');
  const [formLoading, setFormLoading] = useState(false);
  const [successToast, setSuccessToast] = useState('');

  const fetchStats = useCallback(() => {
    getDashboardStats()
      .then((data) => setStats(data))
      .catch((err) => setError('Impossible de charger les statistiques. (' + (err.response?.data?.message || err.message) + ')'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  const handleOpenModal = () => {
    setFormData({
      title: '',
      description: '',
      category: 'SOFTWARE',
      priority: 'NORMALE',
    });
    setFormError('');
    setOpenCreateModal(true);
  };

  const handleCloseModal = () => {
    setOpenCreateModal(false);
  };

  const handleSubmitTicket = async (e) => {
    e.preventDefault();
    setFormError('');

    if (!formData.title.trim()) {
      setFormError('Le titre est obligatoire.');
      return;
    }

    setFormLoading(true);

    try {
      await createTicket(formData);
      setOpenCreateModal(false);
      setSuccessToast('Ticket créé avec succès !');
      fetchStats(); // Actualise les compteurs immédiatement
    } catch (err) {
      setFormError(err.response?.data?.message || err.response?.data || 'Erreur lors de la création du ticket.');
    } finally {
      setFormLoading(false);
    }
  };

  const isAdminOrTech = user?.role === 'ADMIN' || user?.role === 'TECHNICIAN';
  const pageTitle = isAdminOrTech ? 'Aperçu des performances' : 'Mes demandes de support';

  // Affichage propre du nom de l'utilisateur
  const displayName = user?.name && user.name !== user.email
    ? user.name
    : (user?.name || user?.email?.split('@')[0] || 'Utilisateur');

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
        <CircularProgress sx={{ color: '#FF067E' }} />
      </Box>
    );
  }

  if (error) return <Alert severity="error" sx={{ mt: 2, borderRadius: 3 }}>{error}</Alert>;

  return (
    <Box className="animate-fade-in">
      {/* ── Bannière de bienvenue moderne ── */}
      <Paper
        elevation={0}
        sx={{
          p: { xs: 3, md: 4 },
          mb: 4,
          borderRadius: '24px',
          background: 'linear-gradient(135deg, #161733 0%, #252453 50%, #303167 100%)',
          color: '#ffffff',
          position: 'relative',
          overflow: 'hidden',
          boxShadow: '0 12px 35px rgba(22, 23, 51, 0.3)',
          border: '1px solid rgba(255, 255, 255, 0.1)',
        }}
      >
        <Box
          sx={{
            position: 'absolute',
            right: '-10%',
            top: '-30%',
            width: '350px',
            height: '350px',
            borderRadius: '50%',
            background: 'radial-gradient(circle, rgba(255, 6, 126, 0.35) 0%, transparent 70%)',
            filter: 'blur(50px)',
            pointerEvents: 'none',
          }}
        />

        <Stack
          direction={{ xs: 'column', md: 'row' }}
          justifyContent="space-between"
          alignItems={{ xs: 'flex-start', md: 'center' }}
          gap={3}
          sx={{ position: 'relative', zIndex: 1 }}
        >
          <Box>
            <Chip
              icon={<BoltIcon sx={{ fontSize: 16, color: '#FF067E !important' }} />}
              label="ESPACE DE TRAVAIL ACTIF"
              size="small"
              sx={{
                mb: 1.5,
                fontWeight: 800,
                fontSize: '0.7rem',
                letterSpacing: '0.08em',
                bgcolor: 'rgba(255, 6, 126, 0.15)',
                color: '#ff5ea7',
                border: '1px solid rgba(255, 6, 126, 0.3)',
              }}
            />
            <Typography variant="h4" sx={{ fontWeight: 900, mb: 1, letterSpacing: '-0.02em' }}>
              Bonjour, {displayName} 👋
            </Typography>
            <Typography variant="body1" sx={{ color: 'rgba(255, 255, 255, 0.75)', maxWidth: 600 }}>
              {isAdminOrTech
                ? 'Bienvenue sur votre centre de supervision. Consultez l\'activité globale et gérez les incidents IT en temps réel.'
                : 'Consultez le statut de vos tickets et créez de nouvelles demandes d\'assistance avec l\'aide de l\'IA.'}
            </Typography>
          </Box>

          <Stack direction="row" spacing={2}>
            {/* Bouton qui ouvre directement la fenêtre d'ajout de ticket */}
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={handleOpenModal}
              sx={{
                py: 1.4,
                px: 3,
                borderRadius: '14px',
                background: 'linear-gradient(135deg, #FF067E 0%, #d4005f 100%)',
                boxShadow: '0 8px 24px rgba(255, 6, 126, 0.4)',
                fontWeight: 800,
                fontSize: '0.95rem',
                whiteSpace: 'nowrap',
                '&:hover': {
                  background: 'linear-gradient(135deg, #ff2390 0%, #b8004f 100%)',
                  boxShadow: '0 12px 30px rgba(255, 6, 126, 0.55)',
                  transform: 'translateY(-2px)',
                },
              }}
            >
              Nouveau Ticket
            </Button>
          </Stack>
        </Stack>
      </Paper>

      {/* ── Titre de section ── */}
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 900, color: '#1a1a3a', letterSpacing: '-0.02em' }}>
          {pageTitle}
        </Typography>
        <Chip
          label="Mise à jour en temps réel"
          size="small"
          sx={{
            fontWeight: 700,
            bgcolor: 'rgba(65, 207, 124, 0.1)',
            color: '#2eb363',
            border: '1px solid rgba(65, 207, 124, 0.25)',
          }}
        />
      </Stack>

      {/* ── Grille de cartes KPI ── */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title={isAdminOrTech ? 'Total Tickets' : 'Mes Tickets'}
            value={stats?.total || 0}
            subtitle="Volume global enregistré"
            icon={<ConfirmationNumberIcon sx={{ fontSize: 30 }} />}
            color="#303167"
            bgGradient="linear-gradient(135deg, #303167 0%, #4a4baa 100%)"
          />
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Ouverts"
            value={stats?.open || 0}
            subtitle="En attente de prise en charge"
            icon={<ErrorOutlinedIcon sx={{ fontSize: 30 }} />}
            color="#FEB32B"
            bgGradient="linear-gradient(135deg, #FEB32B 0%, #f59e0b 100%)"
          />
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="En Cours"
            value={stats?.progress || 0}
            subtitle="Attribués & en traitement"
            icon={<AutorenewIcon sx={{ fontSize: 30 }} />}
            color="#4890ff"
            bgGradient="linear-gradient(135deg, #4890ff 0%, #7bafff 100%)"
          />
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Résolus / Fermés"
            value={(stats?.resolved || 0) + (stats?.closed || 0)}
            subtitle="Incidents clôturés avec succès"
            icon={<CheckCircleOutlinedIcon sx={{ fontSize: 30 }} />}
            color="#41CF7C"
            bgGradient="linear-gradient(135deg, #41CF7C 0%, #2eb363 100%)"
          />
        </Grid>
      </Grid>

      {/* ── Carte informative Assistant IA ── */}
      <Paper
        elevation={0}
        sx={{
          p: 3.5,
          borderRadius: '20px',
          background: 'rgba(255, 255, 255, 0.9)',
          border: '1px solid rgba(48, 49, 103, 0.08)',
          boxShadow: '0 8px 30px rgba(0, 0, 0, 0.03)',
        }}
      >
        <Stack direction={{ xs: 'column', sm: 'row' }} alignItems="center" justifyContent="space-between" gap={2}>
          <Stack direction="row" alignItems="center" spacing={2}>
            <Box
              sx={{
                width: 52,
                height: 52,
                borderRadius: '16px',
                background: 'linear-gradient(135deg, #FF067E 0%, #ff5ea7 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#fff',
                boxShadow: '0 6px 18px rgba(255, 6, 126, 0.35)',
              }}
            >
              <SmartToyOutlinedIcon sx={{ fontSize: 28 }} />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#1a1a3a', mb: 0.3 }}>
                Besoin d'un dépannage immédiat ?
              </Typography>
              <Typography variant="body2" sx={{ color: '#6b6b8a' }}>
                Cliquez sur le widget IA en bas à droite pour décrire votre problème. L'assistant crée et résout vos tickets en direct.
              </Typography>
            </Box>
          </Stack>
          <Button
            variant="outlined"
            onClick={() => navigate('/tickets')}
            sx={{
              borderRadius: '12px',
              fontWeight: 800,
              px: 2.5,
              py: 1,
              borderColor: 'rgba(48, 49, 103, 0.2)',
              color: '#303167',
              whiteSpace: 'nowrap',
              '&:hover': {
                borderColor: '#303167',
                bgcolor: 'rgba(48, 49, 103, 0.05)',
              },
            }}
          >
            Voir la liste des tickets
          </Button>
        </Stack>
      </Paper>

      {/* ── Modale (Dialog) de Création directe d'un Nouveau Ticket ── */}
      <Dialog
        open={openCreateModal}
        onClose={handleCloseModal}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: '24px',
            p: 1.5,
            boxShadow: '0 24px 70px rgba(0, 0, 0, 0.3)',
          },
        }}
      >
        <DialogTitle sx={{ fontWeight: 900, color: '#1a1a3a', fontSize: '1.25rem', pb: 1 }}>
          Créer un nouveau Ticket
        </DialogTitle>

        <form onSubmit={handleSubmitTicket}>
          <DialogContent dividers sx={{ borderColor: 'rgba(48, 49, 103, 0.08)' }}>
            {formError && <Alert severity="error" sx={{ mb: 2.5, borderRadius: 2 }}>{formError}</Alert>}

            <TextField
              margin="normal"
              required
              fullWidth
              label="Titre du ticket"
              placeholder="Ex: Problème d'accès VPN ou mot de passe"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              autoFocus
              sx={{
                mb: 2,
                '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' },
              }}
            />

            <TextField
              margin="normal"
              fullWidth
              multiline
              rows={3}
              label="Description détaillée"
              placeholder="Décrivez précisément le symptôme rencontré..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              sx={{
                mb: 2,
                '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' },
              }}
            />

            <Stack direction="row" spacing={2} sx={{ mt: 1 }}>
              <TextField
                select
                fullWidth
                label="Catégorie"
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                sx={{ '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
              >
                {CATEGORIES.map((c) => <MenuItem key={c.value} value={c.value}>{c.label}</MenuItem>)}
              </TextField>

              <TextField
                select
                fullWidth
                label="Priorité"
                value={formData.priority}
                onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                sx={{ '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
              >
                {PRIORITIES.map((p) => <MenuItem key={p.value} value={p.value}>{p.label}</MenuItem>)}
              </TextField>
            </Stack>
          </DialogContent>

          <DialogActions sx={{ p: 2.5 }}>
            <Button onClick={handleCloseModal} color="inherit" sx={{ fontWeight: 700, borderRadius: '10px' }}>
              Annuler
            </Button>
            <Button
              type="submit"
              variant="contained"
              disabled={formLoading}
              sx={{
                borderRadius: '12px',
                fontWeight: 800,
                px: 3,
                py: 1,
                background: 'linear-gradient(135deg, #FF067E 0%, #d4005f 100%)',
                boxShadow: '0 6px 20px rgba(255, 6, 126, 0.35)',
              }}
            >
              {formLoading ? 'Création en cours...' : 'Créer le ticket'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Snackbar notification de succès */}
      <Snackbar
        open={Boolean(successToast)}
        autoHideDuration={4000}
        onClose={() => setSuccessToast('')}
        message={successToast}
      />
    </Box>
  );
};

export default Dashboard;
