import { Activity, FlaskConical, LogOut } from "lucide-react";
import type { BackendStatus } from "../types/workspace";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";

type HeaderBarProps = {
  backendStatus: BackendStatus;
};

const statusLabel: Record<BackendStatus, string> = {
  placeholder: "Backend placeholder",
  connected: "Backend connected",
  offline: "Backend offline",
};

export const appName = "CXR Assist";
export const appTagline = "AI-Assisted Radiology Reporting";

export function HeaderBar({ backendStatus }: HeaderBarProps) {
  return (
    <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/95 px-4 shadow-panel backdrop-blur lg:px-8">
      <div className="grid min-h-20 grid-cols-1 items-center gap-3 py-3 md:grid-cols-[1fr_minmax(0,560px)_1fr] md:py-0">
        <div className="hidden items-center gap-3 md:flex">
          <div className="hidden items-center gap-2 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600 lg:flex">
            <Activity className="h-4 w-4" aria-hidden="true" />
            {statusLabel[backendStatus]}
          </div>
          <Badge className="hidden gap-2 border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800 xl:inline-flex">
            <FlaskConical className="h-4 w-4" aria-hidden="true" />
            Research Prototype
          </Badge>
        </div>

        <div className="min-w-0 text-center">
          <p className="text-xs font-semibold uppercase tracking-wide text-blue-700">
            New Analysis
          </p>
          <h1 className="truncate text-lg font-semibold text-ink md:text-xl">
            Create and review a chest X-ray report draft
          </h1>
        </div>

        <div className="flex shrink-0 items-center justify-center gap-3 md:justify-end">
          <div className="flex items-center gap-3 rounded-xl border border-slate-200 bg-white px-3 py-2 shadow-sm">
            <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-blue-700 text-sm font-semibold text-white">
              AC
            </div>
            <div className="hidden text-sm sm:block">
              <p className="font-semibold text-ink">Anni Chen</p>
              <p className="text-xs text-slate-500">Researcher</p>
            </div>
          </div>
          <Button className="hidden gap-2 px-3 md:inline-flex">
            <LogOut className="h-4 w-4" aria-hidden="true" />
            Logout
          </Button>
        </div>
      </div>
    </header>
  );
}
