import { useState } from 'react'
import {
  Box, Button, Card, Dialog, DialogActions, DialogContent, DialogTitle, InputAdornment, MenuItem,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import AddRounded from '@mui/icons-material/AddRounded'
import SearchRounded from '@mui/icons-material/SearchRounded'
import api, { errorMessage } from '../../api/client'
import useApi from '../../hooks/useApi'
import { useNotify } from '../../context/NotificationContext'
import PageHeader from '../../components/PageHeader'
import LoadState from '../../components/LoadState'
import StatusChip from '../../components/StatusChip'

// Statuses the administrator can assign; GRADUATED and ELIMINATED are set by the period closing.
const MANUAL_STATUSES = [
  ['REGULAR', 'Regular'],
  ['POSTPONED', 'Postergación'],
  ['TEMPORARY_WITHDRAWAL', 'Retiro temporal'],
]

const EMPTY = { run: '', fullName: '', email: '', careerId: '' }

function NewStudentDialog({ open, onClose, onCreated, careers }) {
  const notify = useNotify()
  const [form, setForm] = useState(EMPTY)
  const [saving, setSaving] = useState(false)
  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  const submit = async () => {
    setSaving(true)
    try {
      await api.post('/students', form)
      notify('Estudiante registrado')
      setForm(EMPTY)
      onCreated()
    } catch (err) {
      notify(errorMessage(err), 'error')
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ fontWeight: 700 }}>Nuevo estudiante</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        <TextField label="RUN" placeholder="12345678-9" value={form.run} onChange={set('run')} />
        <TextField label="Nombre completo" value={form.fullName} onChange={set('fullName')} />
        <TextField label="Correo electrónico" type="email" value={form.email} onChange={set('email')} />
        <TextField select label="Carrera" value={form.careerId} onChange={set('careerId')}
          helperText="Se asignará automáticamente el plan de estudios vigente de la carrera.">
          {(careers ?? []).filter((c) => c.status === 'ACTIVE').map((c) => (
            <MenuItem key={c.id} value={c.id}>{c.code} · {c.name}</MenuItem>
          ))}
        </TextField>
      </DialogContent>
      <DialogActions sx={{ p: 2 }}>
        <Button onClick={onClose} color="inherit">Cancelar</Button>
        <Button variant="contained" onClick={submit} disabled={saving}>Registrar</Button>
      </DialogActions>
    </Dialog>
  )
}

export default function StudentsPage() {
  const notify = useNotify()
  const students = useApi('/students')
  const careers = useApi('/careers')
  const [dialogOpen, setDialogOpen] = useState(false)
  const [query, setQuery] = useState('')

  const changeStatus = async (student, status) => {
    try {
      await api.put(`/students/${student.id}/status`, { status })
      notify(`Estado de ${student.fullName} actualizado`)
      students.reload()
    } catch (err) {
      notify(errorMessage(err), 'error')
    }
  }

  const q = query.trim().toLowerCase()
  const rows = (students.data ?? []).filter((s) =>
    !q || s.fullName.toLowerCase().includes(q) || s.run.includes(q) || s.email.toLowerCase().includes(q))

  return (
    <>
      <PageHeader
        title="Estudiantes"
        subtitle="Registro de estudiantes, su carrera, plan de estudios y situación académica."
        action={<Button variant="contained" startIcon={<AddRounded />} onClick={() => setDialogOpen(true)}>Nuevo estudiante</Button>}
      />

      <TextField size="small" placeholder="Buscar por nombre, RUN o correo" value={query} onChange={(e) => setQuery(e.target.value)}
        sx={{ mb: 2, width: { xs: '100%', sm: 360 }, bgcolor: '#fff' }}
        slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchRounded fontSize="small" /></InputAdornment> } }} />

      <LoadState loading={students.loading} error={students.error}>
        <Card>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Estudiante</TableCell>
                  <TableCell>RUN</TableCell>
                  <TableCell>Carrera</TableCell>
                  <TableCell>Plan</TableCell>
                  <TableCell>Situación</TableCell>
                  <TableCell>Cambiar estado</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.map((s) => {
                  const manual = MANUAL_STATUSES.some(([value]) => value === s.status)
                  return (
                    <TableRow key={s.id} hover>
                      <TableCell>
                        <Typography variant="subtitle2">{s.fullName}</Typography>
                        <Typography variant="caption" color="text.secondary">{s.email}</Typography>
                      </TableCell>
                      <TableCell sx={{ fontVariantNumeric: 'tabular-nums' }}>{s.run}</TableCell>
                      <TableCell>{s.careerCode}</TableCell>
                      <TableCell>{s.studyPlanIdentifier}</TableCell>
                      <TableCell><StatusChip status={s.status} /></TableCell>
                      <TableCell>
                        {manual ? (
                          <TextField select size="small" value={s.status} onChange={(e) => changeStatus(s, e.target.value)} sx={{ minWidth: 170 }}>
                            {MANUAL_STATUSES.map(([value, label]) => <MenuItem key={value} value={value}>{label}</MenuItem>)}
                          </TextField>
                        ) : (
                          <Typography variant="caption" color="text.secondary">Asignado por el cierre de período</Typography>
                        )}
                      </TableCell>
                    </TableRow>
                  )
                })}
                {rows.length === 0 && (
                  <TableRow><TableCell colSpan={6} sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>No hay estudiantes que coincidan.</TableCell></TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      </LoadState>

      <Box>
        <NewStudentDialog open={dialogOpen} careers={careers.data} onClose={() => setDialogOpen(false)}
          onCreated={() => { setDialogOpen(false); students.reload() }} />
      </Box>
    </>
  )
}
