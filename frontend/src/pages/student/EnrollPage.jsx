import { useState } from 'react'
import {
  Alert, Box, Button, Card, CardActionArea, CardContent, Checkbox, IconButton, Tooltip, Typography,
} from '@mui/material'
import HowToRegRounded from '@mui/icons-material/HowToRegRounded'
import CloseRounded from '@mui/icons-material/CloseRounded'
import api, { errorMessage } from '../../api/client'
import useApi from '../../hooks/useApi'
import { useRole } from '../../context/RoleContext'
import { useNotify } from '../../context/NotificationContext'
import PageHeader from '../../components/PageHeader'
import LoadState from '../../components/LoadState'
import PeriodSelect from '../../components/PeriodSelect'
import ScheduleBlocks from '../../components/ScheduleBlocks'
import ScheduleGrid from '../../components/ScheduleGrid'
import ConfirmDialog from '../../components/ConfirmDialog'

const MIN = 3
const MAX = 6

function SectionOption({ section, checked, onToggle }) {
  const full = section.availableSeats === 0
  return (
    <Card sx={{
      borderColor: checked ? 'primary.main' : 'divider', opacity: full ? 0.55 : 1,
      boxShadow: checked ? '0 0 0 3px rgba(78, 42, 132, 0.12)' : undefined,
    }}>
      <CardActionArea disabled={full} onClick={onToggle}>
        <CardContent sx={{ display: 'flex', gap: 1.5, alignItems: 'flex-start' }}>
          <Checkbox checked={checked} disabled={full} sx={{ p: 0.25 }} />
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Typography variant="caption" sx={{ fontWeight: 700, color: 'secondary.dark' }}>
              {section.subjectCode} · Semestre {section.subjectSemester}
            </Typography>
            <Typography variant="subtitle2" sx={{ fontSize: 15 }}>{section.subjectName}</Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>{section.teacherName}</Typography>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
              <ScheduleBlocks blocks={section.blocks} />
              <Typography variant="caption" sx={{ fontWeight: 700, color: full ? 'error.main' : 'text.secondary' }}>
                {full ? 'Sin cupos' : `${section.availableSeats} cupos`}
              </Typography>
            </Box>
          </Box>
        </CardContent>
      </CardActionArea>
    </Card>
  )
}

