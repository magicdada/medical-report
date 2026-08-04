import type { ReactNode } from "react";
import { mockWorkspace } from "../data/mockWorkspace";
import { useReportGeneration } from "../hooks/useReportGeneration";
import { useReportWorkspace } from "../hooks/useReportWorkspace";
import { useXrayUploads } from "../hooks/useXrayUploads";
import { BottomInfoTabs } from "./BottomInfoTabs";
import { ExportSection } from "./export/ExportSection";
import { GenerationSection } from "./generation/GenerationSection";
import { HeaderBar } from "./HeaderBar";
import { FindingsSection } from "./report/FindingsSection";
import { ImpressionSection } from "./report/ImpressionSection";
import { ReportEditorSection } from "./report/ReportEditorSection";
import { UploadSection } from "./upload/UploadSection";

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
      document.getElementById("generation")?.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }, 0);
  };

  return (
    <div className="min-h-screen scroll-smooth bg-slate-50 text-ink">
      <HeaderBar backendStatus={mockWorkspace.backendStatus} />

      <main className="snap-y snap-proximity max-md:snap-none">
        <SectionContainer
          eyebrow="AI-assisted radiology workspace"
          id="overview"
          title="AI-assisted Chest X-ray Reporting"
        >
          <div className="grid items-center gap-10 lg:grid-cols-[minmax(0,1fr)_420px]">
            <div>
              <p className="max-w-2xl text-lg leading-8 text-slate-600">
                Generate structured chest X-ray report drafts, review medical
                findings, and edit results in one focused workspace.
              </p>
              <p className="mt-5 max-w-2xl rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm leading-6 text-amber-800">
                AI-generated content must be reviewed by a qualified healthcare
                professional.
              </p>
              <div className="mt-8 flex flex-wrap gap-3">
                <a
                  className="inline-flex items-center justify-center rounded-xl bg-blue-700 px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
                  href="#upload"
                >
                  Start analysis
                </a>
                <a
                  className="inline-flex items-center justify-center rounded-xl border border-slate-300 bg-white px-5 py-3 text-sm font-semibold text-slate-700 shadow-sm transition hover:border-blue-200 hover:bg-blue-50 hover:text-blue-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
                  href="#workflow"
                >
                  View workflow
                </a>
              </div>
            </div>

            <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-panel">
              <div className="rounded-2xl bg-slate-950 p-4">
                <div className="aspect-[4/3] rounded-xl border border-slate-800 bg-[radial-gradient(circle_at_center,hsl(215_18%_48%),hsl(222_35%_14%)_58%,hsl(224_42%_7%))]" />
              </div>
              <div className="mt-4 space-y-3">
                <PreviewRow label="Image input" value="Frontal required" />
                <PreviewRow label="Draft status" value="AI-generated draft" />
                <PreviewRow label="Review status" value="Human review required" />
              </div>
            </div>
          </div>
        </SectionContainer>

        <SectionContainer
          description="Upload and review the X-ray inputs before generating a report draft."
          eyebrow="Step 01"
          id="upload"
          tone="muted"
          title="Upload chest X-ray images"
        >
          <UploadSection
            errors={errors}
            onFileSelect={setFileForSlot}
            onRemove={removeFileForSlot}
            slots={mockWorkspace.xraySlots}
            uploads={uploads}
          />
        </SectionContainer>

        <SectionContainer
          description="Follow the mock generation stages before reviewing the prepared draft."
          eyebrow="Step 02"
          id="generation"
          title="Generate the AI report draft"
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
        </SectionContainer>

        <SectionContainer
          description="Read the AI-generated findings draft as a focused review step."
          eyebrow="Step 03"
          id="findings"
          title="Review Findings"
        >
          <FindingsSection
            findings={reportWorkspace.aiDraft.findings}
            onResetAiDraft={reportWorkspace.resetAiDraft}
          />
        </SectionContainer>

        <SectionContainer
          description="Review and edit the impression without automatically changing the review status."
          eyebrow="Step 04"
          id="impression"
          tone="muted"
          title="Review Impression"
        >
          <ImpressionSection
            impression={reportWorkspace.reviewedReport.impression}
            onImpressionChange={(impression) =>
              reportWorkspace.setReviewedReport((current) => ({
                ...current,
                impression,
              }))
            }
            reviewStatus={reportWorkspace.reviewStatus}
          />
        </SectionContainer>

        <SectionContainer
          description="Compare the read-only AI draft with the editable reviewed report."
          eyebrow="Step 05"
          id="workspace"
          title="Human review workspace"
        >
          <ReportEditorSection
            aiDraft={reportWorkspace.aiDraft}
            onReviewStatusChange={reportWorkspace.setReviewStatus}
            reviewedReport={reportWorkspace.reviewedReport}
            reviewStatus={reportWorkspace.reviewStatus}
            setReviewedReport={reportWorkspace.setReviewedReport}
          />
        </SectionContainer>

        <SectionContainer
          description="Confirm the reviewed report and export it with the required research-use notice."
          eyebrow="Step 06"
          id="export"
          tone="muted"
          title="Export the report"
        >
          <ExportSection
            onExportPdf={reportWorkspace.exportReviewedReportAsPdf}
            onExportTxt={reportWorkspace.exportReviewedReportAsTxt}
            report={reportWorkspace.reviewedReport}
            reviewStatus={reportWorkspace.reviewStatus}
          />
        </SectionContainer>

        <SectionContainer
          description="Use the existing mock medical references and similar cases as supporting material."
          eyebrow="Step 07"
          id="reference"
          title="Read supporting reference information"
        >
          <div className="mx-auto w-full max-w-4xl">
            <BottomInfoTabs
              knowledgeItems={mockWorkspace.knowledgeItems}
              similarCases={mockWorkspace.similarCases}
            />
          </div>
        </SectionContainer>

        <SectionContainer
          description="The interface keeps each task focused while preserving the original upload, review, and export behavior."
          eyebrow="Workflow"
          id="workflow"
          title="From image upload to reviewed report"
        >
          <ol className="grid gap-3 md:grid-cols-4 xl:grid-cols-7">
            {[
              "Upload X-ray",
              "Generate AI draft",
              "Review Findings",
              "Review Impression",
              "Read explanations",
              "Edit report",
              "Export",
            ].map((step, index) => (
                <li
                  className="rounded-2xl border border-slate-200 bg-white p-5 shadow-panel"
                  key={step}
                >
                  <span className="text-sm font-semibold text-blue-700">
                    0{index + 1}
                  </span>
                  <p className="mt-3 text-base font-semibold text-ink">{step}</p>
                </li>
            ))}
          </ol>
        </SectionContainer>
      </main>

      <footer className="border-t border-slate-200 bg-white px-4 py-8">
        <div className="mx-auto flex max-w-7xl flex-col gap-3 text-sm text-slate-500 md:flex-row md:items-center md:justify-between">
          <span className="font-semibold text-slate-700">
            AI Chest X-ray Report Drafting Workspace
          </span>
          <span>Research Use Only - University Capstone Project</span>
        </div>
      </footer>
    </div>
  );
}

