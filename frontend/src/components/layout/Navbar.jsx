// src/components/layout/Navbar.jsx
import { AppBar, Toolbar, Typography, Button, Box, Avatar, Stack, Chip } from '@mui/material';
import { useAuth } from '../../context/AuthContext';
import LogoutIcon from '@mui/icons-material/Logout';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import { useLocation } from 'react-router-dom';

const Navbar = () => {
  const { user, logout } = useAuth();
  const location = useLocation();

  let pageTitle = 'Tableau de bord';
  let pageSubtitle = 'Vue d\'ensemble & indicateurs de performance';
  if (location.pathname === '/tickets') {
    pageTitle = 'Gestion des Tickets';
    pageSubtitle = 'Suivi, assignation et historique des demandes';
  }
  if (location.pathname === '/users') {
    pageTitle = 'Gestion des Utilisateurs';
    pageSubtitle = 'Administration des comptes et attributions des rôles';
  }
  if (location.pathname === '/profile') {
    pageTitle = 'Mon Profil';
    pageSubtitle = 'Paramètres de compte et sécurité';
  }

  const getRoleBadgeStyle = (role) => {
    switch (role) {
      case 'ADMIN':
        return { bgcolor: 'rgba(255, 6, 126, 0.12)', color: '#FF067E', border: '1px solid rgba(255, 6, 126, 0.25)' };
      case 'TECHNICIAN':
        return { bgcolor: 'rgba(72, 144, 255, 0.12)', color: '#4890ff', border: '1px solid rgba(72, 144, 255, 0.25)' };
      default:
        return { bgcolor: 'rgba(65, 207, 124, 0.12)', color: '#2eb363', border: '1px solid rgba(65, 207, 124, 0.25)' };
    }
  };

  return (
    <AppBar
      position="fixed"
      elevation={0}
      sx={{
        width: 'calc(100% - var(--sidebar-width))',
        ml: 'var(--sidebar-width)',
        bgcolor: 'rgba(255, 255, 255, 0.92)',
        backdropFilter: 'blur(16px)',
        color: '#1a1a3a',
        borderBottom: '1px solid rgba(48, 49, 103, 0.08)',
        height: 'var(--navbar-height)',
        justifyContent: 'center',
        boxShadow: '0 4px 20px rgba(0, 0, 0, 0.03)',
      }}
    >
      <Toolbar sx={{ justifyContent: 'space-between', px: { xs: 2, md: 4 } }}>
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 900, color: '#1a1a3a', letterSpacing: '-0.02em', lineHeight: 1.2 }}>
            {pageTitle}
          </Typography>
          <Typography variant="caption" sx={{ color: '#6b6b8a', fontWeight: 600, display: { xs: 'none', sm: 'block' } }}>
            {pageSubtitle}
          </Typography>
        </Box>

        <Stack direction="row" spacing={2} alignItems="center">
          {user && (
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 1.5,
                px: 2,
                py: 0.8,
                borderRadius: '14px',
                bgcolor: '#f7f8fc',
                border: '1px solid rgba(48, 49, 103, 0.08)',
              }}
            >
              <Avatar
                sx={{
                  background: 'linear-gradient(135deg, #303167 0%, #4a4baa 100%)',
                  width: 34,
                  height: 34,
                  boxShadow: '0 2px 8px rgba(48, 49, 103, 0.25)',
                }}
              >
                <AccountCircleIcon fontSize="small" />
              </Avatar>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 800, lineHeight: 1.2, color: '#1a1a3a' }}>
                  {user.name || user.email}
                </Typography>
                <Chip
                  label={user.role}
                  size="small"
                  sx={{
                    height: 18,
                    fontSize: '0.65rem',
                    fontWeight: 900,
                    mt: 0.2,
                    ...getRoleBadgeStyle(user.role),
                  }}
                />
              </Box>
            </Box>
          )}

          <Button
            variant="outlined"
            startIcon={<LogoutIcon sx={{ fontSize: 18 }} />}
            onClick={logout}
            sx={{
              borderRadius: '12px',
              fontWeight: 800,
              px: 2,
              py: 0.9,
              color: '#FF067E',
              borderColor: 'rgba(255, 6, 126, 0.3)',
              bgcolor: 'rgba(255, 6, 126, 0.04)',
              transition: 'all 0.2s ease',
              '&:hover': {
                borderColor: '#FF067E',
                bgcolor: 'rgba(255, 6, 126, 0.1)',
                boxShadow: '0 4px 14px rgba(255, 6, 126, 0.2)',
                transform: 'translateY(-1px)',
              },
            }}
          >
            Déconnexion
          </Button>
        </Stack>
      </Toolbar>
    </AppBar>
  );
};

export default Navbar;
