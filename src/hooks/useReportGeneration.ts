import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import type { GenerationStatus } from "../types/workspace";

const GENERATION_STEPS: Array<{
  status: Exclude<GenerationStatus, "idle" | "success" | "error">;
  label: string;
}> = [
  { status: "validating", label: "Validating images" },
  { status: "uploading", label: "Uploading images" },
  { status: "generating", label: "Generating AI report draft" },
  { status: "retrieving_knowledge", label: "Retrieving medical knowledge" },
  { status: "preparing_workspace", label: "Preparing review workspace" },
];

const STEP_DURATION_MS = 900;
const TIMEOUT_MS = 12_000;

export function useReportGeneration() {
  const [status, setStatus] = useState<GenerationStatus>("idle");
  const [startedAt, setStartedAt] = useState<number | null>(null);
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const timersRef = useRef<number[]>([]);

  const clearTimers = useCallback(() => {
    timersRef.current.forEach(window.clearTimeout);
    timersRef.current = [];
  }, []);

  const isGenerating = useMemo(
    () =>
      status === "validating" ||
      status === "uploading" ||
      status === "generating" ||
      status === "retrieving_knowledge" ||
      status === "preparing_workspace",
    [status],
  );

  const startGeneration = useCallback(() => {
    clearTimers();
    setStartedAt(Date.now());
    setElapsedSeconds(0);
    setErrorMessage(null);
    setStatus("validating");

    GENERATION_STEPS.slice(1).forEach((step, index) => {
      const timer = window.setTimeout(() => {
        setStatus(step.status);
      }, STEP_DURATION_MS * (index + 1));
      timersRef.current.push(timer);
    });

    const successTimer = window.setTimeout(() => {
      setStatus("success");
    }, STEP_DURATION_MS * GENERATION_STEPS.length);
    timersRef.current.push(successTimer);

    const timeoutTimer = window.setTimeout(() => {
      setStatus((currentStatus) => {
        if (currentStatus === "success") {
          return currentStatus;
        }

        setErrorMessage(
          "Report generation took longer than expected. Please retry.",
        );
        return "error";
      });
    }, TIMEOUT_MS);
    timersRef.current.push(timeoutTimer);
  }, [clearTimers]);

  const retryGeneration = useCallback(() => {
    startGeneration();
  }, [startGeneration]);

  useEffect(() => {
    if (!isGenerating || startedAt === null) {
      return;
    }

    const interval = window.setInterval(() => {
      setElapsedSeconds(Math.floor((Date.now() - startedAt) / 1000));
    }, 250);

    return () => window.clearInterval(interval);
  }, [isGenerating, startedAt]);

  useEffect(() => clearTimers, [clearTimers]);

  return {
    currentStep:
      GENERATION_STEPS.find((step) => step.status === status)?.label ?? null,
    elapsedSeconds,
    errorMessage,
    isGenerating,
    retryGeneration,
    startGeneration,
    status,
    steps: GENERATION_STEPS,
  };
}
