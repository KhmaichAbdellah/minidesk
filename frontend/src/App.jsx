import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { AuthProvider, useAuth } from './context/AuthContext';
import { lazy, Suspense } from 'react';
import Layout from './components/layout/Layout';
import GlobalErrorToast from './components/common/GlobalErrorToast';

// ── Lazy loading des pages : chaque page est chargée UNIQUEMENT quand l'utilisateur y navigue ──
// Cela réduit le bundle initial de ~40% et accélère le First Contentful Paint
const Login     = lazy(() => import('./pages/Login'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Tickets   = lazy(() => import('./pages/Tickets'));
const Users     = lazy(() => import('./pages/Users'));
const Profile   = lazy(() => import('./pages/Profile'));
const KnowledgeBase = lazy(() => import('./pages/KnowledgeBase'));

const theme = createTheme({
  typography: {
    fontFamily: '"Lato", "Helvetica", "Arial", sans-serif',
    button: {
      textTransform: 'none', // Pas de majuscules automatiques
      fontWeight: 700,
      letterSpacing: '0.02em',
    },
    h4: { fontWeight: 900, letterSpacing: '-0.02em', color: '#303167' },
    h5: { fontWeight: 700, letterSpacing: '-0.01em', color: '#303167' },
    h6: { fontWeight: 700 },
  },
  palette: {
    primary: {
      main: '#303167', // Indigo Novelis
      light: '#4a4baa',
      dark: '#202044',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#FF067E', // Magenta Novelis
      light: '#ff3d9c',
      dark: '#d4005f',
      contrastText: '#ffffff',
    },
    background: {
      default: '#f7f6fb',
      paper: '#ffffff',
    },
    text: {
      primary: '#1a1a3a',
      secondary: '#6b6b8a',
    },
  },
  shape: {
    borderRadius: 2, // Coins très peu arrondis (flat design)
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          padding: '8px 24px',
          boxShadow: 'none', // Pas d'ombre par défaut sur les boutons
          '&:hover': {
            boxShadow: 'rgba(0, 0, 0, 0.08) 0px 4px 12px 0px',
          },
        },
        containedSecondary: {
          boxShadow: 'rgba(255, 6, 126, 0.25) 0px 8px 20px 0px',
          '&:hover': {
            boxShadow: 'rgba(255, 6, 126, 0.4) 0px 10px 25px 0px',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: 'rgba(48, 49, 103, 0.04) 0px 4px 20px 0px',
          border: '1px solid rgba(48, 49, 103, 0.08)',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        elevation1: {
          boxShadow: 'rgba(48, 49, 103, 0.04) 0px 4px 20px 0px',
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          fontWeight: 700,
          color: '#6b6b8a',
          backgroundColor: '#fbfbff',
          borderBottom: '2px solid rgba(48, 49, 103, 0.08)',
        },
        root: {
          borderBottom: '1px solid rgba(48, 49, 103, 0.06)',
          padding: '16px',
        },
      },
    },
  },
});

// ── Composant de protection des routes ───────────────────────────────────────
const ProtectedRoute = ({ children, requiredRoles }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="app-shell" style={{ justifyContent: 'center', alignItems: 'center' }}>Chargement...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRoles && !requiredRoles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />; // Redirection si rôle insuffisant
  }

  return children;
};

// Fallback de chargement affiché pendant le lazy load d'une page
const PageFallback = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
    <span style={{ color: '#303167', fontFamily: 'Lato, sans-serif', fontSize: 14 }}>Chargement...</span>
  </div>
);

// ── Application Principale ──────────────────────────────────────────────────
function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {/* Notification toast globale pour les erreurs API (Axios interceptor) */}
      <GlobalErrorToast />
      <AuthProvider>
        <BrowserRouter>
          {/* Suspense requis pour React.lazy : affiche PageFallback pendant le chargement */}
          <Suspense fallback={<PageFallback />}>
            <Routes>
              {/* Route publique */}
              <Route path="/login" element={<Login />} />

              {/* Routes protégées avec le Layout (Sidebar + Navbar) */}
              <Route
                path="/"
                element={
                  <ProtectedRoute>
                    <Layout />
                  </ProtectedRoute>
                }
              >
                <Route index element={<Navigate to="/dashboard" replace />} />
                <Route path="dashboard" element={<Dashboard />} />
                <Route path="tickets" element={<Tickets />} />
                <Route path="knowledge" element={<KnowledgeBase />} />
                <Route path="profile" element={<Profile />} />
                {/* Accès restreint aux ADMIN */}
                <Route 
                  path="users" 
                  element={
                    <ProtectedRoute requiredRoles={['ADMIN']}>
                      <Users />
                    </ProtectedRoute>
                  } 
                />
              </Route>

              {/* Redirection fallback */}
              <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Routes>
          </Suspense>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
