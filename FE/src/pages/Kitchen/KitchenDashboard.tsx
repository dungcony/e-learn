export default function KitchenDashboard() {
  return (
    <div>
      <h2 className="text-2xl font-bold text-orange-900 mb-6">Đơn Hàng Chờ Xử Lý</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Kanban Card Placeholder */}
        <div className="bg-white rounded-xl shadow-sm border-l-4 border-l-red-500 overflow-hidden">
          <div className="bg-slate-50 px-4 py-2 border-b border-slate-100 flex justify-between items-center">
            <span className="font-bold text-lg text-slate-800">Bàn 12</span>
            <span className="text-red-500 font-medium text-sm">15 phút trước</span>
          </div>
          <div className="p-4 flex flex-col gap-3">
            <div className="flex justify-between items-start">
              <span className="font-semibold text-slate-700">1x Cơm Niêu Bò Lúc Lắc</span>
            </div>
            <div className="flex justify-between items-start">
              <span className="font-semibold text-slate-700">2x Trà Đá</span>
            </div>
            <button className="mt-2 w-full bg-orange-100 text-orange-700 font-bold py-2 rounded-lg hover:bg-orange-200 transition-colors">
              Đánh dấu Xong
            </button>
          </div>
        </div>
        
        {/* Kanban Card Placeholder */}
        <div className="bg-white rounded-xl shadow-sm border-l-4 border-l-amber-500 overflow-hidden">
          <div className="bg-slate-50 px-4 py-2 border-b border-slate-100 flex justify-between items-center">
            <span className="font-bold text-lg text-slate-800">Bàn 04</span>
            <span className="text-amber-500 font-medium text-sm">5 phút trước</span>
          </div>
          <div className="p-4 flex flex-col gap-3">
            <div className="flex justify-between items-start">
              <span className="font-semibold text-slate-700">2x Phở Bò Tái Lăn</span>
              <span className="text-xs bg-slate-100 px-2 py-1 rounded text-slate-500">Ghi chú: Ko hành</span>
            </div>
            <button className="mt-2 w-full bg-orange-100 text-orange-700 font-bold py-2 rounded-lg hover:bg-orange-200 transition-colors">
              Đánh dấu Xong
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

