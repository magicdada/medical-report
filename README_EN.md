# Medical Imaging Report Generation System - Backend Service

## Overview

A deep learning-based chest X-ray diagnostic report generation system. Doctors upload chest X-ray images, and the system automatically invokes the AI model (R2GenGPT: Swin Transformer + LLaMA-2-7B) to generate radiology diagnostic reports. Features include report viewing, editing, confirmation and signing, history management, AI confidence visualization, attention heatmap display, tri-state gating classification (normal/findings/uncertain), AI vs. doctor report comparison analysis, statistics dashboard, and professional PDF/Word export.

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
| WebFlux WebClient | - | HTTP calls to Python AI inference service (with timeout) |
| iText | 7.1.2 | PDF report generation (professional radiology template) |
| Apache POI | 5.2.5 | Word document generation (professional radiology template) |
| Logback | - | Logging framework |

## System Architecture

```
React Frontend (localhost:3000)
    ↕ HTTP Requests (accessToken in Header)
    ↕ Vite proxy forwards /api → localhost:8887
Java Spring Boot Backend (localhost:8887)
    ├── JwtAuthenticationFilter → Parse token, verify in DB, set SecurityContext
    ├── Controller Layer → Receive requests, validate params, call Service
    ├── Service Layer → Business logic, authorization checks
    ├── Mapper Layer → JPA database operations (COUNT/GROUP BY pushed to DB)
    ├── MySQL → Store doctor, patient, report, token data
    └── WebClient → Call Python AI inference service (60s timeout)
    ↕ HTTP Multipart Request (send image files, receive report JSON)
Python FastAPI Inference Service (Desktop 192.168.1.81:8000)
    └── R2GenGPT Model
        ├── Swin Transformer (Visual Encoder)
        ├── MultiGranularityFusion (Multi-granularity Visual Feature Fusion: Stage 2/3/4)
        ├── EnhancedProjection (Enhanced Mapping Module: 2-layer MLP + GELU)
        └── LLaMA-2-7B-Chat (4-bit Quantization, Report Generation)
```

## Project Structure

