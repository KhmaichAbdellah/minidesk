
// Composant de notification globale (Snackbar + Alert MUI).
// Utilisé pour afficher les erreurs API interceptées par l'intercepteur Axios.

import { useState, useEffect } from 'react';
import { Snackbar, Alert, AlertTitle } from '@mui/material';
import { subscribeToApiErrors } from '../../service/errorBus';


const GlobalErrorToast = () => {
  const [open, setOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [severity, setSeverity] = useState('error'); // 'error' | 'warning' | 'info'

  useEffect(() => {
    // Abonnement au bus d'erreurs pour recevoir les erreurs API
    const unsubscribe = subscribeToApiErrors((errorMsg, level = 'error') => {
      setErrorMessage(errorMsg);
      setSeverity(level);
      setOpen(true);
    });

    // Nettoyage : se désabonner quand le composant est démonté
    return () => unsubscribe();
  }, []);

  const handleClose = (event, reason) => {
    // Ne pas fermer si l'utilisateur clique en dehors (clickaway)
    if (reason === 'clickaway') return;
    setOpen(false);
  };

  return (
    <Snackbar
      open={open}
      autoHideDuration={6000}      // Fermeture automatique après 6 secondes
      onClose={handleClose}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
    >
      <Alert
        onClose={handleClose}
        severity={severity}
        variant="filled"
        sx={{ width: '100%', minWidth: 320, boxShadow: '0 4px 20px rgba(0,0,0,0.25)' }}
      >
        <AlertTitle sx={{ fontWeight: 700 }}>
          {severity === 'error' ? 'Erreur' : severity === 'warning' ? 'Avertissement' : 'Information'}
        </AlertTitle>
        {errorMessage}
      </Alert>
    </Snackbar>
  );
};

export default GlobalErrorToast;
