# 城市停车热度预测与可视化平台

## 项目简介
城市停车热度预测与可视化平台，基于 Spring Boot + Vue 3 + PostgreSQL + ECharts 技术栈构建，提供停车场余位监测、历史车流分析、分时段热度预测和拥堵预警展示等核心功能。

## 技术栈
- **后端**: Spring Boot 3.2 + JPA + PostgreSQL 15
- **前端**: Vue 3 + Vite + Element Plus + ECharts 5
- **部署**: Docker + Docker Compose
- **预测算法**: 基于历史数据加权移动平均 + 时段/周末因子调节

## 功能模块

### 1. 停车场余位监测
- 实时展示停车场总数、车位总数、剩余车位、整体占用率
- 各停车场车位占用情况柱状图
- 区域车位分布饼图
- 停车场列表（含占用率进度条、状态）

### 2. 历史车流分析
- 日均时段占用率趋势图
- 出入车流对比（按小时平均）
- 每日车流总量趋势
- 统计摘要：高峰时段、平均占用率、总车流量

### 3. 分时段热度预测
- 24小时热度预测曲线（含等级警戒线）
- 热度等级分布饼图
- 峰值时段、峰值占用率、置信度、极高热度时长统计
- 时段预测详情表格（含出行建议）

### 4. 拥堵预警展示
- 按等级分类的预警统计
- 预警类型分布图
- 最近24小时预警趋势
- 未处理预警列表（支持筛选、标记已处理）

## 快速开始

### 方式一：Docker Compose 一键部署（推荐）

```bash
# 在项目根目录执行
docker-compose up -d --build
```

部署完成后：
- 前端访问: http://localhost
- 后端API: http://localhost:8080/api
- 数据库: localhost:5432 (账号: parking / 密码: parking123)

### 方式二：本地开发调试

#### 1. 启动 PostgreSQL
```bash
docker run -d --name parking-postgres \
  -e POSTGRES_DB=parking_heatmap \
  -e POSTGRES_USER=parking \
  -e POSTGRES_PASSWORD=parking123 \
  -p 5432:5432 \
  postgres:15-alpine
```

#### 2. 启动后端
```bash
cd backend
mvn clean spring-boot:run
```
后端默认端口 8080，启动后自动初始化模拟数据（10个停车场，30天历史车流数据）

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
│   ├── src/main/java/com/parking/heatmap/
│   │   ├── config/                   # 配置类（CORS、数据初始化）
│   │   ├── controller/               # REST API 控制器
│   │   ├── dto/                      # 数据传输对象
│   │   ├── entity/                   # JPA 实体类
│   │   ├── prediction/               # 热度预测算法
│   │   ├── repository/               # JPA Repository
│   │   ├── service/                  # 业务逻辑层
│   │   ├── util/                     # 工具类
│   │   └── ParkingHeatmapApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml           # 应用配置
│   │   └── init.sql                  # 数据库初始化脚本
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── api/                      # API 请求封装
│   │   ├── router/                   # Vue Router 路由
│   │   ├── styles/                   # 全局样式
│   │   ├── views/                    # 页面组件
│   │   │   ├── Monitor.vue           # 余位监测
│   │   │   ├── History.vue           # 历史分析
│   │   │   ├── Prediction.vue        # 热度预测
│   │   │   └── Warning.vue           # 拥堵预警
│   │   ├── App.vue
│   │   └── main.js
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── vite.config.js
│   └── package.json
├── docker-compose.yml
└── README.md
```

## API 接口

### 停车场相关
- `GET /api/parking-lots` - 获取所有停车场
- `GET /api/parking-lots/{id}` - 获取单个停车场详情
- `GET /api/parking-lots/district/{district}` - 按区域查询
- `GET /api/parking-lots/statistics` - 获取总体统计

### 车流数据
- `GET /api/traffic/parking-lot/{id}` - 某日车流数据
- `GET /api/traffic/parking-lot/{id}/range` - 日期范围车流
- `GET /api/traffic/parking-lot/{id}/hourly-stats` - 时段平均统计
- `GET /api/traffic/parking-lot/{id}/daily-stats` - 每日统计

### 预测相关
- `GET /api/predictions/parking-lot/{id}` - 获取某日预测
- `POST /api/predictions/generate` - 批量生成预测

### 预警相关
- `GET /api/warnings/active` - 获取所有未处理预警
- `PUT /api/warnings/{id}/resolve` - 标记预警已处理

### 热力图
- `GET /api/heatmap` - 获取热度热力图数据

## 预测算法说明

采用加权移动平均预测模型，考虑以下因素：
1. **近期数据权重**: 最近7天数据占比 50%
2. **历史数据权重**: 更早数据占比 50%
3. **周末因子**: 周末数据上浮 15%
4. **高峰时段**: 早晚高峰（7-9点、17-19点）上浮 25%
5. **热度分级**:
   - LOW (< 40%): 低热度
   - MODERATE (40-64%): 中等热度
   - HIGH (65-84%): 高热度
   - EXTREME (>= 85%): 极高热度

## 默认模拟数据

系统内置10个北京市核心商圈停车场模拟数据：
- 朝阳区: 国贸中心、三里屯太古里、望京SOHO、蓝色港湾
- 东城区: 王府井百货、崇文门新世界
- 西城区: 西单大悦城、金融街购物中心
- 海淀区: 中关村广场、金源新燕莎MALL

启动时自动生成30天历史车流数据和7天预测数据。
