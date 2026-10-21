import { useState, useRef, useEffect } from 'react';
import { Box, Fab, Drawer, IconButton, TextField, Button, List, ListItem, Typography, CircularProgress, Stack } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SmartToyOutlinedIcon from '@mui/icons-material/SmartToyOutlined';
import SendIcon from '@mui/icons-material/Send';
import BoltIcon from '@mui/icons-material/Bolt';
import { chatWithAi, createTicketFromSession } from '../../service/aiService';
import { useAuth } from '../../context/AuthContext';

/**
 * Widget de chat flottant avec l'assistant IA Ollama.
 */
const ChatWidget = ({ ticketId }) => {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState([]);
  const [sessionId, setSessionId] = useState(null);
  const [suggested, setSuggested] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const { user } = useAuth();
  const messagesEndRef = useRef(null);

  const toggle = () => setOpen((s) => !s);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  const send = async () => {
    if (!input.trim() || isLoading) return;

    const userMessageText = input.trim();
    const userMsg = { role: 'user', content: userMessageText };

    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setIsLoading(true);

    try {
      const payload = {
        ticketId: ticketId || null,
        sessionId: sessionId || null,
        message: userMessageText
      };
      
      const res = await chatWithAi(payload);

      if (res.sessionId) {
        setSessionId(res.sessionId);
      }

      const assistantText = res.reply || "Aucune réponse de l'assistant.";
      setMessages((prev) => [...prev, { role: 'assistant', content: assistantText }]);

      if (res.resolvedByAi) {
        setMessages((prev) => [
          ...prev,
          { role: 'system', content: "🎉 Problème résolu avec succès ! Un ticket de confirmation a été automatiquement généré." }
        ]);
        setSuggested(null);
      } else if (res.suggestedCreateTicket) {
        setSuggested({
          title: res.suggestedTicketTitle || "Ticket créé depuis le chat IA",
          description: res.suggestedTicketDescription || assistantText
        });
      } else {
        setSuggested(null);
      }

    } catch (err) {
      console.error("Erreur lors de l'appel IA:", err);
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', content: "⚠️ Désolé, une erreur est survenue lors de la communication avec l'assistant." }
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const confirmCreateTicket = async () => {
    if (!sessionId) return;
    try {
      await createTicketFromSession(sessionId);
      setMessages((prev) => [
        ...prev,
        { role: 'system', content: '✅ Ticket créé et envoyé à l\'équipe support avec succès !' }
      ]);
      setSuggested(null);
    } catch {
      setMessages((prev) => [
        ...prev,
        { role: 'system', content: '❌ Échec de la création du ticket.' }
      ]);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  };

  return (
    <>
      {/* ── Bouton flottant avec lueur animée ── */}
      {!open && (
        <Fab
          aria-label="chat"
          onClick={toggle}
          sx={{
            position: 'fixed',
            right: 28,
            bottom: 28,
            zIndex: 1400,
            width: 62,
            height: 62,
            background: 'linear-gradient(135deg, #FF067E 0%, #d4005f 100%)',
            color: '#ffffff',
            boxShadow: '0 8px 30px rgba(255, 6, 126, 0.45)',
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:hover': {
              background: 'linear-gradient(135deg, #ff2390 0%, #b8004f 100%)',
              boxShadow: '0 12px 35px rgba(255, 6, 126, 0.6)',
              transform: 'scale(1.08)',
            },
          }}
        >
          <SmartToyOutlinedIcon sx={{ fontSize: 30 }} />
        </Fab>
      )}

      {/* ── Panneau latéral (Drawer) du chat ── */}
      <Drawer
        anchor="right"
        open={open}
        onClose={toggle}
        PaperProps={{
          sx: {
            width: { xs: '100%', sm: 420 },
            p: 0,
            display: 'flex',
            flexDirection: 'column',
            background: '#ffffff',
            boxShadow: '0 20px 70px rgba(0, 0, 0, 0.25)',
          },
        }}
      >
        {/* Header du Chat */}
        <Box
          sx={{
            p: 2.5,
            background: 'linear-gradient(135deg, #161733 0%, #252453 50%, #303167 100%)',
            color: '#ffffff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
          }}
        >
          <Stack direction="row" alignItems="center" spacing={1.5}>
            <Box
              sx={{
                p: 1,
                borderRadius: '12px',
                background: 'linear-gradient(135deg, #FF067E 0%, #ff5ea7 100%)',
                color: '#fff',
                display: 'flex',
                boxShadow: '0 4px 12px rgba(255, 6, 126, 0.35)',
              }}
            >
              <SmartToyOutlinedIcon sx={{ fontSize: 22 }} />
            </Box>
            <Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 800, lineHeight: 1.2, color: '#fff' }}>
                Assistant IA MiniDesk
              </Typography>
              <Stack direction="row" alignItems="center" spacing={0.8} sx={{ mt: 0.2 }}>
                <Box sx={{ width: 7, height: 7, borderRadius: '50%', bgcolor: '#41CF7C' }} />
                <Typography variant="caption" sx={{ color: '#41CF7C', fontWeight: 700, fontSize: '0.7rem' }}>
                  Ollama • En ligne
                </Typography>
              </Stack>
            </Box>
          </Stack>
          <IconButton onClick={toggle} sx={{ color: 'rgba(255, 255, 255, 0.8)', '&:hover': { color: '#fff' } }}>
            <CloseIcon />
          </IconButton>
        </Box>

        {/* Liste des messages */}
        <List sx={{ flexGrow: 1, overflow: 'auto', p: 2.5, display: 'flex', flexDirection: 'column', gap: 1.5, bgcolor: '#fbfbff' }}>
          {messages.length === 0 && (
            <Box sx={{ textAlign: 'center', my: 'auto', py: 6, px: 2 }}>
              <Box
                sx={{
                  width: 64,
                  height: 64,
                  mx: 'auto',
                  mb: 2,
                  borderRadius: '20px',
                  bgcolor: 'rgba(255, 6, 126, 0.08)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#FF067E',
                }}
              >
                <BoltIcon sx={{ fontSize: 34 }} />
              </Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#1a1a3a', mb: 0.5 }}>
                Comment puis-je vous aider ?
              </Typography>
              <Typography variant="body2" sx={{ color: '#6b6b8a', fontSize: '0.88rem', maxWidth: 300, mx: 'auto' }}>
                Posez votre question sur un incident matériel, logiciel ou réseau. L'IA tentera de vous dépanner immédiatement.
              </Typography>
            </Box>
          )}

          {messages.map((m, i) => (
            <ListItem key={i} sx={{ display: 'block', px: 0, py: 0.2 }}>
              <Box sx={{ display: 'flex', justifyContent: m.role === 'user' ? 'flex-end' : 'flex-start' }}>
                <Box
                  sx={{
                    maxWidth: '85%',
                    bgcolor: m.role === 'user' ? '#303167' : (m.role === 'system' ? '#f0fdf4' : '#ffffff'),
                    color: m.role === 'user' ? '#ffffff' : (m.role === 'system' ? '#166534' : '#1a1a3a'),
                    px: 2.2,
                    py: 1.4,
                    borderRadius: m.role === 'user' ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
                    boxShadow: '0 2px 10px rgba(0, 0, 0, 0.05)',
                    border: m.role === 'user' ? 'none' : (m.role === 'system' ? '1px solid #bbf7d0' : '1px solid rgba(48, 49, 103, 0.08)'),
                  }}
                >
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word', lineHeight: 1.5, fontSize: '0.9rem' }}>
                    {m.content}
                  </Typography>
                </Box>
              </Box>
            </ListItem>
          ))}

          {isLoading && (
            <ListItem sx={{ display: 'block', px: 0, py: 0.2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2, color: '#6b6b8a', bgcolor: '#ffffff', p: 1.8, borderRadius: '16px', border: '1px solid rgba(48, 49, 103, 0.08)', width: 'fit-content' }}>
                <CircularProgress size={16} sx={{ color: '#FF067E' }} />
                <Typography variant="caption" sx={{ fontWeight: 700 }}>L'assistant rédige sa réponse...</Typography>
              </Box>
            </ListItem>
          )}

          <div ref={messagesEndRef} />
        </List>

        {/* Suggestion Ticket */}
        {suggested && (
          <Box sx={{ p: 2, m: 2, bgcolor: 'rgba(255, 6, 126, 0.05)', borderRadius: '16px', border: '1px solid rgba(255, 6, 126, 0.25)' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#FF067E', mb: 0.5 }}>
              💡 Suggestion de création de ticket
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 700, color: '#1a1a3a', mb: 0.3 }}>{suggested.title}</Typography>
            <Typography variant="caption" sx={{ color: '#6b6b8a', display: 'block', mb: 1.5 }}>{suggested.description}</Typography>
            <Button
              variant="contained"
              size="small"
              onClick={confirmCreateTicket}
              fullWidth
              sx={{
                borderRadius: '10px',
                fontWeight: 800,
                background: 'linear-gradient(135deg, #FF067E 0%, #d4005f 100%)',
              }}
            >
              Confirmer la création du ticket
            </Button>
          </Box>
        )}

        {/* Zone de saisie */}
        <Box sx={{ p: 2, borderTop: '1px solid rgba(48, 49, 103, 0.08)', bgcolor: '#ffffff' }}>
          <Stack direction="row" spacing={1}>
            <TextField
              fullWidth
              size="small"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyPress}
              placeholder={user ? 'Décrivez votre incident IT...' : "Connectez-vous pour chatter"}
              disabled={!user || isLoading}
              sx={{
                '& .MuiOutlinedInput-root': {
                  bgcolor: '#f7f8fc',
                  borderRadius: '14px',
                },
              }}
            />
            <IconButton
              onClick={send}
              disabled={isLoading || !user || !input.trim()}
              sx={{
                background: 'linear-gradient(135deg, #FF067E 0%, #d4005f 100%)',
                color: '#ffffff',
                borderRadius: '14px',
                p: 1.2,
                '&:hover': {
                  background: 'linear-gradient(135deg, #ff2390 0%, #b8004f 100%)',
                },
                '&.Mui-disabled': {
                  bgcolor: '#e2e8f0',
                  color: '#94a3b8',
                },
              }}
            >
              {isLoading ? <CircularProgress size={20} color="inherit" /> : <SendIcon sx={{ fontSize: 18 }} />}
            </IconButton>
          </Stack>
        </Box>
      </Drawer>
    </>
  );
};

export default ChatWidget;
