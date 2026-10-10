import { Link } from 'react-router-dom';
import { ArrowRight, type LucideIcon } from 'lucide-react';

interface ModuleCardProps {
  slug: string;
  icon: LucideIcon;
  title: string;
  description: string;
  endpoint: string;
}

export default function ModuleCard({ slug, icon: Icon, title, description, endpoint }: ModuleCardProps) {
  return (
    <Link
      to={`/modules/${slug}`}
      className="group bg-white p-6 rounded-xl border border-slate-200 shadow-sm hover:shadow-md hover:border-amber-300 transition flex flex-col justify-between"
    >
      <div>
        <div className="size-12 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center mb-4 group-hover:bg-amber-100 transition">
          <Icon className="size-6" />
        </div>
        <h3 className="font-semibold text-lg text-slate-900 group-hover:text-amber-700 transition flex items-center justify-between">
          <span>{title}</span>
          <ArrowRight className="size-4 text-slate-400 opacity-0 -translate-x-1 group-hover:opacity-100 group-hover:translate-x-0 transition" />
        </h3>
        <p className="text-sm text-slate-500 mt-2 text-pretty">{description}</p>
      </div>

      <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-between text-xs font-medium text-slate-500">
        <span className="font-mono text-amber-600 font-semibold">{endpoint}</span>
        <span className="text-amber-600 font-medium group-hover:underline">Chi tiết &rarr;</span>
      </div>
    </Link>
  );
}
