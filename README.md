# 城市停车热度预测与可视化平台

## 项目简介

城市停车热度预测与可视化平台，基于 **Spring Boot + Vue 3 + PostgreSQL + ECharts** 技术栈构建，提供停车场余位监测、历史车流分析、分时段热度预测和拥堵预警展示等核心功能。

平台采用前后端分离架构，通过 Docker Compose 实现一键部署，内置模拟数据可直接运行体验。

## 技术栈

| 层级 | 技术选型 | 版本 |
|------|----------|------|
| 后端框架 | Spring Boot | 3.2.0 |
| ORM 框架 | MyBatis Plus | 3.5.5 |
| 数据库 | PostgreSQL | 15 |
| 构建工具 | Maven | 3.9+ |
| JDK | Java | 17 |
| 前端框架 | Vue | 3.4+ |
| 构建工具 | Vite | 5.0 |
| UI 组件库 | Element Plus | 2.4.4 |
| 图表库 | ECharts | 5.4.3 |
| 状态管理 | Pinia | 2.1.7 |
| 路由 | Vue Router | 4.2.5 |
| HTTP 客户端 | Axios | 1.6.2 |
| 容器化 | Docker + Docker Compose | 20+ |

## 功能模块

### 1. 停车场余位监测（实时监测）
- 统计卡片：总车位、空闲车位、平均使用率、活跃预警
- 停车场状态监控表格（支持查看车位详情）
- 今日车流趋势折线图
- 区域分布饼图
- 实时预警列表
- 使用率仪表盘
- 车位详情弹窗（含车位网格可视化）

### 2. 历史车流分析
- 统计卡片：总驶入量、总驶出量、平均使用率、高峰时段
- 日均时段车流分布柱状图
- 每日车流趋势折线图
- 周分析对比柱状图
- 各工作日时段对比（Tab 切换）

### 3. 分时段热度预测
- 统计卡片：预测天数、高热度时段、预测峰值、平均置信度
- 热度预测热力图
- 热度等级分布饼图
- 预测趋势对比折线图
- 每日预测详情折叠面板

### 4. 拥堵预警展示
- 统计卡片：活跃预警、今日已处理、高级别预警、预测预警
- 活跃预警列表卡片（支持处理和查看详情）
- 预警类型/级别分布饼图
- 历史预警记录表格（支持按类型/状态筛选和处理）

## 快速开始

### 方式一：Docker Compose 一键部署（推荐）

#### 前置要求
- Docker 20.10+
- Docker Compose 2.0+

#### 启动命令
```bash
# 在项目根目录执行
docker-compose up -d --build
```

#### 访问地址
- 前端访问: http://localhost
- 后端API: http://localhost:8080/api
- 数据库: localhost:5432 (账号: postgres / 密码: postgres / 数据库: parking_db)

#### 常用命令
```bash
# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止服务并删除数据卷（清空数据库）
docker-compose down -v
```

### 方式二：本地开发调试

#### 1. 启动 PostgreSQL
```bash
docker run -d --name parking-postgres \
  -e POSTGRES_DB=parking_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -v ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql:ro \
  postgres:15-alpine
```

#### 2. 启动后端
```bash
cd backend
mvn clean spring-boot:run
```
后端默认端口 8080，API 前缀 `/api`

#### 3. 启动前端
```bash
cd frontend
npm install
npm run dev
```
前端默认端口 5173，访问 http://localhost:5173

## 项目结构

```
.
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/parking/
│   │   ├── config/                   # 配置类（CORS、全局异常、MyBatis Plus）
│   │   ├── controller/               # REST API 控制器（5个）
│   │   ├── dto/                      # 数据传输对象
│   │   ├── entity/                   # 实体类（5个）
│   │   ├── mapper/                   # MyBatis Plus Mapper
│   │   ├── predictor/                # 热度预测算法
│   │   ├── service/                  # 业务逻辑层
│   │   └── ParkingApplication.java   # 主启动类
│   ├── src/main/resources/
│   │   └── application.yml           # 应用配置
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── api/                      # API 请求封装
│   │   ├── router/                   # Vue Router 路由
│   │   ├── stores/                   # Pinia 状态管理
│   │   ├── styles/                   # 全局样式
│   │   ├── utils/                    # 工具函数（ECharts 配置）
│   │   ├── views/                    # 页面组件（4个）
│   │   │   ├── Dashboard.vue         # 实时监测
│   │   │   ├── TrafficAnalysis.vue   # 历史车流分析
│   │   │   ├── HeatPrediction.vue    # 热度预测
│   │   │   └── CongestionAlerts.vue  # 拥堵预警
│   │   ├── App.vue
│   │   └── main.js
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── vite.config.js
│   └── package.json
├── sql/
│   └── init.sql                      # 数据库初始化脚本
├── docker-compose.yml                # Docker 编排配置
├── .gitignore
└── README.md
```

## 数据库设计

