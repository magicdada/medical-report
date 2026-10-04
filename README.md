# 医学影像报告生成系统 - 后端服务

## 项目简介

本系统是一个基于深度学习的胸部X光医学影像报告自动生成系统。医生上传胸部X光影像后,系统自动调用AI模型生成放射科诊断报告。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8 | 开发语言 |
| Spring Boot | 2.7.18 | 后端框架 |
| Spring Security | 5.7.x | 安全认证框架(JWT + 自定义过滤器) |
| Spring Data JPA | 2.7.x | ORM持久层框架 |
| MySQL | 8.x | 关系型数据库 |
| JWT (jjwt) | 0.13.0 | 双Token认证(accessToken + refreshToken) |
| Lombok | 1.18.34 | 简化代码 |
| Gson | - | JSON序列化(AuthUser存入JWT Claims) |
| WebFlux WebClient | - | HTTP调用Python AI推理服务 + 通义千问大模型(带超时控制) |
| iText | 7.1.2 | PDF报告生成(专业放射科报告模板 + QR码嵌入) |
| Apache POI | 5.2.5 | Word文档生成(专业放射科报告模板 + QR码嵌入) |
| ZXing | 3.5.3 | QR码图片生成(ErrorCorrectionLevel.H) |
| DashScope(通义千问) | qwen-turbo | 医生/患者双角色 AI 对话 |
| Logback | - | 日志框架 |

## 系统架构

```
┌────────────────────────────────┐   ┌────────────────────────────────┐
│  医生端 React (localhost:3000)   │   │  患者端 React (手机 H5)         │
│  - 登录 / 报告管理                │   │  - 扫码访问,无需登录            │
│  - 热力图可视化 + 颜色图例         │   │  - 简化版报告展示                │
│  - 医生×AI 对话(专业术语)         │   │  - 患者×AI 对话(通俗语言)       │
└──────────────┬─────────────────┘   └──────────────┬─────────────────┘
               │ accessToken(JWT)                   │ accessCode(UUID)
               │ Vite代理 /api                       │ 独立 axios 实例
               ↓                                    ↓
┌─────────────────────────────────────────────────────────────────────┐
│          Java Spring Boot 后端 (localhost:8887)                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  JwtAuthenticationFilter → 医生接口 JWT 校验                   │  │
│  │  (/api/patient-chat/** 在 ignored.urls 白名单内,跳过 JWT)      │  │
│  └───────────────────────────────────────────────────────────────┘  │
│  ┌─────────────────┬──────────────────┬──────────────────────────┐  │
│  │ Controller 层    │  Service 层       │  Mapper 层                │  │
│  │ AuthController   │  DoctorService    │  DoctorMapper             │  │
│  │ PatientCtrl      │  PatientService   │  PatientMapper            │  │
│  │ ReportCtrl       │  ReportService    │  ReportMapper             │  │
│  │ StatsCtrl        │  StatsService     │  ChatMessageMapper        │  │
│  │ ExportCtrl       │  ExportService    │  PatientChatMapper        │  │
│  │ DoctorChatCtrl   │  DoctorChatSvc    │                           │  │
│  │ PatientChatCtrl  │  PatientChatSvc   │                           │  │
│  │                  │  LlmService       │                           │  │
│  └─────────────────┴──────────────────┴──────────────────────────┘  │
│  WebClient 调用:                                                     │
│   ├── Python R2GenGPT 推理服务(60s 超时)                             │
│   └── 通义千问大模型 API(60s 超时)                                   │
└──────────────┬─────────────────────────────┬────────────────────────┘
               │                             │
               ↓                             ↓
    ┌──────────────────────┐     ┌──────────────────────────────┐
    │  MySQL               │     │  外部 AI 服务                 │
    │  - doctor            │     │  - R2GenGPT (192.168.1.81:8000)│
    │  - doctor_token      │     │    Swin Transformer + LLaMA   │
    │  - patient           │     │  - 通义千问 (dashscope)        │
    │  - report            │     │    qwen-turbo                 │
    │  - chat_message      │     └──────────────────────────────┘
    │  - patient_chat      │
    └──────────────────────┘
```

## 项目结构

