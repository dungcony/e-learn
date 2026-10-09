import { GraduationCap, BookOpen, Users, ShieldCheck, ArrowRight, ExternalLink } from 'lucide-react';

export default function App() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col">
      {/* thanh điều hướng trên cùng */}
      <header className="border-b bg-white sticky top-0 z-50 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-blue-600 text-white flex items-center justify-center font-bold">
              <GraduationCap className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xl font-bold tracking-tight text-slate-900">E-Learning Portal</span>
              <span className="ml-2 text-xs font-medium px-2 py-0.5 rounded-full bg-blue-100 text-blue-800">
                React + Vite
              </span>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <a
              href="http://localhost:8080/swagger-ui.html"
              target="_blank"
              rel="noreferrer"
              className="text-sm font-medium text-slate-600 hover:text-slate-900 flex items-center gap-1"
            >
              <span>Swagger API</span>
              <ExternalLink className="w-3.5 h-3.5" />
            </a>
            <button className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition">
              Đăng nhập
            </button>
          </div>
        </div>
      </header>

      {/* nội dung chính */}
      <main className="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 w-full">
        <div className="text-center max-w-3xl mx-auto mb-12">
          <h1 className="text-4xl font-extrabold text-slate-900 tracking-tight sm:text-5xl">
            Hệ Thống Học Tập Trực Tuyến
          </h1>
          <p className="mt-4 text-lg text-slate-600">
            Khởi tạo thành công Frontend kết nối với Backend Spring Boot 3.4 REST API.
          </p>
        </div>

        {/* danh sách các phân hệ chính */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition">
            <div className="w-12 h-12 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center mb-4">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <h3 className="font-semibold text-lg text-slate-900">Xác thực & Tài khoản</h3>
            <p className="text-sm text-slate-500 mt-2">
              Đăng nhập JWT, đăng ký, quên mật khẩu, cập nhật hồ sơ cá nhân.
            </p>
            <div className="mt-4 flex items-center text-sm font-medium text-blue-600">
              <span>Endpoint: /auth/*</span>
            </div>
          </div>

          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition">
            <div className="w-12 h-12 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center mb-4">
              <Users className="w-6 h-6" />
            </div>
            <h3 className="font-semibold text-lg text-slate-900">Quản lý Người dùng</h3>
            <p className="text-sm text-slate-500 mt-2">
              Quản lý danh sách giảng viên, học viên dành cho Quản trị viên (Admin).
            </p>
            <div className="mt-4 flex items-center text-sm font-medium text-emerald-600">
              <span>Endpoint: /users/*</span>
            </div>
          </div>

          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition">
            <div className="w-12 h-12 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center mb-4">
              <BookOpen className="w-6 h-6" />
            </div>
            <h3 className="font-semibold text-lg text-slate-900">Khóa học & Bài giảng</h3>
            <p className="text-sm text-slate-500 mt-2">
              Tìm kiếm khóa học, quản lý nội dung bài giảng, danh mục khóa học.
            </p>
            <div className="mt-4 flex items-center text-sm font-medium text-amber-600">
              <span>Endpoint: /courses/*</span>
            </div>
          </div>

          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition">
            <div className="w-12 h-12 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center mb-4">
              <GraduationCap className="w-6 h-6" />
            </div>
            <h3 className="font-semibold text-lg text-slate-900">Không gian Học tập</h3>
            <p className="text-sm text-slate-500 mt-2">
              Theo dõi tiến độ học bài, lịch sử khóa học, tham gia bài giảng.
            </p>
            <div className="mt-4 flex items-center text-sm font-medium text-purple-600">
              <span>Endpoint: /learning/*</span>
            </div>
          </div>
        </div>

        {/* thông tin kết nối backend */}
        <div className="mt-12 bg-white rounded-xl border border-slate-200 p-6 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div>
            <h4 className="font-medium text-slate-900">Trạng thái cấu hình máy chủ</h4>
            <p className="text-sm text-slate-500">
              Proxy Vite cấu hình chuyển tiếp request sang Spring Boot tại: <code className="bg-slate-100 px-1 py-0.5 rounded text-blue-600 font-mono">http://localhost:8080</code>
            </p>
          </div>
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 text-white rounded-lg text-sm font-medium hover:bg-slate-800 transition"
          >
            <span>Khám phá API Specs</span>
            <ArrowRight className="w-4 h-4" />
          </a>
        </div>
      </main>

      {/* chân trang */}
      <footer className="border-t bg-white py-6 text-center text-sm text-slate-500">
        Hệ thống E-Learning © 2026. Xây dựng với React 18, Vite, TypeScript & Tailwind CSS.
      </footer>
    </div>
  );
}
