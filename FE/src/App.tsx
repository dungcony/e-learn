import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from '@/context/AuthContext';
import ProtectedRoute from '@/routes/ProtectedRoute';

// Layouts
import CustomerLayout from '@/layouts/CustomerLayout';
import AccountLayout from '@/layouts/AccountLayout';
import AdminLayout from '@/layouts/AdminLayout';
import PosLayout from '@/layouts/PosLayout';
import KitchenLayout from '@/layouts/KitchenLayout';

// Customer Pages
import HomePage from '@/pages/Home/HomePage';
import MenuPage from '@/pages/Home/MenuPage';
import ReservationPage from '@/pages/Home/ReservationPage';
import LoginPage from '@/pages/Auth/LoginPage';
import RegisterPage from '@/pages/Auth/RegisterPage';
import ForgotPasswordPage from '@/pages/Auth/ForgotPasswordPage';
import ResetPasswordPage from '@/pages/Auth/ResetPasswordPage';
import VerifyEmailPage from '@/pages/Auth/VerifyEmailPage';
import ProfilePage from '@/pages/Profile/ProfilePage';
import MyReservationsPage from '@/pages/Profile/MyReservationsPage';
import MyInvoicesPage from '@/pages/Profile/MyInvoicesPage';
import ModuleDetailPage from '@/pages/Modules/ModuleDetailPage';

// Admin Pages
import AdminDashboard from '@/pages/Admin/AdminDashboard';
import AdminMenuPage from '@/pages/Admin/AdminMenuPage';
import AdminTablesPage from '@/pages/Admin/AdminTablesPage';
import AdminUsersPage from '@/pages/Admin/AdminUsersPage';
import AdminReportsPage from '@/pages/Admin/AdminReportsPage';

// POS Pages
import PosOrderPage from '@/pages/Pos/PosOrderPage';
import TableManagement from '@/pages/Tables/TableManagement';

// Kitchen Pages
import KitchenDashboard from '@/pages/Kitchen/KitchenDashboard';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* LUONG KHACH HANG */}
          <Route element={<CustomerLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="/verify-email" element={<VerifyEmailPage />} />
            <Route path="/menu" element={<MenuPage />} />
            <Route path="/reservations" element={<ReservationPage />} />
            
            <Route path="/account" element={<AccountLayout />}>
              <Route index element={<Navigate to="/account/profile" replace />} />
              <Route path="profile" element={<ProfilePage />} />
              <Route path="reservations" element={<MyReservationsPage />} />
              <Route path="invoices" element={<MyInvoicesPage />} />
            </Route>

            <Route path="/modules/:slug" element={<ModuleDetailPage />} />
          </Route>

          {/* LUONG ADMIN */}
          <Route element={<ProtectedRoute allowedRoles={['MANAGER']} />}>
            <Route path="/admin" element={<AdminLayout />}>
              <Route index element={<AdminDashboard />} />
              <Route path="menu" element={<AdminMenuPage />} />
              <Route path="tables" element={<AdminTablesPage />} />
              <Route path="users" element={<AdminUsersPage />} />
              <Route path="reports" element={<AdminReportsPage />} />
            </Route>
          </Route>

          {/* LUONG POS */}
          <Route element={<ProtectedRoute allowedRoles={['MANAGER', 'CASHIER', 'WAITER']} />}>
            <Route path="/pos" element={<PosLayout />}>
              <Route index element={<Navigate to="/pos/tables" replace />} />
              <Route path="tables" element={<div className="h-full bg-slate-900 overflow-auto p-6"><TableManagement /></div>} />
              <Route path="order" element={<PosOrderPage />} />
            </Route>
          </Route>

          {/* LUONG BEP */}
          <Route element={<ProtectedRoute allowedRoles={['MANAGER', 'CHEF']} />}>
            <Route path="/kitchen" element={<KitchenLayout />}>
              <Route index element={<KitchenDashboard />} />
            </Route>
          </Route>
          
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
