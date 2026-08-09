import type { ReviewStatus } from "../../types/workspace";
import { Badge } from "../ui/badge";

type ReportStatusBadgeProps = {
  status: ReviewStatus;
};

const statusClassName: Record<ReviewStatus, string> = {
  unreviewed: "border-amber-200 bg-amber-50 text-amber-800",
  in_review: "border-blue-200 bg-blue-50 text-blue-700",
  reviewed: "border-emerald-200 bg-emerald-50 text-emerald-700",
};

const statusLabel: Record<ReviewStatus, string> = {
  unreviewed: "Clinical review pending",
  in_review: "Clinical review in progress",
  reviewed: "Clinician reviewed",
};

export function ReportStatusBadge({ status }: ReportStatusBadgeProps) {
  return <Badge className={statusClassName[status]}>{statusLabel[status]}</Badge>;
}
