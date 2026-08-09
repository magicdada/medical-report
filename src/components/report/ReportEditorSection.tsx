import type { Dispatch, SetStateAction } from "react";
import type { ReportDraft, ReviewStatus } from "../../types/workspace";
import { Card, CardContent, CardHeader } from "../ui/card";
import { Textarea } from "../ui/textarea";
import { ReportStatusBadge } from "./ReportStatusBadge";

type ReportEditorSectionProps = {
  aiDraft: Pick<ReportDraft, "findings" | "impression">;
  onReviewStatusChange: (status: ReviewStatus) => void;
  reviewStatus: ReviewStatus;
  reviewedReport: Pick<ReportDraft, "findings" | "impression">;
  setReviewedReport: Dispatch<
    SetStateAction<Pick<ReportDraft, "findings" | "impression">>
  >;
};

const reviewStatusOptions: ReviewStatus[] = ["unreviewed", "in_review", "reviewed"];

const reviewStatusLabel: Record<ReviewStatus, string> = {
  unreviewed: "Pending",
  in_review: "In Progress",
  reviewed: "Reviewed",
};

export function ReportEditorSection({
  aiDraft,
  onReviewStatusChange,
  reviewedReport,
  reviewStatus,
  setReviewedReport,
}: ReportEditorSectionProps) {
  return (
    <Card className="overflow-hidden">
      <div className="h-1 bg-blue-700" />
      <CardHeader className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h3 className="text-2xl font-semibold text-ink">Clinical Review Workspace</h3>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">
            Compare the read-only draft with the editable clinical report.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <ReportStatusBadge status={reviewStatus} />
          <ReviewStatusControl
            status={reviewStatus}
            onStatusChange={onReviewStatusChange}
          />
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid gap-5 xl:grid-cols-2">
          <section>
            <h4 className="text-sm font-semibold text-slate-700">Draft Report</h4>
            <Textarea
              className="mt-2 min-h-80 resize-none"
              readOnly
              value={`Findings\n${aiDraft.findings}\n\nImpression\n${aiDraft.impression}`}
            />
          </section>
          <section>
            <h4 className="text-sm font-semibold text-slate-700">Reviewed Report</h4>
            <div className="mt-2 grid gap-4">
              <Textarea
                className="min-h-44 resize-none"
                onChange={(event) =>
                  setReviewedReport((current) => ({
                    ...current,
                    findings: event.target.value,
                  }))
                }
                value={reviewedReport.findings}
              />
              <Textarea
                className="min-h-36 resize-none"
                onChange={(event) =>
                  setReviewedReport((current) => ({
                    ...current,
                    impression: event.target.value,
                  }))
                }
                value={reviewedReport.impression}
              />
            </div>
          </section>
        </div>
      </CardContent>
    </Card>
  );
}

type ReviewStatusControlProps = {
  status: ReviewStatus;
  onStatusChange: (status: ReviewStatus) => void;
};

function ReviewStatusControl({ status, onStatusChange }: ReviewStatusControlProps) {
  return (
    <div className="flex rounded-xl border border-slate-200 bg-white p-1">
      {reviewStatusOptions.map((option) => (
        <button
          className={`rounded-lg px-3 py-1.5 text-sm font-medium transition-colors ${
            status === option
              ? "bg-blue-700 text-white"
              : "text-slate-600 hover:bg-blue-50 hover:text-blue-800"
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
