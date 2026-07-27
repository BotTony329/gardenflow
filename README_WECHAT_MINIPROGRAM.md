# GardenFlow 微信小程序开发 README

> 目标：将现有 GardenFlow Android App 迁移为可在微信生态内使用并提交审核发布的微信小程序。

GardenFlow 的小程序版本仍然保持同一个核心定位：

**一个基于天气、植物状态、AI 护理方案和用户护理记录自动运转的个人园艺 Todo 工具。**

它不是植物百科、社区或商城。用户打开后应该在 10 秒内知道：

- 今天有没有植物需要照料；
- 哪些植物需要浇水或施肥；
- 最近一次护理是什么时候；
- 天气是否会影响今天的浇水提醒。

---

## 1. 产品范围

### 1.1 MVP 必须保留

- 添加植物问卷；
- 手动输入植物名；
- 拍摄或上传种子包装/植物照片；
- OCR 提取包装文字；
- AI 生成护理计划；
- 用户确认植物当前状态；
- 本地/云端保存植物；
- 首页植物卡；
- 植物详情页；
- 成长相册；
- 浇水/施肥快捷记录；
- 护理历史；
- 天气获取；
- 任务提醒；
- 中英文切换；
- 位置搜索与手机定位；
- 微信小程序审核所需隐私合规。

### 1.2 暂不做

- 社区；
- 登录注册体系以外的账号系统；
- 商城；
- 支付；
- 多人共享花园；
- 自动灌溉硬件；
- 病虫害图像诊断；
- iOS/Android 原生代码复用。

---

## 2. 推荐技术选型

### 2.1 小程序前端

推荐使用：

- 微信原生小程序；
- TypeScript；
- WXML + WXSS；
- 小程序组件化；
- 微信开发者工具；
- npm 构建；
- ESLint + Prettier。

不建议第一版使用跨端框架，原因：

- GardenFlow 当前功能严重依赖微信平台能力：拍照、相册、位置、订阅消息、隐私授权；
- 上架微信时原生能力调试更直接；
- MVP 页面数量不大，原生小程序维护成本可控。

### 2.2 后端

推荐使用微信云开发 / CloudBase：

- 云数据库：保存植物、护理记录、设置、任务；
- 云存储：保存植物照片、种子包装照片；
- 云函数：代理 DeepSeek、Open-Meteo、OCR 或位置搜索；
- 定时触发器：定时计算今日任务并发送订阅消息。

如果未来需要更强后端能力，可以替换为：

- Node.js 后端；
- PostgreSQL；
- Redis；
- 对象存储；
- 自建定时任务。

但第一版建议使用云开发降低部署复杂度。

---

## 3. 目录结构建议

```text
gardenflow-miniprogram/
├── miniprogram/
│   ├── app.ts
│   ├── app.json
│   ├── app.wxss
│   ├── pages/
│   │   ├── garden/
│   │   │   ├── index.ts
│   │   │   ├── index.wxml
│   │   │   ├── index.wxss
│   │   │   └── index.json
│   │   ├── add/
│   │   │   ├── start/
│   │   │   ├── name/
│   │   │   ├── status/
│   │   │   ├── dates/
│   │   │   └── confirm/
│   │   ├── plant-detail/
│   │   ├── growth-album/
│   │   ├── settings/
│   │   ├── location-search/
│   │   └── privacy/
│   ├── components/
│   │   ├── plant-card/
│   │   ├── weather-card/
│   │   ├── care-plan-card/
│   │   ├── stage-timeline/
│   │   ├── bottom-nav/
│   │   ├── gf-icon/
│   │   └── date-input/
│   ├── services/
│   │   ├── plant-service.ts
│   │   ├── task-service.ts
│   │   ├── weather-service.ts
│   │   ├── ai-service.ts
│   │   ├── ocr-service.ts
│   │   ├── location-service.ts
│   │   └── i18n-service.ts
│   ├── domain/
│   │   ├── reminder-engine.ts
│   │   ├── growth-stage-calculator.ts
│   │   └── model.ts
│   ├── utils/
│   │   ├── date.ts
│   │   ├── format.ts
│   │   ├── validation.ts
│   │   └── constants.ts
│   ├── assets/
│   │   ├── icons/
│   │   └── empty/
│   └── typings/
├── cloudfunctions/
│   ├── deepseekPlantProfile/
│   ├── openMeteoWeather/
│   ├── geocodeLocation/
│   ├── reverseGeocodeLocation/
│   ├── ocrPacket/
│   ├── generateDailyTasks/
│   └── sendTaskSubscribeMessage/
├── project.config.json
├── project.private.config.json
├── package.json
├── tsconfig.json
└── README.md
```

