import { Box, Card, Chip, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material'
import useApi from '../../hooks/useApi'
import { useRole } from '../../context/RoleContext'
import PageHeader from '../../components/PageHeader'
import LoadState from '../../components/LoadState'

const SUBJECT_STATUS = {
  APPROVED: { label: 'Aprobada', color: 'success' },
  ENROLLED: { label: 'Inscrita', color: 'secondary' },
  FAILED: { label: 'Reprobada', color: 'error' },
  PENDING: { label: 'Pendiente', color: 'default' },
}

export default function MyPlanPage() {
  const { user } = useRole()
  const student = useApi(`/students/${user.studentId}`)
  const subjects = useApi(student.data ? `/study-plans/${student.data.studyPlanId}/subjects` : null)
  const record = useApi(`/students/${user.studentId}/academic-record`)
  const enrollments = useApi(`/students/${user.studentId}/enrollments`)

  const approved = new Set((record.data ?? []).filter((r) => r.result === 'APPROVED').map((r) => r.subjectCode))
  const failed = new Set((record.data ?? []).filter((r) => r.result === 'FAILED').map((r) => r.subjectCode))
  const enrolled = new Set((enrollments.data ?? []).filter((e) => !e.result).map((e) => e.subjectCode))

  const statusOf = (code) =>
    approved.has(code) ? 'APPROVED' : enrolled.has(code) ? 'ENROLLED' : failed.has(code) ? 'FAILED' : 'PENDING'

  const all = subjects.data ?? []
  const totalSct = all.reduce((sum, s) => sum + s.sct, 0)
  const approvedSct = all.filter((s) => approved.has(s.code)).reduce((sum, s) => sum + s.sct, 0)
  const progress = totalSct ? Math.round((approvedSct / totalSct) * 100) : 0

  return (
    <>
      <PageHeader title="Mi plan de estudios"
        subtitle={student.data ? `${student.data.careerCode} · Plan ${student.data.studyPlanIdentifier}` : undefined} />
      <LoadState loading={student.loading || subjects.loading} error={student.error || subjects.error}>
        <Card sx={{ mb: 3 }}>
          <Box sx={{ p: 2.5, display: 'flex', gap: 3, alignItems: 'center', flexWrap: 'wrap' }}>
            <Box>
              <Typography variant="body2" color="text.secondary">Avance del plan</Typography>
              <Typography variant="h4" sx={{ color: 'primary.main' }}>{progress}%</Typography>
            </Box>
            <Box sx={{ flex: 1, minWidth: 220 }}>
              <Box sx={{ height: 10, borderRadius: 5, bgcolor: 'rgba(78, 42, 132, 0.08)', overflow: 'hidden' }}>
                <Box sx={{ width: `${progress}%`, height: '100%', background: 'linear-gradient(90deg, #4E2A84, #E8772E)' }} />
              </Box>
              <Typography variant="caption" color="text.secondary">{approvedSct} de {totalSct} SCT aprobados · {approved.size} de {all.length} asignaturas</Typography>
            </Box>
          </Box>
        </Card>

        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', lg: 'repeat(2, 1fr)' } }}>
          {[1, 2, 3, 4].map((semester) => (
            <Card key={semester}>
              <Box sx={{ px: 2.5, py: 1.5, borderBottom: '1px solid', borderColor: 'divider', display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box sx={{ width: 26, height: 26, borderRadius: 2, bgcolor: 'primary.main', color: '#fff', display: 'grid', placeItems: 'center', fontWeight: 800, fontSize: 13 }}>{semester}</Box>
                <Typography variant="subtitle2">Semestre {semester}</Typography>
              </Box>
              <TableContainer>
                <Table size="small" sx={{ tableLayout: 'fixed' }}>
                  <TableHead>
                    <TableRow>
                      <TableCell sx={{ width: '20%' }}>Código</TableCell>
                      <TableCell sx={{ width: '50%' }}>Asignatura</TableCell>
                      <TableCell sx={{ width: '10%' }}>SCT</TableCell>
                      <TableCell sx={{ width: '20%' }}>Estado</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {all.filter((s) => s.semester === semester).map((s) => {
                      const status = SUBJECT_STATUS[statusOf(s.code)]
                      return (
                        <TableRow key={s.id} hover>
                          <TableCell sx={{ fontWeight: 700, color: 'primary.main' }}>{s.code}</TableCell>
                          <TableCell>{s.name}</TableCell>
                          <TableCell>{s.sct}</TableCell>
                          <TableCell><Chip size="small" label={status.label} color={status.color} variant={status.color === 'default' ? 'outlined' : 'filled'} /></TableCell>
                        </TableRow>
                      )
                    })}
                  </TableBody>
                </Table>
              </TableContainer>
            </Card>
          ))}
        </Box>
      </LoadState>
    </>
  )
}