function EnrollmentForPeriod({ studentId, period, student }) {
  const notify = useNotify()
  const available = useApi(`/students/${studentId}/available-sections?periodId=${period.id}`)
  const current = useApi(`/students/${studentId}/enrollments?periodId=${period.id}`)
  const [selected, setSelected] = useState([])
  const [saving, setSaving] = useState(false)
  const [toCancel, setToCancel] = useState(null)

  const reload = () => { available.reload(); current.reload() }
  const toggle = (id) => setSelected((list) => list.includes(id) ? list.filter((x) => x !== id) : [...list, id])

  const chosen = (available.data ?? []).filter((s) => selected.includes(s.id))
  const enrolled = current.data ?? []
  const total = enrolled.length + chosen.length
  const open = period.status === 'OPEN'

  // Block code for each section, to preview the weekly timetable.
  const offerById = Object.fromEntries((available.data ?? []).map((s) => [s.id, s]))
  const allSections = useApi(`/periods/${period.id}/sections`)
  const sectionById = Object.fromEntries((allSections.data ?? []).map((s) => [s.id, s]))
  const gridItems = [
    ...enrolled.map((e) => ({ code: e.subjectCode, blocks: sectionById[e.sectionId]?.blocks ?? [], tone: 'secondary' })),
    ...chosen.map((s) => ({ code: s.subjectCode, blocks: offerById[s.id].blocks, tone: 'primary' })),
  ]
  const hasConflict = (() => {
    const seen = new Set()
    for (const item of gridItems) for (const b of item.blocks) { if (seen.has(b)) return true; seen.add(b) }
    return false
  })()

  const submit = async () => {
    setSaving(true)
    try {
      const response = await api.post('/enrollments', { periodId: period.id, sectionIds: selected })
      notify(`Inscribiste ${response.data.length} asignaturas`)
      setSelected([])
      reload()
    } catch (err) {
      notify(errorMessage(err), 'error')
    } finally {
      setSaving(false)
    }
  }

  const cancel = async () => {
    try {
      await api.delete(`/enrollments/${toCancel.id}`)
      notify(`Inscripción de ${toCancel.subjectCode} anulada`)
      setToCancel(null)
      reload()
    } catch (err) {
      notify(errorMessage(err), 'error')
      setToCancel(null)
    }
  }

  return (
    <LoadState loading={available.loading || current.loading} error={available.error || current.error}>
      {student.status !== 'REGULAR' && (
        <Alert severity="warning" sx={{ mb: 2, borderRadius: 3 }}>Solo los estudiantes en situación regular pueden inscribir asignaturas.</Alert>
      )}
      <Box sx={{ display: 'grid', gap: 3, gridTemplateColumns: { xs: '1fr', lg: '1.3fr 1fr' }, alignItems: 'start' }}>
        <Box>
          <Typography variant="h6" sx={{ mb: 1.5 }}>Asignaturas disponibles</Typography>
          <Box sx={{ display: 'grid', gap: 1.5 }}>
            {(available.data ?? []).map((s) => (
              <SectionOption key={s.id} section={s} checked={selected.includes(s.id)} onToggle={() => toggle(s.id)} />
            ))}
            {available.data?.length === 0 && (
              <Card><CardContent sx={{ color: 'text.secondary' }}>
                No hay asignaturas disponibles para ti en este período: ya las inscribiste, ya las aprobaste o te falta algún prerrequisito.
              </CardContent></Card>
            )}
          </Box>
        </Box>

        <Box sx={{ position: { lg: 'sticky' }, top: { lg: 88 }, display: 'grid', gap: 2 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', mb: 1 }}>
                <Typography variant="subtitle2">Tu carga del período</Typography>
                <Typography variant="h5" sx={{ fontVariantNumeric: 'tabular-nums', color: total > MAX ? 'error.main' : 'primary.main' }}>
                  {total}<Typography component="span" color="text.secondary"> / {MAX}</Typography>
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', gap: 0.5, mb: 1 }}>
                {Array.from({ length: MAX }).map((_, i) => (
                  <Box key={i} sx={{
                    flex: 1, height: 8, borderRadius: 4,
                    bgcolor: i < enrolled.length ? 'secondary.main' : i < total ? 'primary.main' : 'rgba(78, 42, 132, 0.1)',
                  }} />
                ))}
              </Box>
              <Typography variant="caption" color="text.secondary">
                Debes tener entre {MIN} y {MAX} asignaturas en el período. Naranjo: ya inscritas · Morado: seleccionadas.
              </Typography>
              <Box sx={{ mt: 2 }}>
                <ScheduleGrid items={gridItems} />
              </Box>
              {hasConflict && <Alert severity="error" sx={{ mt: 2, borderRadius: 2 }}>Hay un choque de horario en tu selección.</Alert>}
              <Button fullWidth size="large" variant="contained" color="secondary" startIcon={<HowToRegRounded />} sx={{ mt: 2 }}
                disabled={!open || selected.length === 0 || saving || hasConflict || total > MAX} onClick={submit}>
                Inscribir {selected.length || ''} {selected.length === 1 ? 'asignatura' : 'asignaturas'}
              </Button>
            </CardContent>
          </Card>

          <Card>
            <CardContent>
              <Typography variant="subtitle2" sx={{ mb: 1 }}>Inscritas en {period.code}</Typography>
              {enrolled.length === 0 && <Typography variant="body2" color="text.secondary">Aún no tienes asignaturas inscritas.</Typography>}
              {enrolled.map((e) => (
                <Box key={e.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, py: 0.75, borderBottom: '1px solid', borderColor: 'divider', '&:last-child': { border: 0 } }}>
                  <Box sx={{ flex: 1, minWidth: 0 }}>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>{e.subjectName}</Typography>
                    <Typography variant="caption" color="text.secondary">{e.subjectCode}</Typography>
                  </Box>
                  {open && e.finalGrade == null && (
                    <Tooltip title="Anular inscripción">
                      <IconButton size="small" onClick={() => setToCancel(e)}><CloseRounded fontSize="small" /></IconButton>
                    </Tooltip>
                  )}
                </Box>
              ))}
            </CardContent>
          </Card>
        </Box>
      </Box>

      <ConfirmDialog open={Boolean(toCancel)} title="¿Anular inscripción?" color="error" confirmLabel="Anular"
        text={toCancel ? `Se liberará tu cupo en ${toCancel.subjectName}.` : ''}
        onConfirm={cancel} onClose={() => setToCancel(null)} />
    </LoadState>
  )
}

export default function EnrollPage() {
  const { user } = useRole()
  const student = useApi(`/students/${user.studentId}`)
  const periods = useApi('/periods')
  const [periodId, setPeriodId] = useState('')
  const period = periods.data?.find((p) => p.id === periodId)

  return (
    <>
      <PageHeader title="Inscripción de asignaturas"
        subtitle="Elige las secciones que quieres cursar. El sistema revisa prerrequisitos, cupos y choques de horario."
        action={<PeriodSelect periods={periods.data} value={periodId} onChange={setPeriodId} onlyOpen />} />
      {periods.data && !periods.data.some((p) => p.status === 'OPEN') && (
        <Alert severity="info" sx={{ borderRadius: 3 }}>No hay un período abierto para inscripciones.</Alert>
      )}
      {period && student.data && <EnrollmentForPeriod key={period.id} studentId={user.studentId} period={period} student={student.data} />}
    </>
  )
}
