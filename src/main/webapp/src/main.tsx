import { createRoot } from 'react-dom/client'
import './index.css'
import { router } from './routers';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ThemeProvider>
      <RouterProvider router={router} />
    </ThemeProvider>
  </StrictMode>
)
