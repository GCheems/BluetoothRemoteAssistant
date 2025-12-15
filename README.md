# 蓝牙遥控助手 (Bluetooth Remote Assistant)

一个通用的 BLE 调试与遥控工具 Android 应用，能够扫描、连接并通过特征值（Characteristic）与单片机（MCU）通信。

## 📱 功能特性

- **设备扫描**：扫描附近的 BLE 设备，显示设备名称、MAC 地址、RSSI 信号强度
- **设备连接**：快速连接选定的 BLE 设备
- **服务发现**：自动发现并列出所有 GATT 服务和特征值
- **数据交互**：
  - 读取特征值
  - 写入特征值（支持字符串和 Hex 格式）
  - 启用/禁用通知和指示
- **实时日志**：显示所有蓝牙通信日志，包括数据的 Hex 和 ASCII 格式
- **权限管理**：自动处理 Android 12+ 的蓝牙和位置权限

## 🛠️ 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose (Material Design 3)
- **架构**：MVVM (Model-View-ViewModel)
- **异步处理**：Kotlin Coroutines & Flow
- **蓝牙**：Android 原生 BLE API
- **权限处理**：Accompanist Permissions

## 📋 系统要求

- **最低 SDK**：API 23 (Android 6.0)
- **目标 SDK**：API 34 (Android 14)
- **编译 SDK**：API 34

## 🏗️ 项目结构

```
app/src/main/java/com/example/bluetoothremoteassistant/
├── data/
│   ├── model/
│   │   ├── BleDevice.kt              # 设备数据模型
│   │   ├── BleCharacteristic.kt      # 特征值数据模型
│   │   └── BleService.kt             # 服务数据模型
│   └── BluetoothLeManager.kt         # 蓝牙管理器（核心类）
├── ui/
│   ├── component/
│   │   ├── CharacteristicItem.kt     # 特征值列表项组件
│   │   ├── DataInputDialog.kt        # 数据输入对话框
│   │   └── DeviceListItem.kt         # 设备列表项组件
│   ├── screen/
│   │   ├── DeviceControlScreen.kt    # 设备控制页面
│   │   └── ScanScreen.kt             # 扫描页面
│   ├── theme/                         # Material 3 主题
│   └── MainActivity.kt                # 主活动
├── util/
│   └── DataConverter.kt               # 数据转换工具
├── viewmodel/
│   └── MainViewModel.kt               # 主 ViewModel
└── BluetoothRemoteApp.kt              # Application 类
```

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd BluetoothRemoteAssistant
```

### 2. 在 Android Studio 中打开项目

使用 Android Studio（建议使用最新版本）打开项目。

### 3. 同步 Gradle

项目会自动下载所需依赖。如果没有自动同步，点击 `File > Sync Project with Gradle Files`。

### 4. 运行应用

- 连接 Android 设备或启动模拟器
- 点击运行按钮（绿色三角形）或使用快捷键 `Shift + F10`

**注意**：模拟器需要支持蓝牙功能才能完整测试应用。

## 📖 使用说明

### 扫描设备

1. 启动应用后，在扫描页面点击"开始扫描"按钮
2. 应用会列出附近的所有 BLE 设备
3. 每个设备显示名称、MAC 地址和信号强度

### 连接设备

1. 在设备列表中点击要连接的设备
2. 应用会自动停止扫描并尝试连接
3. 连接成功后会显示设备的所有服务和特征值

### 操作特征值

**读取数据**：
- 点击特征值的"读取"按钮
- 读取结果会显示在日志区域

**写入数据**：
- 点击特征值的"写入"按钮
- 在弹出的对话框中选择数据格式（字符串或 Hex）
- 输入数据后点击"发送"

**启用通知**：
- 点击特征值的"通知"按钮
- 按钮会变为高亮状态表示已启用
- 设备发送的通知数据会实时显示在日志中

### 查看日志

- 日志区域位于页面底部
- 显示所有蓝牙操作记录（读取、写入、通知）
- 数据以 Hex 和 ASCII 两种格式显示
- 点击"清空"按钮可清除所有日志

## 🔧 核心类说明

### BluetoothLeManager

蓝牙管理单例类，封装了所有蓝牙操作：

```kotlin
// 开始扫描
fun startScan()

// 停止扫描
fun stopScan()

// 连接设备
fun connect(device: BleDevice)

// 断开连接
fun disconnect()

// 读取特征值
fun readCharacteristic(serviceUuid: UUID, characteristicUuid: UUID): Boolean

// 写入特征值
fun writeCharacteristic(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray): Boolean

// 设置通知
fun setCharacteristicNotification(serviceUuid: UUID, characteristicUuid: UUID, enable: Boolean): Boolean
```

### MainViewModel

管理应用状态，提供以下 StateFlow：

- `scannedDevices: StateFlow<List<BleDevice>>` - 扫描到的设备列表
- `connectionState: StateFlow<ConnectionState>` - 连接状态
- `services: StateFlow<List<BleService>>` - 服务列表
- `dataLogs: StateFlow<List<String>>` - 数据日志

### DataConverter

数据转换工具类：

```kotlin
// 字符串 <-> ByteArray
fun stringToBytes(str: String): ByteArray
fun bytesToString(bytes: ByteArray): String

// Hex <-> ByteArray
fun hexToBytes(hex: String): ByteArray?
fun bytesToHex(bytes: ByteArray): String

// 验证 Hex 格式
fun isValidHex(hex: String): Boolean
```

## 🔐 权限说明

应用需要以下权限：

### Android 12+ (API 31+)
- `BLUETOOTH_SCAN` - 扫描 BLE 设备
- `BLUETOOTH_CONNECT` - 连接 BLE 设备
- `ACCESS_FINE_LOCATION` - 蓝牙扫描需要（系统要求）

### Android 11 及以下
- `BLUETOOTH` - 基础蓝牙功能
- `BLUETOOTH_ADMIN` - 蓝牙管理
- `ACCESS_FINE_LOCATION` - 蓝牙扫描需要

所有权限在应用启动时会自动请求。

## 🐛 故障排除

### 无法扫描到设备

1. 确认蓝牙已开启
2. 确认已授予所有必需权限
3. 确认目标设备处于广播模式
4. 检查设备是否在有效范围内（一般 10 米以内）

### 连接失败

1. 确认设备未被其他应用连接
2. 尝试关闭并重新开启蓝牙
3. 重启应用
4. 确认设备支持标准 BLE 协议

### 无法写入数据

1. 确认特征值支持写入操作（属性列表中有"写"）
2. 检查数据格式是否正确
3. 确认单次写入数据量不超过 MTU 限制（通常 20-512 字节）

## 📝 开发说明

### 添加新功能

1. 在 `BluetoothLeManager` 中实现底层蓝牙逻辑
2. 在 `MainViewModel` 中暴露相应方法和状态
3. 在 UI 层添加相应的界面和交互

### 自定义主题

修改 `ui/theme/` 目录下的文件：
- `Color.kt` - 颜色定义
- `Theme.kt` - 主题配置
- `Type.kt` - 字体样式

### 数据持久化

如需保存设备列表或配置，可集成：
- DataStore（推荐）
- Room 数据库
- SharedPreferences

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 👨‍💻 作者

资深 Android 开发工程师

---

**注意**：此应用仅供调试和学习使用，请勿用于生产环境的医疗、安全等关键领域。
