import { AlertCircle, CheckCircle2, Loader2, RotateCcw } from "lucide-react";
import type { GenerationStatus } from "../../types/workspace";
import { Button } from "../ui/button";
import { Card, CardContent } from "../ui/card";

type GenerationStep = {
  status: Exclude<GenerationStatus, "idle" | "success" | "error">;
  label: string;
};

type GenerationSectionProps = {
  canGenerateDraft: boolean;
  currentStep: string | null;
  elapsedSeconds: number;
  errorMessage: string | null;
  isGenerating: boolean;
  onGenerate: () => void;
  onRetry: () => void;
  status: GenerationStatus;
  steps: GenerationStep[];
};

export function GenerationSection({
  canGenerateDraft,
  currentStep,
  elapsedSeconds,
  errorMessage,
  isGenerating,
  onGenerate,
  onRetry,
  status,
  steps,
}: GenerationSectionProps) {
  return (
    <Card className="mx-auto max-w-4xl overflow-hidden">
      <div className="h-1 bg-blue-700" />
      <CardContent className="p-8">
        <div className="flex flex-col gap-8 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <div className="flex items-center gap-3">
              <StatusIcon isGenerating={isGenerating} status={status} />
              <div>
                <p className="text-sm font-semibold uppercase tracking-wide text-blue-700">
                  Generation status
                </p>
                <h3 className="mt-1 text-2xl font-semibold text-ink">
                  {getStatusHeading(status)}
                </h3>
              </div>
            </div>
            <p className="mt-5 max-w-xl text-sm leading-6 text-slate-600">
              {getStatusDescription(status, currentStep)}
            </p>
            <p className="mt-3 text-sm text-slate-500">
              Elapsed time: {elapsedSeconds}s
            </p>
            {status === "error" ? (
              <div className="mt-5 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm leading-6 text-red-700">
                {errorMessage ?? "The mock generation flow could not complete."}
              </div>
            ) : null}
          </div>

          <div className="flex flex-wrap gap-3">
            <Button
              className="border-blue-700 bg-blue-700 text-white hover:border-blue-800 hover:bg-blue-800 hover:text-white disabled:border-slate-200 disabled:bg-slate-200 disabled:text-slate-500"
              disabled={!canGenerateDraft || isGenerating}
              onClick={onGenerate}
              title={
                canGenerateDraft
                  ? "Start mock generation flow"
                  : "Upload a frontal chest X-ray first"
              }
            >
              {isGenerating ? "Generating..." : "Generate Report"}
            </Button>
            {status === "error" ? (
              <Button className="gap-2" onClick={onRetry}>
                <RotateCcw className="h-4 w-4" aria-hidden="true" />
                Retry
              </Button>
            ) : null}
          </div>
        </div>

        <ol className="mt-8 space-y-3">
          {steps.map((step, index) => (
            <li
              className="flex items-center gap-4 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3"
              key={step.status}
            >
              <span
                className={`flex h-8 w-8 items-center justify-center rounded-full text-sm font-semibold ${
                  getStepState(status, step.status) === "active"
                    ? "bg-blue-700 text-white"
                    : getStepState(status, step.status) === "done"
                      ? "bg-emerald-100 text-emerald-700"
                      : "bg-white text-slate-400"
                }`}
              >
                {index + 1}
              </span>
              <span className="text-sm font-medium text-slate-700">{step.label}</span>
            </li>
          ))}
        </ol>
      </CardContent>
    </Card>
  );
}

function StatusIcon({
  isGenerating,
  status,
}: {
  isGenerating: boolean;
  status: GenerationStatus;
}) {
  if (isGenerating) {
    return (
      <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-50 text-blue-700">
        <Loader2 className="h-6 w-6 animate-spin" aria-hidden="true" />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-red-50 text-red-700">
        <AlertCircle className="h-6 w-6" aria-hidden="true" />
      </div>
    );
  }

  return (
    <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-700">
      <CheckCircle2 className="h-6 w-6" aria-hidden="true" />
    </div>
  );
}

function getStatusHeading(status: GenerationStatus) {
  if (status === "idle") {
    return "Ready for mock generation";
  }

  if (status === "success") {
    return "AI report draft prepared";
  }

  if (status === "error") {
    return "Generation needs retry";
  }

  return "Generating report draft";
}

function getStatusDescription(status: GenerationStatus, currentStep: string | null) {
  if (status === "idle") {
    return "Click Generate Report after uploading the required frontal chest X-ray to preview the generation workflow.";
  }

  if (status === "success") {
    return "The mock generation flow is complete. Continue to the review workspace to inspect and edit the draft.";
  }

  if (status === "error") {
    return "Uploaded images are preserved. Retry the mock generation flow when ready.";
  }

  return currentStep ?? "Preparing the report generation workflow.";
}

function getStepState(
  currentStatus: GenerationStatus,
  stepStatus: GenerationStep["status"],
) {
  const currentIndex = generationStepOrder.indexOf(currentStatus);
  const stepIndex = generationStepOrder.indexOf(stepStatus);

  if (currentStatus === "success") {
    return "done";
  }

  if (currentStatus === "error" && stepIndex < currentIndex) {
    return "done";
  }

  if (currentStatus === stepStatus) {
    return "active";
  }

  if (stepIndex < currentIndex) {
    return "done";
  }

  return "pending";
}

const generationStepOrder: GenerationStatus[] = [
  "validating",
  "uploading",
  "generating",
  "retrieving_knowledge",
  "preparing_workspace",
];
