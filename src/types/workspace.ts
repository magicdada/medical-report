export type BackendStatus = "placeholder" | "connected" | "offline";

export type XraySlot = {
  id: "frontal" | "lateral";
  label: string;
  required: boolean;
  helperText: string;
};

export type XraySlotId = XraySlot["id"];

export type UploadedXrayImage = {
  slotId: XraySlotId;
  file: File;
  previewUrl: string;
};

export type XrayUploadErrors = Partial<Record<XraySlotId, string>>;

export type ReportDraft = {
  findings: string;
  impression: string;
  reviewNote: string;
};

export type ReviewStatus = "unreviewed" | "in_review" | "reviewed";

export type GenerationStatus =
  | "idle"
  | "validating"
  | "uploading"
  | "generating"
  | "retrieving_knowledge"
  | "preparing_workspace"
  | "success"
  | "error";

export type KnowledgeItem = {
  title: string;
  summary: string;
};

export type SimilarCase = {
  caseId: string;
  finding: string;
  relevance: string;
};

export type WorkspaceMockData = {
  backendStatus: BackendStatus;
  xraySlots: XraySlot[];
  reportDraft: ReportDraft;
  knowledgeItems: KnowledgeItem[];
  similarCases: SimilarCase[];
};
