import { Box, Tooltip } from '@mui/material'

const DAYS = { L: 'Lunes', M: 'Martes', W: 'Miércoles', J: 'Jueves', V: 'Viernes' }
const MODULES = {
  1: '08:15 – 09:35', 2: '09:50 – 11:10', 3: '11:25 – 12:45',
  4: '13:45 – 15:05', 5: '15:20 – 16:40', 6: '16:55 – 18:15',
}

/** Shows schedule blocks such as "L2" as small badges, with the day and time on hover. */
export default function ScheduleBlocks({ blocks }) {
  return (
    <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
      {blocks.map((block) => (
        <Tooltip key={block} title={`${DAYS[block[0]]}, módulo ${block[1]} (${MODULES[block[1]]})`}>
          <Box sx={{
            px: 0.9, py: 0.2, borderRadius: 1.5, fontSize: 12.5, fontWeight: 700,
            bgcolor: 'rgba(78, 42, 132, 0.08)', color: 'primary.main', fontVariantNumeric: 'tabular-nums',
          }}>
            {block}
          </Box>
        </Tooltip>
      ))}
    </Box>
  )
}