```
com.medical
├── ReportSystemApplication.java              -- 启动类
├── common                                    -- 通用模块
│   ├── ResultMessage.java                    -- 统一返回结果封装
│   ├── ResultCode.java                       -- 状态码枚举(用户20xxx/患者30xxx/报告40xxx/文件50xxx/AI服务60xxx/患者对话70xxx)
│   ├── ServiceException.java                 -- 自定义业务异常
│   ├── GlobalControllerExceptionHandler.java -- 全局异常处理
│   ├── BaseEntity.java                       -- 数据库基础实体类(id/createBy/createTime/updateBy/updateTime/deleteFlag)
│   ├── enums/
│   │   ├── SecurityEnum.java                 -- 安全常量(HEADER_TOKEN/USER_CONTEXT)
│   │   ├── ReportStatusEnum.java             -- 报告状态枚举(DRAFT/CONFIRMED/SIGNED)
│   │   ├── DiseaseEnum.java                  -- 疾病类型枚举(含关键词匹配方法)
│   │   ├── SessionTypeEnum.java              -- 对话会话类型(doctor_chat / patient_chat)
│   │   └── ChatRoleEnum.java                 -- 对话角色(system / user / assistant)
│   ├── properties/
│   │   ├── IgnoredUrlsProperties.java        -- 忽略鉴权URL配置
│   │   ├── LlmProperties.java                -- 通义千问大模型配置(apiKey/model/timeout)
│   │   └── PatientChatProperties.java        -- 患者扫码配置(frontendBaseUrl/expireDays/qrcodeSize)
│   ├── security/
│   │   ├── AuthUser.java                     -- 授权用户信息
│   │   ├── UserContext.java                  -- 获取当前登录用户
│   │   ├── SecurityBean.java                 -- BCryptPasswordEncoder + CORS跨域
│   │   ├── CustomAccessDeniedHandler.java    -- 权限不足返回JSON响应
│   │   ├── SecretKeyUtil.java                -- JWT签名密钥管理
│   │   ├── Token.java                        -- 双Token实体
│   │   ├── TokenUtil.java                    -- Token生成/刷新/验证
│   │   └── filter/
│   │       └── JwtAuthenticationFilter.java  -- JWT认证过滤器
│   └── util/
│       ├── ResponseUtil.java                 -- Filter中输出JSON响应工具
│       ├── ResultUtil.java                   -- 返回结果工具类
│       ├── DateUtil.java                     -- 日期工具类
│       ├── QrCodeUtil.java                   -- QR码生成工具(ZXing封装,返回byte[]不写磁盘)
│       └── UuidUtils.java                    -- 高性能UUID生成(mica,比JDK快2-3倍)
├── config/
│   └── SecurityConfig.java                   -- Spring Security核心配置
├── controller/
│   ├── AuthController.java                   -- 认证接口
│   ├── PatientController.java                -- 患者管理接口
│   ├── ReportController.java                 -- 诊断报告接口
│   ├── StatsController.java                  -- 统计接口
│   ├── ExportController.java                 -- 报告导出接口(PDF/Word含QR码)
│   ├── DoctorChatController.java             -- 医生×AI对话接口(走JWT)
│   └── PatientChatController.java            -- 患者×AI对话接口(走accessCode)
├── service/
│   ├── DoctorService.java
│   ├── PatientService.java
│   ├── ReportService.java                    -- 含 validateOwnership 方法(供其他 Service 复用)
│   ├── StatsService.java
│   ├── ExportService.java
│   ├── LlmService.java                       -- 通义千问大模型调用抽象
│   ├── DoctorChatService.java                -- 医生对话业务
│   ├── PatientChatService.java               -- 患者扫码对话业务(访问码管理+报告展示+AI对话)
│   └── impl/
│       ├── DoctorServiceImpl.java
│       ├── PatientServiceImpl.java
│       ├── ReportServiceImpl.java
│       ├── StatsServiceImpl.java
│       ├── ExportServiceImpl.java            -- iText/POI 生成报告 + 嵌入QR码到签名区
│       ├── LlmServiceImpl.java               -- WebClient 调用 DashScope API(qwen-turbo)
│       ├── DoctorChatServiceImpl.java        -- 医生对话:专业英文 system prompt
│       └── PatientChatServiceImpl.java       -- 患者对话:通俗语言 + 免责声明 system prompt
├── mapper/
│   ├── DoctorMapper.java
│   ├── DoctorTokenMapper.java
│   ├── PatientMapper.java
│   ├── ReportMapper.java
│   ├── ChatMessageMapper.java                -- 对话消息(JPQL历史查询 + 软删除)
│   └── PatientChatMapper.java                -- 患者访问凭证(findByAccessCode)
└── entity/
    ├── dos/
    │   ├── Doctor.java
    │   ├── DoctorToken.java
    │   ├── Patient.java
    │   ├── Report.java
    │   ├── ChatMessage.java                  -- 对话消息(session_type 区分医生/患者)
    │   └── PatientChat.java                  -- 患者扫码访问凭证
    ├── dto/
    │   ├── DoctorUpdateDTO.java
    │   ├── DoctorChatSendDTO.java            -- 医生发送消息(reportId + message)
    │   ├── PatientChatSendDTO.java           -- 患者发送消息(accessCode + message)
    │   ├── ChatMessageDTO.java               -- 大模型调用参数(role + content)
    │   └── DashScopeRequest.java             -- 通义千问请求体
    └── vos/
        ├── OverviewVO.java
        ├── MonthlyVolumeVO.java
        ├── DiseaseDistributionVO.java
        ├── ComparisonStatsVO.java
        ├── ComparisonRecordVO.java
        ├── EfficiencyVO.java
        ├── ChatMessageVO.java                -- 对话消息VO(role/content/createTime)
        └── PatientReportVO.java              -- 患者端报告简化版(隐藏findings原文)
```

