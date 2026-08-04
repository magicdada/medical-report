import { RotateCcw } from "lucide-react";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";
import { Card, CardContent, CardHeader } from "../ui/card";
import { Textarea } from "../ui/textarea";

type FindingsSectionProps = {
  findings: string;
  onResetAiDraft: () => void;
};

export function FindingsSection({
  findings,
  onResetAiDraft,
}: FindingsSectionProps) {
  return (
    <Card className="mx-auto max-w-5xl overflow-hidden">
      <div className="h-1 bg-blue-700" />
      <CardHeader className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h3 className="text-2xl font-semibold text-ink">Findings</h3>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">
            Read the AI-generated findings draft separately before editing the
            reviewed report.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Badge className="border-blue-200 bg-blue-50 px-3 py-1 text-sm text-blue-700">
            AI-generated draft
          </Badge>
          <Button className="gap-2" onClick={onResetAiDraft}>
            <RotateCcw className="h-4 w-4" aria-hidden="true" />
            Reset
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <Textarea
          className="min-h-[360px] resize-none bg-white text-base leading-8"
          readOnly
          value={findings}
        />
      </CardContent>
    </Card>
  );
}
