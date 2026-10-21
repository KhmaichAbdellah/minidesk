// src/pages/Tickets.jsx
// Page de gestion de la liste des tickets de support.
// Inclut : recherche par mot-clé, filtre par statut, pagination, suppression, 
// ainsi que les modales de Création, Modification et Consultation de l'historique IA.

import { useState, useEffect, useMemo, useCallback } from 'react';
import { 
  Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, Chip, TextField, MenuItem, Stack, IconButton, TablePagination, CircularProgress,
  Dialog, DialogTitle, DialogContent, DialogActions, Alert, Tooltip, List, ListItem, InputAdornment
} from '@mui/material';
import { getAllTickets, createTicket, updateTicket, deleteTicket } from '../service/ticketService';
import { getAllUsers } from '../service/userService';
import { getAiSessionByTicketId } from '../service/aiService';
import { useAuth } from '../context/AuthContext';
import AddIcon from '@mui/icons-material/Add';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import SmartToyOutlinedIcon from '@mui/icons-material/SmartToyOutlined';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';

// ── Constantes statiques ──
const STATUS_COLORS = {
  OPEN: { bg: 'rgba(254, 179, 43, 0.12)', color: '#d97706', border: 'rgba(254, 179, 43, 0.3)', label: 'Ouvert' },
  PROGRESS: { bg: 'rgba(72, 144, 255, 0.12)', color: '#2563eb', border: 'rgba(72, 144, 255, 0.3)', label: 'En cours' },
  RESOLVED: { bg: 'rgba(65, 207, 124, 0.12)', color: '#16a34a', border: 'rgba(65, 207, 124, 0.3)', label: 'Résolu' },
  CLOSED: { bg: 'rgba(107, 107, 138, 0.12)', color: '#6b6b8a', border: 'rgba(107, 107, 138, 0.3)', label: 'Fermé' },
};

const PRIORITY_COLORS = {
  CRITIQUE: { bg: 'rgba(242, 83, 83, 0.12)', color: '#dc2626', border: 'rgba(242, 83, 83, 0.3)' },
  HAUTE: { bg: 'rgba(254, 179, 43, 0.12)', color: '#d97706', border: 'rgba(254, 179, 43, 0.3)' },
  NORMALE: { bg: 'rgba(72, 144, 255, 0.12)', color: '#2563eb', border: 'rgba(72, 144, 255, 0.3)' },
  BASSE: { bg: 'rgba(65, 207, 124, 0.12)', color: '#16a34a', border: 'rgba(65, 207, 124, 0.3)' },
};

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

const STATUSES = [
  { value: 'OPEN',     label: 'Ouvert (OPEN)' },
  { value: 'PROGRESS', label: 'En cours (PROGRESS)' },
  { value: 'RESOLVED', label: 'Résolu (RESOLVED)' },
  { value: 'CLOSED',   label: 'Fermé (CLOSED)' },
];

const EMPTY_FORM = {
  title: '',
  description: '',
  category: 'SOFTWARE',
  priority: 'NORMALE',
  status: 'OPEN',
  assignedToId: '',
};

