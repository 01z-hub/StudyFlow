# ADR 0001：离线目录与学习进度的数据边界

## 状态

已采用，2026-07-24。

## 背景

StudyFlow 当前没有真实后端。教材目录与练习均由项目自行编写，并随 APK 通过
`assets/catalog.json` 和 `assets/exercises.json` 发布；练习状态、答案、当前题号、
分数和最近学习时间会在运行过程中变化，并且需要在应用重启后恢复。

## 决策

- 将不可变的教材目录和练习题保留在 Assets 中，由 `TextbookRepository` 读取。
- 将可变的学习进度保存到 Room，由 `StudyRepository` 统一读写。
- UI 不直接访问 Assets 或 Room，只消费 `StudyViewModel` 暴露的 `StateFlow`。
- 练习状态转换集中在 `ExerciseWorkflow`，Room 只存储经过状态机确认的结果。
- 使用 Koin 4.1.x 的经典 DSL 装配依赖，保持与 Kotlin 2.2 的兼容。
- 使用 KSP 2.3.x 生成 Room 代码，以兼容 AGP 9 的内置 Kotlin。

## 取舍

没有把 Assets 再复制一份到 Room。当前目录随 APK 发布且没有网络更新源，重复缓存不会
带来离线收益，反而会增加初始化、版本同步和迁移成本。若未来引入公开网络数据源，再把
教材目录改为 Room 驱动的离线优先缓存，并补充缓存命中与更新策略测试。

## 验证

- JVM 单元测试覆盖四态转换、评分和 ViewModel 的 Loading/Error/Empty/Content。
- Android 设备测试使用内存 Room 覆盖开始、保存答案、提交、自批完成和结果读取。
- 手工验收覆盖应用强制结束后的进度恢复，以及手机、横屏、平板和字体缩放。