---

## 4. Android 到小程序的模块映射

| Android 现有模块 | 小程序对应模块 |
|---|---|
| Room | 云数据库 / 本地缓存 `wx.setStorage` |
| WorkManager | 云函数定时触发器 + 页面启动时刷新 |
| Android Notification | 微信订阅消息 |
| CameraX | `wx.chooseMedia` / `wx.chooseImage` |
| Photo Picker | `wx.chooseMedia` |
| ML Kit OCR | 云函数 OCR / 腾讯云 OCR / 小程序 OCR 插件 |
| DeepSeekService | 云函数 `deepseekPlantProfile` |
| OpenMeteoWeatherService | 云函数 `openMeteoWeather` |
| GardenLocationProvider | `wx.getLocation` + 云函数逆地理编码 |
| ReminderEngine | 小程序 domain 逻辑 + 云函数复用 |
| GrowthStageCalculator | 小程序 domain 逻辑 |
| GardenText | i18n 文案字典 |

---

## 5. 页面框架

### 5.1 Garden 首页

页面路径：

```text
pages/garden/index
```

页面目标：

让用户一眼知道今天是否有植物需要照料。

主要 UI：

- 顶部标题：
  - 中文：`今天的花园已经照料好了`
  - 英文：`Your garden is all cared for today`
  - 如果有任务：
    - 中文：`今天有 2 株植物需要你的照料`
    - 英文：`2 plants need care today`
- 天气卡：
  - 天气图标；
  - 城市；
  - 温度；
  - 降雨摘要；
  - 户外护理建议；
- `我的植物 / Your Plants`；
- 植物卡列表；
- 悬浮添加按钮；
- 底部导航：
  - `花园 / Garden`
  - `设置 / Settings`

植物卡字段：

- 植物照片，若用户未上传则显示官方 icon；
- 植物名；
- 品种；
- 当前阶段；
- 距离下次浇水还有几天；
- 快捷按钮：
  - 浇水；
  - 施肥。

交互：

- 点击植物卡进入详情；
- 点击浇水立即写入护理记录；
- 点击施肥立即写入护理记录；
- 点击加号进入添加植物问卷。

---

### 5.2 添加植物问卷

页面入口：

```text
pages/add/start
```

添加流程采用 4 步：

```text
Step 1: 选择添加方式
Step 2: 确认植物名称或 OCR 结果
Step 3: 选择植物当前状态
Step 4: 输入关键日期
Step 5: 确认 AI 护理计划
```

虽然 UI 可显示为 `步骤 1/4`，但实现上建议把“确认页”作为生成结果页独立处理。

#### Step 1：选择添加方式

页面设计：

- 标题：
  - 中文：`添加一株新植物`
  - 英文：`Add a new plant`
- 副标题：
  - 中文：`选择添加方式，AI 会帮你完成剩下的事。`
  - 英文：`Choose how to add it. AI will prepare the care plan.`
- 卡片一：拍摄包装照片；
- 卡片二：选择包装图片；
- 卡片三：手动输入名称。

交互建议：

- 拍摄包装照片：直接打开相机；
- 选择包装图片：打开相册；
- 手动输入名称：展开当前卡片，在卡片内部显示输入框，不额外跳出一个悬浮框。

#### Step 2：OCR / 名称确认

如果来自图片：

- 上传图片到云存储；
- 云函数执行 OCR；
- 显示 OCR 文本；
- 用户可以编辑；
- 点击继续。

如果来自手动输入：

- 显示输入的植物名；
- 点击继续。

#### Step 3：植物当前状态

状态选项：

- 种子 / 种子包装；
- 幼苗；
- 苗圃小植株；
- 已定植植株；
- 成熟植株；
- 不确定。

重要逻辑：

- 不要假设用户一定从种子开始；
- 澳洲常见购买形态可能是成熟植株或苗圃盆栽；
- 用户选的当前状态要传给 AI；
- 用户选的状态也要保存为 `confirmedStageKey` 的初始依据。

#### Step 4：关键日期

字段：

- 什么时候种下的；
- 最近一次浇水日期。

日期输入必须同时支持：

- 手动输入 `YYYY-MM-DD`；
- 日历选择；
- 快捷按钮：
  - 今天；
  - 昨天；
  - 3 天前；
  - 还没种下；
  - 未记录。

