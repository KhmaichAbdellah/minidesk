import { Box } from '@mui/material';
import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import Sidebar from './Sidebar';
import ChatWidget from './ChatWidget';

// ChatWidget inclus dans le layout pour afficher l'icône de chat flottante sur chaque page.

const Layout = () => {
  return (
    <Box className="app-shell">
      {/* Sidebar de navigation à gauche */}
      <Sidebar />
      
      {/* Conteneur principal (Navbar en haut + contenu en dessous) */}
      <Box className="app-main">
        <Navbar />
        <Box className="app-content">
          <Outlet />
        </Box>
        <ChatWidget />
      </Box>
    </Box>
  );
};

export default Layout;
