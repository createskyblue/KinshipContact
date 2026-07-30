package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    gridColumns: Int,
    fontSize: String,
    fontColor: String,
    dialScheme: String,
    hideSettingsButton: Boolean,
    onGridColumnsChanged: (Int) -> Unit,
    onFontSizeChanged: (String) -> Unit,
    onFontColorChanged: (String) -> Unit,
    onDialSchemeChanged: (String) -> Unit,
    onHideSettingsButtonChanged: (Boolean) -> Unit,
    onOpenSystemImport: () -> Unit,
    onBackup: () -> Unit,
    onRestoreFileSelected: (File) -> Unit,
    onExitAdminMode: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val restorePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val contentResolver = context.contentResolver
            val tempFile = File(context.cacheDir, "temp_restore.zip")
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (tempFile.exists()) {
                onRestoreFileSelected(tempFile)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_dialog"),
            color = Color(0xFFF5F7FA)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "家属管理与系统设置",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("close_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "返回主页",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF1565C0)
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card 1: Layout & Display Config
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionHeader("1. 主界面每行卡片数量", Icons.Default.GridOn)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(2 to "2列 (大卡)", 3 to "3列 (中卡)", 4 to "4列 (小卡)").forEach { (cols, label) ->
                                    val isSelected = (gridColumns == cols)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onGridColumnsChanged(cols) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color(0xFF1565C0) else Color(0xFFF1F3F4)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else Color(0xFF424242)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                            SectionHeader("2. 老人界面文字大小", Icons.Default.FormatSize)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("large" to "大号", "xlarge" to "特大", "massive" to "超大").forEach { (key, label) ->
                                    val isSelected = (fontSize == key)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onFontSizeChanged(key) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color(0xFF1565C0) else Color(0xFFF1F3F4)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else Color(0xFF424242)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                            SectionHeader("3. 姓名电话文字颜色", Icons.Default.Palette)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "dark" to ("经典黑" to Color(0xFF1A1C1E)),
                                    "red" to ("靓丽红" to Color(0xFFB71C1C)),
                                    "blue" to ("商务蓝" to Color(0xFF0D47A1)),
                                    "green" to ("沉稳绿" to Color(0xFF1B5E20))
                                ).forEach { (key, pair) ->
                                    val (label, colorVal) = pair
                                    val isSelected = (fontColor == key)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onFontColorChanged(key) },
                                        shape = RoundedCornerShape(10.dp),
                                        border = if (isSelected) BorderStroke(2.dp, Color(0xFF1565C0)) else null,
                                        color = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF1F3F4)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .background(colorVal, shape = CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = label,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color(0xFF1565C0) else Color(0xFF424242)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card 2: Dialing Scheme & Protection Config
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionHeader("4. 拨号方案选择 (呼叫模式)", Icons.Default.Phone)
                            Spacer(modifier = Modifier.height(10.dp))

                            listOf(
                                "CALL" to ("一键直拨模式 (推荐)" to "老人点击卡片确认后直接拨出电话，无需中转，避免误操作"),
                                "DIAL" to ("系统拨号盘模式" to "老人点击卡片后将号码填入手机自带拨号界面，需手动点绿键")
                            ).forEach { (key, pair) ->
                                val (title, desc) = pair
                                val isSelected = (dialScheme == key)

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onDialSchemeChanged(key) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF2E7D32) else Color(0xFFE0E0E0)
                                    ),
                                    color = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFFAFAFA)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFF2E7D32) else Color.LightGray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = title,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFF1B5E20) else Color(0xFF212121)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = desc,
                                                fontSize = 12.sp,
                                                color = Color(0xFF616161)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color(0xFF1565C0)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "隐藏老人界面的设置图标",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF212121)
                                        )
                                        Text(
                                            text = "防止老人误点。隐藏后长按顶部“亲情联系人”标题3秒可重新唤出密码框",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Switch(
                                    checked = hideSettingsButton,
                                    onCheckedChange = onHideSettingsButtonChanged,
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1565C0))
                                )
                            }
                        }
                    }

                    // Card 3: Import & Data Backup Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionHeader("5. 通讯录导入与数据备份", Icons.Default.Contacts)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = onOpenSystemImport,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF1565C0)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = null,
                                    tint = Color(0xFF1565C0),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "从手机系统通讯录批量导入",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onBackup,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF1565C0)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Backup,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFF1565C0)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("数据备份", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                                }

                                OutlinedButton(
                                    onClick = { restorePickerLauncher.launch("application/zip") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF1565C0)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFF1565C0)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("数据还原", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                                }
                            }
                        }
                    }

                    // Card 4: Exit Admin Mode Button
                    OutlinedButton(
                        onClick = onExitAdminMode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "退出管理员设置，返回老人界面",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF1565C0),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E)
        )
    }
}
