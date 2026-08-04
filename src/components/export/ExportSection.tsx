import { Download, FileText } from "lucide-react";
import type { ReportDraft, ReviewStatus } from "../../types/workspace";
import { Button } from "../ui/button";
import { Card, CardContent, CardHeader } from "../ui/card";
import { ReportStatusBadge } from "../report/ReportStatusBadge";

type ExportSectionProps = {
  onExportPdf: () => void;
  onExportTxt: () => void;
  report: Pick<ReportDraft, "findings" | "impression">;
  reviewStatus: ReviewStatus;
};

export function ExportSection({
  onExportPdf,
  onExportTxt,
  report,
  reviewStatus,
}: ExportSectionProps) {
  const generatedAt = new Date().toLocaleString();

  return (
    <Card className="mx-auto max-w-5xl overflow-hidden">
      <div className="h-1 bg-blue-700" />
      <CardHeader className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h3 className="text-2xl font-semibold text-ink">Export Report</h3>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">
            Confirm the reviewed report content before exporting TXT or PDF.
          </p>
        </div>
        <ReportStatusBadge status={reviewStatus} />
      </CardHeader>
      <CardContent>
        <div className="grid gap-4 md:grid-cols-3">
          <SummaryItem label="Case ID" value="CASE-MOCK-001" />
          <SummaryItem label="Generated at" value={generatedAt} />
          <SummaryItem label="Review status" value={reviewStatus} />
        </div>

        {reviewStatus !== "reviewed" ? (
          <div className="mt-5 rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm leading-6 text-amber-800">
            This report has not been reviewed by a qualified professional.
          </div>
        ) : null}

        <div className="mt-6 rounded-2xl border border-slate-200 bg-slate-50 p-5">
          <h4 className="text-sm font-semibold text-slate-700">Export Preview</h4>
          <div className="mt-4 space-y-4 text-sm leading-6 text-slate-700">
            <section>
              <p className="font-semibold text-ink">Findings</p>
              <p className="mt-1">{report.findings}</p>
            </section>
            <section>
              <p className="font-semibold text-ink">Impression</p>
              <p className="mt-1">{report.impression}</p>
            </section>
          </div>
        </div>

        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button className="gap-2" onClick={onExportTxt}>
            <FileText className="h-4 w-4" aria-hidden="true" />
            Export TXT
          </Button>
          <Button className="gap-2" onClick={onExportPdf}>
            <Download className="h-4 w-4" aria-hidden="true" />
            Export PDF
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

type SummaryItemProps = {
  label: string;
  value: string;
};

function SummaryItem({ label, value }: SummaryItemProps) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4">
      <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
        {label}
      </p>
      <p className="mt-2 text-sm font-semibold text-slate-800">{value}</p>
    </div>
  );
}
