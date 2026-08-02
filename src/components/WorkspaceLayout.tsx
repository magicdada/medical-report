import { mockWorkspace } from "../data/mockWorkspace";
import { useXrayUploads } from "../hooks/useXrayUploads";
import { BottomInfoTabs } from "./BottomInfoTabs";
import { HeaderBar } from "./HeaderBar";
import { ReportReviewWorkspacePanel } from "./ReportReviewWorkspacePanel";
import { XrayUploadPanel } from "./upload/XrayUploadPanel";

export function WorkspaceLayout() {
  const {
    errors,
    hasFrontalImage,
    removeFileForSlot,
    setFileForSlot,
    uploads,
  } = useXrayUploads();

  return (
    <div className="min-h-screen bg-slate-100">
      <HeaderBar backendStatus={mockWorkspace.backendStatus} />

      <div className="mx-auto grid w-full max-w-[1500px] gap-4 px-4 py-4 lg:grid-cols-[320px_minmax(0,1fr)] lg:px-6 xl:grid-cols-[320px_minmax(0,1fr)_320px]">
        <XrayUploadPanel
          errors={errors}
          onFileSelect={setFileForSlot}
          onRemove={removeFileForSlot}
          slots={mockWorkspace.xraySlots}
          uploads={uploads}
        />
        <ReportReviewWorkspacePanel
          canGenerateDraft={hasFrontalImage}
          draft={mockWorkspace.reportDraft}
        />
        <BottomInfoTabs
          className="lg:col-span-2 xl:col-span-1"
          knowledgeItems={mockWorkspace.knowledgeItems}
          similarCases={mockWorkspace.similarCases}
        />
      </div>
    </div>
  );
}
