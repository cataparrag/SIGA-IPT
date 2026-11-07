import { Box, Typography } from '@mui/material'

const DAYS = [['L', 'Lun'], ['M', 'Mar'], ['W', 'Mié'], ['J', 'Jue'], ['V', 'Vie']]
const MODULES = [
  [1, '08:15'], [2, '09:50'], [3, '11:25'], [4, '13:45'], [5, '15:20'], [6, '16:55'],
]

/**
 * Weekly timetable. `items` is a list of { code, blocks, tone } where tone is
 * 'primary' (selected) or 'secondary' (already enrolled). Cells with two or
 * more items are shown as conflicts.
 */
export default function ScheduleGrid({ items }) {
  const byBlock = {}
  for (const item of items) {
    for (const block of item.blocks) {
      ;(byBlock[block] ??= []).push(item)
    }
  }

  return (
    <Box sx={{ overflowX: 'auto' }}>
      <Box sx={{ display: 'grid', gridTemplateColumns: '52px repeat(5, minmax(64px, 1fr))', gap: 0.5, minWidth: 380 }}>
        <Box />
        {DAYS.map(([, label]) => (
          <Typography key={label} variant="caption" sx={{ textAlign: 'center', fontWeight: 700, color: 'text.secondary' }}>{label}</Typography>
        ))}
        {MODULES.map(([module, time]) => (
          <Box key={module} sx={{ display: 'contents' }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
              <Typography variant="caption" sx={{ fontWeight: 700, lineHeight: 1 }}>M{module}</Typography>
              <Typography variant="caption" color="text.secondary" sx={{ fontSize: 10.5 }}>{time}</Typography>
            </Box>
            {DAYS.map(([day]) => {
              const cell = byBlock[`${day}${module}`] ?? []
              const conflict = cell.length > 1
              const tone = cell[0]?.tone ?? 'primary'
              return (
                <Box key={day} sx={{
                  height: 40, borderRadius: 1.5, display: 'grid', placeItems: 'center',
                  fontSize: 11.5, fontWeight: 700, textAlign: 'center', lineHeight: 1.1,
                  border: '1px solid',
                  borderColor: conflict ? 'error.main' : cell.length ? 'transparent' : 'divider',
                  bgcolor: conflict ? 'rgba(201, 64, 58, 0.12)'
                    : cell.length ? (tone === 'secondary' ? 'rgba(232, 119, 46, 0.16)' : 'rgba(78, 42, 132, 0.14)') : 'transparent',
                  color: conflict ? 'error.main' : tone === 'secondary' ? 'secondary.dark' : 'primary.main',
                }}>
                  {conflict ? 'Choque' : cell[0]?.code ?? ''}
                </Box>
              )
            })}
          </Box>
        ))}
      </Box>
    </Box>
  )
}
