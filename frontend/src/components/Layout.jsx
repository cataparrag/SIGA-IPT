import { useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import {
  AppBar, Avatar, Box, Button, Drawer, IconButton, List, ListItemButton, ListItemIcon,
  ListItemText, Toolbar, Typography, useMediaQuery,
} from '@mui/material'
import { useTheme } from '@mui/material/styles'
import MenuRounded from '@mui/icons-material/MenuRounded'
import LogoutRounded from '@mui/icons-material/LogoutRounded'
import { NAVIGATION, ROLE_LABELS } from '../navigation'
import { useRole } from '../context/RoleContext'

const DRAWER_WIDTH = 264

function BrandMark() {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
      <Box sx={{
        width: 38, height: 38, borderRadius: '11px', bgcolor: 'primary.main',
        display: 'grid', placeItems: 'center', position: 'relative',
      }}>
        <Typography sx={{ color: '#fff', fontWeight: 800, fontSize: 15 }}>S</Typography>
        <Box sx={{
          position: 'absolute', right: -3, top: -3, width: 12, height: 12,
          borderRadius: '50%', bgcolor: 'secondary.main', border: '2px solid #fff',
        }} />
      </Box>
      <Box>
        <Typography sx={{ fontWeight: 800, lineHeight: 1.1, color: 'text.primary' }}>SIGA IPT</Typography>
        <Typography variant="caption" color="text.secondary">Gestión académica</Typography>
      </Box>
    </Box>
  )
}

function SidebarContent({ onNavigate }) {
  const { role, user, logout } = useRole()
  const items = NAVIGATION[role] ?? []

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', p: 2 }}>
      <Box sx={{ px: 1, py: 1.5, mb: 2 }}>
        <BrandMark />
      </Box>

      <Typography variant="overline" color="text.secondary" sx={{ px: 1.5 }}>
        Menú
      </Typography>
      <List sx={{ flex: 1 }}>
        {items.map(({ label, path, icon: Icon }) => (
          <ListItemButton
            key={path}
            component={NavLink}
            to={path}
            end={path === '/'}
            onClick={onNavigate}
            sx={{
              borderRadius: 2.5, mb: 0.5, color: 'text.secondary',
              '& .MuiListItemIcon-root': { color: 'inherit', minWidth: 38 },
              '&.active': {
                bgcolor: 'primary.main', color: '#fff',
                boxShadow: '0 6px 16px rgba(78, 42, 132, 0.25)',
              },
              '&:not(.active):hover': { bgcolor: 'rgba(78, 42, 132, 0.06)', color: 'primary.main' },
            }}
          >
            <ListItemIcon><Icon fontSize="small" /></ListItemIcon>
            <ListItemText primary={label} slotProps={{ primary: { fontWeight: 600, fontSize: 14.5 } }} />
          </ListItemButton>
        ))}
      </List>

      <Box sx={{ p: 1.5, borderRadius: 3, bgcolor: 'rgba(78, 42, 132, 0.05)', border: '1px solid', borderColor: 'divider' }}>
        <Typography variant="subtitle2" noWrap>{user.name}</Typography>
        <Typography variant="caption" color="text.secondary" noWrap component="div">{user.email}</Typography>
        <Button fullWidth size="small" variant="outlined" color="primary" startIcon={<LogoutRounded />} onClick={logout} sx={{ mt: 1.25 }}>
          Cerrar sesión
        </Button>
      </Box>
    </Box>
  )
}

export default function Layout() {
  const theme = useTheme()
  const isDesktop = useMediaQuery(theme.breakpoints.up('md'))
  const [mobileOpen, setMobileOpen] = useState(false)
  const { role, user } = useRole()
  const location = useLocation()
  const current = (NAVIGATION[role] ?? []).find((item) =>
    item.path === '/' ? location.pathname === '/' : location.pathname.startsWith(item.path))

  const initials = user.name.split(' ').map((w) => w[0]).slice(0, 2).join('')

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <Drawer
        variant={isDesktop ? 'permanent' : 'temporary'}
        open={isDesktop || mobileOpen}
        onClose={() => setMobileOpen(false)}
        sx={{
          width: isDesktop ? DRAWER_WIDTH : 0, flexShrink: 0,
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH, borderRight: '1px solid', borderColor: 'divider' },
        }}
      >
        <SidebarContent onNavigate={() => setMobileOpen(false)} />
      </Drawer>

      <Box sx={{ flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column' }}>
        <AppBar position="sticky" color="inherit" elevation={0} sx={{ bgcolor: 'rgba(246, 244, 250, 0.85)', backdropFilter: 'blur(8px)', borderBottom: '1px solid', borderColor: 'divider' }}>
          <Toolbar sx={{ gap: 1.5 }}>
            {!isDesktop && (
              <IconButton edge="start" onClick={() => setMobileOpen(true)} aria-label="Abrir menú">
                <MenuRounded />
              </IconButton>
            )}
            <Typography variant="h6" sx={{ flex: 1, fontSize: 18 }}>
              {current?.label ?? 'SIGA IPT'}
            </Typography>
            <Box sx={{ textAlign: 'right', display: { xs: 'none', sm: 'block' } }}>
              <Typography variant="subtitle2" sx={{ lineHeight: 1.2 }}>{user.name}</Typography>
              <Typography variant="caption" color="text.secondary">{ROLE_LABELS[role]}</Typography>
            </Box>
            <Avatar sx={{ bgcolor: 'secondary.main', width: 38, height: 38, fontWeight: 700, fontSize: 15 }}>
              {initials}
            </Avatar>
          </Toolbar>
        </AppBar>

        <Box component="main" sx={{ p: { xs: 2, md: 4 }, maxWidth: 1240, width: '100%', mx: 'auto' }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  )
}
