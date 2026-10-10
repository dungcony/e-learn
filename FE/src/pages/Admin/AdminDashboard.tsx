export default function AdminDashboard() {
  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-800 mb-4">Tổng quan hệ thống</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h3 className="text-slate-500 font-medium mb-2">Doanh thu hôm nay</h3>
          <p className="text-3xl font-bold text-slate-900">12.500.000 đ</p>
        </div>
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h3 className="text-slate-500 font-medium mb-2">Đơn hàng mới</h3>
          <p className="text-3xl font-bold text-amber-600">45</p>
        </div>
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h3 className="text-slate-500 font-medium mb-2">Khách hàng mới</h3>
          <p className="text-3xl font-bold text-blue-600">12</p>
        </div>
      </div>
    </div>
  );
}
