import { useState } from 'react';
import {
  Box, Button, TextField, Typography, Paper, Alert, CircularProgress,
  InputAdornment, IconButton, Chip, Stack
} from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';
import SmartToyOutlinedIcon from '@mui/icons-material/SmartToyOutlined';
import SecurityOutlinedIcon from '@mui/icons-material/SecurityOutlined';
import ConfirmationNumberOutlinedIcon from '@mui/icons-material/ConfirmationNumberOutlined';
import BoltOutlinedIcon from '@mui/icons-material/BoltOutlined';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(email, password);
      navigate('/dashboard');
    } catch {
      setError('Identifiants incorrects ou problème de connexion.');
    } finally {
      setLoading(false);
    }
  };

  const handleTogglePassword = () => setShowPassword((prev) => !prev);

  return (
    <Box
      sx={{
        minHeight: '100vh',
        width: '100vw',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        overflow: 'hidden',
        background: 'linear-gradient(135deg, #0c0d1e 0%, #161733 40%, #201f46 70%, #12132b 100%)',
        p: 2,
        '&::before': {
          content: '""',
          position: 'absolute',
          inset: 0,
          backgroundImage: `
            radial-gradient(rgba(255, 255, 255, 0.06) 1.2px, transparent 1.2px),
            linear-gradient(to right, rgba(255, 255, 255, 0.015) 1px, transparent 1px),
            linear-gradient(to bottom, rgba(255, 255, 255, 0.015) 1px, transparent 1px)
          `,
          backgroundSize: '32px 32px, 64px 64px, 64px 64px',
          opacity: 0.8,
          pointerEvents: 'none',
        },
      }}
    >
      {/* ── Orbes lumineux animés en arrière-plan ── */}
      <Box
        sx={{
          position: 'absolute',
          top: '-10%',
          left: '15%',
          width: { xs: '300px', md: '550px' },
          height: { xs: '300px', md: '550px' },
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(255, 6, 126, 0.35) 0%, rgba(255, 6, 126, 0.05) 55%, transparent 70%)',
          filter: 'blur(70px)',
          animation: 'floatOrb1 14s ease-in-out infinite alternate',
          pointerEvents: 'none',
          zIndex: 0,
        }}
      />
      <Box
        sx={{
          position: 'absolute',
          bottom: '-12%',
          right: '12%',
          width: { xs: '320px', md: '600px' },
          height: { xs: '320px', md: '600px' },
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(72, 144, 255, 0.35) 0%, rgba(74, 75, 170, 0.15) 50%, transparent 70%)',
          filter: 'blur(80px)',
          animation: 'floatOrb2 16s ease-in-out infinite alternate',
          pointerEvents: 'none',
          zIndex: 0,
        }}
      />
      <Box
        sx={{
          position: 'absolute',
          top: '40%',
          right: '30%',
          width: { xs: '200px', md: '400px' },
          height: { xs: '200px', md: '400px' },
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(123, 175, 255, 0.25) 0%, rgba(255, 6, 126, 0.1) 45%, transparent 70%)',
          filter: 'blur(60px)',
          animation: 'floatOrb3 18s ease-in-out infinite alternate',
          pointerEvents: 'none',
          zIndex: 0,
        }}
      />

      {/* ── Widget 1 (Haut Gauche) : Assistant IA Ollama & Modèle Local ── */}
      <Box
        sx={{
          display: { xs: 'none', lg: 'flex' },
          flexDirection: 'column',
          gap: 1,
          position: 'absolute',
          left: '6%',
          top: '20%',
          p: 2,
          minWidth: 260,
          borderRadius: '18px',
          background: 'rgba(255, 255, 255, 0.05)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(255, 255, 255, 0.12)',
          boxShadow: '0 16px 40px rgba(0, 0, 0, 0.35)',
          animation: 'floatBadge 6s ease-in-out infinite',
          zIndex: 1,
        }}
      >
        <Stack direction="row" alignItems="center" justifyContent="space-between">
          <Stack direction="row" alignItems="center" spacing={1.5}>
            <Box
              sx={{
                p: 1.1,
                borderRadius: '12px',
                background: 'linear-gradient(135deg, #FF067E, #ff5ea7)',
                display: 'flex',
                boxShadow: '0 4px 14px rgba(255, 6, 126, 0.4)',
              }}
            >
              <SmartToyOutlinedIcon sx={{ fontSize: 22, color: '#fff' }} />
            </Box>
            <Box>
              <Typography variant="body2" sx={{ fontWeight: 800, color: '#ffffff', lineHeight: 1.2 }}>
                Assistant IA Ollama
              </Typography>
              <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.65)' }}>
                Modèle phi3:mini local
              </Typography>
            </Box>
          </Stack>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 0.6,
              bgcolor: 'rgba(65, 207, 124, 0.15)',
              border: '1px solid rgba(65, 207, 124, 0.3)',
              px: 1,
              py: 0.3,
              borderRadius: '20px',
            }}
          >
            <Box sx={{ width: 7, height: 7, borderRadius: '50%', bgcolor: '#41CF7C', animation: 'pulseDot 2s infinite' }} />
            <Typography variant="caption" sx={{ color: '#41CF7C', fontWeight: 800, fontSize: '0.68rem' }}>
              ACTIF
            </Typography>
          </Box>
        </Stack>
        <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.55)', fontSize: '0.75rem', pl: 0.5 }}>
          Auto-catégorisation & diagnostic instantané
        </Typography>
      </Box>

      {/* ── Widget 2 (Haut Droite) : Suivi en Temps Réel ── */}
      <Box
        sx={{
          display: { xs: 'none', lg: 'flex' },
          flexDirection: 'column',
          gap: 1,
          position: 'absolute',
          right: '6%',
          top: '22%',
          p: 2,
          minWidth: 260,
          borderRadius: '18px',
          background: 'rgba(255, 255, 255, 0.05)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(255, 255, 255, 0.12)',
          boxShadow: '0 16px 40px rgba(0, 0, 0, 0.35)',
          animation: 'floatBadge 7s ease-in-out infinite reverse',
          zIndex: 1,
        }}
      >
        <Stack direction="row" alignItems="center" spacing={1.5}>
          <Box
            sx={{
              p: 1.1,
              borderRadius: '12px',
              background: 'linear-gradient(135deg, #4890ff, #7bafff)',
              display: 'flex',
              boxShadow: '0 4px 14px rgba(72, 144, 255, 0.4)',
            }}
          >
            <BoltOutlinedIcon sx={{ fontSize: 22, color: '#fff' }} />
          </Box>
          <Box>
            <Typography variant="body2" sx={{ fontWeight: 800, color: '#ffffff', lineHeight: 1.2 }}>
              Suivi en Temps Réel
            </Typography>
            <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.65)' }}>
              Mise à jour instantanée
            </Typography>
          </Box>
        </Stack>
        <Stack direction="row" alignItems="baseline" spacing={1} sx={{ pl: 0.5 }}>
          <Typography variant="h6" sx={{ color: '#7bafff', fontWeight: 900, lineHeight: 1 }}>
            Notifications & Statuts
          </Typography>
        </Stack>
      </Box>

      {/* ── Widget 3 (Bas Gauche) : Équipe Support & Attribution ── */}
      <Box
        sx={{
          display: { xs: 'none', lg: 'flex' },
          flexDirection: 'column',
          gap: 1,
          position: 'absolute',
          left: '8%',
          bottom: '16%',
          p: 2,
          minWidth: 250,
          borderRadius: '18px',
          background: 'rgba(255, 255, 255, 0.05)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(255, 255, 255, 0.12)',
          boxShadow: '0 16px 40px rgba(0, 0, 0, 0.35)',
          animation: 'floatBadge 8s ease-in-out infinite 1s',
          zIndex: 1,
        }}
      >
        <Stack direction="row" alignItems="center" spacing={1.5}>
          <Box
            sx={{
              p: 1.1,
              borderRadius: '12px',
              background: 'linear-gradient(135deg, #303167, #4a4baa)',
              display: 'flex',
              boxShadow: '0 4px 14px rgba(48, 49, 103, 0.5)',
            }}
          >
            <ConfirmationNumberOutlinedIcon sx={{ fontSize: 22, color: '#fff' }} />
          </Box>
          <Box>
            <Typography variant="body2" sx={{ fontWeight: 800, color: '#ffffff', lineHeight: 1.2 }}>
              Flux IT Centralisé
            </Typography>
            <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.65)' }}>
              Hardware • Software • Réseau
            </Typography>
          </Box>
        </Stack>
        <Stack direction="row" spacing={1} sx={{ mt: 0.3, pl: 0.5 }}>
          <Chip label="Admin" size="small" sx={{ height: 20, fontSize: '0.65rem', fontWeight: 800, bgcolor: 'rgba(255,6,126,0.2)', color: '#ff5ea7', border: '1px solid rgba(255,6,126,0.3)' }} />
          <Chip label="Technicien" size="small" sx={{ height: 20, fontSize: '0.65rem', fontWeight: 800, bgcolor: 'rgba(72,144,255,0.2)', color: '#7bafff', border: '1px solid rgba(72,144,255,0.3)' }} />
          <Chip label="User" size="small" sx={{ height: 20, fontSize: '0.65rem', fontWeight: 800, bgcolor: 'rgba(255,255,255,0.1)', color: 'rgba(255,255,255,0.8)' }} />
        </Stack>
      </Box>

      {/* ── Widget 4 (Bas Droite) : Sécurité & Stack Technique ── */}
      <Box
        sx={{
          display: { xs: 'none', lg: 'flex' },
          flexDirection: 'column',
          gap: 1,
          position: 'absolute',
          right: '8%',
          bottom: '15%',
          p: 2,
          minWidth: 250,
          borderRadius: '18px',
          background: 'rgba(255, 255, 255, 0.05)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(255, 255, 255, 0.12)',
          boxShadow: '0 16px 40px rgba(0, 0, 0, 0.35)',
          animation: 'floatBadge 9s ease-in-out infinite 0.5s',
          zIndex: 1,
        }}
      >
        <Stack direction="row" alignItems="center" spacing={1.5}>
          <Box
            sx={{
              p: 1.1,
              borderRadius: '12px',
              background: 'linear-gradient(135deg, #41CF7C, #28a745)',
              display: 'flex',
              boxShadow: '0 4px 14px rgba(65, 207, 124, 0.3)',
            }}
          >
            <SecurityOutlinedIcon sx={{ fontSize: 22, color: '#fff' }} />
          </Box>
          <Box>
            <Typography variant="body2" sx={{ fontWeight: 800, color: '#ffffff', lineHeight: 1.2 }}>
              Sécurité Entreprise
            </Typography>
            <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.65)' }}>
              JWT Bearer & Chiffrement
            </Typography>
          </Box>
        </Stack>
        <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.55)', fontSize: '0.75rem', pl: 0.5 }}>
          Spring Boot 3.5 • PostgreSQL • Actuator
        </Typography>
      </Box>

      {/* ── Style keyframes injectés ── */}
      <style>
        {`
          @keyframes floatOrb1 {
            0% { transform: translate(0px, 0px) scale(1); }
            50% { transform: translate(50px, 40px) scale(1.1); }
            100% { transform: translate(-30px, 80px) scale(0.95); }
          }
          @keyframes floatOrb2 {
            0% { transform: translate(0px, 0px) scale(1); }
            50% { transform: translate(-60px, -40px) scale(1.15); }
            100% { transform: translate(40px, -70px) scale(0.9); }
          }
          @keyframes floatOrb3 {
            0% { transform: translate(0px, 0px) scale(0.9); }
            50% { transform: translate(30px, -50px) scale(1.2); }
            100% { transform: translate(-40px, 30px) scale(1); }
          }
          @keyframes floatBadge {
            0%, 100% { transform: translateY(0px); }
            50% { transform: translateY(-12px); }
          }
          @keyframes pulseDot {
            0%, 100% { opacity: 1; transform: scale(1); }
            50% { opacity: 0.4; transform: scale(0.85); }
          }
          @keyframes cardGlow {
            0%, 100% { box-shadow: 0 20px 60px rgba(0, 0, 0, 0.45), 0 0 40px rgba(255, 6, 126, 0.12); }
            50% { box-shadow: 0 25px 70px rgba(0, 0, 0, 0.5), 0 0 60px rgba(72, 144, 255, 0.18); }
          }
        `}
      </style>

      {/* ── Carte de connexion Glassmorphic ── */}
      <Paper
        elevation={0}
        sx={{
          p: { xs: 4, sm: 5.5 },
          width: '100%',
          maxWidth: 460,
          borderRadius: 4,
          background: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(24px)',
          border: '1px solid rgba(255, 255, 255, 0.8)',
          animation: 'cardGlow 8s ease-in-out infinite',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          position: 'relative',
          zIndex: 2,
        }}
      >
        {/* Badge supérieur */}
        <Chip
          icon={<BoltOutlinedIcon sx={{ fontSize: 16, color: '#FF067E !important' }} />}
          label="PORTAIL SUPPORT IT INTERNE"
          size="small"
          sx={{
            mb: 2.5,
            fontWeight: 800,
            fontSize: '0.72rem',
            letterSpacing: '0.08em',
            bgcolor: 'rgba(255, 6, 126, 0.08)',
            color: '#FF067E',
            border: '1px solid rgba(255, 6, 126, 0.2)',
            px: 0.5,
          }}
        />

        {/* Logo */}
        <Box
          component="img"
          src="/src/assets/MiniDesk.png"
          alt="MiniDesk Logo"
          sx={{
            height: 68,
            mb: 2.5,
            filter: 'drop-shadow(0 4px 12px rgba(48, 49, 103, 0.15))',
            transition: 'transform 0.3s ease',
            '&:hover': {
              transform: 'scale(1.04)',
            },
          }}
        />

        <Typography variant="h4" sx={{ fontWeight: 900, mb: 0.5, textAlign: 'center', color: '#1a1a3a', letterSpacing: '-0.02em' }}>
          Bienvenue
        </Typography>
        <Typography variant="body2" sx={{ mb: 3.5, textAlign: 'center', color: '#6b6b8a', fontSize: '0.95rem' }}>
          Connectez-vous pour accéder à votre espace d'assistance
        </Typography>

        {error && (
          <Alert severity="error" sx={{ width: '100%', mb: 3, borderRadius: 2, fontSize: '0.9rem' }}>
            {error}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
          {/* Champ Email */}
          <TextField
            margin="normal"
            required
            fullWidth
            id="email"
            label="Adresse Email"
            name="email"
            autoComplete="email"
            autoFocus
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            sx={{
              mb: 2.2,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f7f8fc',
                borderRadius: 2,
                transition: 'all 0.2s ease',
                '&:hover': {
                  bgcolor: '#f1f3fa',
                },
                '&.Mui-focused': {
                  bgcolor: '#ffffff',
                },
              },
              '& input:-webkit-autofill': {
                WebkitBoxShadow: '0 0 0 1000px #f7f8fc inset',
                WebkitTextFillColor: '#1a1a3a',
                borderRadius: '0px',
              },
              '& input:-webkit-autofill:hover, & input:-webkit-autofill:focus': {
                WebkitBoxShadow: '0 0 0 1000px #ffffff inset',
                WebkitTextFillColor: '#1a1a3a',
              },
            }}
          />

          {/* Champ Mot de passe */}
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="Mot de passe"
            type={showPassword ? 'text' : 'password'}
            id="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            sx={{
              mb: 3.5,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f7f8fc',
                borderRadius: 2,
                transition: 'all 0.2s ease',
                '&:hover': {
                  bgcolor: '#f1f3fa',
                },
                '&.Mui-focused': {
                  bgcolor: '#ffffff',
                },
              },
              '& input:-webkit-autofill': {
                WebkitBoxShadow: '0 0 0 1000px #f7f8fc inset',
                WebkitTextFillColor: '#1a1a3a',
                borderRadius: '0px',
              },
              '& input:-webkit-autofill:hover, & input:-webkit-autofill:focus': {
                WebkitBoxShadow: '0 0 0 1000px #ffffff inset',
                WebkitTextFillColor: '#1a1a3a',
              },
            }}
            slotProps={{
              input: {
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label={showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
                      onClick={handleTogglePassword}
                      edge="end"
                      tabIndex={-1}
                      sx={{
                        color: '#6b6b8a',
                        transition: 'color 0.2s ease',
                        '&:hover': { color: '#FF067E' },
                      }}
                    >
                      {showPassword ? (
                        <VisibilityOffIcon sx={{ fontSize: 20 }} />
                      ) : (
                        <VisibilityIcon sx={{ fontSize: 20 }} />
                      )}
                    </IconButton>
                  </InputAdornment>
                ),
              },
            }}
          />

          {/* Bouton de Connexion */}
          <Button
            type="submit"
            fullWidth
            variant="contained"
            size="large"
            disabled={loading}
            sx={{
              py: 1.6,
              fontSize: '1.05rem',
              fontWeight: 800,
              borderRadius: 2,
              background: 'linear-gradient(135deg, #FF067E 0%, #d4005f 100%)',
              boxShadow: '0 8px 25px rgba(255, 6, 126, 0.35)',
              letterSpacing: '0.03em',
              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              '&:hover': {
                background: 'linear-gradient(135deg, #ff2390 0%, #b8004f 100%)',
                boxShadow: '0 12px 30px rgba(255, 6, 126, 0.5)',
                transform: 'translateY(-2px)',
              },
              '&:active': {
                transform: 'translateY(0px)',
              },
            }}
          >
            {loading ? <CircularProgress size={24} color="inherit" /> : 'Se Connecter'}
          </Button>

          {/* Footer sécurisé sous le bouton */}
          <Stack direction="row" alignItems="center" justifyContent="center" spacing={1} sx={{ mt: 3, opacity: 0.75 }}>
            <SecurityOutlinedIcon sx={{ fontSize: 15, color: '#6b6b8a' }} />
            <Typography variant="caption" sx={{ color: '#6b6b8a', fontWeight: 600 }}>
              Connexion chiffrée SSL • Accès restreint
            </Typography>
          </Stack>
        </Box>
      </Paper>
    </Box>
  );
};

export default Login;