```
com.medical
├── ReportSystemApplication.java              -- Application entry
├── common                                    -- Common module
│   ├── ResultMessage.java                    -- Unified response wrapper
│   ├── ResultCode.java                       -- Status code enum (User 20xxx/Patient 30xxx/Report 40xxx/File 50xxx/AI 60xxx)
│   ├── ServiceException.java                 -- Custom business exception
│   ├── GlobalControllerExceptionHandler.java -- Global exception handler (ServiceException/Runtime/Bind/Constraint)
│   ├── BaseEntity.java                       -- Base entity (id/createBy/createTime/updateBy/updateTime/deleteFlag)
│   ├── enums/
│   │   ├── SecurityEnum.java                 -- Security constants (HEADER_TOKEN/USER_CONTEXT)
│   │   ├── ReportStatusEnum.java             -- Report status enum (DRAFT/CONFIRMED/SIGNED)
│   │   └── DiseaseEnum.java                  -- Disease type enum (with keyword matching)
│   ├── properties/
│   │   └── IgnoredUrlsProperties.java        -- Auth-ignored URL config (reads yml ignored.urls)
│   ├── security/
│   │   ├── AuthUser.java                     -- Authorized user info (JSON serialized into JWT Claims)
│   │   ├── UserContext.java                  -- Get current user (parse token from Request Header)
│   │   ├── SecurityBean.java                 -- BCryptPasswordEncoder + CORS config
│   │   ├── CustomAccessDeniedHandler.java    -- Access denied JSON response
│   │   ├── SecretKeyUtil.java                -- JWT signing key management (Base64 encoded)
│   │   ├── Token.java                        -- Dual-token entity (accessToken + refreshToken)
│   │   ├── TokenUtil.java                    -- Token create/refresh/verify (DB stored, transaction-safe)
│   │   └── filter/
│   │       └── JwtAuthenticationFilter.java  -- JWT auth filter (extends BasicAuthenticationFilter)
│   └── util/
│       ├── ResponseUtil.java                 -- JSON response utility for Filters
│       ├── ResultUtil.java                   -- Result wrapper utility
│       └── DateUtil.java                     -- Date utility (monthly stats, formatting)
├── config/
│   └── SecurityConfig.java                   -- Spring Security config (WebSecurityConfigurerAdapter)
├── controller/
│   ├── AuthController.java                   -- Auth API (login/register/refresh/info/logout/update/password)
│   ├── PatientController.java                -- Patient API (add/get/search/list)
│   ├── ReportController.java                 -- Report API (generate/query/status/content edit, with auth checks)
│   ├── StatsController.java                  -- Stats API (overview/monthly/disease/efficiency/comparison)
│   └── ExportController.java                 -- Export API (PDF/Word via HttpServletResponse stream)
├── service/
│   ├── DoctorService.java                    -- Doctor service interface
│   ├── PatientService.java                   -- Patient service interface
│   ├── ReportService.java                    -- Report service interface
│   ├── StatsService.java                     -- Stats service interface
│   ├── ExportService.java                    -- Export service interface
│   └── impl/
│       ├── DoctorServiceImpl.java            -- Doctor service (register/login/logout/update/password)
│       ├── PatientServiceImpl.java           -- Patient service
│       ├── ReportServiceImpl.java            -- Report service (file validation/WebClient AI call/auth checks)
│       ├── StatsServiceImpl.java             -- Stats service (DB aggregate queries, not in-memory)
│       └── ExportServiceImpl.java            -- Export service (iText PDF/POI Word, professional radiology template)
├── mapper/
│   ├── DoctorMapper.java                     -- Doctor data access layer
│   ├── DoctorTokenMapper.java                -- Doctor token data access layer
│   ├── PatientMapper.java                    -- Patient data access layer
│   └── ReportMapper.java                     -- Report data access layer (with native SQL stats queries)
└── entity/
    ├── dos/
    │   ├── Doctor.java                       -- Doctor entity (password @JsonProperty WRITE_ONLY)
    │   ├── DoctorToken.java                  -- Doctor token entity
    │   ├── Patient.java                      -- Patient entity
    │   └── Report.java                       -- Report entity (aiDraft/impression/gate/confidence fields)
    ├── dto/
    │   └── DoctorUpdateDTO.java              -- Doctor update DTO (with @Email validation)
    └── vos/
        ├── OverviewVO.java                   -- Overview stats VO
        ├── MonthlyVolumeVO.java              -- Monthly volume VO
        ├── DiseaseDistributionVO.java        -- Disease distribution VO
        ├── ComparisonStatsVO.java            -- AI comparison stats VO
        ├── ComparisonRecordVO.java           -- AI comparison record VO (with patientName)
        └── EfficiencyVO.java                 -- AI efficiency stats VO
```

## Database Design

### doctor

| Column | Type | Description |
|--------|------|-------------|
| id | varchar(32) | Primary key, auto-generated UUID |
| username | varchar(50) | Username (unique index) |
| password | varchar(255) | Password (BCrypt encrypted, hidden in API responses) |
| real_name | varchar(50) | Real name |
| department | varchar(50) | Department |
| phone | varchar(20) | Phone number |
| email | varchar(100) | Email |
| enabled | bit(1) | Enabled flag, default true |
| create_by | varchar(50) | Created by |
| create_time | datetime(6) | Created time (@PrePersist auto-fill) |
| update_by | varchar(50) | Updated by |
| update_time | datetime(6) | Updated time (@PreUpdate auto-fill) |
| delete_flag | bit(1) | Soft delete flag, default false |

### doctor_token

| Column | Type | Description |
|--------|------|-------------|
| id | varchar(32) | Primary key, UUID |
| doctor_id | varchar(32) | Doctor ID (indexed) |
| access_token | text | Access token |
| refresh_token | text | Refresh token |
| expire_time | datetime | Access token expiry |
| refresh_expire_time | datetime | Refresh token expiry (2x access token) |
| create_time | datetime(6) | Created time |

### patient

| Column | Type | Description |
|--------|------|-------------|
| id | varchar(32) | Primary key, UUID |
| patient_no | varchar(50) | Patient number (indexed) |
| name | varchar(50) | Name |
| gender | varchar(10) | Gender |
| age | int | Age |
| medical_history | varchar(500) | Medical history |
| create_by~delete_flag | - | Same as BaseEntity |

