import { Card, CardContent, Typography } from '@mui/material'
import ConstructionRounded from '@mui/icons-material/ConstructionRounded'
import PageHeader from './PageHeader'

export default function ComingSoon({ title }) {
  return (
    <>
      <PageHeader title={title} />
      <Card>
        <CardContent sx={{ textAlign: 'center', py: 8 }}>
          <ConstructionRounded sx={{ fontSize: 44, color: 'secondary.main' }} />
          <Typography variant="h6" sx={{ mt: 1 }}>Vista en construcción</Typography>
          <Typography color="text.secondary">Esta sección se agrega en los próximos pasos.</Typography>
        </CardContent>
      </Card>
    </>
  )
}
