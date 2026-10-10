import { ArrowRight } from 'lucide-react';

export default function ServerStatus() {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000';

  return (
    // thông tin kết nối backend
    <div className="mt-12 bg-white rounded-xl border border-slate-200 p-6 flex flex-col sm:flex-row items-center justify-between gap-4">
      <div>
        <h4 className="font-medium text-slate-900">Trạng thái cấu hình máy chủ</h4>
        <p className="text-sm text-slate-500 text-pretty">
          Proxy Vite và client đã được cấu hình kết nối Spring Boot tại: <code className="bg-slate-100 px-1 py-0.5 rounded text-amber-600 font-mono">{apiBaseUrl}</code>
        </p>
      </div>
      <a
        href={`${apiBaseUrl}/swagger-ui.html`}
        target="_blank"
        rel="noreferrer"
        className="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 text-white rounded-lg text-sm font-medium hover:bg-slate-800 transition"
      >
        <span>Tài liệu Swagger API</span>
        <ArrowRight className="size-4" />
      </a>
    </div>
  );
}
