import type { TextareaHTMLAttributes } from "react";
import { cn } from "../../lib/utils";

type TextareaProps = TextareaHTMLAttributes<HTMLTextAreaElement>;

export function Textarea({ className, ...props }: TextareaProps) {
  return (
    <textarea
      className={cn(
        "w-full rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm leading-6 text-slate-800 outline-none transition-colors focus:border-blue-400 focus:bg-white focus:ring-2 focus:ring-blue-100",
        className,
      )}
      {...props}
    />
  );
}
