package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "家属管理与设置",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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
                                    contentDescription = "返回",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF1565C0),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Section Header: 界面显示
                    SettingsSectionHeader(title = "界面与显示设置")

                    // Grid columns preference
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ListItem(
                            headlineContent = { Text("主界面排版列数") },
                            supportingContent = { Text("控制联系人卡片的大小与分布") },
                            leadingContent = {
                                Icon(Icons.Default.GridOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2 to "2列 (大卡)", 3 to "3列 (中卡)", 4 to "4列 (小卡)").forEach { (cols, label) ->
                                val isSelected = (gridColumns == cols)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onGridColumnsChanged(cols) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Font size preference
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ListItem(
                            headlineContent = { Text("老人界面文字大小") },
                            supportingContent = { Text("调整卡片姓名与号码字号") },
                            leadingContent = {
                                Icon(Icons.Default.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("large" to "大号", "xlarge" to "特大", "massive" to "超大").forEach { (key, label) ->
                                val isSelected = (fontSize == key)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onFontSizeChanged(key) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Font color preference
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ListItem(
                            headlineContent = { Text("姓名电话文字颜色") },
                            leadingContent = {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
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
                                    shape = RoundedCornerShape(8.dp),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(colorVal, shape = CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Section Header: 呼叫与防护
                    SettingsSectionHeader(title = "呼叫与防护")

                    // Dial scheme options
                    ListItem(
                        headlineContent = { Text("一键直拨模式") },
                        supportingContent = { Text("点击确认后直接拨出电话，避免误操作（推荐）") },
                        leadingContent = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingContent = {
                            RadioButton(
                                selected = (dialScheme == "CALL"),
                                onClick = { onDialSchemeChanged("CALL") }
                            )
                        },
                        modifier = Modifier.clickable { onDialSchemeChanged("CALL") }
                    )

                    ListItem(
                        headlineContent = { Text("系统拨号盘模式") },
                        supportingContent = { Text("点击卡片后填入手机拨号盘，需手动按绿色拨号键") },
                        trailingContent = {
                            RadioButton(
                                selected = (dialScheme == "DIAL"),
                                onClick = { onDialSchemeChanged("DIAL") }
                            )
                        },
                        modifier = Modifier.clickable { onDialSchemeChanged("DIAL") }
                    )

                    // Hide settings icon switch
                    ListItem(
                        headlineContent = { Text("隐藏老人界面的设置图标") },
                        supportingContent = { Text("隐藏后可长按顶部“亲情联系人”3秒重新唤出密码框") },
                        leadingContent = {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingContent = {
                            Switch(
                                checked = hideSettingsButton,
                                onCheckedChange = onHideSettingsButtonChanged
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Section Header: 数据管理
                    SettingsSectionHeader(title = "数据管理与备份")

                    ListItem(
                        headlineContent = { Text("从手机系统通讯录批量导入") },
                        leadingContent = {
                            Icon(Icons.Default.Contacts, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        modifier = Modifier.clickable { onOpenSystemImport() }
                    )

                    ListItem(
                        headlineContent = { Text("数据备份") },
                        supportingContent = { Text("导出联系人及其头像压缩包") },
                        leadingContent = {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        modifier = Modifier.clickable { onBackup() }
                    )

                    ListItem(
                        headlineContent = { Text("数据还原") },
                        supportingContent = { Text("选择备份压缩包（ZIP）恢复联系人") },
                        leadingContent = {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        modifier = Modifier.clickable { restorePickerLauncher.launch("application/zip") }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Exit Admin Mode Action
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "退出家属管理模式",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier.clickable { onExitAdminMode() },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
    )
}

