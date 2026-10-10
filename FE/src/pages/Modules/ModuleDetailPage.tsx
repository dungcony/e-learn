import { useParams, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Sparkles,
  Layers,
  Info,
  Clock,
  CheckCircle2,
  TrendingUp,
  DollarSign,
  ShoppingBag,
  Users,
  CreditCard,
  Shield,
  UserCheck,
} from 'lucide-react';
import { RESTAURANT_MODULES } from '@/utils/restaurantModules';
import { useAuth } from '@/context/AuthContext';
import TableManagement from '@/pages/Tables/TableManagement';

export default function ModuleDetailPage() {
  const { slug } = useParams<{ slug: string }>();
  const { user } = useAuth();
  const currentModule = RESTAURANT_MODULES.find((m) => m.slug === slug);

  if (!currentModule) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center">
        <h2 className="text-2xl font-bold text-slate-800 text-balance">Không tìm thấy phân hệ này</h2>
        <p className="text-sm text-slate-500 mt-2 text-pretty">
          Đường dẫn không hợp lệ hoặc phân hệ chưa được đăng ký trong hệ thống.
        </p>
        <Link
          to="/"
          className="inline-flex items-center gap-2 mt-6 px-4 py-2 bg-amber-600 text-white rounded-lg text-sm font-medium hover:bg-amber-700 transition"
        >
          <ArrowLeft className="size-4" />
          <span>Về trang chủ</span>
        </Link>
      </div>
    );
  }

  const Icon = currentModule.icon;

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 w-full">
      <Link
        to="/"
        className="inline-flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-800 mb-6 transition"
      >
        <ArrowLeft className="size-4" />
        <span>Về Dashboard</span>
      </Link>

      {/* tiêu đề phân hệ */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 sm:p-8 mb-8">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="size-14 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center font-bold">
              <Icon className="size-7" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-2xl font-bold text-slate-900 text-balance">{currentModule.title}</h1>
                <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800">
                  SRS v1.0
                </span>
              </div>
              <p className="text-sm text-slate-600 mt-1 text-pretty">{currentModule.description}</p>
            </div>
          </div>

          <div className="text-right">
            <span className="text-xs font-medium text-slate-400 block">Dải API Backend:</span>
            <code className="text-xs font-mono font-semibold text-amber-700 bg-amber-50 px-2 py-1 rounded">
              {currentModule.endpoint}
            </code>
          </div>
        </div>
      </div>

      {/* thông báo trạng thái tích hợp */}
      <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 mb-8 flex items-start gap-3">
        <Info className="size-5 text-amber-700 shrink-0 mt-0.5" />
        <div className="text-sm text-amber-900">
          <p className="font-semibold text-balance">Khu vực điều phối nghiệp vụ {currentModule.title}</p>
          <p className="mt-1 text-amber-800 text-pretty">
            Backend Spring Boot hiện đã hoàn tất module Xác thực & Tài khoản (`/auth/*`, `/users/me`). Các bảng CSDL và REST API cho phân hệ {currentModule.title} đang được cấu hình theo tài liệu đặc tả SRS. Dưới đây là giao diện điều hành tương tác mẫu:
          </p>
        </div>
      </div>

      {/* mô phỏng giao diện nghiệp vụ theo từng slug */}
      {slug === 'tables' && (
        <>
          {user?.role === 'CUSTOMER' ? (
            <div className="bg-white rounded-2xl border border-slate-200 p-8 text-center shadow-sm">
              <div className="size-16 bg-amber-50 text-amber-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <Layers className="size-8" />
              </div>
              <h3 className="text-xl font-bold text-slate-800 mb-2">Lịch sử & Đặt bàn Trực tuyến</h3>
              <p className="text-slate-500 mb-6 max-w-md mx-auto">
                Chức năng quản lý đặt bàn dành cho khách hàng đang được phát triển và sẽ kết nối trong bản cập nhật tới.
              </p>
            </div>
          ) : (
            <TableManagement />
          )}
        </>
      )}

      {slug === 'dishes' && (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <h3 className="font-semibold text-lg text-slate-900 mb-4 flex items-center gap-2">
            <Sparkles className="size-5 text-amber-600" />
            <span>Danh mục Thực đơn Hiện hữu</span>
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {[
              { name: 'Cơm Niêu Cá Kho Tộ', category: 'Món chính', price: '125.000 đ', available: true },
              { name: 'Canh Cua Rau Đay', category: 'Canh / Súp', price: '75.000 đ', available: true },
              { name: 'Gỏi Cuốn Tôm Thịt (4 cuốn)', category: 'Khai vị', price: '65.000 đ', available: true },
              { name: 'Bò Lúc Lắc Khoai Tây', category: 'Món chính', price: '165.000 đ', available: true },
              { name: 'Chè Hạt Sen Long Nhãn', category: 'Tráng miệng', price: '45.000 đ', available: true },
              { name: 'Trà Hoa Cúc Mật Ong', category: 'Đồ uống', price: '35.000 đ', available: false },
            ].map((dish) => (
              <div key={dish.name} className="p-4 rounded-xl border border-slate-200 hover:shadow-sm transition">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-slate-100 text-slate-700">
                    {dish.category}
                  </span>
                  <span
                    className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
                      dish.available ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700'
                    }`}
                  >
                    {dish.available ? 'Còn món' : 'Tạm hết'}
                  </span>
                </div>
                <h4 className="font-semibold text-slate-900 text-sm">{dish.name}</h4>
                <p className="text-amber-600 font-bold text-sm mt-2 tabular-nums">{dish.price}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {slug === 'kitchen' && (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <h3 className="font-semibold text-lg text-slate-900 mb-4 flex items-center gap-2">
            <Clock className="size-5 text-amber-600" />
            <span>Hàng đợi Chế biến (Màn hình Bếp / KDS)</span>
          </h3>
          <div className="space-y-3">
            {[
              { table: 'Bàn B01', dish: '2x Cơm Niêu Cá Kho Tộ', time: '5 phút trước', status: 'Đang nấu', badge: 'bg-amber-100 text-amber-800' },
              { table: 'Bàn B03', dish: '1x Canh Cua Rau Đay', time: '8 phút trước', status: 'Chờ chế biến', badge: 'bg-blue-100 text-blue-800' },
              { table: 'VIP1', dish: '1x Bò Lúc Lắc Khoai Tây', time: '1 phút trước', status: 'Chờ chế biến', badge: 'bg-blue-100 text-blue-800' },
              { table: 'Bàn B01', dish: '1x Gỏi Cuốn Tôm Thịt', time: '12 phút trước', status: 'Đã hoàn thành', badge: 'bg-emerald-100 text-emerald-800' },
            ].map((order, idx) => (
              <div key={idx} className="flex items-center justify-between p-3.5 rounded-xl border border-slate-200 bg-slate-50/50">
                <div className="flex items-center gap-3">
                  <span className="font-bold text-sm text-slate-900 bg-white border px-2 py-1 rounded-md">
                    {order.table}
                  </span>
                  <div>
                    <h5 className="font-medium text-sm text-slate-800">{order.dish}</h5>
                    <span className="text-xs text-slate-400">{order.time}</span>
                  </div>
                </div>
                <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${order.badge}`}>
                  {order.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {slug === 'invoices' && (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <h3 className="font-semibold text-lg text-slate-900 mb-4 flex items-center gap-2">
            <CheckCircle2 className="size-5 text-amber-600" />
            <span>Quản lý Hóa đơn & Bán hàng (Thu ngân)</span>
          </h3>
          <div className="border border-slate-200 rounded-xl overflow-hidden">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-3 text-left font-semibold text-slate-600">Số HĐ</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-600">Bàn</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-600">Thời gian</th>
                  <th className="px-4 py-3 text-right font-semibold text-slate-600">Tổng tiền</th>
                  <th className="px-4 py-3 text-center font-semibold text-slate-600">Trạng thái</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {[
                  { id: 'HD-20261010-01', table: 'B02', time: '18:45', total: '420.000 đ', status: 'Đã thanh toán (QR)' },
                  { id: 'HD-20261010-02', table: 'B04', time: '19:10', total: '295.000 đ', status: 'Đã thanh toán (Tiền mặt)' },
                  { id: 'HD-20261010-03', table: 'B01', time: '19:25', total: '650.000 đ', status: 'Đang mở (Chưa thanh toán)' },
                ].map((inv) => (
                  <tr key={inv.id} className="hover:bg-slate-50/50">
                    <td className="px-4 py-3 font-mono font-medium text-slate-800">{inv.id}</td>
                    <td className="px-4 py-3 font-semibold text-slate-800">{inv.table}</td>
                    <td className="px-4 py-3 text-slate-500">{inv.time}</td>
                    <td className="px-4 py-3 text-right font-bold text-amber-700 tabular-nums">{inv.total}</td>
                    <td className="px-4 py-3 text-center">
                      <span className="text-xs px-2 py-0.5 rounded-full bg-slate-100 text-slate-700 font-medium">
                        {inv.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {slug === 'reports' && (
        <div className="space-y-6">
          {/* thông báo quyền hạn nếu là customer */}
          {user?.role !== 'MANAGER' && (
            <div className="p-3.5 bg-blue-50 border border-blue-200 rounded-xl flex items-center justify-between text-xs text-blue-800">
              <div className="flex items-center gap-2">
                <Shield className="size-4 text-blue-600 shrink-0" />
                <span>
                  Bạn đang xem phân hệ Báo cáo ở <strong>Chế độ xem trước (Preview Mode)</strong>.
                </span>
              </div>
              <span className="font-semibold uppercase px-2 py-0.5 rounded bg-blue-100 text-blue-700">
                Preview Mode
              </span>
            </div>
          )}

          {/* thẻ chỉ số kpi */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-slate-500">Doanh thu hôm nay</span>
                <div className="size-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
                  <DollarSign className="size-4" />
                </div>
              </div>
              <h4 className="text-2xl font-bold text-slate-900 mt-2 tabular-nums">18.450.000 đ</h4>
              <span className="text-xs font-semibold text-emerald-600 mt-2 flex items-center gap-1">
                <TrendingUp className="size-3.5" />
                <span>+12.5% so với hôm qua</span>
              </span>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-slate-500">Số đơn hoàn thành</span>
                <div className="size-8 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center">
                  <ShoppingBag className="size-4" />
                </div>
              </div>
              <h4 className="text-2xl font-bold text-slate-900 mt-2 tabular-nums">68 đơn</h4>
              <span className="text-xs font-medium text-slate-400 mt-2 block">100% đã quyết toán</span>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-slate-500">Lượng khách phục vụ</span>
                <div className="size-8 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center">
                  <Users className="size-4" />
                </div>
              </div>
              <h4 className="text-2xl font-bold text-slate-900 mt-2 tabular-nums">142 khách</h4>
              <span className="text-xs font-semibold text-purple-600 mt-2 block">Cao điểm: 19h - 20h</span>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-slate-500">Giá trị TB / bàn</span>
                <div className="size-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
                  <CreditCard className="size-4" />
                </div>
              </div>
              <h4 className="text-2xl font-bold text-slate-900 mt-2 tabular-nums">271.000 đ</h4>
              <span className="text-xs font-medium text-slate-400 mt-2 block">~ 4 người / bàn</span>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* biểu đồ phân bổ khung giờ */}
            <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
              <h4 className="font-semibold text-base text-slate-900 mb-4">
                Phân bổ Doanh thu theo Khung giờ
              </h4>
              <div className="space-y-3">
                {[
                  { time: '11:00 - 12:00 (Trưa)', amount: '3.200.000 đ', percent: 45 },
                  { time: '12:00 - 13:00 (Trưa cao điểm)', amount: '5.800.000 đ', percent: 80 },
                  { time: '18:00 - 19:00 (Tối)', amount: '4.100.000 đ', percent: 60 },
                  { time: '19:00 - 20:00 (Tối cao điểm)', amount: '7.150.000 đ', percent: 95 },
                  { time: '20:00 - 21:00 (Tối muộn)', amount: '2.200.000 đ', percent: 35 },
                ].map((item) => (
                  <div key={item.time}>
                    <div className="flex justify-between text-xs font-medium text-slate-700 mb-1">
                      <span>{item.time}</span>
                      <span className="tabular-nums font-bold text-slate-900">{item.amount}</span>
                    </div>
                    <div className="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-amber-500 rounded-full transition-all duration-500"
                        style={{ width: `${item.percent}%` }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* tỷ lệ thanh toán */}
            <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm flex flex-col justify-between">
              <div>
                <h4 className="font-semibold text-base text-slate-900 mb-4">
                  Cơ cấu Phương thức Thanh toán
                </h4>
                <div className="space-y-4">
                  <div>
                    <div className="flex justify-between text-xs mb-1">
                      <span className="text-slate-600 font-medium">Chuyển khoản QR (VietQR)</span>
                      <span className="font-bold text-slate-900 tabular-nums">55% (10.150.000 đ)</span>
                    </div>
                    <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                      <div className="h-full bg-emerald-500 rounded-full" style={{ width: '55%' }} />
                    </div>
                  </div>
                  <div>
                    <div className="flex justify-between text-xs mb-1">
                      <span className="text-slate-600 font-medium">Tiền mặt</span>
                      <span className="font-bold text-slate-900 tabular-nums">35% (6.450.000 đ)</span>
                    </div>
                    <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                      <div className="h-full bg-amber-500 rounded-full" style={{ width: '35%' }} />
                    </div>
                  </div>
                  <div>
                    <div className="flex justify-between text-xs mb-1">
                      <span className="text-slate-600 font-medium">Thẻ tín dụng / POS</span>
                      <span className="font-bold text-slate-900 tabular-nums">10% (1.850.000 đ)</span>
                    </div>
                    <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                      <div className="h-full bg-blue-500 rounded-full" style={{ width: '10%' }} />
                    </div>
                  </div>
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-100 text-xs text-slate-500">
                Thống kê được cập nhật theo thời gian thực từ các giao dịch chốt ca.
              </div>
            </div>
          </div>

          {/* top 5 món bán chạy */}
          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
            <h4 className="font-semibold text-base text-slate-900 mb-4">
              Top 5 Món Ăn Bán Chạy Nhất Trong Ngày
            </h4>
            <div className="border border-slate-200 rounded-xl overflow-hidden">
              <table className="min-w-full divide-y divide-slate-200 text-sm">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-4 py-3 text-left font-semibold text-slate-600">Thứ hạng</th>
                    <th className="px-4 py-3 text-left font-semibold text-slate-600">Tên món ăn</th>
                    <th className="px-4 py-3 text-center font-semibold text-slate-600">Số lượng bán</th>
                    <th className="px-4 py-3 text-right font-semibold text-slate-600">Doanh số đóng góp</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {[
                    { rank: '#1', name: 'Cơm Niêu Cá Kho Tộ', count: '142 đĩa', total: '17.750.000 đ' },
                    { rank: '#2', name: 'Bò Lúc Lắc Khoai Tây', count: '88 đĩa', total: '14.520.000 đ' },
                    { rank: '#3', name: 'Canh Cua Rau Đay', count: '110 tô', total: '8.250.000 đ' },
                    { rank: '#4', name: 'Gỏi Cuốn Tôm Thịt (4 cuốn)', count: '95 phần', total: '6.175.000 đ' },
                    { rank: '#5', name: 'Chè Hạt Sen Long Nhãn', count: '74 bát', total: '3.330.000 đ' },
                  ].map((dish) => (
                    <tr key={dish.rank} className="hover:bg-slate-50/50">
                      <td className="px-4 py-3 font-bold text-amber-700">{dish.rank}</td>
                      <td className="px-4 py-3 font-medium text-slate-800">{dish.name}</td>
                      <td className="px-4 py-3 text-center text-slate-600 tabular-nums">{dish.count}</td>
                      <td className="px-4 py-3 text-right font-bold text-slate-900 tabular-nums">{dish.total}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {slug === 'users' && (
        <div className="space-y-6">
          {/* thông báo quyền hạn nếu là customer */}
          {user?.role !== 'MANAGER' && (
            <div className="p-3.5 bg-blue-50 border border-blue-200 rounded-xl flex items-center justify-between text-xs text-blue-800">
              <div className="flex items-center gap-2">
                <Shield className="size-4 text-blue-600 shrink-0" />
                <span>
                  Bạn đang xem phân hệ Nhân sự & Khách hàng ở <strong>Chế độ xem trước (Preview Mode)</strong>.
                </span>
              </div>
              <span className="font-semibold uppercase px-2 py-0.5 rounded bg-blue-100 text-blue-700">
                Preview Mode
              </span>
            </div>
          )}

          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6">
              <div>
                <h3 className="font-semibold text-lg text-slate-900 flex items-center gap-2">
                  <UserCheck className="size-5 text-amber-600" />
                  <span>Danh sách Nhân sự & Hội viên Nhà hàng</span>
                </h3>
                <p className="text-xs text-slate-500 mt-1">
                  Quản lý quyền hạn và trạng thái hoạt động của nhân sự và khách hàng.
                </p>
              </div>

              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold px-3 py-1 rounded-full bg-slate-100 text-slate-700">
                  Tổng: 5 tài khoản mẫu
                </span>
              </div>
            </div>

            <div className="border border-slate-200 rounded-xl overflow-hidden">
              <table className="min-w-full divide-y divide-slate-200 text-sm">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-4 py-3 text-left font-semibold text-slate-600">Họ và tên</th>
                    <th className="px-4 py-3 text-left font-semibold text-slate-600">Email</th>
                    <th className="px-4 py-3 text-center font-semibold text-slate-600">Vai trò</th>
                    <th className="px-4 py-3 text-center font-semibold text-slate-600">Trạng thái</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {[
                    { name: 'Nguyễn Văn Quản', email: 'manager@restaurant.vn', role: 'MANAGER', badge: 'bg-amber-100 text-amber-800', status: 'Hoạt động' },
                    { name: 'Trần Thị Thu Ngân', email: 'cashier@restaurant.vn', role: 'CASHIER', badge: 'bg-blue-100 text-blue-800', status: 'Hoạt động' },
                    { name: 'Lê Bếp Trưởng', email: 'chef@restaurant.vn', role: 'CHEF', badge: 'bg-emerald-100 text-emerald-800', status: 'Hoạt động' },
                    { name: 'Phạm Phục Vụ', email: 'waiter@restaurant.vn', role: 'WAITER', badge: 'bg-purple-100 text-purple-800', status: 'Hoạt động' },
                    { name: user?.full_name || 'Khách hàng', email: user?.email || 'customer@restaurant.vn', role: 'CUSTOMER', badge: 'bg-slate-100 text-slate-800', status: 'Hoạt động' },
                  ].map((item) => (
                    <tr key={item.email} className="hover:bg-slate-50/50">
                      <td className="px-4 py-3 font-medium text-slate-900">{item.name}</td>
                      <td className="px-4 py-3 text-slate-600">{item.email}</td>
                      <td className="px-4 py-3 text-center">
                        <span className={`text-xs px-2.5 py-1 rounded-full font-semibold ${item.badge}`}>
                          {item.role}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-center">
                        <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 font-medium">
                          {item.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
