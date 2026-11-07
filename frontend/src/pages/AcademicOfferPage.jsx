import { useEffect, useState } from 'react'
import {
  Box, Card, LinearProgress, MenuItem, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import useApi from '../hooks/useApi'
import PageHeader from '../components/PageHeader'
import LoadState from '../components/LoadState'
import StatusChip from '../components/StatusChip'
import ScheduleBlocks from '../components/ScheduleBlocks'

function Seats({ enrolled, capacity }) {
  const percent = capacity ? Math.round((enrolled / capacity) * 100) : 0
  return (
    <Box sx={{ minWidth: 120 }}>
      <Typography variant="body2" sx={{ fontVariantNumeric: 'tabular-nums', fontWeight: 600 }}>
        {enrolled} / {capacity}
      </Typography>
      <LinearProgress variant="determinate" value={percent} color={percent >= 90 ? 'secondary' : 'primary'}
        sx={{ height: 6, borderRadius: 3, mt: 0.5, bgcolor: 'rgba(78, 42, 132, 0.08)' }} />
    </Box>
  )
}

export default function AcademicOfferPage() {
  const periods = useApi('/periods')
  const [periodId, setPeriodId] = useState('')

  // Select the open period (or the most recent one) when the periods arrive.
  useEffect(() => {
    if (periods.data?.length && !periodId) {
      const open = periods.data.find((p) => p.status === 'OPEN') ?? periods.data[0]
      setPeriodId(open.id)
    }
  }, [periods.data, periodId])

  const sections = useApi(periodId ? `/periods/${periodId}/sections` : null)
  const period = periods.data?.find((p) => p.id === periodId)
  const rows = [...(sections.data ?? [])].sort((a, b) =>
    a.subjectSemester - b.subjectSemester || a.subjectCode.localeCompare(b.subjectCode))

  return (
    <>
      <PageHeader
        title="Oferta académica"
        subtitle="Secciones ofrecidas en el período, con su docente, horario y cupos."
        action={
          <TextField select size="small" label="Período" value={periodId} onChange={(e) => setPeriodId(e.target.value)} sx={{ minWidth: 180, bgcolor: '#fff' }}>
            {(periods.data ?? []).map((p) => (
              <MenuItem key={p.id} value={p.id}>{p.code}</MenuItem>
            ))}
          </TextField>
        }
      />

      {period && (
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', mb: 2 }}>
          <Typography variant="body2" color="text.secondary">Estado del período:</Typography>
          <StatusChip status={period.status} />
        </Box>
      )}

      <LoadState loading={periods.loading || sections.loading} error={periods.error || sections.error}>
        <Card>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Asignatura</TableCell>
                  <TableCell>Semestre</TableCell>
                  <TableCell>Docente</TableCell>
                  <TableCell>Horario</TableCell>
                  <TableCell>Cupos</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.map((s) => (
                  <TableRow key={s.id} hover>
                    <TableCell>
                      <Typography variant="subtitle2">{s.subjectName}</Typography>
                      <Typography variant="caption" color="text.secondary">{s.subjectCode}</Typography>
                    </TableCell>
                    <TableCell>{s.subjectSemester}°</TableCell>
                    <TableCell>{s.teacherName}</TableCell>
                    <TableCell><ScheduleBlocks blocks={s.blocks} /></TableCell>
                    <TableCell><Seats enrolled={s.enrolledCount} capacity={s.capacity} /></TableCell>
                  </TableRow>
                ))}
                {rows.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>
                      Este período todavía no tiene secciones.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      </LoadState>
    </>
  )
}