type SectionContainerProps = {
  children: ReactNode;
  description?: string;
  eyebrow: string;
  id: string;
  title: string;
  tone?: "default" | "muted";
};

function SectionContainer({
  children,
  description,
  eyebrow,
  id,
  title,
  tone = "default",
}: SectionContainerProps) {
  return (
    <section
      className={`flex min-h-screen snap-start items-center px-4 py-24 lg:px-6 ${
        tone === "muted" ? "bg-slate-100" : "bg-slate-50"
      }`}
      id={id}
    >
      <div className="mx-auto w-full max-w-6xl">
        <div className="mx-auto mb-10 max-w-3xl text-center">
          <p className="text-sm font-semibold uppercase tracking-wide text-blue-700">
            {eyebrow}
          </p>
          <h2 className="mt-3 text-4xl font-semibold leading-tight text-ink md:text-5xl">
            {title}
          </h2>
          {description ? (
            <p className="mx-auto mt-4 max-w-2xl text-base leading-7 text-slate-600">
              {description}
            </p>
          ) : null}
        </div>
        <div className="mx-auto w-full">{children}</div>
      </div>
    </section>
  );
}

type PreviewRowProps = {
  label: string;
  value: string;
};

function PreviewRow({ label, value }: PreviewRowProps) {
  return (
    <div className="flex items-center justify-between gap-4 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
      <span className="text-sm text-slate-500">{label}</span>
      <span className="text-sm font-semibold text-slate-800">{value}</span>
    </div>
  );
}
