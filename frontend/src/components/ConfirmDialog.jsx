import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material'

export default function ConfirmDialog({ open, title, text, confirmLabel = 'Confirmar', color = 'primary', onConfirm, onClose, busy }) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle sx={{ fontWeight: 700 }}>{title}</DialogTitle>
      <DialogContent><DialogContentText>{text}</DialogContentText></DialogContent>
      <DialogActions sx={{ p: 2 }}>
        <Button onClick={onClose} color="inherit">Cancelar</Button>
        <Button onClick={onConfirm} variant="contained" color={color} disabled={busy}>{confirmLabel}</Button>
      </DialogActions>
    </Dialog>
  )
}