### report

| Column | Type | Description |
|--------|------|-------------|
| id | varchar(32) | Primary key, UUID |
| doctor_id | varchar(32) | Doctor ID (indexed, for authorization) |
| patient_id | varchar(32) | Patient ID (indexed) |
| image_path | varchar(255) | Image file path |
| report_content | text | Doctor-confirmed report content |
| ai_draft | text | AI original generated content (for AI vs. doctor comparison) |
| impression | text | AI-generated impression/conclusion |
| gate | varchar(20) | Tri-state gate: normal/findings/uncertain |
| report_confidence | double | Report confidence score |
| findings_keywords | text | Findings keywords JSON |
| heatmap_path | longtext | Heatmap base64 JSON data |
| pdf_path | varchar(255) | PDF report path |
| status | varchar(20) | Report status: DRAFT/CONFIRMED/SIGNED |
| create_by~delete_flag | - | Same as BaseEntity |

## Security Design

### JWT Dual-Token Authentication Flow

```
Login:
  Doctor submits username/password → DoctorServiceImpl.login() queries DB
  → BCryptPasswordEncoder.matches() verifies password
  → TokenUtil.createToken() generates accessToken + refreshToken
  → Stored in doctor_token table → Returns dual tokens

Request Authentication:
  Frontend sends accessToken in Header → JwtAuthenticationFilter intercepts
  → Parse JWT, extract AuthUser → Verify token exists in DB
  → Pass → Set SecurityContext → Allow request
  → Fail → Return 403 JSON {"message":"Not logged in or token expired"}

Token Refresh:
  accessToken expired → Frontend sends refreshToken to refresh endpoint
  → TokenUtil.refreshToken() validates and generates new dual tokens
  → Delete old tokens → Store new tokens → Return

Logout:
  DoctorService.logout() → Delete token records from DB
  → Frontend clears localStorage → Redirect to login page
```

### Authorization

- Report status update: Service layer verifies `report.getDoctorId().equals(doctorId)` to prevent unauthorized changes
- Report content edit: Service layer verifies report ownership
- Report export: Service layer verifies report ownership before download
- Sensitive data: Doctor.password uses `@JsonProperty(access = WRITE_ONLY)` to prevent serialization

### File Upload Security

- Empty file check (ResultCode.FILE_NOT_EXIST_ERROR)
- File size limit 50MB (ResultCode.FILE_SIZE_EXCEED)
- Extension whitelist .jpg/.jpeg/.png/.dcm (ResultCode.FILE_EXTENSION_NOT_ALLOWED)
- MIME type whitelist check (ResultCode.FILE_TYPE_NOT_SUPPORT)

## API Endpoints

### Auth API (/api/auth)

| Method | Path | Description | Auth | Params |
|--------|------|-------------|------|--------|
| POST | /login | Login | No | @RequestParam: username, password |
| POST | /register | Register | No | @RequestParam: username, password, realName, department |
| GET | /refresh/{refreshToken} | Refresh token | No | @PathVariable: refreshToken |
| GET | /info | Get current doctor info | Yes | None |
| POST | /logout | Logout | Yes | None |
| PUT | /update | Update profile | Yes | @RequestBody: DoctorUpdateDTO |
| PUT | /password | Change password | Yes | @RequestParam: oldPassword, newPassword |

### Patient API (/api/patient) - Token Required

| Method | Path | Description | Params |
|--------|------|-------------|--------|
| POST | /add | Add patient | @RequestBody: Patient |
| GET | /get/{id} | Get patient | @PathVariable: id |
| GET | /search | Search patients | @RequestParam: name |
| GET | /list | List patients | None |

### Report API (/api/report) - Token Required

| Method | Path | Description | Params |
|--------|------|-------------|--------|
| POST | /generate | Generate report (calls AI service) | @RequestParam: patientId, files |
| GET | /getDetail/{id} | Get report detail | @PathVariable: id |
| GET | /list/patient/{patientId} | Patient report history | @PathVariable: patientId |
| GET | /list/mine | Current doctor's reports | None (doctorId from UserContext) |
| PUT | /status/{id} | Update status (with auth check) | @PathVariable: id, @RequestParam: status |
| PUT | /content/{id} | Update report content (with auth check) | @PathVariable: id, @RequestParam: reportContent |

