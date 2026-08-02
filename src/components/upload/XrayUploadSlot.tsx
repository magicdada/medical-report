import { ImageUp, RefreshCw, Trash2 } from "lucide-react";
import { type ChangeEvent, useRef } from "react";
import { MAX_XRAY_FILE_SIZE_MB, XRAY_FILE_ACCEPT } from "../../constants/uploadRules";
import type { UploadedXrayImage, XraySlot, XraySlotId } from "../../types/workspace";
import { Badge } from "../ui/badge";
import { Button } from "../ui/button";

type XrayUploadSlotProps = {
  slot: XraySlot;
  image?: UploadedXrayImage;
  error?: string;
  onFileSelect: (slotId: XraySlotId, file: File) => void;
  onRemove: (slotId: XraySlotId) => void;
};

export function XrayUploadSlot({
  slot,
  image,
  error,
  onFileSelect,
  onRemove,
}: XrayUploadSlotProps) {
  const inputRef = useRef<HTMLInputElement>(null);

  const openFilePicker = () => {
    inputRef.current?.click();
  };

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];

    if (file) {
      onFileSelect(slot.id, file);
    }

    event.target.value = "";
  };

  return (
    <section className="rounded-md border border-dashed border-slate-300 bg-slate-50 p-4">
      <input
        accept={XRAY_FILE_ACCEPT}
        className="hidden"
        onChange={handleChange}
        ref={inputRef}
        type="file"
      />

      <div className="flex items-start gap-3">
        <div className="rounded-md bg-white p-2 text-slate-600 shadow-sm">
          <ImageUp className="h-5 w-5" aria-hidden="true" />
        </div>

        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <h3 className="text-sm font-medium text-ink">{slot.label}</h3>
            <Badge className="bg-white text-slate-500">
              {slot.required ? "Required" : "Optional"}
            </Badge>
          </div>

          <p className="mt-1 text-sm text-slate-500">{slot.helperText}</p>
          <p className="mt-1 text-xs text-slate-400">
            PNG, JPG, or JPEG. Maximum {MAX_XRAY_FILE_SIZE_MB}MB.
          </p>

          {image ? (
            <UploadedPreview
              image={image}
              onRemove={() => onRemove(slot.id)}
              onReplace={openFilePicker}
            />
          ) : (
            <Button className="mt-3" onClick={openFilePicker}>
              Select File
            </Button>
          )}

          {error ? (
            <p className="mt-3 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {error}
            </p>
          ) : null}
        </div>
      </div>
    </section>
  );
}

type UploadedPreviewProps = {
  image: UploadedXrayImage;
  onRemove: () => void;
  onReplace: () => void;
};

function UploadedPreview({ image, onRemove, onReplace }: UploadedPreviewProps) {
  return (
    <div className="mt-3 overflow-hidden rounded-md border border-border bg-white">
      <div className="aspect-[4/3] bg-slate-900">
        <img
          alt={`${image.file.name} preview`}
          className="h-full w-full object-contain"
          src={image.previewUrl}
        />
      </div>

      <div className="space-y-3 p-3">
        <div>
          <p className="truncate text-sm font-medium text-ink">{image.file.name}</p>
          <p className="mt-1 text-xs text-slate-500">
            {(image.file.size / 1024 / 1024).toFixed(2)} MB
          </p>
        </div>

        <div className="flex flex-wrap gap-2">
          <Button className="gap-2" onClick={onReplace}>
            <RefreshCw className="h-4 w-4" aria-hidden="true" />
            Replace
          </Button>
          <Button className="gap-2 text-red-700 hover:bg-red-50" onClick={onRemove}>
            <Trash2 className="h-4 w-4" aria-hidden="true" />
            Remove
          </Button>
        </div>
      </div>
    </div>
  );
}
