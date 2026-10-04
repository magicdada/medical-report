# Medical Imaging Report Generation System - Backend Service

## Overview

A deep learning-based chest X-ray diagnostic report generation system. Doctors upload chest X-ray images, and the system automatically invokes the AI model (R2GenGPT: Swin Transformer + LLaMA-2-7B) to generate radiology diagnostic reports.

## Tech Stack

| Technology | Version | Description |
|------------|---------|-------------|
| Java | 1.8 | Programming language |
| Spring Boot | 2.7.18 | Backend framework |
| Spring Security | 5.7.x | Authentication framework (JWT + custom filter) |
| Spring Data JPA | 2.7.x | ORM persistence layer |
| MySQL | 8.x | Relational database |
| JWT (jjwt) | 0.13.0 | Dual-token authentication (accessToken + refreshToken) |
| Lombok | 1.18.34 | Code simplification |
| Gson | - | JSON serialization (AuthUser stored in JWT Claims) |
| WebFlux WebClient | - | HTTP calls to Python AI inference service + Qwen LLM (with timeout) |
| iText | 7.1.2 | PDF report generation (professional radiology template + QR code embedding) |
| Apache POI | 5.2.5 | Word document generation (professional radiology template + QR code embedding) |
| ZXing | 3.5.3 | QR code image generation (ErrorCorrectionLevel.H) |
| DashScope (Qwen) | qwen-turbo | Dual-role AI chat for doctors/patients |
| Logback | - | Logging framework |

## System Architecture

```
┌────────────────────────────────┐   ┌────────────────────────────────┐
│  Doctor UI React (localhost:3000)│   │  Patient UI React (Mobile H5)   │
│  - Login / Report management     │   │  - QR scan access, no login     │
│  - Heatmap + color legend        │   │  - Simplified report view        │
│  - Doctor×AI chat (professional) │   │  - Patient×AI chat (plain lang)  │
└──────────────┬─────────────────┘   └──────────────┬─────────────────┘
               │ accessToken (JWT)                  │ accessCode (UUID)
               │ Vite proxy /api                    │ Independent axios instance
               ↓                                    ↓
┌─────────────────────────────────────────────────────────────────────┐
│          Java Spring Boot Backend (localhost:8887)                  │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  JwtAuthenticationFilter → JWT validation for doctor APIs     │  │
│  │  (/api/patient-chat/** in ignored.urls whitelist, skips JWT)  │  │
│  └───────────────────────────────────────────────────────────────┘  │
│  ┌─────────────────┬──────────────────┬──────────────────────────┐  │
│  │ Controller      │  Service          │  Mapper                   │  │
│  │ AuthController  │  DoctorService    │  DoctorMapper             │  │
│  │ PatientCtrl     │  PatientService   │  PatientMapper            │  │
│  │ ReportCtrl      │  ReportService    │  ReportMapper             │  │
│  │ StatsCtrl       │  StatsService     │  ChatMessageMapper        │  │
│  │ ExportCtrl      │  ExportService    │  PatientChatMapper        │  │
│  │ DoctorChatCtrl  │  DoctorChatSvc    │                           │  │
│  │ PatientChatCtrl │  PatientChatSvc   │                           │  │
│  │                 │  LlmService       │                           │  │
│  └─────────────────┴──────────────────┴──────────────────────────┘  │
│  WebClient calls:                                                    │
│   ├── Python R2GenGPT inference service (60s timeout)                │
│   └── Qwen LLM API (60s timeout)                                     │
└──────────────┬─────────────────────────────┬────────────────────────┘
               │                             │
               ↓                             ↓
    ┌──────────────────────┐     ┌──────────────────────────────┐
    │  MySQL               │     │  External AI Services         │
    │  - doctor            │     │  - R2GenGPT (192.168.1.81:8000)│
    │  - doctor_token      │     │    Swin Transformer + LLaMA   │
    │  - patient           │     │  - Qwen (dashscope)           │
    │  - report            │     │    qwen-turbo                 │
    │  - chat_message      │     └──────────────────────────────┘
    │  - patient_chat      │
    └──────────────────────┘
```

## Project Structure

