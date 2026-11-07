import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import ComingSoon from './components/ComingSoon'
import Dashboard from './pages/Dashboard'
import CareersPage from './pages/CareersPage'
import AcademicOfferPage from './pages/AcademicOfferPage'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/careers" element={<CareersPage />} />
        <Route path="/offer" element={<AcademicOfferPage />} />
        <Route path="/students" element={<ComingSoon title="Estudiantes" />} />
        <Route path="/teachers" element={<ComingSoon title="Docentes" />} />
        <Route path="/enrollments" element={<ComingSoon title="Inscripciones" />} />
        <Route path="/closing" element={<ComingSoon title="Cierre de período" />} />
        <Route path="/my-sections" element={<ComingSoon title="Mis secciones" />} />
        <Route path="/my-plan" element={<ComingSoon title="Mi plan de estudios" />} />
        <Route path="/enroll" element={<ComingSoon title="Inscripción" />} />
        <Route path="/my-record" element={<ComingSoon title="Mi historial" />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
