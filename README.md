# 医学影像报告生成系统 - 后端服务

## 项目简介

本系统是一个基于深度学习的胸部X光医学影像报告自动生成系统。医生上传胸部X光影像后，系统自动调用AI模型（R2GenGPT）生成放射科诊断报告，并支持报告的查看、编辑、确认签发和历史记录管理。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8 | 开发语言 |
| Spring Boot | 2.7.18 | 后端框架 |
| Spring Security | 5.7.x | 安全认证框架 |
| Spring Data JPA | 2.7.x | ORM持久层框架 |
| MySQL | 8.x | 关系型数据库 |
| JWT (jjwt) | 0.13.0 | Token认证 |
| Lombok | 1.18.34 | 简化代码 |
| Gson | - | JSON序列化 |
| WebFlux WebClient | - | HTTP调用AI推理服务 |
| iText | 7.1.2 | PDF报告生成 |
| Logback | - | 日志框架 |

## 系统架构

```
React前端 (localhost:3000)
    ↕ HTTP请求（携带accessToken）
Java Spring Boot后端 (localhost:8887)
    ├── JWT认证过滤器 → 验证token有效性
    ├── Controller层 → 接收请求，返回结果
    ├── Service层 → 业务逻辑处理
    ├── Mapper层 → JPA数据库操作
    ├── MySQL → 存储医生、患者、报告、token数据
    └── WebClient → 调用Python AI推理服务
    ↕ HTTP请求（发送影像，接收报告）
Python FastAPI推理服务 (台式机:8000)
    └── R2GenGPT模型 → 影像 → 放射科报告
```

## 项目结构

```
com.medical
├── ReportSystemApplication.java          -- 启动类
├── common                                -- 通用模块
│   ├── ResultMessage.java                -- 统一返回结果封装
│   ├── ResultUtil.java                   -- 返回结果工具类
│   ├── ResultCode.java                   -- 状态码枚举
│   ├── ServiceException.java             -- 自定义业务异常
│   ├── GlobalControllerExceptionHandler.java  -- 全局异常处理
│   ├── BaseEntity.java                   -- 数据库基础实体类（id、创建时间、更新时间、逻辑删除）
│   ├── enums/
│   │   └── SecurityEnum.java             -- 安全相关常量（header token名等）
│   ├── properties/
│   │   └── IgnoredUrlsProperties.java    -- 忽略鉴权URL配置
│   ├── security/
│   │   ├── AuthUser.java                 -- 授权用户信息（存入JWT的用户上下文）
│   │   ├── UserContext.java              -- 获取当前登录用户工具类
│   │   ├── SecurityBean.java             -- 密码编码器 + 跨域配置
│   │   ├── CustomAccessDeniedHandler.java -- 权限不足处理
│   │   ├── SecretKeyUtil.java            -- JWT密钥管理
│   │   ├── Token.java                    -- 双Token实体（accessToken + refreshToken）
│   │   ├── TokenUtil.java                -- Token生成/刷新/验证工具类
│   │   └── filter/
│   │       └── JwtAuthenticationFilter.java  -- JWT认证过滤器
│   └── util/
│       ├── ResponseUtil.java             -- Filter中输出JSON响应工具
│       └── ResultUtil.java               -- 返回结果工具类
├── config/
│   └── SecurityConfig.java               -- Spring Security核心配置
├── controller/
│   ├── AuthController.java               -- 认证接口（登录/注册/刷新token）
│   ├── PatientController.java            -- 患者管理接口
│   └── ReportController.java             -- 诊断报告接口
├── service/
│   ├── DoctorService.java                -- 医生业务接口
│   ├── PatientService.java               -- 患者业务接口
│   ├── ReportService.java                -- 报告业务接口
│   └── impl/
│       ├── DoctorServiceImpl.java        -- 医生业务实现
│       ├── PatientServiceImpl.java       -- 患者业务实现
│       └── ReportServiceImpl.java        -- 报告业务实现
├── mapper/
│   ├── DoctorMapper.java                 -- 医生数据访问层
│   ├── DoctorTokenMapper.java            -- 医生Token数据访问层
│   ├── PatientMapper.java                -- 患者数据访问层
│   └── ReportMapper.java                 -- 报告数据访问层
└── entity/
    ├── dos/
    │   ├── Doctor.java                   -- 医生实体
    │   ├── DoctorToken.java              -- 医生Token实体
    │   ├── Patient.java                  -- 患者实体
    │   └── Report.java                   -- 诊断报告实体
    └── dto/
        └── (预留，后续扩展使用)
```

## 数据库设计

### doctor（医生表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键，UUID |
| username | varchar(50) | 用户名（唯一） |
| password | varchar(255) | 密码（BCrypt加密） |
| real_name | varchar(50) | 真实姓名 |
| department | varchar(50) | 科室 |
| phone | varchar(20) | 手机号 |
| email | varchar(100) | 邮箱 |
| enabled | bit(1) | 是否启用 |
| create_by | varchar(50) | 创建者 |
| create_time | datetime(6) | 创建时间 |
| update_by | varchar(50) | 更新者 |
| update_time | datetime(6) | 更新时间 |
| delete_flag | bit(1) | 逻辑删除标志 |

