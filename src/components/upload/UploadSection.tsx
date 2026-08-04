import type {
  UploadedXrayImage,
  XraySlot,
  XraySlotId,
  XrayUploadErrors,
} from "../../types/workspace";
import { XrayUploadPanel } from "./XrayUploadPanel";

type UploadSectionProps = {
  errors: XrayUploadErrors;
  onFileSelect: (slotId: XraySlotId, file: File) => void;
  onRemove: (slotId: XraySlotId) => void;
  slots: XraySlot[];
  uploads: Partial<Record<XraySlotId, UploadedXrayImage>>;
};

export function UploadSection({
  errors,
  onFileSelect,
  onRemove,
  slots,
  uploads,
}: UploadSectionProps) {
  return (
    <div className="mx-auto grid max-w-5xl items-start gap-8 lg:grid-cols-[minmax(0,1fr)_320px]">
      <XrayUploadPanel
        errors={errors}
        onFileSelect={onFileSelect}
        onRemove={onRemove}
        slots={slots}
        uploads={uploads}
      />

      <aside className="rounded-2xl border border-slate-200 bg-white p-6 shadow-panel">
        <p className="text-sm font-semibold uppercase tracking-wide text-blue-700">
          Upload requirements
        </p>
        <ul className="mt-4 space-y-3 text-sm leading-6 text-slate-600">
          <li>Frontal chest X-ray is required before report generation.</li>
          <li>Lateral chest X-ray can be added when available.</li>
          <li>Supported files: JPG, JPEG, and PNG up to 10MB.</li>
          <li>AI-generated content remains a draft until professionally reviewed.</li>
        </ul>
      </aside>
    </div>
  );
}
