import { useState } from 'react';
import { Search, Plus, Edit2, Trash2 } from 'lucide-react';

export default function AdminTablesPage() {
  const [tables] = useState([
    { id: 1, name: 'Bàn 01', capacity: 4, area: 'Tầng 1', status: 'AVAILABLE' },
    { id: 2, name: 'Bàn 02', capacity: 2, area: 'Tầng 1', status: 'AVAILABLE' },
    { id: 3, name: 'Bàn 11', capacity: 6, area: 'Tầng 2', status: 'OCCUPIED' },
    { id: 4, name: 'VIP 1', capacity: 10, area: 'Phòng VIP', status: 'RESERVED' },
  ]);

  const [searchTerm, setSearchTerm] = useState('');

  const filteredTables = tables.filter(table => 
    table.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
      <div className="p-6 border-b border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-800">Quản lý Bàn</h2>
          <p className="text-sm text-slate-500 mt-1">Cấu hình số lượng bàn theo khu vực</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
            <input
              type="text"
              placeholder="Tìm bàn..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-9 pr-4 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500 w-full sm:w-64"
            />
          </div>
          <button className="flex items-center gap-2 px-4 py-2 bg-amber-600 text-white rounded-lg text-sm font-medium hover:bg-amber-700 transition-colors">
            <Plus className="h-4 w-4" />
            Thêm bàn
          </button>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm text-slate-600">
          <thead className="bg-slate-50 text-slate-700 border-b border-slate-200">
            <tr>
              <th className="px-6 py-4 font-semibold">Tên bàn</th>
              <th className="px-6 py-4 font-semibold">Sức chứa</th>
              <th className="px-6 py-4 font-semibold">Khu vực</th>
              <th className="px-6 py-4 font-semibold text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {filteredTables.length > 0 ? (
              filteredTables.map((table) => (
                <tr key={table.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-6 py-4 font-medium text-slate-900">{table.name}</td>
                  <td className="px-6 py-4">{table.capacity} người</td>
                  <td className="px-6 py-4">{table.area}</td>
                  <td className="px-6 py-4 text-right">
                    <button className="p-2 text-slate-400 hover:text-amber-600 transition-colors rounded-lg hover:bg-amber-50 mr-1" title="Chỉnh sửa">
                      <Edit2 className="h-4 w-4" />
                    </button>
                    <button className="p-2 text-slate-400 hover:text-red-600 transition-colors rounded-lg hover:bg-red-50" title="Xóa">
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={4} className="px-6 py-12 text-center text-slate-500">
                  Không tìm thấy bàn nào.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
