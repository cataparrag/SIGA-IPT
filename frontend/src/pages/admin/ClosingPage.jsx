import { useMemo, useState } from 'react'
import { Alert, Box, Button, Card, CardContent, Chip, Typography } from '@mui/material'
import LockRounded from '@mui/icons-material/LockRounded'
import CheckCircleRounded from '@mui/icons-material/CheckCircleRounded'
import PendingActionsRounded from '@mui/icons-material/PendingActionsRounded'
import api, { errorMessage } from '../../api/client'
import useApi from '../../hooks/useApi'
import { useNotify } from '../../context/NotificationContext'
import PageHeader from '../../components/PageHeader'
import LoadState from '../../components/LoadState'
import PeriodSelect from '../../components/PeriodSelect'
import StatusChip from '../../components/StatusChip'
import ConfirmDialog from '../../components/ConfirmDialog'

function Summary({ label, value, tone = 'primary' }) {
  return (
    <Card>
      <CardContent>
        <Typography variant="body2" color="text.secondary">{label}</Typography>
        <Typography variant="h4" sx={{ color: `${tone}.main`, fontVariantNumeric: 'tabular-nums' }}>{value}</Typography>
      </CardContent>
    </Card>
  )
}

export default function ClosingPage() {
  const notify = useNotify()
  const periods = useApi('/periods')
  const [periodId, setPeriodId] = useState('')
  const period = periods.data?.find((p) => p.id === periodId)
  const enrollments = useApi(periodId ? `/periods/${periodId}/enrollments` : null)
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [closing, setClosing] = useState(false)
  const [result, setResult] = useState(null)

  const pendingBySection = useMemo(() => {
    const map = {}
    for (const e of enrollments.data ?? []) {
      if (e.finalGrade == null) map[e.subjectCode] = (map[e.subjectCode] ?? 0) + 1
    }
    return Object.entries(map).sort(([a], [b]) => a.localeCompare(b))
  }, [enrollments.data])

  const total = enrollments.data?.length ?? 0
  const pending = pendingBySection.reduce((sum, [, n]) => sum + n, 0)

  const close = async () => {
    setClosing(true)
    try {
      const response = await api.post(`/periods/${periodId}/close`)
      setResult(response.data)
      notify(`Período ${response.data.periodCode} cerrado`)
      periods.reload()
    } catch (err) {
      notify(errorMessage(err), 'error')
    } finally {
      setClosing(false)
      setConfirmOpen(false)
    }
  }

  return (
    <>
      <PageHeader
        title="Cierre de período"
        subtitle="Al cerrar, las notas pasan al historial académico y se actualiza la situación de cada estudiante."
        action={<PeriodSelect periods={periods.data} value={periodId} onChange={(id) => { setPeriodId(id); setResult(null) }} />}
      />

      {period && (
        <LoadState loading={enrollments.loading} error={enrollments.error}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <Typography variant="h6">Período {period.code}</Typography>
            <StatusChip status={period.status} />
          </Box>

          <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, 1fr)' }, mb: 3 }}>
            <Summary label="Inscripciones" value={total} />
            <Summary label="Con nota registrada" value={total - pending} tone="success" />
            <Summary label="Notas pendientes" value={pending} tone={pending ? 'secondary' : 'success'} />
          </Box>

          {result && (
            <Alert severity="success" icon={<CheckCircleRounded />} sx={{ mb: 3, borderRadius: 3 }}>
              <Typography sx={{ fontWeight: 700 }}>Período {result.periodCode} cerrado</Typography>
              Se crearon {result.recordsCreated} registros en el historial académico.
              {result.eliminatedStudents.length > 0 && ` Eliminados (2ª reprobación): ${result.eliminatedStudents.join(', ')}.`}
              {result.graduatedStudents.length > 0 && ` Egresados: ${result.graduatedStudents.join(', ')}.`}
            </Alert>
          )}

          {period.status === 'OPEN' && (
            <Card>
              <CardContent sx={{ p: 3 }}>
                {pending > 0 ? (
                  <>
                    <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', mb: 1 }}>
                      <PendingActionsRounded color="secondary" />
                      <Typography variant="subtitle2">Secciones con calificaciones pendientes</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
                      {pendingBySection.map(([code, n]) => (
                        <Chip key={code} label={`${code} · ${n} sin nota`} color="secondary" variant="outlined" />
                      ))}
                    </Box>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                      El período solo puede cerrarse cuando todas las inscripciones tengan nota final.
                    </Typography>
                  </>
                ) : (
                  <Typography color="text.secondary" sx={{ mb: 2 }}>
                    Todas las inscripciones tienen nota final. El período está listo para cerrarse.
                  </Typography>
                )}
                <Button variant="contained" color="primary" startIcon={<LockRounded />} onClick={() => setConfirmOpen(true)}>
                  Cerrar período {period.code}
                </Button>
              </CardContent>
            </Card>
          )}
        </LoadState>
      )}

      <ConfirmDialog
        open={confirmOpen}
        title={`¿Cerrar el período ${period?.code}?`}
        text="Las notas pasarán al historial académico, se actualizará la situación de los estudiantes y ya no se podrán registrar notas ni inscripciones en este período. Esta acción no se puede deshacer."
        confirmLabel="Cerrar período"
        onConfirm={close}
        onClose={() => setConfirmOpen(false)}
        busy={closing}
      />
    </>
  )
}
