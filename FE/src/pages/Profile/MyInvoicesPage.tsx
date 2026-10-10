import { useState, useEffect } from 'react';
import { ReceiptText } from 'lucide-react';
import { myInvoiceApi, MyInvoiceResponse } from '@/api/myInvoiceApi';

export default function MyInvoicesPage() {
  const [invoices, setInvoices] = useState<MyInvoiceResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchInvoices();
  }, []);

  const fetchInvoices = async () => {
    try {
      const res = await myInvoiceApi.getMyInvoices();
      setInvoices(res.data.items);
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    if (status === 'PAID') {
      return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">Đã thanh toán</span>;
    }
    return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-slate-100 text-slate-700">{status}</span>;
  };

  if (isLoading) {
    return <div className="p-8 text-center text-slate-500">Đang tải danh sách hóa đơn...</div>;
  }

  return (
    <div className="w-full">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden mb-8">
        <div className="p-6 border-b border-slate-100">
          <h2 className="text-xl font-bold text-slate-900">Lịch sử hóa đơn</h2>
          <p className="text-sm text-slate-500 mt-1">Danh sách các hóa đơn bạn đã thanh toán</p>
        </div>

        <div className="p-6">
          {invoices.length === 0 ? (
            <div className="text-center py-12">
              <ReceiptText className="size-12 text-slate-300 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-slate-900 mb-1">Chưa có hóa đơn nào</h3>
              <p className="text-slate-500 text-sm">Bạn chưa có lịch sử ăn uống tại nhà hàng.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="text-xs text-slate-500 uppercase bg-slate-50 border-y border-slate-200">
                  <tr>
                    <th className="px-4 py-3 font-semibold">Mã Hóa Đơn</th>
                    <th className="px-4 py-3 font-semibold">Ngày tạo</th>
                    <th className="px-4 py-3 font-semibold text-right">Tổng tiền</th>
                    <th className="px-4 py-3 font-semibold text-center">Trạng thái</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {invoices.map((inv) => (
                    <tr key={inv.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-4 py-3 font-mono font-medium text-slate-900">{inv.code}</td>
                      <td className="px-4 py-3 text-slate-600">{new Date(inv.created_at).toLocaleString('vi-VN')}</td>
                      <td className="px-4 py-3 text-right font-bold text-amber-700">
                        {inv.total_amount.toLocaleString('vi-VN')} đ
                      </td>
                      <td className="px-4 py-3 text-center">
                        {getStatusBadge(inv.status)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
