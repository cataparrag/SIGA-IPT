import { useEffect, useState } from 'react'
import {
  Alert, Box, Button, Card, CardActionArea, CardContent, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import SaveRounded from '@mui/icons-material/SaveRounded'
import api, { errorMessage } from '../../api/client'
import useApi from '../../hooks/useApi'
import { useRole } from '../../context/RoleContext'
import { useNotify } from '../../context/NotificationContext'
import PageHeader from '../../components/PageHeader'
import LoadState from '../../components/LoadState'
import StatusChip from '../../components/StatusChip'
import ScheduleBlocks from '../../components/ScheduleBlocks'

/** Accepts "5,5" or "5.5". */
function parseGrade(text) {
  const value = Number(String(text).replace(',', '.'))
  return Number.isFinite(value) ? value : null
}

function GradeRow({ enrollment, teacherId, editable, onSaved }) {
  const notify = useNotify()
  const initial = enrollment.finalGrade != null ? enrollment.finalGrade.toFixed(1).replace('.', ',') : ''
  const [text, setText] = useState(initial)
  const [saving, setSaving] = useState(false)
  const grade = parseGrade(text)
  const invalid = text !== '' && (grade === null || grade < 1 || grade > 7)
  const changed = text !== initial

  const save = async () => {
    setSaving(true)
    try {
      await api.put(`/enrollments/${enrollment.id}/grade`, { finalGrade: grade })
      notify(`Nota de ${enrollment.studentName} guardada`)
      onSaved()
    } catch (err) {
      notify(errorMessage(err), 'error')
    } finally {
      setSaving(false)
    }
  }

  return (
    <TableRow hover>
      <TableCell>
        <Typography variant="subtitle2">{enrollment.studentName}</Typography>
        <Typography variant="caption" color="text.secondary">{enrollment.studentRun}</Typography>
      </TableCell>
      <TableCell sx={{ width: 150 }}>
        <TextField size="small" value={text} onChange={(e) => setText(e.target.value)} placeholder="1,0 – 7,0"
          disabled={!editable} error={invalid} helperText={invalid ? 'Entre 1,0 y 7,0' : ' '}
          slotProps={{ htmlInput: { inputMode: 'decimal', style: { fontWeight: 700, textAlign: 'center' } } }} />
      </TableCell>
      <TableCell>{enrollment.result ? <StatusChip status={enrollment.result} /> : <Typography variant="caption" color="text.secondary">Sin nota</Typography>}</TableCell>
      <TableCell align="right">
        {editable && (
          <Button size="small" variant={changed ? 'contained' : 'outlined'} startIcon={<SaveRounded />}
            disabled={!changed || invalid || text === '' || saving} onClick={save}>
            Guardar
          </Button>
        )}
      </TableCell>
    </TableRow>
  )
}

function SectionStudents({ section, teacherId }) {
  const enrollments = useApi(`/sections/${section.id}/enrollments`)
  const periods = useApi('/periods')
  const open = periods.data?.find((p) => p.id === section.periodId)?.status === 'OPEN'
  const rows = [...(enrollments.data ?? [])].sort((a, b) => a.studentName.localeCompare(b.studentName))

  return (
    <LoadState loading={enrollments.loading} error={enrollments.error}>
      {!open && periods.data && (
        <Alert severity="info" sx={{ mb: 2, borderRadius: 3 }}>El período {section.periodCode} está cerrado: las notas ya no se pueden modificar.</Alert>
      )}
      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Estudiante</TableCell>
                <TableCell>Nota final</TableCell>
                <TableCell>Resultado</TableCell>
                <TableCell />
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((e) => (
                <GradeRow key={`${e.id}-${e.finalGrade}`} enrollment={e} teacherId={teacherId} editable={open} onSaved={enrollments.reload} />
              ))}
              {rows.length === 0 && (
                <TableRow><TableCell colSpan={4} sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>Esta sección aún no tiene estudiantes inscritos.</TableCell></TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>
    </LoadState>
  )
}

export default function MySectionsPage() {
  const { user } = useRole()
  const sections = useApi(`/teachers/${user.teacherId}/sections`)
  const [selectedId, setSelectedId] = useState(null)

  useEffect(() => {
    if (sections.data?.length && selectedId === null) setSelectedId(sections.data[0].id)
  }, [sections.data, selectedId])

  const selected = sections.data?.find((s) => s.id === selectedId)

  return (
    <>
      <PageHeader title="Mis secciones" subtitle="Selecciona una sección para ver a sus estudiantes y registrar las notas finales (nota de aprobación: 4,0)." />
      <LoadState loading={sections.loading} error={sections.error}>
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', lg: 'repeat(3, 1fr)' }, mb: 4 }}>
          {(sections.data ?? []).map((s) => (
            <Card key={s.id} sx={{ borderColor: s.id === selectedId ? 'primary.main' : 'divider', boxShadow: s.id === selectedId ? '0 0 0 3px rgba(78, 42, 132, 0.12)' : undefined }}>
              <CardActionArea onClick={() => setSelectedId(s.id)}>
                <CardContent>
                  <Typography variant="caption" sx={{ fontWeight: 700, color: 'secondary.dark' }}>{s.subjectCode} · {s.periodCode}</Typography>
                  <Typography variant="h6" sx={{ fontSize: 16.5, lineHeight: 1.3, mb: 1 }}>{s.subjectName}</Typography>
                  <ScheduleBlocks blocks={s.blocks} />
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>{s.enrolledCount} estudiantes inscritos</Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          ))}
          {sections.data?.length === 0 && <Typography color="text.secondary">No tienes secciones asignadas.</Typography>}
        </Box>
      </LoadState>
      {selected && (
        <>
          <Typography variant="h5" sx={{ mb: 2 }}>{selected.subjectName}</Typography>
          <SectionStudents key={selected.id} section={selected} teacherId={user.teacherId} />
        </>
      )}
    </>
  )
}