### Stats API (/api/stats) - Token Required

| Method | Path | Description | Data Source |
|--------|------|-------------|------------|
| GET | /overview | Overview stats | DB COUNT queries |
| GET | /monthly | Monthly volume | DB GROUP BY + DATE_FORMAT |
| GET | /disease | Disease distribution | Report content keyword matching (DiseaseEnum) |
| GET | /comparison | AI comparison stats | aiDraft vs reportContent comparison |
| GET | /comparison/records | AI comparison records | Modified reports query (with patientName) |
| GET | /efficiency | AI efficiency stats | DB AVG + TIMESTAMPDIFF |

### Export API (/api/export) - Token Required

| Method | Path | Description | Output |
|--------|------|-------------|--------|
| GET | /pdf/{reportId} | Export PDF (with auth check) | application/pdf stream (filename: patientName_date.pdf) |
| GET | /word/{reportId} | Export Word (with auth check) | application/docx stream (filename: patientName_date.docx) |

### Export Report Template

Both PDF and Word exports use a unified professional radiology report template containing:

- **Patient Information** — Name, age, gender, medical record number
- **Technique** — Examination technique (PA and Lateral Chest Radiograph)
- **Findings** — AI-generated imaging findings
- **Impressions** — AI-generated impression/conclusion
- **Recommendations** — Reserved blank area
- **Signature** — Radiologist signature area (name, date, handwritten signature)
- **Disclaimer** — AI-assisted generation statement

## Exception Handling

The system uses a global exception handler (GlobalControllerExceptionHandler) that catches four exception types:

| Exception Type | Handling |
|---------------|----------|
| ServiceException | Extract ResultCode and return business error message |
| RuntimeException | Return generic error message to prevent internal info leakage |
| BindException | Extract field validation errors (@NotNull/@Email etc.) |
| ConstraintViolationException | Extract constraint violation messages |

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

### Status Code Convention (ResultCode Enum)

| Range | Module | Examples |
|-------|--------|----------|
| 200 | Success | SUCCESS |
| 400 | General error | ERROR |
| 20xxx | User | USER_NOT_EXIST / USER_PASSWORD_ERROR / USER_STATUS_ERROR |
| 30xxx | Patient | PATIENT_NOT_EXIST / PATIENT_NO_EXIST |
| 40xxx | Report | REPORT_NOT_EXIST / REPORT_GENERATE_ERROR |
| 50xxx | File | FILE_NOT_EXIST_ERROR / FILE_SIZE_EXCEED / FILE_EXTENSION_NOT_ALLOWED |
| 60xxx | AI Service | AI_SERVICE_ERROR / AI_SERVICE_TIMEOUT |

## Configuration (application.yml)

| Config | Description | Default |
|--------|-------------|---------|
| server.port | Server port | 8887 |
| spring.datasource | MySQL connection config | localhost:3306/medical_report |
| spring.jpa.hibernate.ddl-auto | DDL strategy | update |
| spring.jpa.open-in-view | Lazy loading | false |
| spring.autoconfigure.exclude | Exclude UserDetailsServiceAutoConfiguration | Configured |
| spring.servlet.multipart.max-file-size | Max upload size | 50MB |
| ignored.urls | URLs without token auth | /api/auth/login, /api/auth/register, /api/auth/refresh/** |
| ai.service.url | Python AI inference service URL | http://192.168.1.81:8000 |
| ai.baseline.avg-report-time | AI efficiency baseline (minutes) | 15 |
| file.upload-dir | Image upload directory | uploads/ |
| logging.level.org.hibernate.SQL | SQL log level | debug |

## Getting Started

1. Ensure MySQL is running, execute `medical-report.sql` to create the `medical_report` database and tables
2. Update database connection and AI service URL in `application.yml`
3. Run `ReportSystemApplication.main()` in IntelliJ IDEA
4. Access the service at `http://localhost:8887`
5. Ensure the Python inference service is running on the desktop machine (`python api.py`), otherwise report generation will be unavailable