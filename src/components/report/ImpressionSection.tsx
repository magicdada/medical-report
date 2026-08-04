import type { ReviewStatus } from "../../types/workspace";
import { Card, CardContent, CardHeader } from "../ui/card";
import { Textarea } from "../ui/textarea";
import { ReportStatusBadge } from "./ReportStatusBadge";

type ImpressionSectionProps = {
  impression: string;
  onImpressionChange: (value: string) => void;
  reviewStatus: ReviewStatus;
};

export function ImpressionSection({
  impression,
  onImpressionChange,
  reviewStatus,
}: ImpressionSectionProps) {
  return (
    <Card className="mx-auto max-w-5xl overflow-hidden">
      <div className="h-1 bg-blue-700" />
      <CardHeader className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h3 className="text-2xl font-semibold text-ink">Impression</h3>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">
            Edit the impression in the reviewed report. The review status is not
            changed automatically.
          </p>
        </div>
        <ReportStatusBadge status={reviewStatus} />
      </CardHeader>
      <CardContent>
        <Textarea
          className="min-h-[320px] resize-none bg-white text-base leading-8"
          onChange={(event) => onImpressionChange(event.target.value)}
          value={impression}
        />
      </CardContent>
    </Card>
  );
}
