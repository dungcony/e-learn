import { useState, useEffect } from 'react';
import { Calendar, Users, FileText, Clock, XCircle, User } from 'lucide-react';
import { myReservationApi, MyReservationResponse } from '@/api/myReservationApi';

export default function MyReservationsPage() {
  const [reservations, setReservations] = useState<MyReservationResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchReservations();
  }, []);

  const fetchReservations = async () => {
    try {
      const res = await myReservationApi.getMyReservations();
      setReservations(res.data.items);
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = async (id: string) => {
    if (!window.confirm('Bạn có chắc chắn muốn hủy đặt bàn này không?')) return;
    try {
      await myReservationApi.cancelMyReservation(id);
      fetchReservations();
    } catch (error: any) {
      alert(error.error?.message || 'Có lỗi xảy ra khi hủy đặt bàn');
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-blue-50 text-blue-700 border border-blue-200">Chờ xác nhận</span>;
      case 'CONFIRMED':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">Đã xác nhận</span>;
      case 'COMPLETED':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-slate-100 text-slate-700 border border-slate-200">Đã hoàn thành</span>;
      case 'CANCELLED':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-red-50 text-red-700 border border-red-200">Đã hủy</span>;
      default:
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-slate-100 text-slate-700">{status}</span>;
    }
  };

  if (isLoading) {
    return <div className="p-8 text-center text-slate-500">Đang tải danh sách đặt bàn...</div>;
  }

  return (
    <div className="w-full">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden mb-8">
        <div className="p-6 border-b border-slate-100">
          <h2 className="text-xl font-bold text-slate-900">Lịch sử đặt bàn</h2>
          <p className="text-sm text-slate-500 mt-1">Danh sách các bàn bạn đã đặt tại nhà hàng</p>
        </div>

        <div className="p-6">
          {reservations.length === 0 ? (
            <div className="text-center py-12">
              <Calendar className="size-12 text-slate-300 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-slate-900 mb-1">Chưa có lượt đặt bàn nào</h3>
              <p className="text-slate-500 text-sm">Bạn chưa từng đặt bàn tại nhà hàng chúng tôi.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {reservations.map(res => (
                <div key={res.id} className="border border-slate-200 rounded-xl p-4 sm:p-5 hover:border-amber-300 transition-colors bg-slate-50/50">
                  <div className="flex flex-col sm:flex-row justify-between items-start gap-4">
                    <div className="space-y-3 flex-1">
                      <div className="flex items-center gap-3">
                        <span className="font-mono font-bold text-amber-700 bg-amber-100 px-2 py-0.5 rounded text-sm">{res.code}</span>
                        {getStatusBadge(res.status)}
                      </div>
                      
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-y-2 gap-x-6 text-sm text-slate-700">
                        <div className="flex items-center gap-2">
                          <Clock className="size-4 text-slate-400" />
                          <span>{new Date(res.reserved_at).toLocaleString('vi-VN')}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Users className="size-4 text-slate-400" />
                          <span>{res.guest_count} người</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <User className="size-4 text-slate-400" />
                          <span>{res.guest_name} ({res.phone})</span>
                        </div>
                        {res.note && (
                          <div className="flex items-center gap-2 sm:col-span-2">
                            <FileText className="size-4 text-slate-400" />
                            <span className="italic text-slate-500">"{res.note}"</span>
                          </div>
                        )}
                      </div>
                    </div>
                    
                    {res.status === 'PENDING' && (
                      <button 
                        onClick={() => handleCancel(res.id)}
                        className="w-full sm:w-auto px-4 py-2 bg-white border border-red-200 text-red-600 hover:bg-red-50 hover:border-red-300 font-medium text-sm rounded-lg transition-colors flex items-center justify-center gap-1.5"
                      >
                        <XCircle className="size-4" />
                        Hủy đặt bàn
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
