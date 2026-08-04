import type { ComponentPropsWithoutRef, ElementType, HTMLAttributes } from "react";
import { cn } from "../../lib/utils";

type CardProps<T extends ElementType = "div"> = {
  as?: T;
  className?: string;
} & Omit<ComponentPropsWithoutRef<T>, "as" | "className">;

export function Card<T extends ElementType = "div">({
  as,
  className,
  ...props
}: CardProps<T>) {
  const Component = as ?? "div";

  return (
    <Component
      className={cn("rounded-2xl border border-slate-200 bg-white shadow-panel", className)}
      {...props}
    />
  );
}

export function CardHeader({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cn("p-6", className)} {...props} />;
}

export function CardContent({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cn("p-6 pt-0", className)} {...props} />;
}
