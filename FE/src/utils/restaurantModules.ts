import {
  UtensilsCrossed,
  LayoutGrid,
  ChefHat,
  Receipt,
  Users2,
  BarChart3,
  type LucideIcon,
} from 'lucide-react';

export interface RestaurantModule {
  slug: string;
  icon: LucideIcon;
  title: string;
  description: string;
  endpoint: string;
}

// danh sách các phân hệ nghiệp vụ theo SRS
export const RESTAURANT_MODULES: RestaurantModule[] = [
  {
    slug: 'tables',
    icon: LayoutGrid,
    title: 'Sơ đồ Bàn & Đặt bàn',
    description:
      'Quản lý danh sách bàn ăn, theo dõi trạng thái bàn (trống, đang dùng, đã đặt) và xử lý đặt bàn trực tuyến.',
    endpoint: '/tables/* & /reservations/*',
  },
  {
    slug: 'dishes',
    icon: UtensilsCrossed,
    title: 'Thực đơn & Món ăn',
    description:
      'Quản lý danh mục món ăn, công thức chế biến, cập nhật đơn giá, hình ảnh và trạng thái còn món hay hết món.',
    endpoint: '/dishes/* & /categories/*',
  },
  {
    slug: 'kitchen',
    icon: ChefHat,
    title: 'Gọi món & Điều phối Bếp',
    description:
      'Nhận order từ nhân viên phục vụ, chuyển phiếu tới màn hình bếp, theo dõi quy trình chế biến món ăn.',
    endpoint: '/orders/* & /kitchen/*',
  },
  {
    slug: 'invoices',
    icon: Receipt,
    title: 'Hóa đơn & Thanh toán',
    description:
      'Tổng hợp order theo bàn, áp dụng chiết khấu thành viên, xuất hóa đơn thanh toán đa phương thức (tiền mặt, thẻ, QR).',
    endpoint: '/invoices/* & /payments/*',
  },
  {
    slug: 'users',
    icon: Users2,
    title: 'Nhân sự & Khách hàng',
    description:
      'Quản lý hồ sơ nhân viên, phân quyền vai trò (Quản lý, Thu ngân, Bếp, Phục vụ) và quản lý hội viên khách hàng.',
    endpoint: '/users/* & /auth/*',
  },
  {
    slug: 'reports',
    icon: BarChart3,
    title: 'Báo cáo & Thống kê',
    description:
      'Báo cáo doanh số theo ca, theo ngày, thống kê các món ăn bán chạy nhất và hiệu suất phục vụ của nhà hàng.',
    endpoint: '/reports/* & /analytics/*',
  },
];
