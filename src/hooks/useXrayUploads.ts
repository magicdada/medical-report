import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  ACCEPTED_XRAY_EXTENSIONS,
  ACCEPTED_XRAY_MIME_TYPES,
  MAX_XRAY_FILE_SIZE_BYTES,
  MAX_XRAY_FILE_SIZE_MB,
} from "../constants/uploadRules";
import type {
  UploadedXrayImage,
  XraySlotId,
  XrayUploadErrors,
} from "../types/workspace";

type XrayUploads = Partial<Record<XraySlotId, UploadedXrayImage>>;

function validateXrayFile(file: File): string | null {
  const extension = `.${file.name.split(".").pop()?.toLowerCase() ?? ""}`;
  const hasValidMimeType = ACCEPTED_XRAY_MIME_TYPES.includes(
    file.type as (typeof ACCEPTED_XRAY_MIME_TYPES)[number],
  );
  const hasValidExtension = ACCEPTED_XRAY_EXTENSIONS.includes(
    extension as (typeof ACCEPTED_XRAY_EXTENSIONS)[number],
  );

  if (!hasValidMimeType || !hasValidExtension) {
    return "Only PNG, JPG, or JPEG files are supported.";
  }

  if (file.size > MAX_XRAY_FILE_SIZE_BYTES) {
    return `File size must not exceed ${MAX_XRAY_FILE_SIZE_MB}MB.`;
  }

  return null;
}

function revokePreviewUrl(image?: UploadedXrayImage) {
  if (image?.previewUrl) {
    URL.revokeObjectURL(image.previewUrl);
  }
}

export function useXrayUploads() {
  const [uploads, setUploads] = useState<XrayUploads>({});
  const [errors, setErrors] = useState<XrayUploadErrors>({});
  const uploadsRef = useRef<XrayUploads>({});

  const setFileForSlot = useCallback((slotId: XraySlotId, file: File) => {
    const validationError = validateXrayFile(file);

    setUploads((currentUploads) => {
      if (validationError) {
        revokePreviewUrl(currentUploads[slotId]);
        return {
          ...currentUploads,
          [slotId]: undefined,
        };
      }

      const nextImage: UploadedXrayImage = {
        slotId,
        file,
        previewUrl: URL.createObjectURL(file),
      };

      revokePreviewUrl(currentUploads[slotId]);

      return {
        ...currentUploads,
        [slotId]: nextImage,
      };
    });

    setErrors((currentErrors) => ({
      ...currentErrors,
      [slotId]: validationError ?? undefined,
    }));
  }, []);

  const removeFileForSlot = useCallback((slotId: XraySlotId) => {
    setUploads((currentUploads) => {
      revokePreviewUrl(currentUploads[slotId]);

      return {
        ...currentUploads,
        [slotId]: undefined,
      };
    });

    setErrors((currentErrors) => ({
      ...currentErrors,
      [slotId]: undefined,
    }));
  }, []);

  useEffect(() => {
    uploadsRef.current = uploads;
  }, [uploads]);

  useEffect(() => {
    return () => {
      Object.values(uploadsRef.current).forEach(revokePreviewUrl);
    };
  }, []);

  const hasFrontalImage = useMemo(() => Boolean(uploads.frontal), [uploads.frontal]);

  return {
    uploads,
    errors,
    hasFrontalImage,
    removeFileForSlot,
    setFileForSlot,
  };
}
