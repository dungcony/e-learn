import { useState } from 'react';
import { Calendar, Users, Clock, CheckCircle2, AlertCircle, ChevronRight, Loader2 } from 'lucide-react';
import { reservationApi } from '@/api/reservationApi';

type Step = 'TIME' | 'INFO' | 'SUCCESS';

export default function ReservationPage() {
  const [step, setStep] = useState<Step>('TIME');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // Form State
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [guestCount, setGuestCount] = useState<number>(2);
  
  const [formData, setFormData] = useState({
    guest_name: '',
    phone: '',
    email: '',
    note: ''
  });

  const [reservationResult, setReservationResult] = useState<any>(null);

  const handleCheckAvailability = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!date || !time) {
      setError('Vui lòng chọn ngày và giờ');
      return;
    }

    // Convert local date and time to ISO 8601 (Giả định múi giờ hệ thống +07:00)
    // Thực tế cần xử lý timezone chuẩn
    const dateStr = `${date}T${time}:00+07:00`;
    const isoDate = new Date(dateStr).toISOString();

    setIsLoading(true);
    setError('');
    
    try {
      const response = await reservationApi.checkAvailability({
        reserved_at: isoDate,
        guest_count: guestCount
      });
      
      const data = response.data;
      if (data?.available) {
        setStep('INFO');
      } else {
        setError('Rất tiếc, khung giờ này đã hết bàn trống.');
      }
    } catch (err: any) {
      setError(err.error?.message || 'Có lỗi xảy ra khi kiểm tra bàn trống.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmitReservation = async (e: React.FormEvent) => {
    e.preventDefault();
    const dateStr = `${date}T${time}:00+07:00`;
    const isoDate = new Date(dateStr).toISOString();

    setIsLoading(true);
    setError('');

    try {
      const payload = {
        ...formData,
        reserved_at: isoDate,
        guest_count: guestCount
      };
      
      const response = await reservationApi.createReservation(payload);
      setReservationResult(response.data);
      setStep('SUCCESS');
    } catch (err: any) {
      setError(err.error?.message || 'Không thể tạo đơn đặt bàn. Vui lòng thử lại.');
    } finally {
      setIsLoading(false);
    }
  };

  // Get today's date in YYYY-MM-DD for min date attribute
  const today = new Date().toLocaleDateString('en-CA');

  return (
    <div className="bg-slate-50 min-h-screen py-12">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center mb-10">
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 mb-4">Đặt Bàn Trực Tuyến</h1>
          <p className="text-slate-600">Vui lòng điền thông tin để chúng tôi chuẩn bị tốt nhất cho bữa ăn của bạn.</p>
        </div>

        {/* Progress Bar */}
        <div className="flex items-center justify-center mb-10 text-sm font-medium">
          <div className={`flex items-center ${step === 'TIME' ? 'text-amber-600' : 'text-slate-800'}`}>
            <span className={`w-8 h-8 rounded-full flex items-center justify-center mr-2 border-2 ${step === 'TIME' ? 'border-amber-600 bg-amber-50' : 'border-amber-600 bg-amber-600 text-white'}`}>1</span>
            Thời gian
          </div>
          <div className={`w-12 h-0.5 mx-4 ${step === 'TIME' ? 'bg-slate-200' : 'bg-amber-600'}`} />
          <div className={`flex items-center ${step === 'INFO' ? 'text-amber-600' : (step === 'SUCCESS' ? 'text-slate-800' : 'text-slate-400')}`}>
            <span className={`w-8 h-8 rounded-full flex items-center justify-center mr-2 border-2 ${step === 'INFO' ? 'border-amber-600 bg-amber-50' : (step === 'SUCCESS' ? 'border-amber-600 bg-amber-600 text-white' : 'border-slate-200 bg-slate-50')}`}>2</span>
            Thông tin
          </div>
          <div className={`w-12 h-0.5 mx-4 ${step === 'SUCCESS' ? 'bg-amber-600' : 'bg-slate-200'}`} />
          <div className={`flex items-center ${step === 'SUCCESS' ? 'text-amber-600' : 'text-slate-400'}`}>
            <span className={`w-8 h-8 rounded-full flex items-center justify-center mr-2 border-2 ${step === 'SUCCESS' ? 'border-amber-600 bg-amber-50' : 'border-slate-200 bg-slate-50'}`}>3</span>
            Hoàn tất
          </div>
        </div>

        {/* Main Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          {error && (
            <div className="p-4 bg-red-50 border-b border-red-100 flex items-start gap-3">
              <AlertCircle className="size-5 text-red-500 mt-0.5" />
              <p className="text-red-700 text-sm">{error}</p>
            </div>
          )}

          <div className="p-6 sm:p-10">
            {step === 'TIME' && (
              <form onSubmit={handleCheckAvailability} className="space-y-6">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">Ngày đặt</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <Calendar className="size-5 text-slate-400" />
                      </div>
                      <input
                        type="date"
                        min={today}
                        required
                        value={date}
                        onChange={(e) => setDate(e.target.value)}
                        className="block w-full pl-10 pr-3 py-3 border border-slate-300 rounded-xl focus:ring-amber-500 focus:border-amber-500"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">Giờ đến</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <Clock className="size-5 text-slate-400" />
                      </div>
                      <input
                        type="time"
                        required
                        value={time}
                        onChange={(e) => setTime(e.target.value)}
                        className="block w-full pl-10 pr-3 py-3 border border-slate-300 rounded-xl focus:ring-amber-500 focus:border-amber-500"
                      />
                    </div>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-2">Số lượng khách</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <Users className="size-5 text-slate-400" />
                    </div>
                    <input
                      type="number"
                      min="1"
                      max="50"
                      required
                      value={guestCount}
                      onChange={(e) => setGuestCount(Number(e.target.value))}
                      className="block w-full pl-10 pr-3 py-3 border border-slate-300 rounded-xl focus:ring-amber-500 focus:border-amber-500"
                    />
                  </div>
                </div>

                <div className="pt-4">
                  <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full flex justify-center items-center py-3.5 px-4 border border-transparent rounded-xl shadow-sm text-lg font-bold text-white bg-amber-600 hover:bg-amber-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-amber-500 disabled:opacity-70 disabled:cursor-not-allowed transition"
                  >
                    {isLoading ? <Loader2 className="size-6 animate-spin" /> : 'Kiểm tra bàn trống'}
                  </button>
                </div>
              </form>
            )}

            {step === 'INFO' && (
              <form onSubmit={handleSubmitReservation} className="space-y-6">
                <div className="bg-amber-50 rounded-xl p-4 mb-6 border border-amber-100 flex items-center justify-between">
                  <div>
                    <p className="text-sm text-slate-600">Thời gian:</p>
                    <p className="font-bold text-slate-900">{time} - {date.split('-').reverse().join('/')}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm text-slate-600">Số lượng:</p>
                    <p className="font-bold text-slate-900">{guestCount} người</p>
                  </div>
                  <button type="button" onClick={() => setStep('TIME')} className="text-amber-700 text-sm font-medium hover:underline">
                    Thay đổi
                  </button>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">Họ và tên *</label>
                    <input
                      type="text"
                      name="guest_name"
                      required
                      value={formData.guest_name}
                      onChange={handleInputChange}
                      className="block w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-amber-500 focus:border-amber-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">Số điện thoại *</label>
                    <input
                      type="tel"
                      name="phone"
                      required
                      pattern="\d{10}"
                      title="Số điện thoại gồm 10 chữ số"
                      value={formData.phone}
                      onChange={handleInputChange}
                      className="block w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-amber-500 focus:border-amber-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-2">Email (Tùy chọn)</label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    className="block w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-amber-500 focus:border-amber-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-2">Ghi chú yêu cầu đặc biệt</label>
                  <textarea
                    name="note"
                    rows={3}
                    value={formData.note}
                    onChange={handleInputChange}
                    className="block w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-amber-500 focus:border-amber-500"
                    placeholder="Dị ứng đậu phộng, ăn chay, trang trí sinh nhật..."
                  />
                </div>

                <div className="pt-4 flex gap-4">
                  <button
                    type="button"
                    onClick={() => setStep('TIME')}
                    disabled={isLoading}
                    className="flex-1 py-3.5 px-4 border border-slate-300 rounded-xl shadow-sm text-lg font-bold text-slate-700 bg-white hover:bg-slate-50 transition"
                  >
                    Quay lại
                  </button>
                  <button
                    type="submit"
                    disabled={isLoading}
                    className="flex-[2] flex justify-center items-center py-3.5 px-4 border border-transparent rounded-xl shadow-sm text-lg font-bold text-white bg-amber-600 hover:bg-amber-700 disabled:opacity-70 disabled:cursor-not-allowed transition"
                  >
                    {isLoading ? <Loader2 className="size-6 animate-spin" /> : 'Xác nhận Đặt Bàn'}
                  </button>
                </div>
              </form>
            )}

            {step === 'SUCCESS' && (
              <div className="text-center py-8">
                <div className="mx-auto flex items-center justify-center size-20 rounded-full bg-green-100 mb-6">
                  <CheckCircle2 className="size-10 text-green-600" />
                </div>
                <h2 className="text-2xl font-bold text-slate-900 mb-2">Đặt bàn thành công!</h2>
                <p className="text-slate-600 mb-8 max-w-md mx-auto">
                  Cảm ơn bạn, <span className="font-bold text-slate-800">{formData.guest_name}</span>. Yêu cầu đặt bàn của bạn đã được ghi nhận. Chúng tôi sẽ liên hệ sớm nhất để xác nhận.
                </p>
                
                <div className="bg-slate-50 rounded-xl p-6 mb-8 text-left max-w-sm mx-auto border border-slate-200">
                  <p className="text-sm text-slate-500 mb-1">Mã đơn đặt bàn</p>
                  <p className="text-lg font-bold text-amber-700 font-mono mb-4">{reservationResult?.code || 'WAITING'}</p>
                  
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="text-slate-500">Thời gian:</p>
                      <p className="font-semibold text-slate-900">{time}</p>
                    </div>
                    <div>
                      <p className="text-slate-500">Ngày:</p>
                      <p className="font-semibold text-slate-900">{date.split('-').reverse().join('/')}</p>
                    </div>
                    <div>
                      <p className="text-slate-500">Số khách:</p>
                      <p className="font-semibold text-slate-900">{guestCount} người</p>
                    </div>
                    <div>
                      <p className="text-slate-500">Số ĐT:</p>
                      <p className="font-semibold text-slate-900">{formData.phone}</p>
                    </div>
                  </div>
                </div>
                
                <button
                  onClick={() => window.location.href = '/'}
                  className="inline-flex items-center text-amber-600 font-bold hover:text-amber-700"
                >
                  Trở về Trang chủ <ChevronRight className="size-5 ml-1" />
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