## 数据库设计

### doctor(医生表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键,UUID自动生成 |
| username | varchar(50) | 用户名(唯一索引) |
| password | varchar(255) | 密码(BCrypt加密,接口返回时隐藏) |
| real_name | varchar(50) | 真实姓名 |
| department | varchar(50) | 科室 |
| phone | varchar(20) | 手机号 |
| email | varchar(100) | 邮箱 |
| enabled | bit(1) | 是否启用,默认true |
| create_by~delete_flag | - | 同BaseEntity |

### doctor_token(医生Token表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键 |
| doctor_id | varchar(32) | 医生ID(索引) |
| access_token | text | 访问Token(JWT,长度不定) |
| refresh_token | text | 刷新Token |
| expire_time | datetime | accessToken过期时间 |
| refresh_expire_time | datetime | refreshToken过期时间 |
| create_time | datetime(6) | 创建时间 |

### patient(患者表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键 |
| patient_no | varchar(50) | 患者编号(索引) |
| name | varchar(50) | 姓名 |
| gender | varchar(10) | 性别 |
| age | int | 年龄 |
| medical_history | varchar(500) | 病史 |
| create_by~delete_flag | - | 同BaseEntity |

### report(诊断报告表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键 |
| doctor_id | varchar(32) | 医生ID(索引,用于权限校验) |
| patient_id | varchar(32) | 患者ID(索引) |
| image_path | varchar(255) | 影像文件路径 |
| report_content | text | 医生确认后的报告内容 |
| ai_draft | text | AI原始生成的报告内容(用于AI vs 医生对比) |
| impression | text | AI生成的印象/结论 |
| gate | varchar(20) | 三态门控:normal/findings/uncertain |
| report_confidence | double | 报告置信度 |
| findings_keywords | text | 发现关键词JSON |
| heatmap_path | longtext | 热力图base64 JSON数据 |
| pdf_path | varchar(255) | PDF报告路径 |
| status | varchar(20) | 报告状态:DRAFT/CONFIRMED/SIGNED |
| create_by~delete_flag | - | 同BaseEntity |

### chat_message(对话消息表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键 |
| report_id | varchar(32) | 关联报告ID(索引) |
| session_type | varchar(20) | 会话类型:doctor_chat / patient_chat |
| role | varchar(20) | 角色:user / assistant |
| content | text | 消息内容 |
| create_by~delete_flag | - | 同BaseEntity(patient 场景 create_by="patient") |

**索引**:`idx_report_session(report_id, session_type)` — 快速查询某报告的某类对话历史

### patient_chat(患者扫码访问凭证表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键 |
| report_id | varchar(32) | 关联报告ID(索引) |
| access_code | varchar(64) | 访问码(32位UUID,唯一索引) |
| expire_time | datetime | 过期时间(默认30天) |
| revoked | bit(1) | 是否已撤销 |
| create_by~delete_flag | - | 同BaseEntity(create_by 记录发码医生) |

**索引**:`uk_access_code`(唯一) + `idx_report_id`

## 双重认证体系

### 医生:JWT 双Token认证

```
登录:
  用户名密码 → DoctorServiceImpl.login()
  → BCryptPasswordEncoder.matches() 验密
  → TokenUtil.createToken() 生成 accessToken + refreshToken
  → 存入 doctor_token 表 → 返回双Token

请求认证:
  Header 携带 accessToken → JwtAuthenticationFilter 拦截
  → 解析JWT提取AuthUser → 查数据库验证token存在性
  → 通过 → SecurityContext → 放行
  → 失败 → 403 JSON

Token刷新:
  accessToken过期 → 携带refreshToken请求 /refresh/{refreshToken}
  → TokenUtil.refreshToken() → 返回新双Token

退出:
  DoctorService.logout() → 删除 doctor_token 记录
```

