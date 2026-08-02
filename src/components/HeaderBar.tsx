import { Activity, FlaskConical } from "lucide-react";
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
    <header className="flex min-h-16 items-center justify-between border-b border-border bg-surface px-4 shadow-panel lg:px-6">
      <div>
        <h1 className="text-lg font-semibold text-ink">
          AI Chest X-ray Report Drafting Workspace
        </h1>
        <p className="text-sm text-slate-500">AI-generated draft · Human review required</p>
      </div>

      <div className="flex items-center gap-3">
        <div className="hidden items-center gap-2 rounded-md border border-border bg-muted px-3 py-2 text-sm text-slate-600 sm:flex">
          <Activity className="h-4 w-4" aria-hidden="true" />
          {statusLabel[backendStatus]}
        </div>
        <Badge className="gap-2 border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
          <FlaskConical className="h-4 w-4" aria-hidden="true" />
          Research use only
        </Badge>
      </div>
    </header>
  );
}
