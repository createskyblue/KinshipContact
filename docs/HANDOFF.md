# 项目交接文档

## 项目概述

将原 Kotlin 项目 [KinshipContact](https://github.com/createskyblue/KinshipContact) 用 React Native (Expo SDK 57) 重写。
App 功能：**亲情通讯录** — 老人模式一键拨号 + 管理模式增删改联系人。

## 技术栈

| 项 | 值 |
|---|---|
| 框架 | React Native 0.86 + Expo SDK 57 |
| 语言 | TypeScript |
| 状态管理 | React Context + useReducer |
| 数据存储 | AsyncStorage（JSON 数组，非 SQLite） |
| 构建 | `newArchEnabled=false`（禁用新架构，不用 NDK） |
| 开发调试 | Expo Go（热更新快，但无 CALL_PHONE 权限） |
| 最终测试 | 独立 debug APK（有 CALL_PHONE 权限，可直接拨打） |

## 项目结构

```
KinshipContactRN/
├── App.tsx                              # 主界面（顶栏 + 联系人网格 + 所有弹窗）
├── app.json                             # Expo 配置（权限、插件）
├── index.js                             # 入口
├── android/                             # prebuild 生成的 Android 原生工程
├── src/
│   ├── types/contact.ts                 # 类型定义 + 默认设置
│   ├── store/AppContext.tsx              # 全局状态（Reducer + Context）
│   ├── utils/
│   │   ├── storage.ts                   # AsyncStorage 存取联系人和设置
│   │   ├── dialer.ts                    # 拨号（先申请权限，再直接拨打）
│   │   └── backup.ts                    # JSON 备份/还原
│   └── components/
│       ├── ContactCard.tsx              # 联系人卡片（支持头像照片）
│       ├── CallConfirmModal.tsx         # 红色拨号确认弹窗
│       ├── AdminPasswordModal.tsx       # 管理员密码验证
│       ├── ContactEditModal.tsx         # 添加/编辑联系人（含头像选择）
│       ├── DeleteConfirmModal.tsx       # 删除确认
│       ├── SettingsModal.tsx            # 设置（列数/字号/颜色/拨号方案等）
│       ├── SystemContactsImportModal.tsx # 系统通讯录导入
│       └── PermissionGuideModal.tsx     # 首次权限引导
└── docs/
    └── HANDOFF.md                       # 本文档
```

## 双模式逻辑

### 老人模式（默认）
- 显示联系人卡片网格，点卡片 → 红色"确认拨号"弹窗 → 拨打
- 长按标题栏 → 弹出密码框（当"隐藏设置按钮"开启时）
- 右上角"管理"按钮 → 弹密码框 → 进入管理模式

### 管理模式
- 密码：默认 `123`，可在设置中修改
- 顶部黄色条提示"管理模式"
- 可添加/编辑/删除联系人，上移/下移排序
- 右上角"设置"→ 调整列数/字号/颜色/拨号方案/备份还原
- 右上角"退出"→ 回到老人模式

## 拨号方案

| 方案 | 行为 |
|---|---|
| 拨号盘（DIAL） | 打开拨号盘，号码已填好，用户手动按拨出 |
| 直接拨打（CALL） | 申请 CALL_PHONE 权限 → 直接拨出 |

权限请求在 `dialer.ts` 中通过 `PermissionsAndroid.request()` 实现。

## 构建与安装

### 开发（Expo Go，热更新）
```bash
npx expo start
```
然后在 Expo Go App 里扫码或连 `localhost:8081`。
**注意：Expo Go 没有 CALL_PHONE 权限，直接拨打会降级到拨号盘。**

### 测试独立 APK（有完整权限）
```bash
# 1. 确保 newArchEnabled=false（已设置好）
grep newArchEnabled android/gradle.properties

# 2. 构建
export ANDROID_HOME="/c/Users/lhw/AppData/Local/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export JAVA_HOME="/c/Program Files/Microsoft/jdk-21.0.8.9-hotspot"
cd android && ./gradlew assembleDebug

# 3. 安装到模拟器
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

增量编译（只改了 JS）约 14 秒，全量编译约 5 分钟。

## 当前环境

| 工具 | 路径 |
|---|---|
| Android SDK | `C:\Users\lhw\AppData\Local\Android\Sdk` |
| Java 21 | `C:\Program Files\Microsoft\jdk-21.0.8.9-hotspot` |
| Android CLI | winget 安装的 `android` 命令 |
| 模拟器 AVD | `phone_35`（Android 35, Google APIs, x86_64） |
| NDK | 已删除（不需要，newArchEnabled=false） |
| CMake（SDK 内） | 已删除（不需要） |

## 环境变量备份

`C:\Users\lhw\Desktop\env_backup_20260731_075321.txt`

## 已修复的 UI 问题

1. 右上角"管理"/"设置"按钮触摸区域改为 44×44px（之前 28px 按不到）
2. 顶栏被系统状态栏遮挡 → 替换 SafeAreaView，手动加 StatusBar.currentHeight
3. 老人模式"设置"改名为"管理"，避免与管理模式"设置"混淆
4. 设置弹窗去掉多余"关闭"按钮，只保留"退出管理模式"
5. 设置弹窗遮罩改为纯黑不透明（之前半透明能看到顶栏按钮造成混淆）
6. 系统通讯录导入报错/加载时加了返回按钮（之前卡死）
7. 编辑联系人增加了头像选择（expo-image-picker）

## 待处理

- JS 没有打进 APK（debug 包需要 Metro server），如需离线 APK 需要 `npx react-native bundle` 打包 JS 到 assets
- 通讯录导入功能需要完整测试
- 头像功能需要完整测试
- 备份/还原功能需要测试
- 可考虑创建 release APK 用于正式分发

## 参考

- 原 Kotlin 项目：`C:\Users\lhw\AppData\Local\Temp\KinshipContact`（仅作参考，不修改）
- Expo SDK 57 文档：https://docs.expo.dev/versions/v57.0.0/
