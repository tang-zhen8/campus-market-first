# 校园二手交易（Campus Market）

一个校园二手书交易 Android 应用（无支付版），使用 Kotlin + Android 原生开发。

## 功能

- 首页商品列表（下拉刷新、卡片展示价格 / 分类 / 留言数）
- 商品详情（收藏 / 取消收藏、留言互动）
- 发布闲置（标题、价格、分类、描述）
- 消息页（汇总我发布的商品收到的留言）
- 我的（我的收藏、我的发布）

## 技术栈

- Kotlin + ViewBinding
- 单 Activity + 底部导航 + 多 Fragment
- Material 3 组件（卡片、输入框、Chip、底部导航）
- LiveData 单向数据流（data 层仓库模式，后续可平滑替换为 Room / 云端接口）
- ListAdapter + DiffUtil 高效刷新列表

## 构建

用 Android Studio 打开项目直接运行，或执行：

```bash
./gradlew assembleDebug
```

> 注意：`local.properties` 包含本机 SDK 路径，不入库（见 `.gitignore`）。