逻辑：

- 如果用户选择成熟植株但输入了种植日期，必须保存日期；
- 不能因为“成熟/已定植”就清空种植日期；
- 最近一次浇水日期用于计算下一次浇水时间。

#### Step 5：确认护理计划

页面目标：

只展示用户需要确认的护理计划，不展示内部推理。

内容：

- 植物名；
- 品种；
- 当前状态；
- AI 种植指南；
- 浇水计划；
- 施肥计划；
- 适宜温度；
- 预计收获/结果；
- 加入我的花园；
- 重新生成。

注意：

- 确认页不需要展示包装照片；
- 照片应进入成长相册或植物资料；
- AI 结果需要支持中英文展示；
- 中文模式下不能混入英文护理建议。

---

### 5.3 植物详情页

页面路径：

```text
pages/plant-detail/index
```

页面目标：

查看单株植物的完整状态，并允许用户校准预测。

UI 内容：

- 顶部导航：
  - 返回；
  - 标题 GardenFlow；
  - 更多菜单；
- 植物照片 block：
  - 有照片：显示真实照片；
  - 无照片：显示上传按钮；
- 植物名称；
- 品种；
- 当前阶段 pill；
- 生长时间线；
- 护理计划；
- 成长相册入口；
- 护理记录；
- 删除植物。

阶段校准：

- 时间线每一项都可点击；
- 点击后更新 `confirmedStageKey` 和 `confirmedStageDate`；
- 首页卡片必须同步显示已校准阶段；
- 阶段预测只是 Estimated，不代表系统真实知道植物状态。

护理记录展示：

- 中文：
  - `浇水 · 2026-07-25 08:30`
  - `施肥 · 2026-07-25 08:35`
- 英文：
  - `Water · 2026-07-25 08:30`
  - `Fertilise · 2026-07-25 08:35`

禁止展示：

- `WATER - 2026-07-21T14:00:00Z`
- 未翻译 AI 原文；
- 未格式化 UTC 时间。

---

### 5.4 成长相册

页面路径：

```text
pages/growth-album/index
```

功能：

- 上传植物真实照片；
- 更换照片；
- 拍照；
- 从相册选择；
- 上传后植物卡优先展示用户照片；
- 未上传时植物卡展示 icon。

实现：

- 使用 `wx.chooseMedia`；
- 上传到云存储；
- 保存 `photoFileId`；
- 首页卡片和详情页使用同一张主图。

---

### 5.5 设置页

页面路径：

```text
pages/settings/index
```

设置项：

- 花园位置；
- 搜索地名；
- 使用手机定位；
- 提醒开关；
- 提醒检查时间；
- 语言；
- DeepSeek 配置状态；
- 关于 GardenFlow；
- 清除数据。

位置逻辑：

1. 用户输入城市或区域；
2. 调用云函数搜索地名；
3. 返回候选列表；
4. 用户选择正确城市；
5. 保存城市名、纬度、经度；
6. 首页显示城市名，不显示 `Phone location` 这类占位文案。

提醒逻辑：

- 用户可以关闭提醒；
- 不是每天固定提醒所有植物；
- 只有今天有到期任务时才发送订阅消息；
- 后台定时任务只负责检查是否有到期任务。

语言逻辑：

- 支持系统默认；
- 中文；
- 英文；
- 切换后所有 UI 文案、AI 展示文案、错误提示都必须统一。

---

## 6. 数据模型

### 6.1 plants

```ts
interface Plant {
  _id: string
  openid: string
  name: string
  variety?: string
  iconName?: string
  photoFileId?: string
  packageImageFileId?: string
  rawPackageText?: string
  sowingDate?: string
  createdAt: Date
  wateringIntervalDays: number
  wateringAmountMm?: number
  fertilisingIntervalDays: number
  fertilisingAdvice?: string
  rainSkipThresholdMm: number
  preferredTempMinC?: number
  preferredTempMaxC?: number
  harvestMinDays?: number
  harvestMaxDays?: number
  notes: string[]
  growthStages: GrowthStage[]
  confirmedStageKey?: string
  confirmedStageDate?: string
  plantStartStatus?: PlantStartStatus
}
```

### 6.2 growthStages

可以嵌入 `plants.growthStages`，MVP 不必单独建表。

```ts
interface GrowthStage {
  key: string
  label: string
  icon?: string
  startDay: number
  endDay: number
}
```

