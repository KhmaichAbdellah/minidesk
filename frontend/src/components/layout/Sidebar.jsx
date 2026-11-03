// src/components/layout/Sidebar.jsx
import { Box, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Typography } from '@mui/material';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ConfirmationNumberIcon from '@mui/icons-material/ConfirmationNumber';
import PeopleIcon from '@mui/icons-material/People';
import PersonIcon from '@mui/icons-material/Person';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import BoltIcon from '@mui/icons-material/Bolt';

const Sidebar = () => {
  const { user } = useAuth();

  return (
    <Box
      sx={{
        width: 'var(--sidebar-width)',
        background: 'linear-gradient(180deg, #161733 0%, #101124 100%)',
        color: '#ffffff',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
        display: 'flex',
        flexDirection: 'column',
        zIndex: (theme) => theme.zIndex.drawer + 2,
        borderRight: '1px solid rgba(255, 255, 255, 0.08)',
        boxShadow: '4px 0 24px rgba(0, 0, 0, 0.25)',
      }}
    >
      {/* ── Logo & Identité ── */}
      <Box
        sx={{
          p: 3,
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          height: 'var(--navbar-height)',
          borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
        }}
      >
        <Typography
          variant="h5"
          sx={{
            fontWeight: 900,
            letterSpacing: '0.08em',
            textTransform: 'uppercase',
            fontSize: '1.45rem',
            display: 'flex',
            alignItems: 'center',
          }}
        >
          <Box component="span" sx={{ color: '#ffffff' }}>MINI</Box>
          <Box
            component="span"
            sx={{
              background: 'linear-gradient(135deg, #FF067E 0%, #ff5ea7 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              ml: 0.5,
            }}
          >
            DESK
          </Box>
        </Typography>
      </Box>

      {/* ── Navigation ── */}
      <List sx={{ px: 2, py: 3, flexGrow: 1, display: 'flex', flexDirection: 'column', gap: 1 }}>
        <ListItem disablePadding>
          <ListItemButton
            component={NavLink}
            to="/dashboard"
            sx={{
              borderRadius: '12px',
              py: 1.2,
              px: 2,
              transition: 'all 0.2s ease',
              '&.active': {
                background: 'linear-gradient(135deg, rgba(255, 6, 126, 0.2) 0%, rgba(255, 6, 126, 0.08) 100%)',
                border: '1px solid rgba(255, 6, 126, 0.4)',
                boxShadow: '0 4px 16px rgba(255, 6, 126, 0.25)',
                '& .MuiListItemIcon-root': { color: '#FF067E' },
                '& .MuiListItemText-primary': { color: '#ffffff', fontWeight: 800 },
              },
              '&:hover:not(.active)': {
                bgcolor: 'rgba(255, 255, 255, 0.06)',
              },
            }}
          >
            <ListItemIcon sx={{ color: 'rgba(255, 255, 255, 0.65)', minWidth: 38 }}>
              <DashboardIcon sx={{ fontSize: 21 }} />
            </ListItemIcon>
            <ListItemText primary="Tableau de bord" primaryTypographyProps={{ fontWeight: 700, fontSize: '0.92rem' }} />
          </ListItemButton>
        </ListItem>

        <ListItem disablePadding>
          <ListItemButton
            component={NavLink}
            to="/tickets"
            sx={{
              borderRadius: '12px',
              py: 1.2,
              px: 2,
              transition: 'all 0.2s ease',
              '&.active': {
                background: 'linear-gradient(135deg, rgba(255, 6, 126, 0.2) 0%, rgba(255, 6, 126, 0.08) 100%)',
                border: '1px solid rgba(255, 6, 126, 0.4)',
                boxShadow: '0 4px 16px rgba(255, 6, 126, 0.25)',
                '& .MuiListItemIcon-root': { color: '#FF067E' },
                '& .MuiListItemText-primary': { color: '#ffffff', fontWeight: 800 },
              },
              '&:hover:not(.active)': {
                bgcolor: 'rgba(255, 255, 255, 0.06)',
              },
            }}
          >
            <ListItemIcon sx={{ color: 'rgba(255, 255, 255, 0.65)', minWidth: 38 }}>
              <ConfirmationNumberIcon sx={{ fontSize: 21 }} />
            </ListItemIcon>
            <ListItemText primary="Tickets" primaryTypographyProps={{ fontWeight: 700, fontSize: '0.92rem' }} />
          </ListItemButton>
        </ListItem>

        <ListItem disablePadding>
          <ListItemButton
            component={NavLink}
            to="/knowledge"
            sx={{
              borderRadius: '12px',
              py: 1.2,
              px: 2,
              transition: 'all 0.2s ease',
              '&.active': {
                background: 'linear-gradient(135deg, rgba(255, 6, 126, 0.2) 0%, rgba(255, 6, 126, 0.08) 100%)',
                border: '1px solid rgba(255, 6, 126, 0.4)',
                boxShadow: '0 4px 16px rgba(255, 6, 126, 0.25)',
                '& .MuiListItemIcon-root': { color: '#FF067E' },
                '& .MuiListItemText-primary': { color: '#ffffff', fontWeight: 800 },
              },
              '&:hover:not(.active)': {
                bgcolor: 'rgba(255, 255, 255, 0.06)',
              },
            }}
          >
            <ListItemIcon sx={{ color: 'rgba(255, 255, 255, 0.65)', minWidth: 38 }}>
              <MenuBookIcon sx={{ fontSize: 21 }} />
            </ListItemIcon>
            <ListItemText primary="Base de connaissances" primaryTypographyProps={{ fontWeight: 700, fontSize: '0.92rem' }} />
          </ListItemButton>
        </ListItem>

        {user?.role === 'ADMIN' && (
          <ListItem disablePadding>
            <ListItemButton
              component={NavLink}
              to="/users"
              sx={{
                borderRadius: '12px',
                py: 1.2,
                px: 2,
                transition: 'all 0.2s ease',
                '&.active': {
                  background: 'linear-gradient(135deg, rgba(255, 6, 126, 0.2) 0%, rgba(255, 6, 126, 0.08) 100%)',
                  border: '1px solid rgba(255, 6, 126, 0.4)',
                  boxShadow: '0 4px 16px rgba(255, 6, 126, 0.25)',
                  '& .MuiListItemIcon-root': { color: '#FF067E' },
                  '& .MuiListItemText-primary': { color: '#ffffff', fontWeight: 800 },
                },
                '&:hover:not(.active)': {
                  bgcolor: 'rgba(255, 255, 255, 0.06)',
                },
              }}
            >
              <ListItemIcon sx={{ color: 'rgba(255, 255, 255, 0.65)', minWidth: 38 }}>
                <PeopleIcon sx={{ fontSize: 21 }} />
              </ListItemIcon>
              <ListItemText primary="Utilisateurs" primaryTypographyProps={{ fontWeight: 700, fontSize: '0.92rem' }} />
            </ListItemButton>
          </ListItem>
        )}

        <ListItem disablePadding>
          <ListItemButton
            component={NavLink}
            to="/profile"
            sx={{
              borderRadius: '12px',
              py: 1.2,
              px: 2,
              transition: 'all 0.2s ease',
              '&.active': {
                background: 'linear-gradient(135deg, rgba(255, 6, 126, 0.2) 0%, rgba(255, 6, 126, 0.08) 100%)',
                border: '1px solid rgba(255, 6, 126, 0.4)',
                boxShadow: '0 4px 16px rgba(255, 6, 126, 0.25)',
                '& .MuiListItemIcon-root': { color: '#FF067E' },
                '& .MuiListItemText-primary': { color: '#ffffff', fontWeight: 800 },
              },
              '&:hover:not(.active)': {
                bgcolor: 'rgba(255, 255, 255, 0.06)',
              },
            }}
          >
            <ListItemIcon sx={{ color: 'rgba(255, 255, 255, 0.65)', minWidth: 38 }}>
              <PersonIcon sx={{ fontSize: 21 }} />
            </ListItemIcon>
            <ListItemText primary="Mon Profil" primaryTypographyProps={{ fontWeight: 700, fontSize: '0.92rem' }} />
          </ListItemButton>
        </ListItem>
      </List>

      {/* ── Pied de Sidebar avec Badge IA & Copyright ── */}
      <Box sx={{ p: 2.5, borderTop: '1px solid rgba(255, 255, 255, 0.08)' }}>
        <Box
          sx={{
            p: 1.5,
            borderRadius: '12px',
            bgcolor: 'rgba(255, 255, 255, 0.04)',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            mb: 2,
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
            <BoltIcon sx={{ fontSize: 16, color: '#FF067E' }} />
            <Typography variant="caption" sx={{ fontWeight: 800, color: '#ffffff' }}>
              MiniDesk v1.0
            </Typography>
          </Box>
          <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.5)', display: 'block', fontSize: '0.72rem' }}>
            Support IT Intelligent
          </Typography>
        </Box>
        <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.4)', display: 'block', textAlign: 'center', fontSize: '0.72rem' }}>
          © 2026 MiniDesk
        </Typography>
      </Box>
    </Box>
  );
};

export default Sidebar;