```
com.medical
├── ReportSystemApplication.java              -- Application entry
├── common                                    -- Common module
│   ├── ResultMessage.java                    -- Unified response wrapper
│   ├── ResultCode.java                       -- Status code enum (User 20xxx/Patient 30xxx/Report 40xxx/File 50xxx/AI 60xxx/PatientChat 70xxx)
│   ├── ServiceException.java                 -- Custom business exception
│   ├── GlobalControllerExceptionHandler.java -- Global exception handler
│   ├── BaseEntity.java                       -- Base entity (id/createBy/createTime/updateBy/updateTime/deleteFlag)
│   ├── enums/
│   │   ├── SecurityEnum.java                 -- Security constants (HEADER_TOKEN/USER_CONTEXT)
│   │   ├── ReportStatusEnum.java             -- Report status enum (DRAFT/CONFIRMED/SIGNED)
│   │   ├── DiseaseEnum.java                  -- Disease type enum (with keyword matching)
│   │   ├── SessionTypeEnum.java              -- Chat session type (doctor_chat / patient_chat)
│   │   └── ChatRoleEnum.java                 -- Chat role (system / user / assistant)
│   ├── properties/
│   │   ├── IgnoredUrlsProperties.java        -- Ignored auth URL config
│   │   ├── LlmProperties.java                -- Qwen LLM config (apiKey/model/timeout)
│   │   └── PatientChatProperties.java        -- Patient scan config (frontendBaseUrl/expireDays/qrcodeSize)
│   ├── security/
│   │   ├── AuthUser.java                     -- Authorized user info
│   │   ├── UserContext.java                  -- Get current logged-in user
│   │   ├── SecurityBean.java                 -- BCryptPasswordEncoder + CORS config
│   │   ├── CustomAccessDeniedHandler.java    -- Access denied JSON response
│   │   ├── SecretKeyUtil.java                -- JWT signing key management
│   │   ├── Token.java                        -- Dual-token entity
│   │   ├── TokenUtil.java                    -- Token generation/refresh/validation
│   │   └── filter/
│   │       └── JwtAuthenticationFilter.java  -- JWT authentication filter
│   └── util/
│       ├── ResponseUtil.java                 -- JSON response utility for filters
│       ├── ResultUtil.java                   -- Response wrapping utility
│       ├── DateUtil.java                     -- Date utility
│       ├── QrCodeUtil.java                   -- QR code utility (ZXing wrapper, returns byte[] without disk I/O)
│       └── UuidUtils.java                    -- High-performance UUID generation (mica, 2-3x faster than JDK)
├── config/
│   └── SecurityConfig.java                   -- Spring Security core config
├── controller/
│   ├── AuthController.java                   -- Authentication endpoints
│   ├── PatientController.java                -- Patient management endpoints
│   ├── ReportController.java                 -- Diagnostic report endpoints
│   ├── StatsController.java                  -- Statistics endpoints
│   ├── ExportController.java                 -- Report export (PDF/Word with QR code)
│   ├── DoctorChatController.java             -- Doctor × AI chat (JWT-authenticated)
│   └── PatientChatController.java            -- Patient × AI chat (accessCode-based)
├── service/
│   ├── DoctorService.java
│   ├── PatientService.java
│   ├── ReportService.java                    -- Includes validateOwnership for reuse by other services
│   ├── StatsService.java
│   ├── ExportService.java
│   ├── LlmService.java                       -- Qwen LLM call abstraction
│   ├── DoctorChatService.java                -- Doctor chat business logic
│   ├── PatientChatService.java               -- Patient scan chat (access management + report view + AI chat)
│   └── impl/
│       ├── DoctorServiceImpl.java
│       ├── PatientServiceImpl.java
│       ├── ReportServiceImpl.java
│       ├── StatsServiceImpl.java
│       ├── ExportServiceImpl.java            -- iText/POI generates reports + embeds QR code in signature area
│       ├── LlmServiceImpl.java               -- WebClient calls DashScope API (qwen-turbo)
│       ├── DoctorChatServiceImpl.java        -- Doctor chat: professional English system prompt
│       └── PatientChatServiceImpl.java       -- Patient chat: plain language + disclaimer system prompt
├── mapper/
│   ├── DoctorMapper.java
│   ├── DoctorTokenMapper.java
│   ├── PatientMapper.java
│   ├── ReportMapper.java
│   ├── ChatMessageMapper.java                -- Chat messages (JPQL history query + soft delete)
│   └── PatientChatMapper.java                -- Patient access credentials (findByAccessCode)
└── entity/
    ├── dos/
    │   ├── Doctor.java
    │   ├── DoctorToken.java
    │   ├── Patient.java
    │   ├── Report.java
    │   ├── ChatMessage.java                  -- Chat message (session_type distinguishes doctor/patient)
    │   └── PatientChat.java                  -- Patient QR access credential
    ├── dto/
    │   ├── DoctorUpdateDTO.java
    │   ├── DoctorChatSendDTO.java            -- Doctor send message (reportId + message)
    │   ├── PatientChatSendDTO.java           -- Patient send message (accessCode + message)
    │   ├── ChatMessageDTO.java               -- LLM call params (role + content)
    │   └── DashScopeRequest.java             -- Qwen request body
    └── vos/
        ├── OverviewVO.java
        ├── MonthlyVolumeVO.java
        ├── DiseaseDistributionVO.java
        ├── ComparisonStatsVO.java
        ├── ComparisonRecordVO.java
        ├── EfficiencyVO.java
        ├── ChatMessageVO.java                -- Chat message VO (role/content/createTime)
        └── PatientReportVO.java              -- Patient-side simplified report (hides findings original text)
```

