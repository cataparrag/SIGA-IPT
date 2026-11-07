import { useEffect, useState } from 'react'
import {
  Box, Card, CardActionArea, CardContent, Chip, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Typography,
} from '@mui/material'
import useApi from '../hooks/useApi'
import PageHeader from '../components/PageHeader'
import LoadState from '../components/LoadState'
import StatusChip from '../components/StatusChip'

function CareerCard({ career, selected, onSelect }) {
  return (
    <Card sx={{
      borderColor: selected ? 'primary.main' : 'divider',
      boxShadow: selected ? '0 0 0 3px rgba(78, 42, 132, 0.12)' : undefined,
    }}>
      <CardActionArea onClick={onSelect} sx={{ height: '100%' }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
            <Typography variant="caption" sx={{ fontWeight: 700, color: 'secondary.dark' }}>{career.code}</Typography>
            <StatusChip status={career.status} />
          </Box>
          <Typography variant="h6" sx={{ fontSize: 17, lineHeight: 1.3 }}>{career.name}</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            {career.durationSemesters} semestres · {career.description}
          </Typography>
        </CardContent>
      </CardActionArea>
    </Card>
  )
}

function PlanSubjects({ plan }) {
  const subjects = useApi(`/study-plans/${plan.id}/subjects`)
  const bySemester = [1, 2, 3, 4].map((semester) => ({
    semester,
    subjects: (subjects.data ?? []).filter((s) => s.semester === semester),
  }))

  return (
    <LoadState loading={subjects.loading} error={subjects.error}>
      <Box sx={{ display: 'grid', gap: 2 }}>
        {bySemester.map(({ semester, subjects: list }) => (
          <Card key={semester}>
            <Box sx={{ px: 2.5, py: 1.5, borderBottom: '1px solid', borderColor: 'divider', display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ width: 26, height: 26, borderRadius: 2, bgcolor: 'primary.main', color: '#fff', display: 'grid', placeItems: 'center', fontWeight: 800, fontSize: 13 }}>
                {semester}
              </Box>
              <Typography variant="subtitle2">Semestre {semester}</Typography>
              <Typography variant="caption" color="text.secondary" sx={{ ml: 'auto' }}>
                {list.reduce((sum, s) => sum + s.sct, 0)} SCT
              </Typography>
            </Box>
            <TableContainer>
              <Table size="small" sx={{ tableLayout: 'fixed', minWidth: 640 }}>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ width: '12%' }}>Código</TableCell>
                    <TableCell sx={{ width: '42%' }}>Asignatura</TableCell>
                    <TableCell sx={{ width: '10%' }}>TEL</TableCell>
                    <TableCell sx={{ width: '8%' }}>SCT</TableCell>
                    <TableCell sx={{ width: '28%' }}>Prerrequisitos</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {list.map((s) => (
                    <TableRow key={s.id} hover>
                      <TableCell sx={{ fontWeight: 700, color: 'primary.main' }}>{s.code}</TableCell>
                      <TableCell>{s.name}</TableCell>
                      <TableCell sx={{ fontVariantNumeric: 'tabular-nums' }}>{s.tel}</TableCell>
                      <TableCell>{s.sct}</TableCell>
                      <TableCell>
                        {s.prerequisiteCodes.length === 0
                          ? <Typography variant="body2" color="text.disabled">—</Typography>
                          : <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                              {s.prerequisiteCodes.map((code) => (
                                <Chip key={code} label={code} size="small" variant="outlined" color="secondary" />
                              ))}
                            </Box>}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        ))}
      </Box>
    </LoadState>
  )
}

export default function CareersPage() {
  const careers = useApi('/careers')
  const [selectedId, setSelectedId] = useState(null)

  useEffect(() => {
    if (careers.data?.length && selectedId === null) setSelectedId(careers.data[0].id)
  }, [careers.data, selectedId])

  const plans = useApi(selectedId ? `/careers/${selectedId}/study-plans` : null)
  const currentPlan = plans.data?.find((p) => p.status === 'CURRENT') ?? plans.data?.[0]

  return (
    <>
      <PageHeader title="Carreras y planes" subtitle="Carreras del instituto, su plan de estudios vigente y sus asignaturas." />
      <LoadState loading={careers.loading} error={careers.error}>
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' }, mb: 4 }}>
          {(careers.data ?? []).map((career) => (
            <CareerCard key={career.id} career={career} selected={career.id === selectedId} onSelect={() => setSelectedId(career.id)} />
          ))}
        </Box>
      </LoadState>

      {currentPlan && (
        <>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2, flexWrap: 'wrap' }}>
            <Typography variant="h5">Plan de estudios {currentPlan.identifier}</Typography>
            <StatusChip status={currentPlan.status} />
            {plans.data.length > 1 && (
              <Typography variant="body2" color="text.secondary">
                ({plans.data.length} planes en total)
              </Typography>
            )}
          </Box>
          <PlanSubjects key={currentPlan.id} plan={currentPlan} />
        </>
      )}
    </>
  )
}
