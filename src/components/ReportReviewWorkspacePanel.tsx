import { Download, FileText, RotateCcw } from "lucide-react";
import { useMemo, useState } from "react";
import type { ReportDraft, ReviewStatus } from "../types/workspace";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Card, CardContent, CardHeader } from "./ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { Textarea } from "./ui/textarea";

type ReportReviewWorkspacePanelProps = {
  draft: ReportDraft;
  canGenerateDraft: boolean;
};

const reviewStatusLabel: Record<ReviewStatus, string> = {
  unreviewed: "Unreviewed",
  in_review: "In Review",
  reviewed: "Reviewed",
};

const reviewStatusOptions: ReviewStatus[] = ["unreviewed", "in_review", "reviewed"];

function buildReportText(
  report: Pick<ReportDraft, "findings" | "impression">,
  reviewStatus: ReviewStatus,
) {
  const exportedAt = new Date().toLocaleString();

  return [
    "AI Chest X-ray Report Draft",
    "",
    `Human Review Status: ${reviewStatusLabel[reviewStatus]}`,
    `Exported At: ${exportedAt}`,
    "",
    "Findings",
    report.findings,
    "",
    "Impression",
    report.impression,
    "",
    "Research Use Notice",
    "This report is an editable AI-generated draft and must be reviewed by a human clinician.",
  ].join("\n");
}

