import { Alert, Box, CircularProgress } from '@mui/material'

/** Shows a spinner while loading or an error message; otherwise renders the children. */
export default function LoadState({ loading, error, children }) {
  if (loading) {
    return <Box sx={{ display: 'grid', placeItems: 'center', py: 8 }}><CircularProgress /></Box>
  }
  if (error) {
    return <Alert severity="error" sx={{ borderRadius: 3 }}>{error}</Alert>
  }
  return children
}
