import type { ReportDraft } from "../types/workspace";
import { Badge } from "./ui/badge";
import { Card, CardContent, CardHeader } from "./ui/card";
import { Textarea } from "./ui/textarea";

type ReportDraftPanelProps = {
  draft: ReportDraft;
};

export function ReportDraftPanel({ draft }: ReportDraftPanelProps) {
  return (
    <Card as="main">
      <CardHeader className="flex flex-row items-start justify-between gap-4">
        <div>
          <h2 className="text-base font-semibold text-ink">Report Draft</h2>
          <p className="mt-1 text-sm text-slate-500">
            Mock data for Findings and Impression.
          </p>
        </div>
        <Badge className="bg-blue-50 px-3 py-1 text-sm text-blue-700">
          Draft report
        </Badge>
      </CardHeader>

      <CardContent>
      <div className="grid gap-4 xl:grid-cols-2">
        <section>
          <label className="text-sm font-medium text-slate-700" htmlFor="findings">
            Findings
          </label>
          <Textarea
            className="mt-2 min-h-56 resize-none"
            defaultValue={draft.findings}
            id="findings"
          />
        </section>

        <section>
          <label className="text-sm font-medium text-slate-700" htmlFor="impression">
            Impression
          </label>
          <Textarea
            className="mt-2 min-h-56 resize-none"
            defaultValue={draft.impression}
            id="impression"
          />
        </section>
      </div>

      <div className="mt-4 rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
        {draft.reviewNote}
      </div>
      </CardContent>
    </Card>
  );
}
