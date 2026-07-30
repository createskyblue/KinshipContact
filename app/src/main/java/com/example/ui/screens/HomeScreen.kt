package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.Contact
import com.example.ui.components.AdminActionDialog
import com.example.ui.components.AdminPasswordDialog
import com.example.ui.components.CallConfirmDialog
import com.example.ui.components.ContactCard
import com.example.ui.components.ContactEditDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.PermissionGuideDialog
import com.example.ui.components.PhotoCropperDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SystemContactsImportDialog
import com.example.ui.viewmodel.ContactViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: ContactViewModel
) {
    val context = LocalContext.current

    val contacts by viewModel.contacts.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val pendingCallContact by viewModel.pendingCallContact.collectAsState()
    val isCallLocked by viewModel.isCallLocked.collectAsState()

    val adminActionContact by viewModel.adminActionContact.collectAsState()
    val editingContact by viewModel.editingContact.collectAsState()
    val isCreatingNew by viewModel.isCreatingNew.collectAsState()
    val deleteCandidateContact by viewModel.deleteCandidateContact.collectAsState()

    val showAdminPasswordDialog by viewModel.showAdminPasswordDialog.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()

    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
    val showSystemImportDialog by viewModel.showSystemImportDialog.collectAsState()
    val showPermissionGuide by viewModel.showPermissionGuide.collectAsState()

    val cropSourceUri by viewModel.cropSourceUri.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val gridColumns by viewModel.gridColumns.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val fontColor by viewModel.fontColor.collectAsState()
    val dialScheme by viewModel.dialScheme.collectAsState()
    val hideSettingsButton by viewModel.hideSettingsButton.collectAsState()

    // Permission Launcher
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.dismissPermissionGuide()
    }

    // Toast listener
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    if (hideSettingsButton && !isAdminMode) {
                                        viewModel.openAdminPasswordDialog()
                                    }
                                }
                            )
                    ) {
                        Text(
                            text = "亲情联系人",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        if (isAdminMode) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFFFD54F),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "家属管理模式",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF37474F),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (isAdminMode) {
                        IconButton(
                            onClick = { viewModel.openSettingsDialog() },
                            modifier = Modifier.testTag("admin_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "界面设置",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { viewModel.exitAdminMode() },
                            modifier = Modifier.testTag("exit_admin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "退出设置",
                                tint = Color(0xFFFFCDD2)
                            )
                        }
                    } else {
                        if (!hideSettingsButton) {
                            IconButton(
                                onClick = { viewModel.openAdminPasswordDialog() },
                                modifier = Modifier.testTag("open_admin_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "设置",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isAdminMode) Color(0xFF1565C0) else Color(0xFFD32F2F)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F7FA))
        ) {
            // Admin Action Banner
            if (isAdminMode) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.openAddContactDialog() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1565C0),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("add_contact_button")
                        ) {
                            Text(
                                text = "添加联系人",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Button(
                            onClick = { viewModel.openSystemImportDialog() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1565C0),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Text(
                                text = "从通讯录导入",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Contact Cards Grid
            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "暂无联系人卡片",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        if (isAdminMode) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.openAddContactDialog() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                            ) {
                                Text("点击立即添加联系人", fontSize = 16.sp)
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    itemsIndexed(contacts, key = { _, item -> item.id }) { index, item ->
                        ContactCard(
                            contact = item,
                            fontSizeKey = fontSize,
                            fontColorKey = fontColor,
                            isAdminMode = isAdminMode,
                            isCallLocked = isCallLocked,
                            canMoveUp = index > 0,
                            canMoveDown = index < contacts.size - 1,
                            onClick = { viewModel.onContactCardClicked(item) },
                            onMoveUp = { viewModel.swapContactOrder(index, index - 1) },
                            onMoveDown = { viewModel.swapContactOrder(index, index + 1) }
                        )
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    // 1. Red Elderly Call Confirm Dialog
    pendingCallContact?.let { targetContact ->
        CallConfirmDialog(
            contact = targetContact,
            onConfirmCall = {
                executeDialing(context, targetContact.phoneNumber, dialScheme)
                viewModel.onCallExecuted()
            },
            onDismiss = { viewModel.dismissCallConfirmDialog() }
        )
    }

    // 2. Admin Password Dialog
    if (showAdminPasswordDialog) {
        AdminPasswordDialog(
            hasError = passwordError,
            onVerify = { viewModel.verifyAdminPassword(it) },
            onDismiss = { viewModel.dismissAdminPasswordDialog() }
        )
    }

    // 3. Admin Click Card Action Dialog (Edit vs Delete)
    adminActionContact?.let { targetContact ->
        AdminActionDialog(
            contact = targetContact,
            onProceedEdit = { viewModel.proceedToEditFromAdminAction(targetContact) },
            onProceedDelete = { viewModel.proceedToDeleteFromAdminAction(targetContact) },
            onDismiss = { viewModel.dismissAdminActionDialog() }
        )
    }

    // 4. Secondary Delete Confirmation Dialog
    deleteCandidateContact?.let { candidate ->
        DeleteConfirmDialog(
            contact = candidate,
            onConfirmDelete = { deletePhoto -> viewModel.confirmDeleteContact(deletePhoto) },
            onDismiss = { viewModel.dismissDeleteConfirmDialog() }
        )
    }

    // 5. Contact Edit / Add Dialog
    editingContact?.let { contact ->
        ContactEditDialog(
            contact = contact,
            isCreatingNew = isCreatingNew,
            onPickImageRequested = { uri -> viewModel.setCropSourceUri(uri) },
            onSave = { name, phone, photo -> viewModel.saveContact(name, phone, photo) },
            onDeleteRequested = {
                viewModel.dismissContactEditDialog()
                viewModel.proceedToDeleteFromAdminAction(contact)
            },
            onDismiss = { viewModel.dismissContactEditDialog() }
        )
    }

    // 6. Photo Cropper Dialog
    cropSourceUri?.let { uri ->
        PhotoCropperDialog(
            imageUri = uri,
            onCroppedAndSaved = { savedPath -> viewModel.onAvatarCroppedAndSaved(savedPath) },
            onDismiss = { viewModel.dismissPhotoCropper() }
        )
    }

    // 7. Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            gridColumns = gridColumns,
            fontSize = fontSize,
            fontColor = fontColor,
            dialScheme = dialScheme,
            hideSettingsButton = hideSettingsButton,
            onGridColumnsChanged = { viewModel.updateGridColumns(it) },
            onFontSizeChanged = { viewModel.updateFontSize(it) },
            onFontColorChanged = { viewModel.updateFontColor(it) },
            onDialSchemeChanged = { viewModel.updateDialScheme(it) },
            onHideSettingsButtonChanged = { viewModel.updateHideSettingsButton(it) },
            onOpenSystemImport = {
                viewModel.dismissSettingsDialog()
                viewModel.openSystemImportDialog()
            },
            onBackup = { viewModel.triggerBackup() },
            onRestoreFileSelected = { zipFile -> viewModel.triggerRestore(zipFile) },
            onExitAdminMode = { viewModel.exitAdminMode() },
            onDismiss = { viewModel.dismissSettingsDialog() }
        )
    }

    // 8. System Contacts Batch Import Dialog
    if (showSystemImportDialog) {
        SystemContactsImportDialog(
            onImportConfirmed = { selectedList -> viewModel.importSystemContacts(selectedList) },
            onDismiss = { viewModel.dismissSystemImportDialog() }
        )
    }

    // 9. First Launch Permission Guide Dialog
    if (showPermissionGuide) {
        PermissionGuideDialog(
            onRequestPermissions = {
                multiplePermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_CONTACTS
                    )
                )
            }
        )
    }
}

/**
 * Executes dialing operation according to user's configured scheme (DIAL vs CALL).
 */
private fun executeDialing(context: Context, rawPhone: String, dialScheme: String) {
    try {
        val cleanPhone = rawPhone.replace(" ", "").replace("-", "")
        if (dialScheme == "CALL") {
            val hasCallPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED

            if (hasCallPermission) {
                val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanPhone"))
                context.startActivity(callIntent)
            } else {
                // Fallback to ACTION_DIAL if CALL_PHONE permission not granted
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
                context.startActivity(dialIntent)
            }
        } else {
            // Default ACTION_DIAL system dialer prefill
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
            context.startActivity(dialIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "无法发起拨号: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