function downloadTextFile(filename: string, content: string) {
  const blob = new Blob([content], { type: "text/plain;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}

function escapeHtml(value: string) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function openPrintableReport(
  report: Pick<ReportDraft, "findings" | "impression">,
  reviewStatus: ReviewStatus,
) {
  const printWindow = window.open("", "_blank", "noopener,noreferrer");

  if (!printWindow) {
    return;
  }

  const exportedAt = new Date().toLocaleString();
  const documentHtml = `<!doctype html>
<html lang="zh-CN">
  <head>
    <meta charset="utf-8" />
    <title>AI Chest X-ray Report Draft</title>
    <style>
      body {
        color: #111827;
        font-family: Arial, "Microsoft YaHei", sans-serif;
        line-height: 1.6;
        margin: 40px;
      }

      h1 {
        border-bottom: 1px solid #d1d5db;
        font-size: 22px;
        margin: 0 0 16px;
        padding-bottom: 12px;
      }

      h2 {
        font-size: 16px;
        margin: 24px 0 8px;
      }

      .meta,
      .notice {
        color: #4b5563;
        font-size: 13px;
      }

      .section {
        white-space: pre-wrap;
      }

      .notice {
        border-top: 1px solid #e5e7eb;
        margin-top: 28px;
        padding-top: 12px;
      }
    </style>
  </head>
  <body>
    <h1>AI Chest X-ray Report Draft</h1>
    <div class="meta">Human Review Status: ${escapeHtml(reviewStatusLabel[reviewStatus])}</div>
    <div class="meta">Exported At: ${escapeHtml(exportedAt)}</div>

    <h2>Findings</h2>
    <div class="section">${escapeHtml(report.findings)}</div>

    <h2>Impression</h2>
    <div class="section">${escapeHtml(report.impression)}</div>

    <div class="notice">
      This report is an editable AI-generated draft and must be reviewed by a human clinician.
    </div>
  </body>
</html>`;

  printWindow.document.write(documentHtml);
  printWindow.document.close();
  printWindow.focus();
  printWindow.print();
}

export function ReportReviewWorkspacePanel({
  canGenerateDraft,
  draft,
}: ReportReviewWorkspacePanelProps) {
  const initialDraft = useMemo(
    () => ({
      findings: draft.findings,
      impression: draft.impression,
    }),
    [draft.findings, draft.impression],
  );

  const [aiDraft, setAiDraft] = useState(initialDraft);
  const [reviewedReport, setReviewedReport] = useState(initialDraft);
  const [reviewStatus, setReviewStatus] = useState<ReviewStatus>("unreviewed");

  const resetAiDraft = () => {
    setAiDraft(initialDraft);
  };

  const exportReviewedReportAsTxt = () => {
    downloadTextFile(
      "reviewed-chest-xray-report.txt",
      buildReportText(reviewedReport, reviewStatus),
    );
  };

  const exportReviewedReportAsPdf = () => {
    openPrintableReport(reviewedReport, reviewStatus);
  };

  return (
    <Card as="main">
      <CardHeader className="flex flex-row items-start justify-between gap-4">
        <div>
          <h2 className="text-base font-semibold text-ink">Report Workspace</h2>
          <p className="mt-1 text-sm text-slate-500">
            Separate the AI-generated draft from the human-reviewed report.
          </p>
        </div>

        <div className="flex flex-wrap justify-end gap-2">
          <Badge className="bg-blue-50 px-3 py-1 text-sm text-blue-700">
            AI-generated draft
          </Badge>
          <Badge className="bg-amber-50 px-3 py-1 text-sm text-amber-800">
            {reviewStatusLabel[reviewStatus]}
          </Badge>
          <Button
            className="bg-blue-600 text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-500"
            disabled={!canGenerateDraft}
            title={canGenerateDraft ? "Frontend placeholder action" : "Upload a frontal chest X-ray first"}
          >
            Generate Report
          </Button>
        </div>
      </CardHeader>

      <CardContent>
        <Tabs defaultValue="ai-draft">
          <TabsList>
            <TabsTrigger value="ai-draft">AI Draft</TabsTrigger>
            <TabsTrigger value="reviewed-report">Reviewed Report</TabsTrigger>
          </TabsList>

          <TabsContent className="px-0 pb-0" value="ai-draft">
            <div className="mb-4 flex items-center justify-between gap-3">
              <p className="text-sm text-slate-500">Read-only draft. Human review is required.</p>
              <Button className="gap-2" onClick={resetAiDraft}>
                <RotateCcw className="h-4 w-4" aria-hidden="true" />
                Reset
              </Button>
            </div>

            <ReportFields
              findings={aiDraft.findings}
              impression={aiDraft.impression}
              mode="readonly"
            />
          </TabsContent>

          <TabsContent className="px-0 pb-0" value="reviewed-report">
            <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
              <p className="text-sm text-slate-500">The reviewed report can be edited.</p>
              <ReviewStatusControl
                status={reviewStatus}
                onStatusChange={setReviewStatus}
              />
            </div>

            <ReportFields
              findings={reviewedReport.findings}
              impression={reviewedReport.impression}
              mode="editable"
              onFindingsChange={(findings) =>
                setReviewedReport((current) => ({ ...current, findings }))
              }
              onImpressionChange={(impression) =>
                setReviewedReport((current) => ({ ...current, impression }))
              }
            />
          </TabsContent>
        </Tabs>

        <div className="mt-4 rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
          {draft.reviewNote}
        </div>

        <div className="mt-4 flex flex-wrap items-center justify-end gap-2 border-t border-border pt-4">
          <Button className="gap-2" onClick={exportReviewedReportAsTxt}>
            <FileText className="h-4 w-4" aria-hidden="true" />
            Export TXT
          </Button>
          <Button className="gap-2" onClick={exportReviewedReportAsPdf}>
            <Download className="h-4 w-4" aria-hidden="true" />
            Export PDF
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

type ReportFieldsProps = {
  findings: string;
  impression: string;
  mode: "readonly" | "editable";
  onFindingsChange?: (value: string) => void;
  onImpressionChange?: (value: string) => void;
};

function ReportFields({
  findings,
  impression,
  mode,
  onFindingsChange,
  onImpressionChange,
}: ReportFieldsProps) {
  const isReadOnly = mode === "readonly";

  return (
    <div className="grid gap-4 xl:grid-cols-2">
      <section>
        <label className="text-sm font-medium text-slate-700" htmlFor={`${mode}-findings`}>
          Findings
        </label>
        <Textarea
          className="mt-2 min-h-56 resize-none"
          id={`${mode}-findings`}
          onChange={(event) => onFindingsChange?.(event.target.value)}
          readOnly={isReadOnly}
          value={findings}
        />
      </section>

      <section>
        <label className="text-sm font-medium text-slate-700" htmlFor={`${mode}-impression`}>
          Impression
        </label>
        <Textarea
          className="mt-2 min-h-56 resize-none"
          id={`${mode}-impression`}
          onChange={(event) => onImpressionChange?.(event.target.value)}
          readOnly={isReadOnly}
          value={impression}
        />
      </section>
    </div>
  );
}

type ReviewStatusControlProps = {
  status: ReviewStatus;
  onStatusChange: (status: ReviewStatus) => void;
};

function ReviewStatusControl({ status, onStatusChange }: ReviewStatusControlProps) {
  return (
    <div className="flex rounded-md border border-border bg-white p-1">
      {reviewStatusOptions.map((option) => (
        <button
          className={`rounded px-3 py-1.5 text-sm font-medium transition-colors ${
            status === option
              ? "bg-blue-600 text-white"
              : "text-slate-600 hover:bg-slate-100"
          }`}
          key={option}
          onClick={() => onStatusChange(option)}
          type="button"
        >
          {reviewStatusLabel[option]}
        </button>
      ))}
    </div>
  );
}
