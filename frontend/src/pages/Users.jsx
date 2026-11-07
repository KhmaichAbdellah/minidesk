// src/pages/Users.jsx
// Page de gestion des utilisateurs de l'application (Réservé au rôle ADMIN).

import { useState, useEffect, useMemo, useCallback } from 'react';
import { 
  Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, Chip, TextField, Stack, IconButton, TablePagination, CircularProgress, MenuItem, Switch,
  Dialog, DialogTitle, DialogContent, DialogActions, Alert, InputAdornment, Tooltip
} from '@mui/material';
import { getAllUsers, createUser, updateUser, deleteUser, toggleUserStatus, resetUserPassword } from '../service/userService';
import AddIcon from '@mui/icons-material/Add';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';
import PeopleIcon from '@mui/icons-material/People';
import KeyIcon from '@mui/icons-material/Key';

const ROLE_STYLES = {
  ADMIN: { bg: 'rgba(255, 6, 126, 0.12)', color: '#FF067E', border: 'rgba(255, 6, 126, 0.3)' },
  TECHNICIAN: { bg: 'rgba(72, 144, 255, 0.12)', color: '#2563eb', border: 'rgba(72, 144, 255, 0.3)' },
  USER: { bg: 'rgba(65, 207, 124, 0.12)', color: '#16a34a', border: 'rgba(65, 207, 124, 0.3)' },
};

