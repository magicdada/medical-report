import type { KnowledgeItem, SimilarCase } from "../types/workspace";
import { Badge } from "./ui/badge";
import { Card, CardContent, CardHeader } from "./ui/card";

type BottomInfoTabsProps = {
  className?: string;
  knowledgeItems: KnowledgeItem[];
  similarCases: SimilarCase[];
};

export function BottomInfoTabs({
  className,
  knowledgeItems,
  similarCases,
}: BottomInfoTabsProps) {
  return (
    <Card as="aside" className={className}>
      <CardHeader>
        <h2 className="text-base font-semibold text-ink">Reference Information</h2>
        <p className="mt-1 text-sm text-slate-500">
          Mock medical terminology and similar case data.
        </p>
      </CardHeader>

      <CardContent className="space-y-5">
        <section>
          <h3 className="text-sm font-semibold text-ink">Medical Knowledge</h3>
          <div className="mt-3 space-y-3">
            {knowledgeItems.map((item) => (
              <article
                className="rounded-md border border-border bg-slate-50 p-3"
                key={item.title}
              >
                <h4 className="text-sm font-semibold text-ink">{item.title}</h4>
                <p className="mt-2 text-sm leading-6 text-slate-600">{item.summary}</p>
              </article>
            ))}
          </div>
        </section>

        <section>
          <h3 className="text-sm font-semibold text-ink">Similar Cases</h3>
          <div className="mt-3 space-y-3">
            {similarCases.map((item) => (
              <article
                className="rounded-md border border-border bg-slate-50 p-3"
                key={item.caseId}
              >
                <div className="flex items-center justify-between gap-3">
                  <h4 className="text-sm font-semibold text-ink">{item.caseId}</h4>
                  <Badge className="bg-white text-slate-500">{item.relevance}</Badge>
                </div>
                <p className="mt-2 text-sm leading-6 text-slate-600">{item.finding}</p>
              </article>
            ))}
          </div>
        </section>
      </CardContent>
    </Card>
  );
}
