import {
  BarChart3,
  BrainCircuit,
  FilePlus2,
  FileText,
  FolderOpen,
  Settings,
  Stethoscope,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
import type { ReactNode } from "react";
import { mockWorkspace } from "../data/mockWorkspace";
import { useReportGeneration } from "../hooks/useReportGeneration";
import { useReportWorkspace } from "../hooks/useReportWorkspace";
import { useXrayUploads } from "../hooks/useXrayUploads";
import { BottomInfoTabs } from "./BottomInfoTabs";
import { ExportSection } from "./export/ExportSection";
import { GenerationSection } from "./generation/GenerationSection";
import { appName, appTagline, HeaderBar } from "./HeaderBar";
import { ReportEditorSection } from "./report/ReportEditorSection";
import { UploadSection } from "./upload/UploadSection";

const navigationItems: Array<{
  href: string;
  icon: LucideIcon;
  label: string;
  active?: boolean;
}> = [
  { href: "#dashboard", icon: BarChart3, label: "Dashboard" },
  { href: "#new-analysis", icon: FilePlus2, label: "New Analysis", active: true },
  { href: "#case-records", icon: FolderOpen, label: "Case Records" },
  { href: "#reports", icon: FileText, label: "Reports" },
  { href: "#model-insights", icon: BrainCircuit, label: "Model Insights" },
  { href: "#settings", icon: Settings, label: "Settings" },
];

export function WorkspaceLayout() {
  const {
    errors,
    hasFrontalImage,
    removeFileForSlot,
    setFileForSlot,
    uploads,
  } = useXrayUploads();
  const generation = useReportGeneration();
  const reportWorkspace = useReportWorkspace(mockWorkspace.reportDraft);

  const handleGenerateDraft = () => {
    if (!hasFrontalImage || generation.isGenerating) {
      return;
    }

    generation.startGeneration();
    window.setTimeout(() => {
      document.getElementById("generation-panel")?.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }, 0);
  };

  return (
    <div className="min-h-screen bg-slate-100 text-ink md:flex">
      <SidebarNavigation />

      <div className="min-w-0 flex-1">
        <HeaderBar backendStatus={mockWorkspace.backendStatus} />
        <MobileNavigation />

        <main className="px-4 py-6 lg:px-8">
          <div className="mx-auto max-w-6xl space-y-8">
            <section
              className="rounded-2xl border border-slate-200 bg-white p-6 text-center shadow-panel md:p-8"
              id="new-analysis"
            >
              <div className="mx-auto max-w-4xl">
                <div>
                  <p className="text-sm font-semibold uppercase tracking-wide text-blue-700">
                    New Analysis
                  </p>
                  <h2 className="mt-2 text-3xl font-semibold leading-tight text-ink md:text-4xl">
                    Chest X-ray reporting workspace
                  </h2>
                  <p className="mx-auto mt-4 max-w-3xl text-base leading-7 text-slate-600">
                    Upload a study, generate a draft report, complete clinical
                    review, and export the final content from one workspace.
                  </p>
                </div>

                <div className="mt-6 grid gap-3 sm:grid-cols-3">
                  <StatusTile label="Image input" value="Frontal required" />
                  <StatusTile label="Draft status" value="Draft report" />
                  <StatusTile
                    label="Clinical review"
                    value="Clinical review pending"
                  />
                </div>
              </div>
            </section>

            <WorkSection
              description="Upload and review the X-ray inputs before generating a report draft."
              id="upload"
              title="Image Input"
            >
              <UploadSection
                errors={errors}
                onFileSelect={setFileForSlot}
                onRemove={removeFileForSlot}
                slots={mockWorkspace.xraySlots}
                uploads={uploads}
              />
            </WorkSection>

            <WorkSection
              description="Generate a draft report after the required frontal image is available."
              id="generation-panel"
              title="Draft Generation"
            >
              <GenerationSection
                canGenerateDraft={hasFrontalImage}
                currentStep={generation.currentStep}
                elapsedSeconds={generation.elapsedSeconds}
                errorMessage={generation.errorMessage}
                isGenerating={generation.isGenerating}
                onGenerate={handleGenerateDraft}
                onRetry={generation.retryGeneration}
                status={generation.status}
                steps={generation.steps}
              />
            </WorkSection>

            <WorkSection
              description="Compare the read-only draft with the editable clinical report."
              id="clinical-review"
              title="Clinical Review Workspace"
            >
              <ReportEditorSection
                aiDraft={reportWorkspace.aiDraft}
                onReviewStatusChange={reportWorkspace.setReviewStatus}
                reviewedReport={reportWorkspace.reviewedReport}
                reviewStatus={reportWorkspace.reviewStatus}
                setReviewedReport={reportWorkspace.setReviewedReport}
              />
            </WorkSection>

            <div className="grid gap-8 xl:grid-cols-[minmax(0,1fr)_420px]">
              <WorkSection
                description="Confirm the reviewed report content before exporting TXT or PDF."
                id="reports"
                title="Report Export"
              >
                <ExportSection
                  onExportPdf={reportWorkspace.exportReviewedReportAsPdf}
                  onExportTxt={reportWorkspace.exportReviewedReportAsTxt}
                  report={reportWorkspace.reviewedReport}
                  reviewStatus={reportWorkspace.reviewStatus}
                />
              </WorkSection>

              <WorkSection
                description="Review supporting terminology and similar mock cases."
                id="model-insights"
                title="Model Insights"
              >
                <BottomInfoTabs
                  knowledgeItems={mockWorkspace.knowledgeItems}
                  similarCases={mockWorkspace.similarCases}
                />
              </WorkSection>
            </div>
          </div>
        </main>

        <footer className="border-t border-slate-200 bg-white px-4 py-6 lg:px-8">
          <div className="mx-auto flex max-w-6xl flex-col gap-3 text-center text-sm text-slate-500 md:flex-row md:items-center md:justify-between md:text-left">
            <span className="font-semibold text-slate-700">{appName}</span>
            <span>
              For research and educational use only. Not intended for clinical
              diagnosis.
            </span>
          </div>
        </footer>
      </div>
    </div>
  );
}

function SidebarNavigation() {
  return (
    <aside className="hidden min-h-screen w-64 shrink-0 border-r border-slate-200 bg-white px-4 py-5 md:sticky md:top-0 md:flex md:flex-col">
      <div className="flex flex-col items-center gap-3 px-2 text-center">
        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-blue-700 text-white shadow-sm">
          <Stethoscope className="h-5 w-5" aria-hidden="true" />
        </div>
        <div className="min-w-0">
          <p className="truncate text-lg font-semibold text-ink">{appName}</p>
          <p className="truncate text-xs text-slate-500">{appTagline}</p>
        </div>
      </div>

      <nav className="mt-8 space-y-2">
        {navigationItems.map((item) => (
          <SidebarLink key={item.label} {...item} />
        ))}
      </nav>
    </aside>
  );
}

function MobileNavigation() {
  return (
    <nav className="border-b border-slate-200 bg-white px-4 py-3 md:hidden">
      <div className="flex gap-2 overflow-x-auto sm:justify-center">
        {navigationItems.map((item) => {
          const Icon = item.icon;

          return (
            <a
              className={`flex shrink-0 items-center gap-2 rounded-xl px-3 py-2 text-sm font-semibold ${
                item.active
                  ? "bg-blue-700 text-white"
                  : "bg-slate-50 text-slate-600"
              }`}
              href={item.href}
              key={item.label}
            >
              <Icon className="h-4 w-4" aria-hidden="true" />
              {item.label}
            </a>
          );
        })}
      </div>
    </nav>
  );
}

function SidebarLink({
  active = false,
  href,
  icon: Icon,
  label,
}: {
  active?: boolean;
  href: string;
  icon: LucideIcon;
  label: string;
}) {
  return (
    <a
      className={`flex items-center justify-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold transition-colors ${
        active
          ? "bg-blue-700 text-white shadow-sm"
          : "text-slate-600 hover:bg-blue-50 hover:text-blue-800"
      }`}
      href={href}
    >
      <Icon className="h-4 w-4" aria-hidden="true" />
      {label}
    </a>
  );
}

type WorkSectionProps = {
  children: ReactNode;
  description: string;
  id: string;
  title: string;
};

function WorkSection({ children, description, id, title }: WorkSectionProps) {
  return (
    <section className="scroll-mt-24" id={id}>
      <div className="mx-auto mb-4 max-w-3xl text-center">
        <h2 className="text-2xl font-semibold text-ink">{title}</h2>
        <p className="mt-2 text-sm leading-6 text-slate-600">
          {description}
        </p>
      </div>
      {children}
    </section>
  );
}

type StatusTileProps = {
  label: string;
  value: string;
};

function StatusTile({ label, value }: StatusTileProps) {
  return (
    <div className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
      <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
        {label}
      </p>
      <p className="mt-1 text-sm font-semibold text-slate-800">{value}</p>
    </div>
  );
}
