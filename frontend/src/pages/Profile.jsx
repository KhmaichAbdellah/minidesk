// src/pages/Profile.jsx
// Page de gestion du profil de l'utilisateur connecté.

import { useState, useEffect } from 'react';
import { 
  Box, Typography, Paper, TextField, Button, Alert, Grid, Divider, CircularProgress, Stack, Chip, Avatar
} from '@mui/material';
import { getMyProfile, updateMyProfile, changePassword } from '../service/userService';
import { useAuth } from '../context/AuthContext';
import PersonIcon from '@mui/icons-material/Person';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';

const Profile = () => {
  const { user } = useAuth();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [profileSuccess, setProfileSuccess] = useState('');
  const [profileError, setProfileError] = useState('');
  const [profileLoading, setProfileLoading] = useState(false);

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [passwordLoading, setPasswordLoading] = useState(false);

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMyProfile()
      .then((data) => {
        setName(data.name || '');
        setEmail(data.email || '');
      })
      .catch(() => {
        setProfileError('Erreur lors du chargement des données de profil.');
      })
      .finally(() => setLoading(false));
  }, []);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setProfileSuccess('');
    setProfileError('');
    setProfileLoading(true);

    try {
      await updateMyProfile({ name, email });
      setProfileSuccess('Vos informations de profil ont été mises à jour avec succès.');
    } catch (err) {
      setProfileError(err.response?.data?.message || err.response?.data || 'Erreur lors de la mise à jour du profil.');
    } finally {
      setProfileLoading(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setPasswordSuccess('');
    setPasswordError('');

    if (newPassword !== confirmPassword) {
      setPasswordError('Le nouveau mot de passe et sa confirmation ne correspondent pas.');
      return;
    }

    if (newPassword.length < 6) {
      setPasswordError('Le nouveau mot de passe doit contenir au moins 6 caractères.');
      return;
    }

    setPasswordLoading(true);

    try {
      await changePassword({ currentPassword, newPassword });
      setPasswordSuccess('Votre mot de passe a été modifié avec succès.');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      setPasswordError(err.response?.data?.message || err.response?.data || 'Erreur lors du changement de mot de passe. Vérifiez votre mot de passe actuel.');
    } finally {
      setPasswordLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
        <CircularProgress sx={{ color: '#FF067E' }} />
      </Box>
    );
  }

  return (
    <Box className="animate-fade-in" sx={{ maxWidth: 900, mx: 'auto' }}>
      {/* ── En-tête profil carte de visite ── */}
      <Paper
        elevation={0}
        sx={{
          p: 3.5,
          mb: 4,
          borderRadius: '24px',
          background: 'linear-gradient(135deg, #161733 0%, #252453 50%, #303167 100%)',
          color: '#ffffff',
          boxShadow: '0 12px 35px rgba(22, 23, 51, 0.25)',
          border: '1px solid rgba(255, 255, 255, 0.1)',
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        <Stack direction={{ xs: 'column', sm: 'row' }} alignItems={{ xs: 'flex-start', sm: 'center' }} spacing={3}>
          <Avatar
            sx={{
              width: 72,
              height: 72,
              fontSize: '1.8rem',
              fontWeight: 900,
              background: 'linear-gradient(135deg, #FF067E 0%, #d4005f 100%)',
              boxShadow: '0 8px 24px rgba(255, 6, 126, 0.4)',
            }}
          >
            {name ? name.charAt(0).toUpperCase() : 'U'}
          </Avatar>
          <Box>
            <Stack direction="row" alignItems="center" spacing={1.5} sx={{ mb: 0.5 }}>
              <Typography variant="h5" sx={{ fontWeight: 900, color: '#ffffff' }}>
                {name || 'Utilisateur MiniDesk'}
              </Typography>
              <Chip
                label={user?.role || 'USER'}
                size="small"
                sx={{
                  fontWeight: 900,
                  fontSize: '0.72rem',
                  bgcolor: 'rgba(255, 6, 126, 0.2)',
                  color: '#ff5ea7',
                  border: '1px solid rgba(255, 6, 126, 0.4)',
                }}
              />
            </Stack>
            <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.75)' }}>
              {email}
            </Typography>
          </Box>
        </Stack>
      </Paper>

      <Grid container spacing={3.5}>
        {/* ── Section 1 : Informations Personnelles ── */}
        <Grid item xs={12} md={6}>
          <Paper
            elevation={0}
            sx={{
              p: 4,
              borderRadius: '20px',
              background: 'rgba(255, 255, 255, 0.95)',
              backdropFilter: 'blur(20px)',
              border: '1px solid rgba(48, 49, 103, 0.08)',
              boxShadow: '0 8px 30px rgba(0, 0, 0, 0.03)',
              height: '100%',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2.5 }}>
              <Box sx={{ p: 1, borderRadius: '10px', bgcolor: 'rgba(48, 49, 103, 0.08)', color: '#303167', display: 'flex' }}>
                <PersonIcon sx={{ fontSize: 20 }} />
              </Box>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#1a1a3a' }}>
                Informations du compte
              </Typography>
            </Box>
            <Divider sx={{ mb: 3, borderColor: 'rgba(48, 49, 103, 0.08)' }} />

            {profileSuccess && <Alert severity="success" sx={{ mb: 2.5, borderRadius: 2 }}>{profileSuccess}</Alert>}
            {profileError && <Alert severity="error" sx={{ mb: 2.5, borderRadius: 2 }}>{profileError}</Alert>}

            <Box component="form" onSubmit={handleUpdateProfile}>
              <TextField
                fullWidth
                label="Nom Complet"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                sx={{ mb: 2.5, '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
              />
              <TextField
                fullWidth
                label="Adresse Email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                sx={{ mb: 3.5, '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
              />
              <Button
                type="submit"
                variant="contained"
                disabled={profileLoading}
                sx={{
                  py: 1.3,
                  px: 3,
                  borderRadius: '12px',
                  fontWeight: 800,
                  background: 'linear-gradient(135deg, #303167 0%, #4a4baa 100%)',
                  boxShadow: '0 6px 18px rgba(48, 49, 103, 0.25)',
                }}
              >
                {profileLoading ? 'Enregistrement...' : 'Mettre à jour mes infos'}
              </Button>
            </Box>
          </Paper>
        </Grid>

        {/* ── Section 2 : Changement de Mot de Passe ── */}
        <Grid item xs={12} md={6}>
          <Paper
            elevation={0}
            sx={{
              p: 4,
              borderRadius: '20px',
              background: 'rgba(255, 255, 255, 0.95)',
              backdropFilter: 'blur(20px)',
              border: '1px solid rgba(48, 49, 103, 0.08)',
              boxShadow: '0 8px 30px rgba(0, 0, 0, 0.03)',
              height: '100%',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2.5 }}>
              <Box sx={{ p: 1, borderRadius: '10px', bgcolor: 'rgba(255, 6, 126, 0.08)', color: '#FF067E', display: 'flex' }}>
                <LockOutlinedIcon sx={{ fontSize: 20 }} />
              </Box>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#1a1a3a' }}>
                Sécurité & Mot de passe
              </Typography>
            </Box>
            <Divider sx={{ mb: 3, borderColor: 'rgba(48, 49, 103, 0.08)' }} />

            {passwordSuccess && <Alert severity="success" sx={{ mb: 2.5, borderRadius: 2 }}>{passwordSuccess}</Alert>}
            {passwordError && <Alert severity="error" sx={{ mb: 2.5, borderRadius: 2 }}>{passwordError}</Alert>}

            <Box component="form" onSubmit={handleChangePassword}>
              <TextField
                fullWidth
                label="Mot de passe actuel"
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                required
                sx={{ mb: 2.2, '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
              />
              <TextField
                fullWidth
                label="Nouveau mot de passe"
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
                sx={{ mb: 2.2, '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
              />
              <TextField
                fullWidth
                label="Confirmer le nouveau mot de passe"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                sx={{ mb: 3.5, '& .MuiOutlinedInput-root': { bgcolor: '#f7f8fc', borderRadius: '12px' } }}
              />
              <Button
                type="submit"
                variant="contained"
                disabled={passwordLoading}
                sx={{
                  py: 1.3,
                  px: 3,
                  borderRadius: '12px',
                  fontWeight: 800,
                  background: 'linear-gradient(135deg, #FF067E 0%, #d4005f 100%)',
                  boxShadow: '0 6px 18px rgba(255, 6, 126, 0.35)',
                }}
              >
                {passwordLoading ? 'Modification...' : 'Changer le mot de passe'}
              </Button>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Profile;