### 患者:accessCode 资源凭证

```
生成(医生导出报告时):
  ExportService.exportPdf/Word()
  → PatientChatService.createAccessCode(reportId, doctorId)
  → UuidUtils.getUUID() 生成32位十六进制访问码
  → 存入 patient_chat 表(30天过期)
  → 返回 PatientChat 实体
  → QrCodeUtil.generatePng() 生成QR码
  → 嵌入到 PDF/Word 的签名区右下角

患者访问:
  手机扫QR码 → 浏览器打开 http://xxx/patient/chat/{accessCode}
  → 前端带 accessCode 请求 /api/patient-chat/report/{accessCode}
  → 【JWT过滤器跳过:/api/patient-chat/** 在 ignored.urls 白名单】
  → PatientChatService.validateAccessCode(accessCode)
    → 查 patient_chat 表
    → 校验 revoked / expire_time
  → 返回简化版报告 VO(隐藏医生信息、findings原文)
```

### 两种方案对比

| 对比项 | JWT(医生) | accessCode(患者) |
|-------|-----------|------------------|
| 用户是否需要账号 | ✅ 需要登录 | ❌ 无需账号 |
| 凭证格式 | JWT(200+字符) | UUID(32字符) |
| 身份识别对象 | 医生账户 | 一份具体的报告 |
| 校验方式 | 验签 + 查库 | 数据库存在性查询 |
| 可访问数据范围 | 医生全部数据 | **只能访问绑定的那一份报告** |
| 过期管理 | JWT exp claim | 数据库 expire_time 字段 |
| 适用场景 | 内部系统认证 | 一次性外部扫码访问 |

## 大模型对话体系

### 双角色 System Prompt 设计

同一个大模型(通义千问 qwen-turbo),通过**不同的系统提示词**实现医生/患者双角色对话。

#### 医生端(专业模式)

```
You are an experienced radiologist AI assistant, helping doctors 
review and interpret chest X-ray AI-generated reports.
- Answer in concise, professional, accurate English
- May discuss differential diagnoses, imaging findings, treatment suggestions
- Current report context: [Impression] [Report] [Detected findings] [AI confidence]
```

#### 患者端(通俗模式)

```
You are a friendly health assistant helping a patient understand their report.
1. Use simple, everyday language. Avoid medical jargon.
2. Be warm, empathetic, reassuring.
3. DO NOT give specific treatment recommendations, drug names, or dosages.
4. DO NOT make a definitive diagnosis.
5. For serious concerns, ALWAYS recommend consulting their doctor.
6. Keep answers concise (3-5 sentences).
```

### 对话实际效果对比

**同一份报告,不同角色,不同回答**:

| 医生端提问 | 患者端提问 |
|-----------|----------|
| "Why is this diagnosed as emphysema?" | "What does this diagnosis mean?" |
| **AI回复**:引用 "pulmonary hyperinflation" / "flattening of diaphragms" / confidence 0.3826,给出 7 条鉴别诊断、药物名(roflumilast)、数值标准(PaO₂ < 55 mmHg) | **AI回复**:"your lungs appear to be more inflated than usual, which can happen in emphysema. Emphysema is a lung disease that makes it harder to breathe..." + 建议咨询医生 |

### 多轮上下文

所有历史对话从 `chat_message` 表按时间排序取出,传给大模型 → **AI 能基于前文继续讨论**。例如:
- 第一问:"Why is this diagnosed as emphysema?"
- 第二问:"What differential diagnoses should I consider?" → AI 不用重新解释是什么病,直接给鉴别诊断

## 接口列表

### 认证接口(/api/auth)

| 方法 | 路径 | 说明 | 鉴权 | 参数 |
|------|------|------|------|------|
| POST | /login | 登录 | 否 | @RequestParam: username, password |
| POST | /register | 注册 | 否 | @RequestParam: username, password, realName, department |
| GET | /refresh/{refreshToken} | 刷新Token | 否 | @PathVariable |
| GET | /info | 获取当前医生信息 | 是 | 无 |
| POST | /logout | 退出登录 | 是 | 无 |
| PUT | /update | 更新个人信息 | 是 | @RequestBody: DoctorUpdateDTO |
| PUT | /password | 修改密码 | 是 | @RequestParam: oldPassword, newPassword |