### 6.3 careHistory

```ts
interface CareHistory {
  _id: string
  openid: string
  plantId: string
  actionType: 'WATER' | 'FERTILISE' | 'CHECK_GROWTH' | 'HARVEST' | 'CUSTOM'
  performedAt: Date
  note?: string
}
```

### 6.4 gardenTasks

```ts
interface GardenTask {
  _id: string
  openid: string
  plantId: string
  type: 'WATER' | 'FERTILISE' | 'CHECK_GROWTH' | 'HARVEST' | 'CUSTOM'
  status: 'DUE' | 'COMPLETED' | 'SNOOZED' | 'SKIPPED' | 'CANCELLED_BY_WEATHER'
  dueAt: Date
  createdAt: Date
  completedAt?: Date
  snoozedUntil?: Date
  reason?: string
}
```

### 6.5 appSettings

```ts
interface AppSettings {
  _id: string
  openid: string
  latitude?: number
  longitude?: number
  locationName?: string
  reminderHour: number
  reminderMinute: number
  notificationsEnabled: boolean
  languageCode?: 'system' | 'zh' | 'en'
}
```

---

## 7. 核心业务逻辑

### 7.1 ReminderEngine

输入：

- plants；
- careHistory；
- weatherSnapshot；
- currentTime。

输出：

- gardenTasks。

浇水规则：

```ts
const wateringDue = daysSinceLastWatering >= wateringIntervalDays
const enoughRecentRain = rainLast24HoursMm >= rainSkipThresholdMm
const enoughUpcomingRain = rainNext12HoursMm >= rainSkipThresholdMm

if (wateringDue && !enoughRecentRain && !enoughUpcomingRain) {
  createWaterTask()
}
```

高温规则：

- 今日最高温大于等于 30°C 时，浇水周期缩短 1 天；
- 最低不能小于 1 天；
- 仍需结合降雨判断。

施肥规则：

```ts
if (daysSinceLastFertilising >= fertilisingIntervalDays) {
  createFertiliseTask()
}
```

天气失败：

- 不阻塞 App；
- 使用基础周期生成任务；
- UI 显示天气不可用。

---

### 7.2 GrowthStageCalculator

逻辑：

```ts
function calculateCurrentStage(
  sowingDate: string | undefined,
  stages: GrowthStage[],
  today: string,
  confirmedStageKey?: string
): GrowthStage | undefined
```

优先级：

1. 如果有 `confirmedStageKey`，使用用户确认阶段；
2. 否则用 `sowingDate` 计算天数；
3. 如果没有日期，显示“未设置日期”或“已校准”。

首页和详情页必须使用同一套计算函数，避免阶段不一致。

---

## 8. 云函数设计

### 8.1 deepseekPlantProfile

用途：

- 输入植物名称、OCR 文本、当前状态、语言；
- 调用 DeepSeek；
- 返回结构化护理方案。

安全要求：

- DeepSeek API Key 只能放在云函数环境变量；
- 小程序前端不得保存任何 API Key；
- 不在日志打印完整 Key；
- 只返回必要字段。

环境变量：

```text
DEEPSEEK_API_URL=
DEEPSEEK_API_KEY=
DEEPSEEK_MODEL=deepseek-chat
```

建议请求：

- 前端语言为中文：使用中文 prompt，但仍要求 JSON 字段名为英文；
- 前端语言为英文：使用英文 prompt；
- 返回 JSON 后由后端清洗，再给小程序前端。

输出结构：

```json
{
  "plant_name": "Dwarf Lemon",
  "variety": "Meyer",
  "icon_name": "lemon",
  "source_summary": "Generated from user input",
  "watering_interval_days": 7,
  "watering_amount_mm": 25,
  "fertilising_interval_days": 60,
  "fertilising_advice": "Use citrus fertiliser during active growth.",
  "rain_skip_threshold_mm": 10,
  "preferred_temp_min_c": 10,
  "preferred_temp_max_c": 35,
  "harvest_min_days": 90,
  "harvest_max_days": 365,
  "notes": [],
  "growth_stages": []
}
```

### 8.2 openMeteoWeather

用途：

- 根据经纬度获取天气；
- 转为统一 `WeatherSnapshot`；
- 不把 Open-Meteo 原始响应直接传给页面。

输出：

```ts
interface WeatherSnapshot {
  currentTemperatureC: number
  rainLast24HoursMm: number
  rainNext12HoursMm: number
  precipitationProbabilityPercent: number
  maxTemperatureTodayC: number
  minTemperatureTodayC: number
  humidityPercent: number
  fetchedAt: string
}
```

