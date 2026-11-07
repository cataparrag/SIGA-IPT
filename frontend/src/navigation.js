import DashboardRounded from '@mui/icons-material/DashboardRounded'
import SchoolRounded from '@mui/icons-material/SchoolRounded'
import PeopleAltRounded from '@mui/icons-material/PeopleAltRounded'
import BadgeRounded from '@mui/icons-material/BadgeRounded'
import EventNoteRounded from '@mui/icons-material/EventNoteRounded'
import AssignmentTurnedInRounded from '@mui/icons-material/AssignmentTurnedInRounded'
import LockClockRounded from '@mui/icons-material/LockClockRounded'
import ClassRounded from '@mui/icons-material/ClassRounded'
import MenuBookRounded from '@mui/icons-material/MenuBookRounded'
import HowToRegRounded from '@mui/icons-material/HowToRegRounded'
import HistoryEduRounded from '@mui/icons-material/HistoryEduRounded'

/** Menu items for each role (section 4.3 of the assignment). */
export const NAVIGATION = {
  ADMIN: [
    { label: 'Inicio', path: '/', icon: DashboardRounded },
    { label: 'Carreras y planes', path: '/careers', icon: SchoolRounded },
    { label: 'Estudiantes', path: '/students', icon: PeopleAltRounded },
    { label: 'Docentes', path: '/teachers', icon: BadgeRounded },
    { label: 'Oferta académica', path: '/offer', icon: EventNoteRounded },
    { label: 'Inscripciones', path: '/enrollments', icon: AssignmentTurnedInRounded },
    { label: 'Cierre de período', path: '/closing', icon: LockClockRounded },
  ],
  TEACHER: [
    { label: 'Inicio', path: '/', icon: DashboardRounded },
    { label: 'Mis secciones', path: '/my-sections', icon: ClassRounded },
  ],
  STUDENT: [
    { label: 'Inicio', path: '/', icon: DashboardRounded },
    { label: 'Mi plan de estudios', path: '/my-plan', icon: MenuBookRounded },
    { label: 'Oferta académica', path: '/offer', icon: EventNoteRounded },
    { label: 'Inscripción', path: '/enroll', icon: HowToRegRounded },
    { label: 'Mi historial', path: '/my-record', icon: HistoryEduRounded },
  ],
}

export const ROLE_LABELS = {
  ADMIN: 'Administrador académico',
  TEACHER: 'Docente',
  STUDENT: 'Estudiante',
}
