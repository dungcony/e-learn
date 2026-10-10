import { Outlet, Link } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { LogOut, ArrowLeft, MonitorPlay } from 'lucide-react';

export default function PosLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col text-slate-100">
      {/* POS Header (tối giản, tối màu để tập trung) */}
      <header className="h-14 bg-slate-950 border-b border-slate-800 flex items-center justify-between px-4 shrink-0">
        <div className="flex items-center gap-4">
          <Link to="/" className="p-2 -ml-2 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors">
            <ArrowLeft className="size-5" />
          </Link>
          <div className="flex items-center gap-2 text-amber-500 font-bold text-lg">
            <MonitorPlay className="size-5" />
            <span>POS System</span>
          </div>
          
          <div className="h-6 w-px bg-slate-800 mx-2" />
          
          <nav className="flex items-center gap-1">
            <Link to="/pos/tables" className="px-3 py-1.5 text-sm font-medium bg-slate-800 text-white rounded-md">
              Sơ đồ bàn
            </Link>
            <Link to="/pos/order" className="px-3 py-1.5 text-sm font-medium text-slate-400 hover:text-white hover:bg-slate-800/50 rounded-md">
              Gọi món (Menu)
            </Link>
          </nav>
        </div>

        <div className="flex items-center gap-4">
          <div className="text-right hidden sm:block">
            <div className="text-sm font-semibold text-white">{user?.full_name}</div>
            <div className="text-xs text-amber-500">{user?.role}</div>
          </div>
          <button
            onClick={logout}
            className="p-2 text-slate-400 hover:text-red-400 rounded-lg hover:bg-slate-800 transition-colors"
            title="Đăng xuất"
          >
            <LogOut className="size-5" />
          </button>
        </div>
      </header>

      {/* POS Content - Tràn viền để dễ chạm */}
      <main className="flex-1 overflow-hidden relative">
        <Outlet />
      </main>
    </div>
  );
}