### 8.3 geocodeLocation

用途：

- 用户输入地名后搜索候选城市；
- 返回城市名、州/省、国家、纬度、经度；
- 用户选择后保存。

### 8.4 reverseGeocodeLocation

用途：

- 用户使用手机定位后，把经纬度转换为城市名；
- 首页不得显示 `Phone location`；
- 至少显示城市或区域。

### 8.5 generateDailyTasks

用途：

- 定时检查植物；
- 获取天气；
- 运行 ReminderEngine；
- 写入到期任务；
- 如果有任务，触发订阅消息。

### 8.6 sendTaskSubscribeMessage

用途：

- 发送微信订阅消息；
- 只在用户授权且当天有任务时发送。

---

## 9. 微信权限与隐私

### 9.1 需要的权限

- 相机：拍摄种子包装或植物照片；
- 相册：选择包装图片或植物照片；
- 位置：获取花园位置；
- 订阅消息：发送任务提醒。

原则：

- 不在启动时一次性申请；
- 只在用户触发功能时申请；
- 用户拒绝位置后允许手动搜索城市；
- 用户拒绝订阅消息后 App 仍可使用。

### 9.2 隐私保护指引

需要在小程序后台声明：

- 位置信息；
- 图片/相册信息；
- 用户操作记录；
- 设备基础信息；
- 云存储数据；
- 第三方服务调用，包括 DeepSeek、Open-Meteo、OCR 服务。

注意：

- 只有声明过的隐私接口才能正常调用；
- 隐私弹窗和授权逻辑需要在开发阶段就接入；
- 上架前必须在小程序后台完成隐私保护指引配置。

---

## 10. 订阅消息

Android 通知迁移为微信订阅消息。

消息触发：

- 今日有浇水任务；
- 今日有施肥任务；
- 天气导致浇水跳过；
- Snooze 到期后仍需护理。

不发送：

- 没有到期任务；
- 用户关闭提醒；
- 用户没有授权订阅消息；
- 天气已经取消浇水任务。

消息示例：

```text
GardenFlow
今天有 2 株植物需要照料
番茄：浇水
柠檬：施肥
```

---

## 11. 国际化

支持：

- 系统默认；
- 中文；
- 英文。

实现建议：

```text
services/i18n-service.ts
locales/zh-CN.ts
locales/en-US.ts
```

所有页面禁止硬编码展示文案。

AI 结果处理：

- 后端保存结构化字段；
- 前端按当前语言渲染；
- 如果 AI 返回英文说明，中文模式下必须在后端或前端翻译为中文；
- 阶段名必须使用统一映射，不允许首页和详情页各自翻译。

---

## 12. Icon 与植物图片

优先级：

1. 用户上传的真实植物照片；
2. AI 返回的 `icon_name` 对应官方 icon；
3. 默认植物 icon。

规则：

- 植物卡图标与详情页必须使用同一来源；
- 用户上传照片后，首页卡片立即切换为照片；
- 删除照片后回到 icon；
- icon 文件名需要标准化：
  - `lemon`
  - `tomato`
  - `potato`
  - `succulent`
  - `cactus`
  - `herb`
  - `plant_other`

---

## 13. 安全要求

### 13.1 禁止

- 禁止在小程序前端写入 DeepSeek API Key；
- 禁止在前端写 AppSecret；
- 禁止在日志打印完整密钥；
- 禁止前端直接调用 DeepSeek；
- 禁止把用户隐私数据发给未声明服务。

### 13.2 必须

- DeepSeek 调用走云函数；
- Open-Meteo 可走云函数统一代理；
- 云函数校验 `openid`；
- 数据库按 `openid` 隔离；
- 云存储路径按用户隔离；
- 所有网络请求使用 HTTPS；
- 配置 request 合法域名；
- 上架前检查隐私保护指引。

---

## 14. 审核与发布流程

发布前准备：

1. 注册微信小程序账号；
2. 获取 AppID；
3. 安装微信开发者工具；
4. 开通云开发环境；
5. 配置服务器域名；
6. 配置隐私保护指引；
7. 配置订阅消息模板；
8. 完成小程序类目选择；
9. 准备图标、名称、简介、截图；
10. 上传体验版；
11. 内测完整流程；
12. 提交审核；
13. 审核通过后发布。

审核重点：

