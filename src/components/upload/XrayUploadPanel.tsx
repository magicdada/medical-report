import type {
  UploadedXrayImage,
  XraySlot,
  XraySlotId,
  XrayUploadErrors,
} from "../../types/workspace";
import { Card, CardContent, CardHeader } from "../ui/card";
import { XrayUploadSlot } from "./XrayUploadSlot";

type XrayUploadPanelProps = {
  slots: XraySlot[];
  uploads: Partial<Record<XraySlotId, UploadedXrayImage>>;
  errors: XrayUploadErrors;
  onFileSelect: (slotId: XraySlotId, file: File) => void;
  onRemove: (slotId: XraySlotId) => void;
};

export function XrayUploadPanel({
  slots,
  uploads,
  errors,
  onFileSelect,
  onRemove,
}: XrayUploadPanelProps) {
  return (
    <Card as="aside">
      <CardHeader>
        <h2 className="text-base font-semibold text-ink">Image Upload</h2>
        <p className="mt-1 text-sm text-slate-500">
          Upload a frontal chest X-ray and an optional lateral view.
        </p>
      </CardHeader>

      <CardContent className="space-y-3">
        {slots.map((slot) => (
          <XrayUploadSlot
            error={errors[slot.id]}
            image={uploads[slot.id]}
            key={slot.id}
            onFileSelect={onFileSelect}
            onRemove={onRemove}
            slot={slot}
          />
        ))}
      </CardContent>
    </Card>
  );
}
