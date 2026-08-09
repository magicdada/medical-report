import { useMemo, useState } from "react";
import type { ReportDraft, ReviewStatus } from "../types/workspace";

const reviewStatusLabel: Record<ReviewStatus, string> = {
  unreviewed: "Clinical review pending",
  in_review: "Clinical review in progress",
  reviewed: "Clinician reviewed",
};

function buildReportText(
  report: Pick<ReportDraft, "findings" | "impression">,
  reviewStatus: ReviewStatus,
) {
  const exportedAt = new Date().toLocaleString();

  return [
    "CXR Assist Report Draft",
    "",
    `Clinical Review Status: ${reviewStatusLabel[reviewStatus]}`,
    `Exported At: ${exportedAt}`,
    "",
    "Findings",
    report.findings,
    "",
    "Impression",
    report.impression,
    "",
    "Disclaimer",
    "For research and educational use only. Not intended for clinical diagnosis.",
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
    <title>CXR Assist Report Draft</title>
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
    <h1>CXR Assist Report Draft</h1>
    <div class="meta">Clinical Review Status: ${escapeHtml(reviewStatusLabel[reviewStatus])}</div>
    <div class="meta">Exported At: ${escapeHtml(exportedAt)}</div>

    <h2>Findings</h2>
    <div class="section">${escapeHtml(report.findings)}</div>

    <h2>Impression</h2>
    <div class="section">${escapeHtml(report.impression)}</div>

    <div class="notice">
      <strong>Disclaimer</strong><br />
      For research and educational use only. Not intended for clinical diagnosis.
    </div>
  </body>
</html>`;

  printWindow.document.write(documentHtml);
  printWindow.document.close();
  printWindow.focus();
  printWindow.print();
}

export function useReportWorkspace(draft: ReportDraft) {
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

  return {
    aiDraft,
    exportReviewedReportAsPdf,
    exportReviewedReportAsTxt,
    resetAiDraft,
    reviewedReport,
    reviewStatus,
    reviewStatusLabel,
    setReviewedReport,
    setReviewStatus,
  };
}
