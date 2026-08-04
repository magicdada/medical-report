import { Activity, FlaskConical, Stethoscope } from "lucide-react";
import type { BackendStatus } from "../types/workspace";
import { Badge } from "./ui/badge";

type HeaderBarProps = {
  backendStatus: BackendStatus;
};

const statusLabel: Record<BackendStatus, string> = {
  placeholder: "Backend placeholder",
  connected: "Backend connected",
  offline: "Backend offline",
};

export function HeaderBar({ backendStatus }: HeaderBarProps) {
  return (
    <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/95 px-4 shadow-panel backdrop-blur lg:px-6">
      <div className="mx-auto grid min-h-16 max-w-7xl grid-cols-[minmax(0,1fr)_auto] items-center gap-4 2xl:grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)]">
        <div className="flex min-w-0 items-center gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-blue-700 text-white shadow-sm">
            <Stethoscope className="h-5 w-5" aria-hidden="true" />
          </div>
          <div className="min-w-0">
            <h1 className="truncate text-base font-semibold text-ink md:text-lg">
              AI Chest X-ray Report Drafting Workspace
            </h1>
            <p className="truncate text-sm text-slate-500">
              AI-generated draft - Human review required
            </p>
          </div>
        </div>

        <nav className="hidden items-center gap-4 justify-self-center text-sm font-medium text-slate-500 2xl:flex">
          <a className="hover:text-blue-700" href="#upload">
            Upload
          </a>
          <a className="hover:text-blue-700" href="#generation">
            Generate
          </a>
          <a className="hover:text-blue-700" href="#findings">
            Findings
          </a>
          <a className="hover:text-blue-700" href="#impression">
            Impression
          </a>
          <a className="hover:text-blue-700" href="#workspace">
            Review
          </a>
          <a className="hover:text-blue-700" href="#export">
            Export
          </a>
          <a className="hover:text-blue-700" href="#workflow">
            Workflow
          </a>
        </nav>

        <div className="flex shrink-0 items-center justify-end gap-3">
          <div className="hidden items-center gap-2 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600 sm:flex">
            <Activity className="h-4 w-4" aria-hidden="true" />
            {statusLabel[backendStatus]}
          </div>
          <Badge className="gap-2 border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
            <FlaskConical className="h-4 w-4" aria-hidden="true" />
            Research use only
          </Badge>
        </div>
      </div>
    </header>
  );
}