### 核心数据表

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `parking_lots` | 停车场信息 | id, name, address, total_spaces, available_spaces, longitude, latitude, district, type |
| `traffic_flow` | 车流记录 | id, parking_lot_id, record_date, hour, in_count, out_count, occupancy_rate |
| `parking_space_status` | 车位状态 | id, parking_lot_id, space_number, status, last_updated |
| `prediction_records` | 预测记录 | id, parking_lot_id, prediction_date, hour, predicted_occupancy, heat_level, confidence |
| `congestion_alerts` | 拥堵预警 | id, parking_lot_id, alert_type, alert_level, message, status, created_at |

### 初始化数据
- 8个城市核心区域停车场
- 30天历史车流模拟数据
- 实时车位状态数据

## API 接口

### 停车场管理 `/api/parking-lots`
- `GET /api/parking-lots` - 获取所有停车场列表
- `GET /api/parking-lots/{id}` - 获取单个停车场详情
- `GET /api/parking-lots/statistics` - 获取停车场总体统计
- `GET /api/parking-lots/status` - 获取所有停车场实时状态

### 车流数据 `/api/traffic-flow`
- `GET /api/traffic-flow/hourly/{parkingLotId}` - 获取某日24小时车流
- `GET /api/traffic-flow/daily/{parkingLotId}` - 获取日期范围车流
- `GET /api/traffic-flow/average/{parkingLotId}` - 获取时段平均统计
- `GET /api/traffic-flow/weekly/{parkingLotId}` - 获取周分析数据

### 预测管理 `/api/predictions`
- `GET /api/predictions/{parkingLotId}` - 获取某日预测数据
- `GET /api/predictions/range/{parkingLotId}` - 获取日期范围预测
- `POST /api/predictions/generate/{parkingLotId}` - 生成预测数据
- `GET /api/predictions/heatmap` - 获取热度热力图数据

### 预警管理 `/api/alerts`
- `GET /api/alerts/active` - 获取活跃预警列表
- `GET /api/alerts/history` - 获取历史预警记录
- `GET /api/alerts/statistics` - 获取预警统计
- `PUT /api/alerts/{id}/resolve` - 标记预警已处理

### 车位状态 `/api/parking-spaces`
- `GET /api/parking-spaces/{parkingLotId}` - 获取停车场车位状态
- `PUT /api/parking-spaces/{id}` - 更新车位状态

## 预测算法说明

采用**加权移动平均预测模型**，综合考虑多维度因素：

### 核心算法
1. **历史模式权重 (60%)**：基于过去4周同日同时段的平均数据
2. **近期趋势权重 (15%)**：基于最近7天同时段数据，反映近期变化
3. **日期类型调节**：
   - 工作日：正常水平
   - 周末/节假日：上浮 20%
4. **时段因子**：
   - 早高峰 (7-9点)：上浮 15%
   - 晚高峰 (17-19点)：上浮 20%
5. **随机因素 (±5%)**：模拟实际波动

### 热度分级
| 等级 | 占用率范围 | 颜色标识 |
|------|------------|----------|
| 低热度 | < 40% | 🟢 绿色 |
| 中热度 | 40% - 64% | 🟡 黄色 |
| 高热度 | 65% - 84% | 🟠 橙色 |
| 极高热度 | ≥ 85% | 🔴 红色 |

### 拥堵预警阈值
- **警告级别**：占用率 ≥ 80%
- **危险级别**：占用率 ≥ 90%

## 定时任务

系统内置定时任务自动更新数据：

1. **实时拥堵检测**（每小时执行）
   - 检查各停车场实时占用率
   - 超过阈值自动生成预警

2. **预测数据生成**（每日 6:00 执行）
   - 生成未来7天的预测数据
   - 基于预测生成预警提醒

## 前端特性

- 🎨 现代化深色主题 UI 设计
- 📊 丰富的 ECharts 图表（折线图、柱状图、饼图、热力图、仪表盘）
- 🔄 30秒自动刷新实时数据
- 📱 响应式布局适配
- 🎯 状态集中管理（Pinia）
- 🛡️ 统一的 API 请求和错误处理

## 部署说明

### Docker Compose 服务编排

`docker-compose.yml` 定义了三个服务：

1. **postgres**: PostgreSQL 数据库
   - 端口映射: 5432:5432
   - 数据持久化: postgres-data 卷
   - 自动执行初始化脚本

2. **backend**: Spring Boot 后端
   - 端口映射: 8080:8080
   - 依赖数据库健康检查
   - 环境变量注入数据库连接

3. **frontend**: Vue 3 前端 (Nginx)
   - 端口映射: 80:80
   - 内置反向代理 `/api` 到后端
   - 支持前端路由 History 模式

## 注意事项

1. **首次启动**：数据库初始化和后端启动可能需要 2-3 分钟，请耐心等待
2. **端口冲突**：如端口被占用，请修改 `docker-compose.yml` 中的端口映射
3. **数据持久化**：使用 `docker-compose down` 不会删除数据，使用 `-v` 参数才会清空
4. **预测数据**：首次启动后需等待定时任务执行，或手动调用 API 生成预测
5. **时区设置**：所有容器统一使用 `Asia/Shanghai` 时区

## 扩展建议

- 接入真实停车场 API 替换模拟数据
- 集成机器学习模型优化预测精度
- 添加用户权限管理系统
- 支持多城市切换
- 添加地图可视化（如高德地图、百度地图）
- 增加移动端适配

## 许可证

MIT License
