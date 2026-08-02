import type { WorkspaceMockData } from "../types/workspace";

export const mockWorkspace: WorkspaceMockData = {
  backendStatus: "placeholder",
  xraySlots: [
    {
      id: "frontal",
      label: "Frontal chest X-ray",
      required: true,
      helperText: "Primary image used to generate the report draft.",
    },
    {
      id: "lateral",
      label: "Lateral chest X-ray",
      required: false,
      helperText: "Optional image used to support additional review.",
    },
  ],
  reportDraft: {
    findings:
      "The lungs are clear. No focal consolidation, pleural effusion, or pneumothorax is seen. The cardiomediastinal silhouette is within normal size limits.",
    impression: "AI-generated draft: No acute cardiopulmonary abnormality.",
    reviewNote: "This draft must be reviewed by a human clinician before it can be used as a final report.",
  },
  knowledgeItems: [
    {
      title: "Report Draft Notice",
      summary: "The current content is an AI-generated draft and does not represent an automatic diagnosis.",
    },
    {
      title: "Review Guidance",
      summary: "A radiologist should confirm the findings with the original image, clinical context, and prior studies.",
    },
  ],
  similarCases: [
    {
      caseId: "CASE-1024",
      finding: "Normal heart size with no acute pulmonary abnormality.",
      relevance: "Similar imaging description",
    },
    {
      caseId: "CASE-1186",
      finding: "No focal consolidation. Costophrenic angles are clear.",
      relevance: "Similar report structure",
    },
  ],
};
