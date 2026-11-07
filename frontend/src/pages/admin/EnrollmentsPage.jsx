import { useState } from 'react'
import {
  Box, Button, Card, IconButton, MenuItem, Tab, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Tabs, TextField, Tooltip, Typography,
} from '@mui/material'
import PersonAddAlt1Rounded from '@mui/icons-material/PersonAddAlt1Rounded'
import DeleteOutlineRounded from '@mui/icons-material/DeleteOutlineRounded'
import api, { errorMessage } from '../../api/client'
import useApi from '../../hooks/useApi'
import { useNotify } from '../../context/NotificationContext'
import PageHeader from '../../components/PageHeader'
import LoadState from '../../components/LoadState'
import PeriodSelect from '../../components/PeriodSelect'
import StatusChip from '../../components/StatusChip'

function formatDate(value) {
  return value ? new Date(value).toLocaleString('es-CL', { dateStyle: 'short', timeStyle: 'short' }) : '—'
}

function Matriculations({ period }) {
  const notify = useNotify()
  const matriculations = useApi(`/periods/${period.id}/matriculations`)
  const students = useApi('/students')
  const [studentId, setStudentId] = useState('')

  const matriculatedIds = new Set((matriculations.data ?? []).map((m) => m.studentId))
  const candidates = (students.data ?? []).filter((s) => !matriculatedIds.has(s.id))

  const add = async () => {
    try {
      await api.post(`/periods/${period.id}/matriculations`, { studentId })
      notify('Matrícula registrada')
      setStudentId('')
      matriculations.reload()
    } catch (err) {
      notify(errorMessage(err), 'error')
    }
  }

  const remove = async (m) => {
    try {
      await api.delete(`/matriculations/${m.id}`)
      notify('Matrícula eliminada')
      matriculations.reload()
    } catch (err) {
      notify(errorMessage(err), 'error')
    }
  }

  return (
    <>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        La matrícula se gestiona fuera de SIGA IPT. Aquí se registra qué estudiantes están matriculados, porque es requisito para inscribir asignaturas.
      </Typography>
      {period.status === 'OPEN' && (
        <Box sx={{ display: 'flex', gap: 1.5, mb: 2, flexWrap: 'wrap' }}>
          <TextField select size="small" label="Estudiante" value={studentId} onChange={(e) => setStudentId(e.target.value)} sx={{ minWidth: 300, bgcolor: '#fff' }}>
            {candidates.map((s) => <MenuItem key={s.id} value={s.id}>{s.fullName} · {s.run}</MenuItem>)}
          </TextField>
          <Button variant="contained" startIcon={<PersonAddAlt1Rounded />} disabled={!studentId} onClick={add}>Matricular</Button>
        </Box>
      )}
      <LoadState loading={matriculations.loading} error={matriculations.error}>
        <Card>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Estudiante</TableCell>
                  <TableCell>RUN</TableCell>
                  <TableCell>Registrada</TableCell>
                  <TableCell align="right" />
                </TableRow>
              </TableHead>
              <TableBody>
                {(matriculations.data ?? []).map((m) => (
                  <TableRow key={m.id} hover>
                    <TableCell><Typography variant="subtitle2">{m.studentName}</Typography></TableCell>
                    <TableCell>{m.studentRun}</TableCell>
                    <TableCell>{m.registeredAt}</TableCell>
                    <TableCell align="right">
                      {period.status === 'OPEN' && (
                        <Tooltip title="Eliminar matrícula">
                          <IconButton size="small" onClick={() => remove(m)}><DeleteOutlineRounded fontSize="small" /></IconButton>
                        </Tooltip>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
                {matriculations.data?.length === 0 && (
                  <TableRow><TableCell colSpan={4} sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>Aún no hay estudiantes matriculados en este período.</TableCell></TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      </LoadState>
    </>
  )
}

function Enrollments({ period }) {
  const enrollments = useApi(`/periods/${period.id}/enrollments`)
  const rows = [...(enrollments.data ?? [])].sort((a, b) =>
    a.studentName.localeCompare(b.studentName) || a.subjectCode.localeCompare(b.subjectCode))

  return (
    <LoadState loading={enrollments.loading} error={enrollments.error}>
      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Estudiante</TableCell>
                <TableCell>Asignatura</TableCell>
                <TableCell>Fecha de inscripción</TableCell>
                <TableCell>Nota final</TableCell>
                <TableCell>Resultado</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((e) => (
                <TableRow key={e.id} hover>
                  <TableCell>
                    <Typography variant="subtitle2">{e.studentName}</Typography>
                    <Typography variant="caption" color="text.secondary">{e.studentRun}</Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">{e.subjectName}</Typography>
                    <Typography variant="caption" color="text.secondary">{e.subjectCode}</Typography>
                  </TableCell>
                  <TableCell>{formatDate(e.enrollmentDate)}</TableCell>
                  <TableCell sx={{ fontWeight: 700, fontVariantNumeric: 'tabular-nums' }}>
                    {e.finalGrade != null ? e.finalGrade.toFixed(1).replace('.', ',') : '—'}
                  </TableCell>
                  <TableCell>{e.result ? <StatusChip status={e.result} /> : <Typography variant="caption" color="text.secondary">Pendiente</Typography>}</TableCell>
                </TableRow>
              ))}
              {rows.length === 0 && (
                <TableRow><TableCell colSpan={5} sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>Todavía no hay inscripciones en este período.</TableCell></TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>
    </LoadState>
  )
}

export default function EnrollmentsPage() {
  const periods = useApi('/periods')
  const [periodId, setPeriodId] = useState('')
  const [tab, setTab] = useState(0)
  const period = periods.data?.find((p) => p.id === periodId)

  return (
    <>
      <PageHeader
        title="Inscripciones"
        subtitle="Matrículas del período e inscripciones de asignaturas de los estudiantes."
        action={<PeriodSelect periods={periods.data} value={periodId} onChange={setPeriodId} />}
      />
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }} textColor="primary" indicatorColor="secondary">
        <Tab label="Inscripciones" />
        <Tab label="Matrículas" />
      </Tabs>
      {period && (tab === 0 ? <Enrollments key={period.id} period={period} /> : <Matriculations key={period.id} period={period} />)}
    </>
  )
}
