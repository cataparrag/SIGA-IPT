import { createContext, useContext, useState } from 'react'

/**
 * TEMPORARY: lets you switch roles to see each view while Keycloak is not connected.
 * Tomorrow the role and the user will come from the Keycloak token.
 */
const DEMO_USERS = {
  ADMIN: { name: 'Marcela Díaz', role: 'ADMIN' },
  TEACHER: { name: 'Ana Rojas', role: 'TEACHER', teacherId: 1 },
  STUDENT: { name: 'Pedro Soto', role: 'STUDENT', studentId: 1 },
}

const RoleContext = createContext(null)

export function RoleProvider({ children }) {
  const [role, setRole] = useState('ADMIN')
  const value = { role, setRole, user: DEMO_USERS[role] }
  return <RoleContext.Provider value={value}>{children}</RoleContext.Provider>
}

export function useRole() {
  return useContext(RoleContext)
}