### doctor_token（医生Token表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键，UUID |
| doctor_id | varchar(32) | 医生ID |
| access_token | text | 访问Token |
| refresh_token | text | 刷新Token |
| expire_time | datetime | accessToken过期时间 |
| refresh_expire_time | datetime | refreshToken过期时间 |
| create_time | datetime(6) | 创建时间 |

### patient（患者表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键，UUID |
| patient_no | varchar(50) | 患者编号 |
| name | varchar(50) | 姓名 |
| gender | varchar(10) | 性别 |
| age | int | 年龄 |
| medical_history | varchar(500) | 病史 |
| create_by~delete_flag | - | 同BaseEntity |

### report（诊断报告表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(32) | 主键，UUID |
| doctor_id | varchar(32) | 医生ID |
| patient_id | varchar(32) | 患者ID |
| image_path | varchar(255) | 影像文件路径 |
| report_content | text | AI生成的报告内容 |
| heatmap_path | varchar(255) | GradCAM热力图路径 |
| pdf_path | varchar(255) | PDF报告路径 |
| status | varchar(20) | 报告状态：DRAFT/CONFIRMED/SIGNED |
| create_by~delete_flag | - | 同BaseEntity |

## 认证流程

### 登录认证

```
1. 医生提交用户名和密码
2. DoctorServiceImpl.login() 查询数据库验证用户
3. BCryptPasswordEncoder 比对密码
4. TokenUtil.createToken() 生成双Token（accessToken + refreshToken）
5. Token存入数据库 doctor_token 表
6. 返回双Token给前端
```

### 请求认证

```
1. 前端在请求Header中携带 accessToken
2. JwtAuthenticationFilter 拦截请求
3. 解析JWT，提取AuthUser信息
4. 查询数据库验证Token是否存在（防止Token被刷新后旧Token仍可用）
5. 验证通过 → 设置SecurityContext → 放行
6. 验证失败 → 返回403 JSON错误信息
```

### Token刷新

```
1. accessToken过期后，前端携带refreshToken请求刷新接口
2. TokenUtil.refreshToken() 验证refreshToken有效性
3. 删除旧的双Token
4. 生成并存储新的双Token
5. 返回新Token给前端
```

## 接口列表

### 认证接口（/api/auth）- 无需Token

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /api/auth/login | 登录 | username, password |
| POST | /api/auth/register | 注册 | username, password, realName, department |
| GET | /api/auth/refresh/{refreshToken} | 刷新Token | refreshToken(路径参数) |

### 患者接口（/api/patient）- 需要Token

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /api/patient/add | 新增患者 | JSON Body |
| GET | /api/patient/get/{id} | 获取患者 | id(路径参数) |
| GET | /api/patient/search | 搜索患者 | name(查询参数) |
| GET | /api/patient/list | 患者列表 | 无 |

### 报告接口（/api/report）- 需要Token

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /api/report/generate | 生成报告 | patientId, file(影像文件) |
| GET | /api/report/get/{id} | 获取报告 | id(路径参数) |
| GET | /api/report/list/patient/{patientId} | 患者历史报告 | patientId(路径参数) |
| GET | /api/report/list/mine | 我的报告 | 无 |
| PUT | /api/report/status/{id} | 更新状态 | id(路径参数), status(查询参数) |

## 异常处理

系统采用全局异常处理机制，所有业务异常通过 `ServiceException` 抛出，由 `GlobalControllerExceptionHandler` 统一捕获并返回标准JSON格式：

```json
{
    "success": false,
    "message": "错误信息",
    "code": 20002,
    "timestamp": 1786278063727,
    "result": null
}
```

常用状态码定义在 `ResultCode` 枚举中，包括用户相关（20xxx）、患者相关（30xxx）、报告相关（40xxx）、文件相关（50xxx）、AI服务相关（60xxx）。

## 配置说明

主要配置项在 `application.yml` 中：

| 配置项 | 说明 |
|--------|------|
| server.port | 服务端口，默认8887 |
| spring.datasource | MySQL数据库连接配置 |
| spring.jpa | JPA/Hibernate配置 |
| ignored.urls | 不需要Token认证的URL列表 |
| ai.service.url | Python AI推理服务地址 |
| file.upload-dir | 影像文件上传目录 |
| logging | 日志配置 |

## 编码规范

- 参数接收：简单参数使用 `@RequestParam`，复杂对象使用 `DTO` + `@RequestBody`
- 参数校验：使用 `@NotNull` 等注解
- 统一返回：所有接口返回 `ResultMessage<T>` 格式
- 异常处理：业务异常使用 `ServiceException(ResultCode)` 抛出
- 实体类：继承 `BaseEntity`，使用 `@Data`、`@Entity` 注解
- 日志：使用 `@Slf4j` + `log.info()` / `log.error()`
- 逻辑删除：通过 `deleteFlag` 字段实现，不做物理删除

## 启动方式

1. 确保MySQL服务已启动，执行建表SQL创建 `medical_report` 数据库及相关表
2. 修改 `application.yml` 中的数据库连接信息
3. 运行 `ReportSystemApplication.main()` 启动服务
4. 服务启动后访问 `http://localhost:8887`