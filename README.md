# 医学影像报告生成系统 - 后端服务

## 项目简介

本系统是一个基于深度学习的胸部X光医学影像报告自动生成系统。医生上传胸部X光影像后，系统自动调用AI模型（R2GenGPT: Swin Transformer + LLaMA-2-7B）生成放射科诊断报告，并支持报告的查看、编辑、确认签发、历史记录管理、AI置信度可视化、AI与医生报告对比分析、数据统计大屏以及多格式导出等功能。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8 | 开发语言 |
| Spring Boot | 2.7.18 | 后端框架 |
| Spring Security | 5.7.x | 安全认证框架（JWT + 自定义过滤器） |
| Spring Data JPA | 2.7.x | ORM持久层框架 |
| MySQL | 8.x | 关系型数据库 |
| JWT (jjwt) | 0.13.0 | 双Token认证（accessToken + refreshToken） |
| Lombok | 1.18.34 | 简化代码 |
| Gson | - | JSON序列化（AuthUser存入JWT Claims） |
| WebFlux WebClient | - | HTTP调用Python AI推理服务（带超时控制） |
| iText | 7.1.2 | PDF报告生成 |
| Apache POI | 5.2.5 | Word文档生成 |
| Logback | - | 日志框架 |

## 系统架构

```
React前端 (localhost:3000)
    ↕ HTTP请求（Header携带accessToken）
    ↕ Vite代理转发 /api → localhost:8887
Java Spring Boot后端 (localhost:8887)
    ├── JwtAuthenticationFilter → 解析token、验证数据库、设置SecurityContext
    ├── Controller层 → 接收请求，参数校验，调用Service
    ├── Service层 → 业务逻辑处理，权限校验
    ├── Mapper层 → JPA数据库操作（COUNT/GROUP BY等统计下推数据库）
    ├── MySQL → 存储医生、患者、报告、token数据
    └── WebClient → 调用Python AI推理服务（60秒超时）
    ↕ HTTP Multipart请求（发送影像文件，接收报告JSON）
Python FastAPI推理服务 (台式机 192.168.1.81:8000)
    └── R2GenGPT模型（Delta Alignment, Epoch 14）
        ├── Swin Transformer（视觉编码器 + LoRA微调）
        ├── Linear Projection（视觉-语言映射层）
        └── LLaMA-2-7B-Chat（4-bit量化，报告生成）
```

## 项目结构

