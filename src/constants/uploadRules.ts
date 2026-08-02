export const ACCEPTED_XRAY_MIME_TYPES = ["image/png", "image/jpeg"] as const;

export const ACCEPTED_XRAY_EXTENSIONS = [".png", ".jpg", ".jpeg"] as const;

export const MAX_XRAY_FILE_SIZE_MB = 10;

export const MAX_XRAY_FILE_SIZE_BYTES = MAX_XRAY_FILE_SIZE_MB * 1024 * 1024;

export const XRAY_FILE_ACCEPT = ACCEPTED_XRAY_MIME_TYPES.join(",");
