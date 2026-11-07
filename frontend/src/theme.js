import { createTheme } from '@mui/material/styles'

// Purple and orange palette. Neutrals carry a slight purple tint.
const purple = {
  main: '#4E2A84',
  light: '#7A55B0',
  dark: '#341A5E',
  contrastText: '#FFFFFF',
}

const orange = {
  main: '#E8772E',
  light: '#F49A5C',
  dark: '#B85A1C',
  contrastText: '#FFFFFF',
}

const theme = createTheme({
  palette: {
    primary: purple,
    secondary: orange,
    background: {
      default: '#F6F4FA',
      paper: '#FFFFFF',
    },
    text: {
      primary: '#1E1A2B',
      secondary: '#5E586E',
    },
    divider: '#E6E1EF',
    success: { main: '#2E8B57' },
    warning: { main: '#D9901A' },
    error: { main: '#C9403A' },
  },
  shape: { borderRadius: 12 },
  typography: {
    fontFamily: '"Plus Jakarta Sans", "Segoe UI", system-ui, sans-serif',
    h4: { fontWeight: 800, letterSpacing: '-0.02em' },
    h5: { fontWeight: 700, letterSpacing: '-0.01em' },
    h6: { fontWeight: 700 },
    subtitle2: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
    overline: { fontWeight: 700, letterSpacing: '0.08em' },
  },
  components: {
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: { root: { borderRadius: 10, paddingInline: 16 } },
    },
    MuiPaper: {
      defaultProps: { elevation: 0 },
      styleOverrides: { root: { backgroundImage: 'none' } },
    },
    MuiCard: {
      styleOverrides: {
        root: { border: '1px solid #E6E1EF', boxShadow: '0 1px 2px rgba(52, 26, 94, 0.04)' },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: { fontWeight: 700, color: '#5E586E', fontSize: '0.78rem', textTransform: 'uppercase', letterSpacing: '0.04em' },
      },
    },
    MuiChip: {
      styleOverrides: { root: { fontWeight: 600 } },
    },
  },
})

export default theme