```
com.medical
├── ReportSystemApplication.java              -- 启动类
├── common                                    -- 通用模块
│   ├── ResultMessage.java                    -- 统一返回结果封装
│   ├── ResultCode.java                       -- 状态码枚举（用户20xxx/患者30xxx/报告40xxx/文件50xxx/AI服务60xxx）
│   ├── ServiceException.java                 -- 自定义业务异常
│   ├── GlobalControllerExceptionHandler.java -- 全局异常处理（ServiceException/Runtime/Bind/Constraint）
│   ├── BaseEntity.java                       -- 数据库基础实体类（id/createBy/createTime/updateBy/updateTime/deleteFlag）
│   ├── enums/
│   │   ├── SecurityEnum.java                 -- 安全常量（HEADER_TOKEN/USER_CONTEXT）
│   │   ├── ReportStatusEnum.java             -- 报告状态枚举（DRAFT/CONFIRMED/SIGNED）
│   │   └── DiseaseEnum.java                  -- 疾病类型枚举（含关键词匹配方法）
│   ├── properties/
│   │   └── IgnoredUrlsProperties.java        -- 忽略鉴权URL配置（读取yml的ignored.urls）
│   ├── security/
│   │   ├── AuthUser.java                     -- 授权用户信息（JSON序列化存入JWT Claims）
│   │   ├── UserContext.java                  -- 获取当前登录用户（从Request Header解析token）
│   │   ├── SecurityBean.java                 -- BCryptPasswordEncoder + CORS跨域配置
│   │   ├── CustomAccessDeniedHandler.java    -- 权限不足返回JSON响应
│   │   ├── SecretKeyUtil.java                -- JWT签名密钥管理（Base64编码）
│   │   ├── Token.java                        -- 双Token实体（accessToken + refreshToken）
│   │   ├── TokenUtil.java                    -- Token生成/刷新/验证（数据库存储，支持事务回滚）
│   │   └── filter/
│   │       └── JwtAuthenticationFilter.java  -- JWT认证过滤器（继承BasicAuthenticationFilter）
│   └── util/
│       ├── ResponseUtil.java                 -- Filter中输出JSON响应工具
│       ├── ResultUtil.java                   -- 返回结果工具类
│       └── DateUtil.java                     -- 日期工具类（月份统计、格式化）
├── config/
│   └── SecurityConfig.java                   -- Spring Security核心配置（WebSecurityConfigurerAdapter）
├── controller/
│   ├── AuthController.java                   -- 认证接口（登录/注册/刷新token/获取信息/退出/修改信息/修改密码）
│   ├── PatientController.java                -- 患者管理接口（新增/查询/搜索/列表）
│   ├── ReportController.java                 -- 诊断报告接口（生成/查询/状态更新，含权限校验）
│   ├── StatsController.java                  -- 统计接口（总览/月度/疾病分布/效率/AI对比）
│   └── ExportController.java                 -- 报告导出接口（PDF/Word，通过HttpServletResponse流输出）
├── service/
│   ├── DoctorService.java                    -- 医生业务接口
│   ├── PatientService.java                   -- 患者业务接口
│   ├── ReportService.java                    -- 报告业务接口
│   ├── StatsService.java                     -- 统计业务接口
│   ├── ExportService.java                    -- 报告导出业务接口
│   └── impl/
│       ├── DoctorServiceImpl.java            -- 医生业务实现（注册/登录/退出/信息修改/密码修改）
│       ├── PatientServiceImpl.java           -- 患者业务实现
│       ├── ReportServiceImpl.java            -- 报告业务实现（文件校验/WebClient调用AI/权限校验）
│       ├── StatsServiceImpl.java             -- 统计业务实现（数据库聚合查询，非内存全量计算）
│       └── ExportServiceImpl.java            -- 导出业务实现（iText生成PDF/POI生成Word）
├── mapper/
│   ├── DoctorMapper.java                     -- 医生数据访问层
│   ├── DoctorTokenMapper.java                -- 医生Token数据访问层
│   ├── PatientMapper.java                    -- 患者数据访问层
│   └── ReportMapper.java                     -- 报告数据访问层（含原生SQL统计查询）
└── entity/
    ├── dos/
    │   ├── Doctor.java                       -- 医生实体（密码字段@JsonProperty WRITE_ONLY）
    │   ├── DoctorToken.java                  -- 医生Token实体
    │   ├── Patient.java                      -- 患者实体
    │   └── Report.java                       -- 诊断报告实体（含aiDraft字段保存AI原始报告）
    ├── dto/
    │   └── DoctorUpdateDTO.java              -- 医生信息更新DTO（含@Email校验）
    └── vos/
        ├── OverviewVO.java                   -- 总览统计VO
        ├── MonthlyVolumeVO.java              -- 月度报告量VO
        ├── DiseaseDistributionVO.java        -- 疾病分布VO
        ├── ComparisonStatsVO.java            -- AI对比统计VO
        ├── ComparisonRecordVO.java           -- AI对比记录VO
        └── EfficiencyVO.java                 -- AI效率统计VO
```

## 数据库设计

### doctor（医生表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键，UUID自动生成 |
| username | varchar(50) | 用户名（唯一索引） |
| password | varchar(255) | 密码（BCrypt加密，接口返回时隐藏） |
| real_name | varchar(50) | 真实姓名 |
| department | varchar(50) | 科室 |
| phone | varchar(20) | 手机号 |
| email | varchar(100) | 邮箱 |
| enabled | bit(1) | 是否启用，默认true |
| create_by | varchar(50) | 创建者 |
| create_time | datetime(6) | 创建时间（@PrePersist自动填充） |
| update_by | varchar(50) | 更新者 |
| update_time | datetime(6) | 更新时间（@PreUpdate自动填充） |
| delete_flag | bit(1) | 逻辑删除标志，默认false |

### doctor_token（医生Token表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键，UUID |
| doctor_id | varchar(32) | 医生ID（索引） |
| access_token | text | 访问Token |
| refresh_token | text | 刷新Token |
| expire_time | datetime | accessToken过期时间 |
| refresh_expire_time | datetime | refreshToken过期时间（accessToken的2倍） |
| create_time | datetime(6) | 创建时间 |

### patient（患者表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键，UUID |
| patient_no | varchar(50) | 患者编号（索引） |
| name | varchar(50) | 姓名 |
| gender | varchar(10) | 性别 |
| age | int | 年龄 |
| medical_history | varchar(500) | 病史 |
| create_by~delete_flag | - | 同BaseEntity |