### 患者接口(/api/patient)- 需要JWT

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /add | 新增患者 |
| GET | /get/{id} | 获取患者 |
| GET | /search | 搜索患者(按姓名) |
| GET | /list | 患者列表 |

### 报告接口(/api/report)- 需要JWT

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /generate | 生成报告(调用AI推理服务) |
| GET | /getDetail/{id} | 获取报告详情 |
| GET | /list/patient/{patientId} | 患者历史报告 |
| GET | /list/mine | 当前医生的报告 |
| PUT | /status/{id} | 更新状态(含权限校验) |
| PUT | /content/{id} | 更新报告内容(含权限校验) |

### 统计接口(/api/stats)- 需要JWT

| 方法 | 路径 | 说明 | 数据来源 |
|------|------|------|----------|
| GET | /overview | 总览统计 | 数据库COUNT |
| GET | /monthly | 月度报告量 | GROUP BY + DATE_FORMAT |
| GET | /disease | 疾病分布 | 关键词匹配(DiseaseEnum) |
| GET | /comparison | AI对比统计 | aiDraft vs reportContent |
| GET | /comparison/records | AI对比记录列表 | 查修改过的报告 |
| GET | /efficiency | AI效率统计 | AVG + TIMESTAMPDIFF |

### 导出接口(/api/export)- 需要JWT

| 方法 | 路径 | 说明 | 输出 |
|------|------|------|------|
| GET | /pdf/{reportId} | 导出PDF(含QR码) | application/pdf 文件流 |
| GET | /word/{reportId} | 导出Word(含QR码) | application/docx 文件流 |

