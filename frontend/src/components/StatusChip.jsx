import { Chip } from '@mui/material'

// Spanish labels and colors for every status the backend returns.
const STATUS = {
  ACTIVE: { label: 'Activa', color: 'success' },
  INACTIVE: { label: 'Inactiva', color: 'default' },
  CURRENT: { label: 'Vigente', color: 'primary' },
  NOT_CURRENT: { label: 'No vigente', color: 'default' },
  OPEN: { label: 'Abierto', color: 'success' },
  CLOSED: { label: 'Cerrado', color: 'default' },
  REGULAR: { label: 'Regular', color: 'success' },
  POSTPONED: { label: 'Postergación', color: 'warning' },
  TEMPORARY_WITHDRAWAL: { label: 'Retiro temporal', color: 'warning' },
  GRADUATED: { label: 'Egresado', color: 'primary' },
  ELIMINATED: { label: 'Eliminado', color: 'error' },
  APPROVED: { label: 'Aprobado', color: 'success' },
  FAILED: { label: 'Reprobado', color: 'error' },
}

export default function StatusChip({ status, label }) {
  const config = STATUS[status] ?? { label: status, color: 'default' }
  return <Chip size="small" label={label ?? config.label} color={config.color} variant={config.color === 'default' ? 'outlined' : 'filled'} />
}
