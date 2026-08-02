import { ImageUp } from "lucide-react";
import type { XraySlot } from "../types/workspace";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Card, CardContent, CardHeader } from "./ui/card";

type XrayUploadPanelProps = {
  slots: XraySlot[];
};

export function XrayUploadPanel({ slots }: XrayUploadPanelProps) {
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
          <section
            className="rounded-md border border-dashed border-slate-300 bg-slate-50 p-4"
            key={slot.id}
          >
            <div className="flex items-start gap-3">
              <div className="rounded-md bg-white p-2 text-slate-600 shadow-sm">
                <ImageUp className="h-5 w-5" aria-hidden="true" />
              </div>
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2">
                  <h3 className="text-sm font-medium text-ink">{slot.label}</h3>
                  <Badge className="bg-white text-slate-500">
                    {slot.required ? "Required" : "Optional"}
                  </Badge>
                </div>
                <p className="mt-1 text-sm text-slate-500">{slot.helperText}</p>
                <Button className="mt-3">Select File</Button>
              </div>
            </div>
          </section>
        ))}
      </CardContent>
    </Card>
  );
}