**导出模板**(PDF 和 Word 统一):
- Patient Information — 患者姓名/年龄/性别/病历号
- Technique — 检查技术
- Findings — AI生成的影像发现
- Impressions — AI生成的印象/结论
- Recommendations — 建议(预留)
- **Signature** — 医生签名(姓名/日期/签名/**QR码扫码对话**)
- Disclaimer — AI辅助声明

### 医生对话接口(/api/doctor-chat)- 需要JWT

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /send | 医生发送消息,获取AI回复 | @RequestBody: DoctorChatSendDTO |
| GET | /history/{reportId} | 获取对话历史 | @PathVariable |
| DELETE | /history/{reportId} | 清空对话历史(软删除) | @PathVariable |

### 患者对话接口(/api/patient-chat)- 走accessCode,**不走JWT**

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /report/{accessCode} | 获取简化版报告 | @PathVariable |
| POST | /send | 患者发送消息,获取AI回复 | @RequestBody: PatientChatSendDTO |
| GET | /history/{accessCode} | 获取对话历史 | @PathVariable |

**特点**:
- Controller 不依赖 UserContext(患者无登录态)
- 所有校验基于 accessCode(查 patient_chat 表)
- 患者看到的 PatientReportVO **隐藏** findings 原文、医生信息、热力图、置信度(避免患者看到专业术语而焦虑)

## 异常处理

全局异常处理机制(GlobalControllerExceptionHandler)捕获四种异常:

| 异常类型 | 处理方式 |
|---------|---------|
| ServiceException | 提取ResultCode返回业务错误 |
| RuntimeException | 返回通用错误,防止信息泄露 |
| BindException | 提取字段校验错误 |
| ConstraintViolationException | 提取约束违反信息 |

统一返回格式:

```json
{
    "success": false,
    "message": "错误信息",
    "code": 20002,
    "timestamp": 1786278063727,
    "result": null
}
```

### 状态码规范

| 范围 | 模块 | 示例 |
|------|------|------|
| 200 | 成功 | SUCCESS |
| 400 | 通用错误 | ERROR |
| 20xxx | 用户相关 | USER_NOT_EXIST / USER_PASSWORD_ERROR |
| 30xxx | 患者相关 | PATIENT_NOT_EXIST / PATIENT_NO_EXIST |
| 40xxx | 报告相关 | REPORT_NOT_EXIST / REPORT_GENERATE_ERROR |
| 50xxx | 文件相关 | FILE_NOT_EXIST_ERROR / FILE_SIZE_EXCEED |
| 60xxx | AI服务 | AI_SERVICE_ERROR / AI_SERVICE_TIMEOUT / LLM_SERVICE_ERROR / LLM_SERVICE_TIMEOUT / LLM_RESPONSE_EMPTY / LLM_RESPONSE_INVALID |
| 70xxx | 患者扫码对话 | PATIENT_CHAT_INVALID / PATIENT_CHAT_EXPIRED / PATIENT_CHAT_REVOKED |

## 配置说明(application.yml)

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| server.port | 服务端口 | 8887 |
| spring.datasource | MySQL连接 | localhost:3306/medical_report |
| spring.jpa.hibernate.ddl-auto | DDL策略 | update |
| spring.servlet.multipart.max-file-size | 文件上传大小 | 50MB |
| **ignored.urls** | JWT白名单 | /api/auth/login, /api/auth/register, /api/auth/refresh/\*\*, **/api/patient-chat/\*\*** |
| ai.service.url | Python AI推理服务 | http://192.168.1.81:8000 |
| ai.baseline.avg-report-time | AI效率基准(分钟) | 15 |
| file.upload-dir | 文件上传目录 | uploads/ |
| **llm.dashscope.base-url** | 通义千问API地址 | https://dashscope.aliyuncs.com |
| **llm.dashscope.api-key** | 通义千问API Key | sk-xxx |
| **llm.dashscope.model** | 使用的模型 | qwen-turbo |
| **llm.dashscope.timeout-seconds** | 超时(秒) | 60 |
| **llm.dashscope.max-tokens** | 最大生成长度 | 1500 |
| **patient-chat.frontend-base-url** | 患者端前端域名(QR码里用) | http://192.168.1.XXX:3000 |
| **patient-chat.expire-days** | 访问码有效期(天) | 30 |
| **patient-chat.qrcode-size** | QR码图片尺寸(像素) | 180 |

## 编码规范

本项目严格遵循编码规范:

### 分层规范
- Controller → Service → Mapper 严格分层
- Controller不直接注入Mapper(ExportService 为历史遗留,计划重构)

### 参数传递
- 简单参数(1-3个):@RequestParam
- 复杂参数(4+):DTO + @RequestBody + 校验注解
- 路径参数:@PathVariable
- 不使用 Map 接收/返回数据

### 返回值
- 统一 ResultMessage<T>,使用 ResultUtil.data()/success()/error()
- 列表查询返回 VO,不直接返回实体类
- 文件导出通过 HttpServletResponse 流输出

### 异常处理
- 业务异常:throw new ServiceException(ResultCode.XXX)
- 每种错误场景对应独立 ResultCode 枚举值
- 不使用 throw new RuntimeException("xxx")

### 代码规范
- 硬编码字符串用枚举类(ReportStatusEnum / DiseaseEnum / SessionTypeEnum / ChatRoleEnum)
- 日期操作用 DateUtil 工具类
- QR 码生成用 QrCodeUtil 工具类
- UUID 生成用 UuidUtils(高性能版本)
- 重复逻辑提取为私有方法
- 统计查询下推数据库(COUNT/GROUP BY/AVG)
- 适当使用Lambda和Stream,复杂逻辑保持for循环

### 安全规范
- "我的数据"从UserContext取doctorId,不由前端传递
- "他人数据"在Service层校验归属权(validateOwnership)
- 文件上传三重校验(大小/扩展名/MIME)
- 患者扫码场景走白名单,独立 accessCode 校验,不依赖JWT

## 启动方式

### 1. 数据库准备
```bash
mysql -u root -p
> source DB/medical-report.sql;   # 创建 medical_report 库 + 全部表
```

### 2. 配置修改

修改 `application.yml`:

- **数据库连接**:username / password
- **AI 推理服务地址**:`ai.service.url`(台式机 IP)
- **通义千问 API Key**:`llm.dashscope.api-key`
- **患者端前端域名**:`patient-chat.frontend-base-url`(局域网 IP,`ipconfig` 查看)

### 3. 启动顺序

```bash
# 1. 启动台式机 Python 推理服务
cd R2GenGPT && python api.py

# 2. 启动 Spring Boot 后端
# IDEA 运行 ReportSystemApplication.main()
# 或 mvn spring-boot:run

# 3. 启动前端
cd medical-report-ui && npm run dev
```

### 4. 访问

- 医生端:`http://localhost:3000`
- 后端:`http://localhost:8887`
- 患者端(手机扫 PDF 里的 QR 码):`http://{局域网IP}:3000/patient/chat/{accessCode}`

### 5. 手机测试要点

- 手机和电脑必须在**同一 WiFi**
- Vite 需配置 `server.host: '0.0.0.0'` 让局域网可访问
- Windows 防火墙开放 3000 和 8887 端口