### report（诊断报告表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键，UUID |
| doctor_id | varchar(32) | 医生ID（索引，用于权限校验） |
| patient_id | varchar(32) | 患者ID（索引） |
| image_path | varchar(255) | 影像文件路径 |
| report_content | text | 医生确认后的报告内容 |
| ai_draft | text | AI原始生成的报告内容（用于AI vs 医生对比分析） |
| heatmap_path | varchar(255) | GradCAM热力图路径 |
| pdf_path | varchar(255) | PDF报告路径 |
| status | varchar(20) | 报告状态：DRAFT/CONFIRMED/SIGNED（使用ReportStatusEnum） |
| create_by~delete_flag | - | 同BaseEntity |

## 安全设计

### JWT双Token认证流程

```
登录:
  医生提交用户名密码 → DoctorServiceImpl.login() 查库验密
  → BCryptPasswordEncoder.matches() 比对密码
  → TokenUtil.createToken() 生成accessToken + refreshToken
  → 存入doctor_token表 → 返回双Token

请求认证:
  前端Header携带accessToken → JwtAuthenticationFilter拦截
  → 解析JWT提取AuthUser → 查数据库验证token存在性
  → 通过 → 设置SecurityContext → 放行
  → 失败 → 返回403 JSON {"message":"未登录或token失效"}

Token刷新:
  accessToken过期 → 前端携带refreshToken请求刷新接口
  → TokenUtil.refreshToken() 验证并生成新双Token
  → 删除旧Token → 存储新Token → 返回

退出登录:
  DoctorService.logout() → 删除数据库中的Token记录
  → 前端清除localStorage → 跳转登录页
```

### 权限控制

- 报告状态更新：Service层校验 `report.getDoctorId().equals(doctorId)`，防止越权修改
- 报告导出：Service层校验报告归属权，防止未授权下载
- 敏感信息保护：Doctor.password 字段使用 `@JsonProperty(access = WRITE_ONLY)` 阻止序列化输出

### 文件上传安全

- 文件为空校验（ResultCode.FILE_NOT_EXIST_ERROR）
- 文件大小限制50MB（ResultCode.FILE_SIZE_EXCEED）
- 文件扩展名白名单 .jpg/.jpeg/.png/.dcm（ResultCode.FILE_EXTENSION_NOT_ALLOWED）
- MIME类型白名单校验（ResultCode.FILE_TYPE_NOT_SUPPORT）

## 接口列表

### 认证接口（/api/auth）

| 方法 | 路径 | 说明 | 鉴权 | 参数 |
|------|------|------|------|------|
| POST | /login | 登录 | 否 | @RequestParam: username, password |
| POST | /register | 注册 | 否 | @RequestParam: username, password, realName, department |
| GET | /refresh/{refreshToken} | 刷新Token | 否 | @PathVariable: refreshToken |
| GET | /info | 获取当前医生信息 | 是 | 无 |
| POST | /logout | 退出登录 | 是 | 无 |
| PUT | /update | 更新个人信息 | 是 | @RequestBody: DoctorUpdateDTO |
| PUT | /password | 修改密码 | 是 | @RequestParam: oldPassword, newPassword |

### 患者接口（/api/patient）- 需要Token

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /add | 新增患者 | @RequestBody: Patient |
| GET | /get/{id} | 获取患者 | @PathVariable: id |
| GET | /search | 搜索患者 | @RequestParam: name |
| GET | /list | 患者列表 | 无 |

### 报告接口（/api/report）- 需要Token

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /generate | 生成报告（调用AI推理服务） | @RequestParam: patientId, file |
| GET | /get/{id} | 获取报告 | @PathVariable: id |
| GET | /list/patient/{patientId} | 患者历史报告 | @PathVariable: patientId |
| GET | /list/mine | 当前医生的报告 | 无（从UserContext获取doctorId） |
| PUT | /status/{id} | 更新状态（含权限校验） | @PathVariable: id, @RequestParam: status |

### 统计接口（/api/stats）- 需要Token

| 方法 | 路径 | 说明 | 数据来源 |
|------|------|------|----------|
| GET | /overview | 总览统计 | 数据库COUNT查询 |
| GET | /monthly | 月度报告量 | 数据库GROUP BY + DATE_FORMAT |
| GET | /disease | 疾病分布 | 报告内容关键词匹配（DiseaseEnum） |
| GET | /comparison | AI对比统计 | aiDraft vs reportContent比对 |
| GET | /comparison/records | AI对比记录列表 | 查询被修改的报告 |
| GET | /efficiency | AI效率统计 | 数据库AVG + TIMESTAMPDIFF |

### 导出接口（/api/export）- 需要Token

| 方法 | 路径 | 说明 | 输出 |
|------|------|------|------|
| GET | /pdf/{reportId} | 导出PDF（含权限校验） | application/pdf 文件流 |
| GET | /word/{reportId} | 导出Word（含权限校验） | application/docx 文件流 |