## Database Design

### doctor (Doctor Table)

| Field | Type | Description |
|-------|------|-------------|
| id | varchar(32) | Primary key, auto-generated UUID |
| username | varchar(50) | Username (unique index) |
| password | varchar(255) | Password (BCrypt-encrypted, hidden in API responses) |
| real_name | varchar(50) | Real name |
| department | varchar(50) | Department |
| phone | varchar(20) | Phone number |
| email | varchar(100) | Email |
| enabled | bit(1) | Enabled flag, default true |
| create_by~delete_flag | - | Same as BaseEntity |

### doctor_token (Doctor Token Table)

| Field | Type | Description |
|-------|------|-------------|
| id | varchar(32) | Primary key |
| doctor_id | varchar(32) | Doctor ID (indexed) |
| access_token | text | Access token (JWT, variable length) |
| refresh_token | text | Refresh token |
| expire_time | datetime | accessToken expiration |
| refresh_expire_time | datetime | refreshToken expiration |
| create_time | datetime(6) | Creation time |

### patient (Patient Table)

| Field | Type | Description |
|-------|------|-------------|
| id | varchar(32) | Primary key |
| patient_no | varchar(50) | Patient number (indexed) |
| name | varchar(50) | Name |
| gender | varchar(10) | Gender |
| age | int | Age |
| medical_history | varchar(500) | Medical history |
| create_by~delete_flag | - | Same as BaseEntity |

### report (Diagnostic Report Table)

| Field | Type | Description |
|-------|------|-------------|
| id | varchar(32) | Primary key |
| doctor_id | varchar(32) | Doctor ID (indexed, for authorization checks) |
| patient_id | varchar(32) | Patient ID (indexed) |
| image_path | varchar(255) | Image file path |
| report_content | text | Doctor-confirmed report content |
| ai_draft | text | AI-generated original report content (for AI vs doctor comparison) |
| impression | text | AI-generated impression/conclusion |
| gate | varchar(20) | Tri-state gating: normal/findings/uncertain |
| report_confidence | double | Report confidence |
| findings_keywords | text | Keywords JSON |
| heatmap_path | longtext | Heatmap base64 JSON data |
| pdf_path | varchar(255) | PDF report path |
| status | varchar(20) | Report status: DRAFT/CONFIRMED/SIGNED |
| create_by~delete_flag | - | Same as BaseEntity |

### chat_message (Chat Message Table)

| Field | Type | Description |
|-------|------|-------------|
| id | varchar(32) | Primary key |
| report_id | varchar(32) | Associated report ID (indexed) |
| session_type | varchar(20) | Session type: doctor_chat / patient_chat |
| role | varchar(20) | Role: user / assistant |
| content | text | Message content |
| create_by~delete_flag | - | Same as BaseEntity (for patient context, create_by="patient") |

**Index**: `idx_report_session(report_id, session_type)` — fast query for a report's conversation history

### patient_chat (Patient QR Access Credential Table)

| Field | Type | Description |
|-------|------|-------------|
| id | varchar(32) | Primary key |
| report_id | varchar(32) | Associated report ID (indexed) |
| access_code | varchar(64) | Access code (32-char UUID, unique index) |
| expire_time | datetime | Expiration time (default 30 days) |
| revoked | bit(1) | Revoked flag |
| create_by~delete_flag | - | Same as BaseEntity (create_by records the issuing doctor) |

**Index**: `uk_access_code` (unique) + `idx_report_id`

## Dual Authentication System

### Doctor: JWT Dual-Token Authentication

