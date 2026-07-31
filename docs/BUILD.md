# 构建、安装、调试指南

## 环境变量（每次新终端都要设）

```bash
export ANDROID_HOME="/c/Users/lhw/AppData/Local/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export JAVA_HOME="/c/Program Files/Microsoft/jdk-21.0.8.9-hotspot"
```

## 方式一：Expo Go 开发（日常调试，推荐）

```bash
# 启动（项目根目录）
npx expo start --clear

# 模拟器连接
adb devices                              # 确认模拟器在线
adb reverse tcp:8081 tcp:8081            # 端口转发（真机需要）
```

然后在模拟器的 Expo Go App 里打开 `exp://localhost:8081`，或者扫码。

**优点**：改代码秒级热更新，不用编译
**缺点**：Expo Go 没有 CALL_PHONE 权限，直接拨号会降级到拨号盘

## 方式二：独立 APK（测试权限功能）

### 第一次构建（全量，约 5 分钟）

```bash
# 确保新架构已关闭（不要动这行）
grep newArchEnabled android/gradle.properties
# 应该输出: newArchEnabled=false

# 构建
cd android && ./gradlew assembleDebug
```

### 后续构建（增量，约 15 秒）

只改了 JS 代码时，增量编译很快：

```bash
cd android && ./gradlew assembleDebug
```

### 安装到模拟器

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

APK 位置：`android/app/build/outputs/apk/debug/app-debug.apk`

### 如果需要离线 APK（不依赖 Metro server）

debug APK 默认从电脑加载 JS（`10.0.2.2:8081`）。要打一个完全离线的 APK：

```bash
# 1. 打包 JS bundle 到 assets
npx react-native bundle \
  --platform android \
  --dev false \
  --entry-file index.js \
  --bundle-output android/app/src/main/assets/index.android.bundle \
  --assets-dest android/app/src/main/res

# 2. 重新构建
cd android && ./gradlew assembleDebug

# 3. 安装
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

## 调试技巧

```bash
# 看设备列表
adb devices

# 看 App 日志（实时）
adb logcat -s ReactNative:V ReactNativeJS:V

# 看 App 权限状态
adb shell dumpsys package com.anonymous.KinshipContactRN | grep "granted"

# 卸载 App
adb uninstall com.anonymous.KinshipContactRN

# 重启模拟器
adb reboot

# 模拟器是否启动完成
adb shell getprop sys.boot_completed
```

## 模拟器

- AVD 名称：`phone_35`
- 系统：Android 35, Google APIs, x86_64
- 启动命令：`emulator -avd phone_35`

## 修改代码后的流程

### 如果用 Expo Go：
改完代码 → 自动热更新 → 模拟器秒级刷新

### 如果用独立 APK：
改完 JS → `cd android && ./gradlew assembleDebug`（15秒）→ `adb install -r ...` → 手动打开 App

## 常见问题

| 问题 | 解决 |
|------|------|
| `BUILD FAILED` 提到 NDK | 检查 `newArchEnabled=false` |
| 端口 8081 被占用 | `npx kill-port 8081` |
| APK 安装失败 | 先 `adb uninstall com.anonymous.KinshipContactRN` |
| TypeScript 报错 | 先在项目根目录跑 `npx tsc --noEmit` 检查 |
| 直接拨打不工作 | 检查 `adb shell dumpsys package com.anonymous.KinshipContactRN \| grep CALL_PHONE`，看 granted 是不是 true |