## 异常处理

系统采用全局异常处理机制（GlobalControllerExceptionHandler），捕获四种异常类型：

| 异常类型 | 处理方式 |
|---------|---------|
| ServiceException | 提取ResultCode返回业务错误信息 |
| RuntimeException | 返回通用错误信息，防止内部信息泄露 |
| BindException | 提取字段校验错误信息（@NotNull/@Email等） |
| ConstraintViolationException | 提取约束违反信息 |

统一返回格式：

```json
{
    "success": false,
    "message": "错误信息",
    "code": 20002,
    "timestamp": 1786278063727,
    "result": null
}
```

### 状态码规范（ResultCode枚举）

| 范围 | 模块 | 示例 |
|------|------|------|
| 200 | 成功 | SUCCESS |
| 400 | 通用错误 | ERROR |
| 20xxx | 用户相关 | USER_NOT_EXIST / USER_PASSWORD_ERROR / USER_STATUS_ERROR |
| 30xxx | 患者相关 | PATIENT_NOT_EXIST / PATIENT_NO_EXIST |
| 40xxx | 报告相关 | REPORT_NOT_EXIST / REPORT_GENERATE_ERROR |
| 50xxx | 文件相关 | FILE_NOT_EXIST_ERROR / FILE_SIZE_EXCEED / FILE_EXTENSION_NOT_ALLOWED |
| 60xxx | AI服务相关 | AI_SERVICE_ERROR / AI_SERVICE_TIMEOUT |

## 配置说明（application.yml）

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| server.port | 服务端口 | 8887 |
| spring.datasource | MySQL数据库连接配置 | localhost:3306/medical_report |
| spring.jpa.hibernate.ddl-auto | DDL策略 | update |
| spring.jpa.open-in-view | 懒加载 | false |
| spring.autoconfigure.exclude | 排除UserDetailsServiceAutoConfiguration | 已配置 |
| spring.servlet.multipart.max-file-size | 文件上传大小限制 | 50MB |
| ignored.urls | 不需要Token认证的URL列表 | /api/auth/login, /api/auth/register, /api/auth/refresh/** |
| ai.service.url | Python AI推理服务地址 | http://192.168.1.81:8000 |
| ai.baseline.avg-report-time | AI效率基准时间（分钟） | 15 |
| file.upload-dir | 影像文件上传目录 | uploads/ |
| logging.level.org.hibernate.SQL | SQL日志级别 | debug |

## 编码规范

本项目严格遵循 Lilishop（gitee.com/beijing_hongye_huicheng/lilishop）编码规范：

### 分层规范
- Controller → Service → Mapper 严格分层，Controller不直接注入Mapper
- Controller只负责参数接收和结果返回，业务逻辑全部在Service层

### 参数传递规范
- 简单参数（1-3个字段）：使用 `@RequestParam` 直接接收
- 复杂参数（4+字段）：使用 `DTO` + `@RequestBody` + 字段校验注解
- 路径参数：使用 `@PathVariable`
- 不使用 `Map` 接收或返回数据

### 返回值规范
- 统一返回 `ResultMessage<T>` 格式，使用 `ResultUtil.data()` / `ResultUtil.success()` / `ResultUtil.error()`
- 列表查询返回 `VO` 对象，不直接返回实体类
- 文件导出通过 `HttpServletResponse` 流输出

### 异常处理规范
- 业务异常使用 `throw new ServiceException(ResultCode.XXX)` 抛出
- 每种错误场景对应独立的 `ResultCode` 枚举值
- 不使用 `throw new RuntimeException("xxx")` 直接抛异常

### 代码规范
- 硬编码字符串使用枚举类（ReportStatusEnum / DiseaseEnum）
- 日期操作使用 `DateUtil` 工具类
- 重复逻辑提取为私有方法（hasAiDraft / isUnmodified / isMinorEdit）
- 统计查询下推数据库（COUNT / GROUP BY / AVG），不在内存中全量计算
- 适当使用Lambda和Stream，复杂逻辑保持for循环

### 安全规范
- 涉及"我的数据"从UserContext取doctorId，不由前端传递
- 涉及"他人数据"在Service层校验归属权
- 文件上传做大小、扩展名、MIME类型三重校验

## 启动方式

1. 确保MySQL服务已启动，执行建表SQL创建 `medical_report` 数据库及相关表
2. 修改 `application.yml` 中的数据库连接信息和AI服务地址
3. 在IDEA中运行 `ReportSystemApplication.main()` 启动服务
4. 服务启动后访问 `http://localhost:8887`
5. 确保台式机上的Python推理服务已启动（`python api.py`），否则报告生成功能不可用