- 位置权限是否有明确用途；
- 相机/相册是否按需申请；
- AI 内容是否有明确功能说明；
- 是否存在误导性的医疗/农业保证；
- 是否收集不必要个人信息；
- 是否能在无 DeepSeek 配置或网络失败时正常使用；
- 页面是否存在英文残留或乱码；
- 是否有隐私指引弹窗。

---

## 15. 开发里程碑

### Phase 1：项目基础

- 创建小程序项目；
- TypeScript 配置；
- 云开发初始化；
- 底部导航；
- 主题变量；
- i18n 基础。

### Phase 2：数据与领域逻辑

- 数据模型；
- 云数据库集合；
- ReminderEngine；
- GrowthStageCalculator；
- 护理记录。

### Phase 3：首页与详情

- 首页天气卡；
- 植物卡；
- 快捷浇水/施肥；
- 植物详情；
- 阶段校准；
- 护理历史。

### Phase 4：添加植物问卷

- 添加方式；
- 拍照；
- 相册；
- OCR；
- 状态选择；
- 日期选择；
- AI 确认页。

### Phase 5：天气与位置

- 手机定位；
- 逆地理编码；
- 地名搜索；
- Open-Meteo；
- 天气跳过浇水。

### Phase 6：提醒

- 订阅消息授权；
- 定时云函数；
- 到期任务生成；
- 任务消息发送。

### Phase 7：审核准备

- 隐私保护指引；
- 合法域名；
- 小程序后台配置；
- 测试账号；
- 截图；
- 体验版；
- 审核说明。

---

## 16. 测试清单

### 功能测试

- 手动添加番茄；
- 手动添加成熟柠檬树；
- 拍照上传种子包装；
- 从相册选择包装；
- OCR 结果可编辑；
- AI 失败时生成离线初始计划；
- AI 成功时生成护理计划；
- 中文模式无英文残留；
- 英文模式布局不溢出；
- 阶段校准后首页同步；
- 浇水记录进入护理历史；
- 施肥记录进入护理历史；
- 最近一次浇水影响下次浇水时间；
- 用户照片替代植物 icon；
- 删除照片回到 icon；
- 手机定位显示城市名；
- 手动搜索地名能选择候选城市；
- 无位置时天气关闭但 App 可用；
- 有雨时跳过浇水；
- 关闭提醒后不发送消息。

### 审核前测试

- 新用户首次启动；
- 拒绝位置权限；
- 拒绝相机权限；
- 拒绝订阅消息；
- 网络断开；
- DeepSeek API 失败；
- 云函数超时；
- 数据库读写失败；
- 小屏手机；
- 大屏手机；
- 微信深色模式；
- 中文；
- 英文。

---

## 17. 与现有 Android 版本保持一致的关键规则

- AI 负责生成护理规则，不负责每天实时做决策；
- 天气 API 只提供天气数据；
- ReminderEngine 负责是否生成任务；
- 页面不写业务判断；
- 第三方服务必须模块化；
- API Key 不进前端；
- 任意单个 API 失败不能导致 App 无法使用；
- 用户可手动校准植物状态；
- 首页和详情必须使用同一套阶段计算逻辑；
- 用户看到的是“预计/Estimated”，不是绝对判断。

---

## 18. 官方文档入口

由于微信小程序后台和审核规则会变化，以下链接作为开发入口，最终以上架当天后台显示为准：

- 微信小程序开发文档：https://developers.weixin.qq.com/miniprogram/dev/framework/
- 微信开发者工具：https://developers.weixin.qq.com/miniprogram/dev/devtools/devtools.html
- 微信云开发：https://developers.weixin.qq.com/miniprogram/dev/wxcloud/basis/getting-started.html
- 小程序隐私保护：https://developers.weixin.qq.com/miniprogram/dev/framework/user-privacy/
- 腾讯云 CloudBase 文档：https://docs.cloudbase.net/

---

## 19. 第一版交付物

```text
gardenflow-miniprogram/
├── 可在微信开发者工具打开的小程序项目
├── 云函数源码
├── 云数据库集合说明
├── 隐私保护指引填写说明
├── 订阅消息模板说明
├── 测试清单
└── 上架发布说明
```

完成标准：

- 开发者工具无编译错误；
- 真机预览可用；
- 添加植物全流程可用；
- 天气和位置可用；
- AI 失败不崩溃；
- 中文/英文可切换；
- 订阅消息可触发；
- 隐私接口已声明；
- 可以提交微信审核。
