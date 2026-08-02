import type { TextareaHTMLAttributes } from "react";
import { cn } from "../../lib/utils";

type TextareaProps = TextareaHTMLAttributes<HTMLTextAreaElement>;

export function Textarea({ className, ...props }: TextareaProps) {
  return (
    <textarea
      className={cn(
        "w-full rounded-md border border-border bg-slate-50 p-3 text-sm leading-6 text-slate-800 outline-none transition-colors focus:border-blue-400 focus:bg-white",
        className,
      )}
      {...props}
    />
  );
}
