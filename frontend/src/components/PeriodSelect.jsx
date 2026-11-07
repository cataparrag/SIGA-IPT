import { useEffect } from 'react'
import { MenuItem, TextField } from '@mui/material'

/** Period dropdown. Selects the OPEN period (or the most recent) when nothing is selected. */
export default function PeriodSelect({ periods, value, onChange, onlyOpen = false }) {
  const options = (periods ?? []).filter((p) => !onlyOpen || p.status === 'OPEN')

  useEffect(() => {
    if (options.length && !options.some((p) => p.id === value)) {
      const open = options.find((p) => p.status === 'OPEN') ?? options[0]
      onChange(open.id)
    }
  }, [options, value, onChange])

  return (
    <TextField select size="small" label="Período" value={options.some((p) => p.id === value) ? value : ''}
      onChange={(e) => onChange(e.target.value)} sx={{ minWidth: 180, bgcolor: '#fff' }}>
      {options.map((p) => (
        <MenuItem key={p.id} value={p.id}>{p.code}{p.status === 'CLOSED' ? ' (cerrado)' : ''}</MenuItem>
      ))}
    </TextField>
  )
}