const Users = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const [openModal, setOpenModal] = useState(false);
  const [editingUser, setEditingUser] = useState(null);

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    role: 'USER',
  });

  const [formError, setFormError] = useState('');
  const [formSuccessInfo, setFormSuccessInfo] = useState('');
  const [formLoading, setFormLoading] = useState(false);

  const fetchUsers = useCallback(() => {
    setLoading(true);
    getAllUsers()
      .then((data) => setUsers(data))
      .catch((err) => console.error("Erreur chargement utilisateurs:", err))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleOpenCreateModal = () => {
    setEditingUser(null);
    setFormData({ name: '', email: '', role: 'USER' });
    setFormError('');
    setFormSuccessInfo('');
    setOpenModal(true);
  };

  const handleOpenEditModal = (userToEdit) => {
    setEditingUser(userToEdit);
    setFormData({
      name: userToEdit.name || '',
      email: userToEdit.email || '',
      role: userToEdit.role || 'USER',
    });
    setFormError('');
    setFormSuccessInfo('');
    setOpenModal(true);
  };

  const handleCloseModal = () => {
    setOpenModal(false);
    setEditingUser(null);
  };

  const handleSubmitForm = async (e) => {
    e.preventDefault();
    setFormError('');
    setFormSuccessInfo('');

    if (!formData.name.trim() || !formData.email.trim()) {
      setFormError('Le nom et l\'email sont obligatoires.');
      return;
    }

    setFormLoading(true);

    try {
      if (editingUser) {
        await updateUser(editingUser.id, formData);
        handleCloseModal();
        fetchUsers();
      } else {
        const res = await createUser(formData);
        setFormSuccessInfo(`Utilisateur créé ! Mot de passe temporaire : ${res.temporaryPassword || 'Admin@2026!'}`);
        fetchUsers();
      }
    } catch (err) {
      setFormError(err.response?.data?.message || err.response?.data || 'Erreur lors de l\'enregistrement de l\'utilisateur.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Voulez-vous vraiment supprimer cet utilisateur ?')) {
      try {
        await deleteUser(id);
        fetchUsers();
      } catch (err) {
        console.error("Erreur suppression:", err);
      }
    }
  };

  const handleToggleStatus = async (user) => {
    try {
      await toggleUserStatus(user.id);
      fetchUsers();
    } catch (err) {
      console.error("Erreur toggle status:", err);
    }
  };

  const handleResetPassword = async (user) => {
    if (window.confirm(`Générer un nouveau mot de passe pour ${user.name || user.email} ?`)) {
      try {
        const res = await resetUserPassword(user.id);
        alert(`Nouveau mot de passe pour ${user.email} :\n\n${res.temporaryPassword}\n\nNotez-le et transmettez-le à l'utilisateur.`);
      } catch (err) {
        console.error("Erreur réinitialisation mot de passe:", err);
      }
    }
  };

  const filteredUsers = useMemo(() => {
    const lowerSearch = searchTerm.toLowerCase();
    return users.filter((u) => {
      const matchesSearch =
        (u.name && u.name.toLowerCase().includes(lowerSearch)) ||
        (u.email && u.email.toLowerCase().includes(lowerSearch));
      const matchesRole = roleFilter === 'ALL' || u.role === roleFilter;
      return matchesSearch && matchesRole;
    });
  }, [users, searchTerm, roleFilter]);

  const paginatedUsers = useMemo(
    () => filteredUsers.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage),
    [filteredUsers, page, rowsPerPage]
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
      {/* ── Filtres et Actions ── */}
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
              placeholder="Rechercher par nom ou email..."
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
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
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
              <MenuItem value="ALL">Tous les rôles</MenuItem>
              <MenuItem value="ADMIN">Administrateur</MenuItem>
              <MenuItem value="TECHNICIAN">Technicien</MenuItem>
              <MenuItem value="USER">Utilisateur</MenuItem>
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
            Nouvel Utilisateur
          </Button>
        </Stack>
      </Paper>

      {/* ── Tableau Utilisateurs ── */}
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
                <TableCell sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc' }}>Nom & Prénom</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc' }}>Email de connexion</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc' }}>Rôle</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc' }}>Compte Actif</TableCell>
                <TableCell align="right" sx={{ fontWeight: 800, color: '#303167', bgcolor: '#f7f8fc', pr: 3 }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginatedUsers.map((u) => {
                const roleStyle = ROLE_STYLES[u.role] || ROLE_STYLES.USER;

                return (
                  <TableRow
                    hover
                    key={u.id}
                    sx={{
                      transition: 'background-color 0.2s ease',
                      '&:hover': { bgcolor: 'rgba(255, 6, 126, 0.02) !important' },
                    }}
                  >
                    <TableCell sx={{ fontWeight: 800, color: '#6b6b8a', fontSize: '0.85rem' }}>
                      #{u.id}
                    </TableCell>
                    <TableCell sx={{ fontWeight: 800, color: '#1a1a3a' }}>
                      {u.name || '-'}
                    </TableCell>
                    <TableCell sx={{ color: '#4a4baa', fontWeight: 600 }}>
                      {u.email}
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={u.role}
                        sx={{
                          fontWeight: 800,
                          fontSize: '0.72rem',
                          bgcolor: roleStyle.bg,
                          color: roleStyle.color,
                          border: `1px solid ${roleStyle.border}`,
                          borderRadius: '8px',
                        }}
                      />
                    </TableCell>
                    <TableCell>
                      <Switch
                        checked={u.enabled !== false}
                        onChange={() => handleToggleStatus(u)}
                        size="small"
                        color="secondary"
                      />
                    </TableCell>
                    <TableCell align="right" sx={{ pr: 2 }}>
                      <Stack direction="row" spacing={0.5} justifyContent="flex-end">
                        <Tooltip title="Réinitialiser le mot de passe">
                          <IconButton
                            size="small"
                            onClick={() => handleResetPassword(u)}
                            sx={{
                              color: '#ea580c',
                              bgcolor: 'rgba(234, 88, 12, 0.08)',
                              borderRadius: '10px',
                              '&:hover': { bgcolor: 'rgba(234, 88, 12, 0.18)' },
                            }}
                          >
                            <KeyIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Modifier">
                          <IconButton
                            size="small"
                            onClick={() => handleOpenEditModal(u)}
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
                        <Tooltip title="Supprimer">
                          <IconButton
                            size="small"
                            onClick={() => handleDelete(u.id)}
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
                      </Stack>
                    </TableCell>
                  </TableRow>
                );
              })}
              {filteredUsers.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6 }}>
                    <Typography variant="body1" sx={{ fontWeight: 700, color: '#6b6b8a' }}>
                      Aucun utilisateur trouvé
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
          count={filteredUsers.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={(e, newPage) => setPage(newPage)}
          onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
          labelRowsPerPage="Lignes par page:"
          sx={{ borderTop: '1px solid rgba(48, 49, 103, 0.08)' }}
        />
      </Paper>

      {/* ── Modale Création / Modification Utilisateur ── */}
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
        <DialogTitle sx={{ fontWeight: 900, color: '#1a1a3a', fontSize: '1.25rem', pb: 1, display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Box sx={{ p: 0.8, borderRadius: '10px', background: 'linear-gradient(135deg, #303167, #4a4baa)', color: '#fff', display: 'flex' }}>
            <PeopleIcon sx={{ fontSize: 20 }} />
          </Box>
          {editingUser ? `Modifier l'utilisateur #${editingUser.id}` : 'Créer un nouvel utilisateur'}
        </DialogTitle>

        <form onSubmit={handleSubmitForm}>
          <DialogContent dividers sx={{ borderColor: 'rgba(48, 49, 103, 0.08)' }}>
            {formError && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{formError}</Alert>}
            {formSuccessInfo && <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }}>{formSuccessInfo}</Alert>}

            <TextField
              margin="normal"
              required
              fullWidth
              label="Nom Complet"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              autoFocus
              sx={{ mb: 2, '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              label="Adresse Email"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              sx={{ mb: 2, '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
            />

            <TextField
              select
              fullWidth
              margin="normal"
              label="Rôle attribué"
              value={formData.role}
              onChange={(e) => setFormData({ ...formData, role: e.target.value })}
              sx={{ '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
            >
              <MenuItem value="USER">Utilisateur standard</MenuItem>
              <MenuItem value="TECHNICIAN">Technicien support</MenuItem>
              <MenuItem value="ADMIN">Administrateur système</MenuItem>
            </TextField>
          </DialogContent>

          <DialogActions sx={{ p: 2.5 }}>
            <Button onClick={handleCloseModal} color="inherit" sx={{ fontWeight: 700, borderRadius: '10px' }}>
              Fermer
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
              {formLoading ? 'Enregistrement...' : (editingUser ? 'Mettre à jour' : 'Créer l\'utilisateur')}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default Users;
