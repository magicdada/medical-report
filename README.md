# Medical Report Generation Capstone

CSIT998 capstone project prototype for automatic chest X-ray report generation with R2GenGPT, medical knowledge retrieval, and human review.

This repository currently contains the frontend mock prototype. It does not connect to the real R2GenGPT model, backend API, vector database, or medical knowledge retrieval service yet.

## Frontend Scope

The current frontend prototype supports:

- Frontal and lateral chest X-ray upload slots.
- Local image preview, replacement, and removal.
- Basic file validation for PNG, JPG, and JPEG images.
- Mock AI report draft with separate Findings and Impression fields.
- Editable human-reviewed report fields.
- Human review status selection.
- Mock medical knowledge and similar case sections.
- Export TXT for the reviewed report.
- Export PDF through the browser print dialog.

The frontend uses mock data from `src/data/mockWorkspace.ts`. Replacing the mock data with real API responses is a later integration task.

## Not Included

The frontend prototype does not:

- Load or run R2GenGPT.
- Implement backend API endpoints.
- Implement RAG, FAISS, or vector retrieval.
- Build the medical knowledge base.
- Handle GPU inference.
- Store reports in a database.

## Requirements

- Node.js
- npm

The project was developed with Vite, React, TypeScript, and Tailwind CSS.

## Install

```bash
npm install
```

## Run The Frontend

```bash
npm run dev
```

Then open the local Vite URL shown in the terminal. By default it is usually:

```text
http://localhost:5173
```

## Build

```bash
npm run build
```

The production build is written to `dist/`.

## Preview Production Build

```bash
npm run preview
```

Then open the preview URL shown in the terminal.

## Export Behavior

`Export TXT` downloads the current reviewed report as a `.txt` file.

`Export PDF` opens a print-ready report page. Choose "Save as PDF" in the browser print dialog to export a PDF file.