const Tickets = () => {
  const { user } = useAuth();
  const [tickets, setTickets] = useState([]);
  const [technicians, setTechnicians] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filtres & recherche
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  // Modale Création / Modification
  const [openModal, setOpenModal] = useState(false);
  const [editingTicket, setEditingTicket] = useState(null);

  // Modale Historique IA
  const [openHistoryModal, setOpenHistoryModal] = useState(false);
  const [selectedTicketForHistory, setSelectedTicketForHistory] = useState(null);
  const [historySession, setHistorySession] = useState(null);
  const [historyLoading, setHistoryLoading] = useState(false);

  // Formulaire
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [formLoading, setFormLoading] = useState(false);

  const fetchTickets = useCallback(() => {
    setLoading(true);
    getAllTickets()
      .then((data) => setTickets(data))
      .catch((err) => console.error('Erreur chargement tickets:', err))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    fetchTickets();
    if (user?.role === 'ADMIN' || user?.role === 'TECHNICIAN') {
      getAllUsers()
        .then((usersData) => setTechnicians(usersData))
        .catch((err) => console.error('Erreur chargement utilisateurs:', err));
    }
  }, [user, fetchTickets]);

  const handleOpenCreateModal = useCallback(() => {
    setEditingTicket(null);
    setFormData(EMPTY_FORM);
    setFormError('');
    setOpenModal(true);
  }, []);

  const handleOpenEditModal = useCallback((ticket) => {
    setEditingTicket(ticket);
    setFormData({
      title:        ticket.title        || '',
      description:  ticket.description  || '',
      category:     ticket.category     || 'SOFTWARE',
      priority:     ticket.priority     || 'NORMALE',
      status:       ticket.status       || 'OPEN',
      assignedToId: ticket.assignedToId || '',
    });
    setFormError('');
    setOpenModal(true);
  }, []);

  const handleCloseModal = useCallback(() => {
    setOpenModal(false);
    setEditingTicket(null);
  }, []);

  const handleOpenHistoryModal = async (ticket) => {
    setSelectedTicketForHistory(ticket);
    setHistorySession(null);
    setHistoryLoading(true);
    setOpenHistoryModal(true);
    try {
      const session = await getAiSessionByTicketId(ticket.id);
      setHistorySession(session);
    } catch {
      setHistorySession(null);
    } finally {
      setHistoryLoading(false);
    }
  };

  const handleCloseHistoryModal = () => {
    setOpenHistoryModal(false);
    setSelectedTicketForHistory(null);
    setHistorySession(null);
  };

  const handleSubmitForm = async (e) => {
    e.preventDefault();
    setFormError('');

    if (!formData.title.trim()) {
      setFormError('Le titre est obligatoire.');
      return;
    }

    setFormLoading(true);

    try {
      const payload = {
        ...formData,
        assignedToId: formData.assignedToId ? Number(formData.assignedToId) : null,
      };

      if (editingTicket) {
        await updateTicket(editingTicket.id, payload);
      } else {
        await createTicket(payload);
      }

      handleCloseModal();
      fetchTickets();
    } catch (err) {
      setFormError(err.response?.data?.message || err.response?.data || 'Erreur lors de l\'enregistrement du ticket.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Voulez-vous vraiment supprimer ce ticket ?')) {
      try {
        await deleteTicket(id);
        fetchTickets();
      } catch (err) {
        console.error("Erreur suppression:", err);
      }
    }
  };

  const filteredTickets = useMemo(() => {
    const lowerSearch = searchTerm.toLowerCase();
    return tickets.filter((ticket) => {
      const matchesSearch =
        ticket.title.toLowerCase().includes(lowerSearch) ||
        (ticket.description && ticket.description.toLowerCase().includes(lowerSearch)) ||
        (ticket.id && String(ticket.id).includes(lowerSearch));
      const matchesStatus = statusFilter === 'ALL' || ticket.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [tickets, searchTerm, statusFilter]);

  const paginatedTickets = useMemo(
    () => filteredTickets.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage),
    [filteredTickets, page, rowsPerPage]
  );

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
        <CircularProgress sx={{ color: '#FF067E' }} />
      </Box>
    );
  }

  return (
    <Box className="animate-fade-in">
      {/* ── Barre d'actions & Filtres ── */}
      <Paper
        elevation={0}
        sx={{
          p: 2.5,
          mb: 3.5,
          borderRadius: '18px',
          background: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(48, 49, 103, 0.08)',
          boxShadow: '0 8px 25px rgba(0, 0, 0, 0.03)',
        }}
      >
        <Stack direction={{ xs: 'column', md: 'row' }} justifyContent="space-between" alignItems="center" gap={2}>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ width: { xs: '100%', md: 'auto' } }}>
            <TextField
              size="small"
              placeholder="Rechercher par titre, ID ou contenu..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ fontSize: 20, color: '#6b6b8a' }} />
                  </InputAdornment>
                ),
              }}
              sx={{
                minWidth: { xs: '100%', sm: 300 },
                '& .MuiOutlinedInput-root': {
                  bgcolor: '#f7f8fc',
                  borderRadius: '12px',
                  '&:hover': { bgcolor: '#f1f3fa' },
                  '&.Mui-focused': { bgcolor: '#ffffff' },
                },
              }}
            />
            <TextField
              select
              size="small"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <FilterListIcon sx={{ fontSize: 18, color: '#6b6b8a' }} />
                  </InputAdornment>
                ),
              }}
              sx={{
                minWidth: { xs: '100%', sm: 180 },
                '& .MuiOutlinedInput-root': {
                  bgcolor: '#f7f8fc',
                  borderRadius: '12px',
                  '&:hover': { bgcolor: '#f1f3fa' },
                  '&.Mui-focused': { bgcolor: '#ffffff' },
                },
              }}
            >
              <MenuItem value="ALL">Tous les statuts</MenuItem>
              <MenuItem value="OPEN">Ouvert</MenuItem>
              <MenuItem value="PROGRESS">En cours</MenuItem>
              <MenuItem value="RESOLVED">Résolu</MenuItem>
              <MenuItem value="CLOSED">Fermé</MenuItem>
            </TextField>
          </Stack>

          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenCreateModal}
            sx={{
              py: 1.2,
              px: 3,
              borderRadius: '12px',
              background: 'linear-gradient(135deg, #FF067E 0%, #d4005f 100%)',
              boxShadow: '0 6px 20px rgba(255, 6, 126, 0.35)',
              fontWeight: 800,
              fontSize: '0.92rem',
              whiteSpace: 'nowrap',
              width: { xs: '100%', md: 'auto' },
              '&:hover': {
                background: 'linear-gradient(135deg, #ff2390 0%, #b8004f 100%)',
                boxShadow: '0 10px 25px rgba(255, 6, 126, 0.45)',
                transform: 'translateY(-1px)',
              },
            }}
          >
            Nouveau Ticket
          </Button>
        </Stack>
      </Paper>

      {/* ── Tableau des Tickets ── */}
      <Paper
        elevation={0}
        sx={{
          width: '100%',
          overflow: 'hidden',
          borderRadius: '20px',
          background: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(48, 49, 103, 0.08)',
          boxShadow: '0 8px 30px rgba(0, 0, 0, 0.03)',
        }}
      >
        <TableContainer sx={{ maxHeight: 'calc(100vh - 300px)' }}>
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc', py: 2 }}>ID</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc' }}>Titre & Objet</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc' }}>Catégorie</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc' }}>Priorité</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc' }}>Statut</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc' }}>Demandeur</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc' }}>Assigné à</TableCell>
                <TableCell align="right" sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc', pr: 3 }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginatedTickets.map((ticket) => {
                const statusStyle = STATUS_COLORS[ticket.status] || STATUS_COLORS.CLOSED;
                const priorityStyle = PRIORITY_COLORS[ticket.priority] || PRIORITY_COLORS.NORMALE;

                return (
                  <TableRow
                    hover
                    key={ticket.id}
                    sx={{
                      transition: 'background-color 0.2s ease',
                      '&:hover': { bgcolor: 'rgba(255, 6, 126, 0.02) !important' },
                    }}
                  >
                    <TableCell sx={{ fontWeight: 800, color: '#6b6b8a', fontSize: '0.85rem' }}>
                      #{ticket.id}
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 800, color: '#1a1a3a', lineHeight: 1.2 }}>
                        {ticket.title}
                      </Typography>
                      {ticket.description && (
                        <Typography
                          variant="caption"
                          sx={{
                            color: '#6b6b8a',
                            display: '-webkit-box',
                            WebkitLineClamp: 1,
                            WebkitBoxOrient: 'vertical',
                            overflow: 'hidden',
                            maxWidth: 320,
                            mt: 0.3,
                          }}
                        >
                          {ticket.description}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={ticket.category}
                        sx={{
                          fontWeight: 700,
                          fontSize: '0.72rem',
                          bgcolor: '#f1f3fa',
                          color: '#303167',
                          borderRadius: '8px',
                        }}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={ticket.priority}
                        sx={{
                          fontWeight: 800,
                          fontSize: '0.72rem',
                          bgcolor: priorityStyle.bg,
                          color: priorityStyle.color,
                          border: `1px solid ${priorityStyle.border}`,
                          borderRadius: '8px',
                        }}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={statusStyle.label || ticket.status}
                        sx={{
                          fontWeight: 800,
                          fontSize: '0.72rem',
                          bgcolor: statusStyle.bg,
                          color: statusStyle.color,
                          border: `1px solid ${statusStyle.border}`,
                          borderRadius: '8px',
                        }}
                      />
                    </TableCell>
                    <TableCell sx={{ color: '#1a1a3a', fontWeight: 600, fontSize: '0.88rem' }}>
                      {ticket.createdByName || '-'}
                    </TableCell>
                    <TableCell sx={{ color: '#6b6b8a', fontWeight: 600, fontSize: '0.88rem' }}>
                      {ticket.assignedToName ? (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8 }}>
                          <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: '#4890ff' }} />
                          <Typography variant="body2" sx={{ fontWeight: 700, color: '#303167' }}>
                            {ticket.assignedToName}
                          </Typography>
                        </Box>
                      ) : (
                        <Typography variant="caption" sx={{ color: '#9899b5', fontStyle: 'italic' }}>
                          Non assigné
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell align="right" sx={{ pr: 2 }}>
                      <Stack direction="row" spacing={0.5} justifyContent="flex-end">
                        <Tooltip title="Historique discussion IA">
                          <IconButton
                            size="small"
                            onClick={() => handleOpenHistoryModal(ticket)}
                            sx={{
                              color: '#FF067E',
                              bgcolor: 'rgba(255, 6, 126, 0.08)',
                              borderRadius: '10px',
                              '&:hover': { bgcolor: 'rgba(255, 6, 126, 0.18)' },
                            }}
                          >
                            <SmartToyOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>

                        <Tooltip title="Modifier le ticket">
                          <IconButton
                            size="small"
                            onClick={() => handleOpenEditModal(ticket)}
                            sx={{
                              color: '#303167',
                              bgcolor: 'rgba(48, 49, 103, 0.08)',
                              borderRadius: '10px',
                              '&:hover': { bgcolor: 'rgba(48, 49, 103, 0.18)' },
                            }}
                          >
                            <EditOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>

                        {user?.role === 'ADMIN' && (
                          <Tooltip title="Supprimer le ticket">
                            <IconButton
                              size="small"
                              onClick={() => handleDelete(ticket.id)}
                              sx={{
                                color: '#dc2626',
                                bgcolor: 'rgba(220, 38, 38, 0.08)',
                                borderRadius: '10px',
                                '&:hover': { bgcolor: 'rgba(220, 38, 38, 0.18)' },
                              }}
                            >
                              <DeleteOutlineIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                      </Stack>
                    </TableCell>
                  </TableRow>
                );
              })}
              {filteredTickets.length === 0 && (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 6 }}>
                    <Typography variant="body1" sx={{ fontWeight: 700, color: '#6b6b8a', mb: 0.5 }}>
                      Aucun ticket trouvé
                    </Typography>
                    <Typography variant="caption" sx={{ color: '#9899b5' }}>
                      Essayez de modifier vos critères de recherche ou créez un nouveau ticket.
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>

        <TablePagination
          rowsPerPageOptions={[10, 25, 50]}
          component="div"
          count={filteredTickets.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={(e, newPage) => setPage(newPage)}
          onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
          labelRowsPerPage="Lignes par page:"
          sx={{ borderTop: '1px solid rgba(48, 49, 103, 0.08)' }}
        />
      </Paper>

      {/* ── Modale Historique Discussion IA ── */}
      <Dialog
        open={openHistoryModal}
        onClose={handleCloseHistoryModal}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: '20px',
            p: 1,
            boxShadow: '0 20px 60px rgba(0, 0, 0, 0.25)',
          },
        }}
      >
        <DialogTitle sx={{ fontWeight: 800, color: '#303167', display: 'flex', alignItems: 'center', gap: 1.5, pb: 1 }}>
          <Box sx={{ p: 0.8, borderRadius: '10px', background: 'linear-gradient(135deg, #FF067E, #ff5ea7)', color: '#fff', display: 'flex' }}>
            <SmartToyOutlinedIcon sx={{ fontSize: 20 }} />
          </Box>
          Historique IA — Ticket #{selectedTicketForHistory?.id}
        </DialogTitle>
        <DialogContent dividers sx={{ borderColor: 'rgba(48, 49, 103, 0.08)' }}>
          {historyLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}><CircularProgress sx={{ color: '#FF067E' }} /></Box>
          ) : historySession && historySession.messages && Array.isArray(historySession.messages) && historySession.messages.length > 0 ? (
            <List sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, py: 1 }}>
              {historySession.messages.map((m, i) => (
                <ListItem key={i} sx={{ display: 'block', px: 0, py: 0.2 }}>
                  <Box sx={{ display: 'flex', justifyContent: m.role === 'user' ? 'flex-end' : 'flex-start' }}>
                    <Box
                      sx={{
                        maxWidth: '85%',
                        bgcolor: m.role === 'user' ? '#303167' : '#f4f5fb',
                        color: m.role === 'user' ? '#fff' : '#1a1a3a',
                        px: 2.2,
                        py: 1.4,
                        borderRadius: m.role === 'user' ? '16px 16px 4px 16px' : '16px 16px 16px 4px',
                        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)',
                        border: m.role === 'user' ? 'none' : '1px solid rgba(48, 49, 103, 0.06)',
                      }}
                    >
                      <Typography variant="caption" sx={{ fontWeight: 800, display: 'block', mb: 0.4, opacity: 0.85, color: m.role === 'user' ? '#ff5ea7' : '#303167' }}>
                        {m.role === 'user' ? (historySession.userName || 'Utilisateur') : 'Assistant IA Ollama'}
                      </Typography>
                      <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word', lineHeight: 1.5 }}>
                        {m.content}
                      </Typography>
                    </Box>
                  </Box>
                </ListItem>
              ))}
            </List>
          ) : (
            <Typography variant="body2" color="text.secondary" align="center" sx={{ py: 4 }}>
              Aucun historique de conversation IA associé à ce ticket.
            </Typography>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button
            onClick={handleCloseHistoryModal}
            variant="contained"
            sx={{
              borderRadius: '10px',
              fontWeight: 800,
              background: 'linear-gradient(135deg, #303167 0%, #4a4baa 100%)',
            }}
          >
            Fermer
          </Button>
        </DialogActions>
      </Dialog>

      {/* ── Modale Création / Modification ── */}
      <Dialog
        open={openModal}
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
          {editingTicket ? `Modifier le Ticket #${editingTicket.id}` : 'Créer un nouveau Ticket'}
        </DialogTitle>
        
        <form onSubmit={handleSubmitForm}>
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

            {(user?.role === 'ADMIN' || user?.role === 'TECHNICIAN') && editingTicket && (
              <Stack direction="row" spacing={2} sx={{ mt: 2.5 }}>
                <TextField
                  select
                  fullWidth
                  label="Statut"
                  value={formData.status}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                  sx={{ '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
                >
                  {STATUSES.map((s) => <MenuItem key={s.value} value={s.value}>{s.label}</MenuItem>)}
                </TextField>

                <TextField
                  select
                  fullWidth
                  label="Assigner à"
                  value={formData.assignedToId}
                  onChange={(e) => setFormData({ ...formData, assignedToId: e.target.value })}
                  sx={{ '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
                >
                  <MenuItem value="">-- Non assigné --</MenuItem>
                  {technicians.map((tech) => (
                    <MenuItem key={tech.id} value={tech.id}>
                      {tech.name} ({tech.role})
                    </MenuItem>
                  ))}
                </TextField>
              </Stack>
            )}
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
              {formLoading ? 'Enregistrement...' : (editingTicket ? 'Mettre à jour' : 'Créer le ticket')}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default Tickets;
