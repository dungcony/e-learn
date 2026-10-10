import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  User,
  Mail,
  Phone,
  Calendar,
  Shield,
  KeyRound,
  CheckCircle2,
  AlertCircle,
  Save,
} from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { userApi } from '@/api/userApi';
import type { ErrorResponse, Gender } from '@/utils/apiTypes';

export default function ProfilePage() {
  const navigate = useNavigate();
  const { profile, isAuthenticated, isLoading: isAuthLoading, refreshProfile } = useAuth();

  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [gender, setGender] = useState<Gender>('OTHER');
  const [dateOfBirth, setDateOfBirth] = useState('');

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);
  const [profileSuccessMsg, setProfileSuccessMsg] = useState<string | null>(null);
  const [profileErrorMsg, setProfileErrorMsg] = useState<string | null>(null);

  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [pwdSuccessMsg, setPwdSuccessMsg] = useState<string | null>(null);
  const [pwdErrorMsg, setPwdErrorMsg] = useState<string | null>(null);

  // chuyển hướng đến trang đăng nhập nếu chưa xác thực
  useEffect(() => {
    if (!isAuthLoading && !isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthLoading, isAuthenticated, navigate]);

  // đồng bộ dữ liệu vào form khi có profile
  useEffect(() => {
    if (profile) {
      setFullName(profile.full_name || '');
      setPhone(profile.phone || '');
      setGender(profile.gender || 'OTHER');
      setDateOfBirth(profile.date_of_birth || '');
    }
  }, [profile]);

  // cập nhật thông tin cá nhân
  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setProfileSuccessMsg(null);
    setProfileErrorMsg(null);
    setIsUpdatingProfile(true);

    try {
      await userApi.updateProfile({
        full_name: fullName,
        phone: phone || undefined,
        gender,
        date_of_birth: dateOfBirth || undefined,
      });
      await refreshProfile();
      setProfileSuccessMsg('Cập nhật hồ sơ thành công!');
    } catch (err: unknown) {
      const errorData = err as ErrorResponse;
      if (errorData?.error?.fields && errorData.error.fields.length > 0) {
        const fieldDetails = errorData.error.fields.map((f) => f.message).join('. ');
        setProfileErrorMsg(fieldDetails);
      } else {
        setProfileErrorMsg(errorData?.error?.message || 'Không thể cập nhật hồ sơ cá nhân.');
      }
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  // cập nhật mật khẩu
  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setPwdSuccessMsg(null);
    setPwdErrorMsg(null);

    if (newPassword !== confirmPassword) {
      setPwdErrorMsg('Mật khẩu mới và xác nhận mật khẩu không khớp.');
      return;
    }

    setIsChangingPassword(true);
    try {
      await userApi.changePassword({
        old_password: currentPassword,
        new_password: newPassword,
        confirm_new_password: confirmPassword,
      });
      setPwdSuccessMsg('Đổi mật khẩu thành công!');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err: unknown) {
      const errorData = err as ErrorResponse;
      if (errorData?.error?.fields && errorData.error.fields.length > 0) {
        const fieldDetails = errorData.error.fields.map((f) => f.message).join('. ');
        setPwdErrorMsg(fieldDetails);
      } else {
        setPwdErrorMsg(errorData?.error?.message || 'Đổi mật khẩu thất bại. Vui lòng kiểm tra lại mật khẩu cũ.');
      }
    } finally {
      setIsChangingPassword(false);
    }
  };

  if (isAuthLoading || !profile) {
    return (
      <div className="flex-1 flex items-center justify-center py-20 text-slate-500">
        <span>Đang tải thông tin tài khoản...</span>
      </div>
    );
  }

  return (
    <div className="w-full">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden mb-8">
        <div className="p-6 sm:p-8 border-b border-slate-100 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="size-16 rounded-full bg-amber-100 text-amber-700 font-bold text-2xl flex items-center justify-center">
              {profile.full_name ? profile.full_name.charAt(0).toUpperCase() : 'U'}
            </div>
            <div>
              <h1 className="text-2xl font-bold text-slate-900 text-balance">{profile.full_name}</h1>
              <p className="text-sm text-slate-500 flex items-center gap-1.5 mt-0.5">
                <Mail className="size-3.5" />
                <span>{profile.email}</span>
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="px-3 py-1 text-xs font-semibold rounded-full bg-amber-100 text-amber-800 flex items-center gap-1">
              <Shield className="size-3.5" />
              <span>{profile.role}</span>
            </span>
            <span className="px-3 py-1 text-xs font-semibold rounded-full bg-emerald-100 text-emerald-800">
              {profile.status}
            </span>
          </div>
        </div>

        {/* cập nhật thông tin cá nhân */}
        <div className="p-6 sm:p-8 border-b border-slate-100">
          <h2 className="text-lg font-semibold text-slate-900 mb-4 flex items-center gap-2">
            <User className="size-5 text-amber-600" />
            <span>Thông tin cá nhân</span>
          </h2>

          {profileSuccessMsg && (
            <div className="mb-4 p-3 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm flex items-center gap-2">
              <CheckCircle2 className="size-4 shrink-0" />
              <span>{profileSuccessMsg}</span>
            </div>
          )}

          {profileErrorMsg && (
            <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm flex items-center gap-2">
              <AlertCircle className="size-4 shrink-0" />
              <span>{profileErrorMsg}</span>
            </div>
          )}

          <form onSubmit={handleUpdateProfile} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="p-name" className="block text-sm font-medium text-slate-700 mb-1">
                Họ và tên
              </label>
              <input
                id="p-name"
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-amber-500 text-sm"
              />
            </div>

            <div>
              <label htmlFor="p-phone" className="block text-sm font-medium text-slate-700 mb-1">
                Số điện thoại
              </label>
              <div className="relative">
                <Phone className="size-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  id="p-phone"
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="0912345678"
                  className="w-full pl-9 pr-3 py-2 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-amber-500 text-sm"
                />
              </div>
            </div>

            <div>
              <label htmlFor="p-gender" className="block text-sm font-medium text-slate-700 mb-1">
                Giới tính
              </label>
              <select
                id="p-gender"
                value={gender}
                onChange={(e) => setGender(e.target.value as Gender)}
                className="w-full px-3 py-2 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-amber-500 text-sm bg-white"
              >
                <option value="MALE">Nam</option>
                <option value="FEMALE">Nữ</option>
                <option value="OTHER">Khác</option>
              </select>
            </div>

            <div>
              <label htmlFor="p-dob" className="block text-sm font-medium text-slate-700 mb-1">
                Ngày sinh
              </label>
              <div className="relative">
                <Calendar className="size-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  id="p-dob"
                  type="date"
                  value={dateOfBirth}
                  onChange={(e) => setDateOfBirth(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-amber-500 text-sm"
                />
              </div>
            </div>

            <div className="sm:col-span-2 flex justify-end mt-2">
              <button
                type="submit"
                disabled={isUpdatingProfile}
                className="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white rounded-lg text-sm font-medium transition disabled:opacity-50 flex items-center gap-2"
              >
                <Save className="size-4" />
                <span>{isUpdatingProfile ? 'Đang lưu...' : 'Lưu thông tin'}</span>
              </button>
            </div>
          </form>
        </div>

        {/* đổi mật khẩu */}
        <div className="p-6 sm:p-8">
          <h2 className="text-lg font-semibold text-slate-900 mb-4 flex items-center gap-2">
            <KeyRound className="size-5 text-amber-600" />
            <span>Đổi mật khẩu</span>
          </h2>

          {pwdSuccessMsg && (
            <div className="mb-4 p-3 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm flex items-center gap-2">
              <CheckCircle2 className="size-4 shrink-0" />
              <span>{pwdSuccessMsg}</span>
            </div>
          )}

          {pwdErrorMsg && (
            <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm flex items-center gap-2">
              <AlertCircle className="size-4 shrink-0" />
              <span>{pwdErrorMsg}</span>
            </div>
          )}

          <form onSubmit={handleChangePassword} className="space-y-4 max-w-lg">
            <div>
              <label htmlFor="cur-pwd" className="block text-sm font-medium text-slate-700 mb-1">
                Mật khẩu hiện tại
              </label>
              <input
                id="cur-pwd"
                type="password"
                required
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-amber-500 text-sm"
              />
            </div>

            <div>
              <label htmlFor="new-pwd" className="block text-sm font-medium text-slate-700 mb-1">
                Mật khẩu mới
              </label>
              <input
                id="new-pwd"
                type="password"
                required
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-amber-500 text-sm"
              />
            </div>

            <div>
              <label htmlFor="conf-pwd" className="block text-sm font-medium text-slate-700 mb-1">
                Xác nhận mật khẩu mới
              </label>
              <input
                id="conf-pwd"
                type="password"
                required
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-amber-500 text-sm"
              />
            </div>

            <button
              type="submit"
              disabled={isChangingPassword}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-900 text-white rounded-lg text-sm font-medium transition disabled:opacity-50 flex items-center gap-2"
            >
              <KeyRound className="size-4" />
              <span>{isChangingPassword ? 'Đang đổi...' : 'Cập nhật mật khẩu'}</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

