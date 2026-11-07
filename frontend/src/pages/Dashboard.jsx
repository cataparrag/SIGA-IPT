import { Box, Card, CardContent, Typography } from '@mui/material'
import SchoolRounded from '@mui/icons-material/SchoolRounded'
import PeopleAltRounded from '@mui/icons-material/PeopleAltRounded'
import BadgeRounded from '@mui/icons-material/BadgeRounded'
import EventAvailableRounded from '@mui/icons-material/EventAvailableRounded'
import ClassRounded from '@mui/icons-material/ClassRounded'
import AssignmentTurnedInRounded from '@mui/icons-material/AssignmentTurnedInRounded'
import useApi from '../hooks/useApi'
import { useRole } from '../context/RoleContext'
import StatusChip from '../components/StatusChip'

function StatCard({ icon: Icon, label, value, accent = 'primary', footer }) {
  return (
    <Card>
      <CardContent sx={{ display: 'flex', gap: 2, alignItems: 'center', p: 2.5 }}>
        <Box sx={{
          width: 48, height: 48, borderRadius: 3, display: 'grid', placeItems: 'center', flexShrink: 0,
          bgcolor: accent === 'primary' ? 'rgba(78, 42, 132, 0.1)' : 'rgba(232, 119, 46, 0.12)',
          color: `${accent}.main`,
        }}>
          <Icon />
        </Box>
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="body2" color="text.secondary">{label}</Typography>
          <Typography variant="h5" sx={{ fontVariantNumeric: 'tabular-nums' }}>{value ?? '—'}</Typography>
          {footer}
        </Box>
      </CardContent>
    </Card>
  )
}

function Welcome({ name, text }) {
  return (
    <Card sx={{
      mb: 3, border: 0, color: '#fff', position: 'relative', overflow: 'hidden',
      background: 'linear-gradient(120deg, #4E2A84 0%, #341A5E 100%)',
    }}>
      <Box sx={{ position: 'absolute', right: -40, top: -40, width: 180, height: 180, borderRadius: '50%', bgcolor: 'secondary.main', opacity: 0.9 }} />
      <Box sx={{ position: 'absolute', right: 90, bottom: -60, width: 120, height: 120, borderRadius: '50%', border: '18px solid rgba(255,255,255,0.08)' }} />
      <CardContent sx={{ p: { xs: 3, md: 4 }, position: 'relative', maxWidth: 620 }}>
        <Typography variant="overline" sx={{ opacity: 0.8 }}>Bienvenida</Typography>
        <Typography variant="h4" sx={{ mt: 0.5 }}>Hola, {name}</Typography>
        <Typography sx={{ mt: 1, opacity: 0.85 }}>{text}</Typography>
      </CardContent>
    </Card>
  )
}

const grid = { display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', lg: 'repeat(4, 1fr)' } }

function AdminDashboard({ user }) {
  const careers = useApi('/careers')
  const students = useApi('/students')
  const teachers = useApi('/teachers')
  const periods = useApi('/periods')
  const openPeriod = periods.data?.find((p) => p.status === 'OPEN')

  return (
    <>
      <Welcome name={user.name.split(' ')[0]} text="Administra carreras, docentes, estudiantes y la oferta académica de cada período." />
      <Box sx={grid}>
        <StatCard icon={SchoolRounded} label="Carreras" value={careers.data?.length} />
        <StatCard icon={PeopleAltRounded} label="Estudiantes" value={students.data?.length} />
        <StatCard icon={BadgeRounded} label="Docentes" value={teachers.data?.length} />
        <StatCard icon={EventAvailableRounded} label="Período actual" accent="secondary"
          value={openPeriod?.code ?? 'Sin período abierto'}
          footer={openPeriod && <StatusChip status={openPeriod.status} />} />
      </Box>
    </>
  )
}

function TeacherDashboard({ user }) {
  const sections = useApi(`/teachers/${user.teacherId}/sections`)
  const totalStudents = sections.data?.reduce((sum, s) => sum + s.enrolledCount, 0)
  return (
    <>
      <Welcome name={user.name.split(' ')[0]} text="Revisa tus secciones y registra las calificaciones finales de tus estudiantes." />
      <Box sx={grid}>
        <StatCard icon={ClassRounded} label="Mis secciones" value={sections.data?.length} />
        <StatCard icon={PeopleAltRounded} label="Estudiantes inscritos" value={totalStudents} accent="secondary" />
      </Box>
    </>
  )
}

function StudentDashboard({ user }) {
  const student = useApi(`/students/${user.studentId}`)
  const enrollments = useApi(`/students/${user.studentId}/enrollments`)
  return (
    <>
      <Welcome name={user.name.split(' ')[0]} text="Consulta tu plan de estudios, la oferta del período y realiza tu inscripción de asignaturas." />
      <Box sx={grid}>
        <StatCard icon={SchoolRounded} label="Mi carrera" value={student.data?.careerCode}
          footer={student.data && <Typography variant="caption" color="text.secondary">Plan {student.data.studyPlanIdentifier}</Typography>} />
        <StatCard icon={AssignmentTurnedInRounded} label="Asignaturas inscritas" value={enrollments.data?.length} accent="secondary" />
        <StatCard icon={EventAvailableRounded} label="Situación académica"
          value={student.data && <StatusChip status={student.data.status} />} />
      </Box>
    </>
  )
}

export default function Dashboard() {
  const { role, user } = useRole()
  if (role === 'TEACHER') return <TeacherDashboard user={user} />
  if (role === 'STUDENT') return <StudentDashboard user={user} />
  return <AdminDashboard user={user} />
}
