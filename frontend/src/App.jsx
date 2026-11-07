import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import CareersPage from './pages/CareersPage'
import AcademicOfferPage from './pages/AcademicOfferPage'
import StudentsPage from './pages/admin/StudentsPage'
import TeachersPage from './pages/admin/TeachersPage'
import EnrollmentsPage from './pages/admin/EnrollmentsPage'
import ClosingPage from './pages/admin/ClosingPage'
import MySectionsPage from './pages/teacher/MySectionsPage'
import MyPlanPage from './pages/student/MyPlanPage'
import EnrollPage from './pages/student/EnrollPage'
import MyRecordPage from './pages/student/MyRecordPage'
import { useRole } from './context/RoleContext'

/** Renders the page only for the given roles; others are sent to the start page. */
function RequireRole({ roles, children }) {
  const { role } = useRole()
  return roles.includes(role) ? children : <Navigate to="/" replace />
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/offer" element={<RequireRole roles={['ADMIN', 'STUDENT']}><AcademicOfferPage /></RequireRole>} />

        <Route path="/careers" element={<RequireRole roles={['ADMIN']}><CareersPage /></RequireRole>} />
        <Route path="/students" element={<RequireRole roles={['ADMIN']}><StudentsPage /></RequireRole>} />
        <Route path="/teachers" element={<RequireRole roles={['ADMIN']}><TeachersPage /></RequireRole>} />
        <Route path="/enrollments" element={<RequireRole roles={['ADMIN']}><EnrollmentsPage /></RequireRole>} />
        <Route path="/closing" element={<RequireRole roles={['ADMIN']}><ClosingPage /></RequireRole>} />

        <Route path="/my-sections" element={<RequireRole roles={['TEACHER']}><MySectionsPage /></RequireRole>} />

        <Route path="/my-plan" element={<RequireRole roles={['STUDENT']}><MyPlanPage /></RequireRole>} />
        <Route path="/enroll" element={<RequireRole roles={['STUDENT']}><EnrollPage /></RequireRole>} />
        <Route path="/my-record" element={<RequireRole roles={['STUDENT']}><MyRecordPage /></RequireRole>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