```
Login:
  Username/password → DoctorServiceImpl.login()
  → BCryptPasswordEncoder.matches() verify password
  → TokenUtil.createToken() generates accessToken + refreshToken
  → Save to doctor_token table → Return dual tokens

Request Authentication:
  Header carries accessToken → JwtAuthenticationFilter intercepts
  → Parse JWT, extract AuthUser → Verify token existence in DB
  → Pass → Set SecurityContext → Allow
  → Fail → 403 JSON

Token Refresh:
  accessToken expired → Call /refresh/{refreshToken} with refreshToken
  → TokenUtil.refreshToken() → Return new dual tokens

Logout:
  DoctorService.logout() → Delete doctor_token record
```

### Patient: accessCode Resource Credential

```
Generation (when doctor exports report):
  ExportService.exportPdf/Word()
  → PatientChatService.createAccessCode(reportId, doctorId)
  → UuidUtils.getUUID() generates 32-char hex access code
  → Save to patient_chat table (30-day expiration)
  → Return PatientChat entity
  → QrCodeUtil.generatePng() generates QR code image
  → Embed into bottom-right signature area of PDF/Word

Patient Access:
  Mobile scans QR → Browser opens http://xxx/patient/chat/{accessCode}
  → Frontend sends accessCode to /api/patient-chat/report/{accessCode}
  → [JWT filter skipped: /api/patient-chat/** in ignored.urls whitelist]
  → PatientChatService.validateAccessCode(accessCode)
    → Query patient_chat table
    → Check revoked / expire_time
  → Return simplified report VO (hides doctor info and findings original text)
```

### Comparison of Two Approaches

| Dimension | JWT (Doctor) | accessCode (Patient) |
|-----------|--------------|---------------------|
| Requires account | ✅ Login required | ❌ No account needed |
| Credential format | JWT (200+ chars) | UUID (32 chars) |
| Identifies | Doctor account | A specific report |
| Validation method | Signature verification + DB check | Database existence query |
| Accessible data scope | All doctor's data | **Only the bound report** |
| Expiration management | JWT exp claim | DB expire_time field |
| Use case | Internal system auth | One-time external QR access |

## LLM Chat System

### Dual-Role System Prompt Design

The same LLM (Qwen qwen-turbo) implements doctor/patient dual-role chat through **different system prompts**.

#### Doctor Side (Professional Mode)

```
You are an experienced radiologist AI assistant, helping doctors 
review and interpret chest X-ray AI-generated reports.
- Answer in concise, professional, accurate English
- May discuss differential diagnoses, imaging findings, treatment suggestions
- Current report context: [Impression] [Report] [Detected findings] [AI confidence]
```

#### Patient Side (Plain-Language Mode)

```
You are a friendly health assistant helping a patient understand their report.
1. Use simple, everyday language. Avoid medical jargon.
2. Be warm, empathetic, reassuring.
3. DO NOT give specific treatment recommendations, drug names, or dosages.
4. DO NOT make a definitive diagnosis.
5. For serious concerns, ALWAYS recommend consulting their doctor.
6. Keep answers concise (3-5 sentences).
```

### Actual Chat Comparison

**Same report, different roles, different responses**:

| Doctor's Question | Patient's Question |
|-------------------|--------------------|
| "Why is this diagnosed as emphysema?" | "What does this diagnosis mean?" |
| **AI reply**: cites "pulmonary hyperinflation" / "flattening of diaphragms" / confidence 0.3826, gives 7 differential diagnoses, drug names (roflumilast), numerical criteria (PaO₂ < 55 mmHg) | **AI reply**: "your lungs appear to be more inflated than usual, which can happen in emphysema. Emphysema is a lung disease that makes it harder to breathe..." + recommends consulting a doctor |

### Multi-Turn Context

All historical messages are retrieved from the `chat_message` table in chronological order and passed to the LLM → **AI can continue the discussion based on prior context**. For example:
- Q1: "Why is this diagnosed as emphysema?"
- Q2: "What differential diagnoses should I consider?" → AI gives differentials directly without re-explaining the condition

## API Endpoints

### Authentication (/api/auth)

| Method | Path | Description | Auth | Parameters |
|--------|------|-------------|------|------------|
| POST | /login | Login | No | @RequestParam: username, password |
| POST | /register | Register | No | @RequestParam: username, password, realName, department |
| GET | /refresh/{refreshToken} | Refresh Token | No | @PathVariable |
| GET | /info | Get current doctor info | Yes | None |
| POST | /logout | Logout | Yes | None |
| PUT | /update | Update personal info | Yes | @RequestBody: DoctorUpdateDTO |
| PUT | /password | Change password | Yes | @RequestParam: oldPassword, newPassword |

