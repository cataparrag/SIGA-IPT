import { createContext, useCallback, useContext, useState } from 'react'
import { Alert, Snackbar } from '@mui/material'

const NotificationContext = createContext(() => {})

/** Shows short success/error messages at the bottom of the screen. */
export function NotificationProvider({ children }) {
  const [message, setMessage] = useState(null)

  const notify = useCallback((text, severity = 'success') => {
    setMessage({ text, severity, key: Date.now() })
  }, [])

  return (
    <NotificationContext.Provider value={notify}>
      {children}
      <Snackbar
        key={message?.key}
        open={Boolean(message)}
        autoHideDuration={message?.severity === 'error' ? 7000 : 3500}
        onClose={() => setMessage(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        {message ? (
          <Alert severity={message.severity} variant="filled" onClose={() => setMessage(null)} sx={{ borderRadius: 3, maxWidth: 560 }}>
            {message.text}
          </Alert>
        ) : undefined}
      </Snackbar>
    </NotificationContext.Provider>
  )
}

export function useNotify() {
  return useContext(NotificationContext)
}
