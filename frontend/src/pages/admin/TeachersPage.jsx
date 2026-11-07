import { useState } from 'react'
import {
  Button, Card, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, Switch,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import AddRounded from '@mui/icons-material/AddRounded'
import api, { errorMessage } from '../../api/client'
import useApi from '../../hooks/useApi'
import { useNotify } from '../../context/NotificationContext'
import PageHeader from '../../components/PageHeader'
import LoadState from '../../components/LoadState'
import StatusChip from '../../components/StatusChip'

const EMPTY = { run: '', fullName: '', email: '', professionalTitle: '', academicDegree: '' }

function NewTeacherDialog({ open, onClose, onCreated }) {
  const notify = useNotify()
  const [form, setForm] = useState(EMPTY)
  const [saving, setSaving] = useState(false)
  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  const submit = async () => {
    setSaving(true)
    try {
      await api.post('/teachers', form)
      notify('Docente registrado')
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
      <DialogTitle sx={{ fontWeight: 700 }}>Nuevo docente</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        <TextField label="RUN" placeholder="12345678-9" value={form.run} onChange={set('run')} />
        <TextField label="Nombre completo" value={form.fullName} onChange={set('fullName')} />
        <TextField label="Correo electrónico" type="email" value={form.email} onChange={set('email')} />
        <TextField label="Título profesional" value={form.professionalTitle} onChange={set('professionalTitle')} />
        <TextField label="Grado académico" placeholder="Licenciado, Magíster, Doctor…" value={form.academicDegree} onChange={set('academicDegree')} />
      </DialogContent>
      <DialogActions sx={{ p: 2 }}>
        <Button onClick={onClose} color="inherit">Cancelar</Button>
        <Button variant="contained" onClick={submit} disabled={saving}>Registrar</Button>
      </DialogActions>
    </Dialog>
  )
}

export default function TeachersPage() {
  const notify = useNotify()
  const teachers = useApi('/teachers')
  const [dialogOpen, setDialogOpen] = useState(false)

  const toggleStatus = async (teacher) => {
    const status = teacher.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    try {
      await api.put(`/teachers/${teacher.id}/status`, { status })
      notify(`${teacher.fullName} ahora está ${status === 'ACTIVE' ? 'activo' : 'inactivo'}`)
      teachers.reload()
    } catch (err) {
      notify(errorMessage(err), 'error')
    }
  }

  return (
    <>
      <PageHeader
        title="Docentes"
        subtitle="Docentes que pueden asignarse a las secciones de cada período."
        action={<Button variant="contained" startIcon={<AddRounded />} onClick={() => setDialogOpen(true)}>Nuevo docente</Button>}
      />
      <LoadState loading={teachers.loading} error={teachers.error}>
        <Card>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Docente</TableCell>
                  <TableCell>RUN</TableCell>
                  <TableCell>Título</TableCell>
                  <TableCell>Grado</TableCell>
                  <TableCell>Estado</TableCell>
                  <TableCell>Activo</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(teachers.data ?? []).map((t) => (
                  <TableRow key={t.id} hover>
                    <TableCell>
                      <Typography variant="subtitle2">{t.fullName}</Typography>
                      <Typography variant="caption" color="text.secondary">{t.email}</Typography>
                    </TableCell>
                    <TableCell sx={{ fontVariantNumeric: 'tabular-nums' }}>{t.run}</TableCell>
                    <TableCell>{t.professionalTitle}</TableCell>
                    <TableCell>{t.academicDegree}</TableCell>
                    <TableCell><StatusChip status={t.status} label={t.status === 'ACTIVE' ? 'Activo' : 'Inactivo'} /></TableCell>
                    <TableCell>
                      <FormControlLabel label="" control={<Switch checked={t.status === 'ACTIVE'} onChange={() => toggleStatus(t)} />} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      </LoadState>
      <NewTeacherDialog open={dialogOpen} onClose={() => setDialogOpen(false)}
        onCreated={() => { setDialogOpen(false); teachers.reload() }} />
    </>
  )
}