### Patient (/api/patient) - JWT required

| Method | Path | Description |
|--------|------|-------------|
| POST | /add | Add patient |
| GET | /get/{id} | Get patient |
| GET | /search | Search patients (by name) |
| GET | /list | Patient list |

### Report (/api/report) - JWT required

| Method | Path | Description |
|--------|------|-------------|
| POST | /generate | Generate report (calls AI inference service) |
| GET | /getDetail/{id} | Get report detail |
| GET | /list/patient/{patientId} | Patient's historical reports |
| GET | /list/mine | Current doctor's reports |
| PUT | /status/{id} | Update status (with authorization check) |
| PUT | /content/{id} | Update report content (with authorization check) |

### Statistics (/api/stats) - JWT required

| Method | Path | Description | Data Source |
|--------|------|-------------|-------------|
| GET | /overview | Overview stats | DB COUNT queries |
| GET | /monthly | Monthly report volume | GROUP BY + DATE_FORMAT |
| GET | /disease | Disease distribution | Keyword matching (DiseaseEnum) |
| GET | /comparison | AI comparison stats | aiDraft vs reportContent |
| GET | /comparison/records | AI comparison records | Modified reports |
| GET | /efficiency | AI efficiency stats | AVG + TIMESTAMPDIFF |

### Export (/api/export) - JWT required

| Method | Path | Description | Output |
|--------|------|-------------|--------|
| GET | /pdf/{reportId} | Export PDF (with QR code) | application/pdf stream |
| GET | /word/{reportId} | Export Word (with QR code) | application/docx stream |

