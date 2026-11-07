import { Box, Card, CardContent, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material'
import useApi from '../../hooks/useApi'
import { useRole } from '../../context/RoleContext'
import PageHeader from '../../components/PageHeader'
import LoadState from '../../components/LoadState'
import StatusChip from '../../components/StatusChip'

const formatGrade = (g) => g.toFixed(1).replace('.', ',')

export default function MyRecordPage() {
  const { user } = useRole()
  const record = useApi(`/students/${user.studentId}/academic-record`)
  const rows = record.data ?? []
  const byPeriod = rows.reduce((groups, r) => ({ ...groups, [r.periodCode]: [...(groups[r.periodCode] ?? []), r] }), {})
  const approved = rows.filter((r) => r.result === 'APPROVED')
  const average = rows.length ? rows.reduce((sum, r) => sum + r.finalGrade, 0) / rows.length : null

  return (
    <>
      <PageHeader title="Mi historial académico" subtitle="Asignaturas cursadas en períodos cerrados, con su nota final y resultado." />
      <LoadState loading={record.loading} error={record.error}>
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, 1fr)' }, mb: 3 }}>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Promedio</Typography>
            <Typography variant="h4" sx={{ color: 'primary.main' }}>{average != null ? formatGrade(Math.round(average * 10) / 10) : '—'}</Typography>
          </CardContent></Card>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">Asignaturas aprobadas</Typography>
            <Typography variant="h4" sx={{ color: 'success.main' }}>{approved.length}</Typography>
          </CardContent></Card>
          <Card><CardContent>
            <Typography variant="body2" color="text.secondary">SCT aprobados</Typography>
            <Typography variant="h4" sx={{ color: 'secondary.main' }}>{approved.reduce((sum, r) => sum + r.sct, 0)}</Typography>
          </CardContent></Card>
        </Box>

        {rows.length === 0 && (
          <Card><CardContent sx={{ textAlign: 'center', py: 6 }}>
            <Typography color="text.secondary">Tu historial se completa cuando se cierra cada período académico.</Typography>
          </CardContent></Card>
        )}

        {Object.entries(byPeriod).sort(([a], [b]) => b.localeCompare(a)).map(([period, items]) => (
          <Card key={period} sx={{ mb: 2 }}>
            <Box sx={{ px: 2.5, py: 1.5, borderBottom: '1px solid', borderColor: 'divider' }}>
              <Typography variant="subtitle2">Período {period}</Typography>
            </Box>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Código</TableCell>
                    <TableCell>Asignatura</TableCell>
                    <TableCell>Semestre</TableCell>
                    <TableCell>SCT</TableCell>
                    <TableCell>Nota</TableCell>
                    <TableCell>Resultado</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {items.map((r) => (
                    <TableRow key={r.id} hover>
                      <TableCell sx={{ fontWeight: 700, color: 'primary.main' }}>{r.subjectCode}</TableCell>
                      <TableCell>{r.subjectName}</TableCell>
                      <TableCell>{r.subjectSemester}°</TableCell>
                      <TableCell>{r.sct}</TableCell>
                      <TableCell sx={{ fontWeight: 700, fontVariantNumeric: 'tabular-nums' }}>{formatGrade(r.finalGrade)}</TableCell>
                      <TableCell><StatusChip status={r.result} /></TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        ))}
      </LoadState>
    </>
  )
}