**Export template** (unified for PDF and Word):
- Patient Information — name/age/gender/medical record number
- Technique — examination technique
- Findings — AI-generated imaging findings
- Impressions — AI-generated impression/conclusion
- Recommendations — recommendations (reserved)
- **Signature** — doctor signature area (name/date/signature/**QR code for AI chat**)
- Disclaimer — AI-assisted generation statement

### Doctor Chat (/api/doctor-chat) - JWT required

| Method | Path | Description | Parameters |
|--------|------|-------------|------------|
| POST | /send | Doctor sends message, gets AI reply | @RequestBody: DoctorChatSendDTO |
| GET | /history/{reportId} | Get chat history | @PathVariable |
| DELETE | /history/{reportId} | Clear chat history (soft delete) | @PathVariable |

### Patient Chat (/api/patient-chat) - accessCode-based, **no JWT**

| Method | Path | Description | Parameters |
|--------|------|-------------|------------|
| GET | /report/{accessCode} | Get simplified report | @PathVariable |
| POST | /send | Patient sends message, gets AI reply | @RequestBody: PatientChatSendDTO |
| GET | /history/{accessCode} | Get chat history | @PathVariable |

**Characteristics**:
- Controller does not rely on UserContext (patients have no login state)
- All validation based on accessCode (queries patient_chat table)
- The `PatientReportVO` returned to patients **hides** findings original text, doctor info, heatmap, and confidence (to avoid patient anxiety from technical terms)

## Exception Handling

Global exception handler (GlobalControllerExceptionHandler) catches four exception types:

| Exception Type | Handling |
|----------------|----------|
| ServiceException | Extract ResultCode and return business error |
| RuntimeException | Return generic error, prevent info leakage |
| BindException | Extract field validation errors |
| ConstraintViolationException | Extract constraint violation info |

Unified response format:

```json
{
    "success": false,
    "message": "Error message",
    "code": 20002,
    "timestamp": 1786278063727,
    "result": null
}
```

### Status Code Convention

| Range | Module | Example |
|-------|--------|---------|
| 200 | Success | SUCCESS |
| 400 | Generic error | ERROR |
| 20xxx | User-related | USER_NOT_EXIST / USER_PASSWORD_ERROR |
| 30xxx | Patient-related | PATIENT_NOT_EXIST / PATIENT_NO_EXIST |
| 40xxx | Report-related | REPORT_NOT_EXIST / REPORT_GENERATE_ERROR |
| 50xxx | File-related | FILE_NOT_EXIST_ERROR / FILE_SIZE_EXCEED |
| 60xxx | AI service | AI_SERVICE_ERROR / AI_SERVICE_TIMEOUT / LLM_SERVICE_ERROR / LLM_SERVICE_TIMEOUT / LLM_RESPONSE_EMPTY / LLM_RESPONSE_INVALID |
| 70xxx | Patient QR chat | PATIENT_CHAT_INVALID / PATIENT_CHAT_EXPIRED / PATIENT_CHAT_REVOKED |

## Configuration (application.yml)

| Config | Description | Default |
|--------|-------------|---------|
| server.port | Server port | 8887 |
| spring.datasource | MySQL connection | localhost:3306/medical_report |
| spring.jpa.hibernate.ddl-auto | DDL strategy | update |
| spring.servlet.multipart.max-file-size | Upload size limit | 50MB |
| **ignored.urls** | JWT whitelist | /api/auth/login, /api/auth/register, /api/auth/refresh/\*\*, **/api/patient-chat/\*\*** |
| ai.service.url | Python AI inference service | http://192.168.1.81:8000 |
| ai.baseline.avg-report-time | AI efficiency baseline (min) | 15 |
| file.upload-dir | Upload directory | uploads/ |
| **llm.dashscope.base-url** | Qwen API endpoint | https://dashscope.aliyuncs.com |
| **llm.dashscope.api-key** | Qwen API Key | sk-xxx |
| **llm.dashscope.model** | Model name | qwen-turbo |
| **llm.dashscope.timeout-seconds** | Timeout (seconds) | 60 |
| **llm.dashscope.max-tokens** | Max generation length | 1500 |
| **patient-chat.frontend-base-url** | Patient frontend URL (used in QR code) | http://192.168.1.XXX:3000 |
| **patient-chat.expire-days** | Access code validity (days) | 30 |
| **patient-chat.qrcode-size** | QR code image size (px) | 180 |

## Coding Conventions

This project strictly follows the coding conventions:

### Layering
- Strict Controller → Service → Mapper separation
- Controllers do not inject Mappers directly (ExportService is legacy, planned for refactoring)

### Parameter Passing
- Simple params (1-3): @RequestParam
- Complex params (4+): DTO + @RequestBody + validation annotations
- Path params: @PathVariable
- No Map for request/response data

### Return Values
- Unified ResultMessage<T>, use ResultUtil.data()/success()/error()
- List queries return VO, not entity classes directly
- File exports via HttpServletResponse stream

### Exception Handling
- Business exceptions: throw new ServiceException(ResultCode.XXX)
- Each error scenario has a dedicated ResultCode enum value
- Avoid throw new RuntimeException("xxx")

### Code Style
- Hardcoded strings use enum classes (ReportStatusEnum / DiseaseEnum / SessionTypeEnum / ChatRoleEnum)
- Date operations via DateUtil utility
- QR code generation via QrCodeUtil utility
- UUID generation via UuidUtils (high-performance)
- Repeated logic extracted into private methods
- Statistical queries pushed to the database (COUNT/GROUP BY/AVG), not computed in memory
- Lambdas and Streams used appropriately; complex logic kept as for-loops

### Security
- "My data" obtained from UserContext for doctorId, never passed from the frontend
- "Others' data" verified at the Service layer (validateOwnership)
- File uploads triple-checked (size/extension/MIME)
- Patient QR scenarios use whitelist, independent accessCode validation, no JWT dependency

## Startup

### 1. Database Setup
```bash
mysql -u root -p
> source DB/medical-report.sql;   # Creates medical_report database + all tables
```

### 2. Configuration

Modify `application.yml`:

- **Database connection**: username / password
- **AI inference service address**: `ai.service.url` (desktop IP)
- **Qwen API Key**: `llm.dashscope.api-key`
- **Patient frontend URL**: `patient-chat.frontend-base-url` (LAN IP, use `ipconfig` to check)

### 3. Launch Order

```bash
# 1. Start Python inference service (on desktop with GPU)
cd R2GenGPT && python api.py

# 2. Start Spring Boot backend
# Run ReportSystemApplication.main() in IDEA
# Or: mvn spring-boot:run

# 3. Start frontend
cd medical-report-ui && npm run dev
```

### 4. Access

- Doctor UI: `http://localhost:3000`
- Backend: `http://localhost:8887`
- Patient UI (mobile scans QR in PDF): `http://{LAN-IP}:3000/patient/chat/{accessCode}`

### 5. Mobile Testing Notes

- Mobile and PC must be on the **same Wi-Fi**
- Vite requires `server.host: '0.0.0.0'` for LAN access
- Windows Firewall must allow ports 3000 and 8887