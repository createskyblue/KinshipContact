package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.Contact
import com.example.data.ContactRepository
import com.example.utils.BackupUtils
import com.example.utils.ImageUtils
import com.example.utils.SystemContactsUtils
import com.example.utils.VibrationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository
    val appSettings: AppSettings

    val contacts: StateFlow<List<Contact>>

    // Mode state
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Normal mode call confirmation state
    private val _pendingCallContact = MutableStateFlow<Contact?>(null)
    val pendingCallContact: StateFlow<Contact?> = _pendingCallContact.asStateFlow()

    private val _isCallLocked = MutableStateFlow(false)
    val isCallLocked: StateFlow<Boolean> = _isCallLocked.asStateFlow()

    // Admin dialog states
    private val _adminActionContact = MutableStateFlow<Contact?>(null)
    val adminActionContact: StateFlow<Contact?> = _adminActionContact.asStateFlow()

    private val _editingContact = MutableStateFlow<Contact?>(null)
    val editingContact: StateFlow<Contact?> = _editingContact.asStateFlow()
    private val _isCreatingNew = MutableStateFlow(false)
    val isCreatingNew: StateFlow<Boolean> = _isCreatingNew.asStateFlow()

    private val _deleteCandidateContact = MutableStateFlow<Contact?>(null)
    val deleteCandidateContact: StateFlow<Contact?> = _deleteCandidateContact.asStateFlow()

    private val _showAdminPasswordDialog = MutableStateFlow(false)
    val showAdminPasswordDialog: StateFlow<Boolean> = _showAdminPasswordDialog.asStateFlow()

    private val _passwordError = MutableStateFlow(false)
    val passwordError: StateFlow<Boolean> = _passwordError.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showSystemImportDialog = MutableStateFlow(false)
    val showSystemImportDialog: StateFlow<Boolean> = _showSystemImportDialog.asStateFlow()

    private val _showPermissionGuide = MutableStateFlow(false)
    val showPermissionGuide: StateFlow<Boolean> = _showPermissionGuide.asStateFlow()

    // Crop image URI state
    private val _cropSourceUri = MutableStateFlow<Uri?>(null)
    val cropSourceUri: StateFlow<Uri?> = _cropSourceUri.asStateFlow()

    // Toast / Message feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Dynamic Preference States
    private val _gridColumns = MutableStateFlow(2)
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _fontSize = MutableStateFlow("large")
    val fontSize: StateFlow<String> = _fontSize.asStateFlow()

    private val _fontColor = MutableStateFlow("dark")
    val fontColor: StateFlow<String> = _fontColor.asStateFlow()

    private val _dialScheme = MutableStateFlow("DIAL")
    val dialScheme: StateFlow<String> = _dialScheme.asStateFlow()

    private val _hideSettingsButton = MutableStateFlow(false)
    val hideSettingsButton: StateFlow<Boolean> = _hideSettingsButton.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).contactDao()
        repository = ContactRepository(dao)
        appSettings = AppSettings(application)

        // Load settings values
        _gridColumns.value = appSettings.gridColumns
        _fontSize.value = appSettings.fontSize
        _fontColor.value = appSettings.fontColor
        _dialScheme.value = appSettings.dialScheme
        _hideSettingsButton.value = appSettings.hideSettingsButton

        contacts = repository.allContacts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Check first launch permission guide & sample contact card
        viewModelScope.launch {
            if (!appSettings.isPermissionGuided) {
                _showPermissionGuide.value = true
            }

            if (repository.getContactCount() == 0) {
                // Insert initial sample contact card
                val placeholderFile = ImageUtils.createDefaultPlaceholder(application, "***")
                val sample = Contact(
                    name = "***",
                    phoneNumber = "123",
                    photoPath = placeholderFile.absolutePath,
                    orderIndex = 0
                )
                repository.insertContact(sample)
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun dismissPermissionGuide() {
        _showPermissionGuide.value = false
        appSettings.isPermissionGuided = true
    }

    // Normal mode click contact -> red call confirmation; Admin mode -> direct edit dialog
    fun onContactCardClicked(contact: Contact) {
        if (_isCallLocked.value) return

        if (_isAdminMode.value) {
            _isCreatingNew.value = false
            _editingContact.value = contact
        } else {
            _isCallLocked.value = true
            _pendingCallContact.value = contact
        }
    }

    fun dismissCallConfirmDialog() {
        _pendingCallContact.value = null
        _isCallLocked.value = false
    }

    fun onCallExecuted() {
        _pendingCallContact.value = null
        _isCallLocked.value = false
    }

    // Admin password dialog
    fun openAdminPasswordDialog() {
        _passwordError.value = false
        _showAdminPasswordDialog.value = true
    }

    fun dismissAdminPasswordDialog() {
        _showAdminPasswordDialog.value = false
        _passwordError.value = false
    }

    fun verifyAdminPassword(input: String) {
        if (input == appSettings.adminPassword) {
            _showAdminPasswordDialog.value = false
            _passwordError.value = false
            _isAdminMode.value = true
            _toastMessage.value = "已进入家属管理模式"
        } else {
            _passwordError.value = true
        }
    }

    fun exitAdminMode() {
        _isAdminMode.value = false
        _showSettingsDialog.value = false
        _toastMessage.value = "已退出管理模式，返回老人界面"
    }

    // Admin action dialog (Edit vs Delete)
    fun dismissAdminActionDialog() {
        _adminActionContact.value = null
    }

    fun proceedToEditFromAdminAction(contact: Contact) {
        _adminActionContact.value = null
        _isCreatingNew.value = false
        _editingContact.value = contact
    }

    fun proceedToDeleteFromAdminAction(contact: Contact) {
        _adminActionContact.value = null
        _deleteCandidateContact.value = contact
    }

    // Delete contact
    fun dismissDeleteConfirmDialog() {
        _deleteCandidateContact.value = null
    }

    fun confirmDeleteContact(deletePhotoFile: Boolean) {
        val candidate = _deleteCandidateContact.value ?: return
        viewModelScope.launch {
            if (deletePhotoFile && !candidate.photoPath.isNullOrBlank()) {
                ImageUtils.deleteAvatarFile(candidate.photoPath)
            }
            repository.deleteContact(candidate)
            _deleteCandidateContact.value = null
            VibrationUtils.triggerVibration(getApplication(), 150)
            _toastMessage.value = "已删除联系人: ${candidate.name}"
        }
    }

    // Add / Edit contact
    fun openAddContactDialog() {
        _isCreatingNew.value = true
        val maxOrder = contacts.value.maxOfOrNull { it.orderIndex } ?: 0
        _editingContact.value = Contact(
            name = "",
            phoneNumber = "",
            photoPath = null,
            orderIndex = maxOrder + 1
        )
    }

    fun dismissContactEditDialog() {
        _editingContact.value = null
        _isCreatingNew.value = false
    }

    fun setCropSourceUri(uri: Uri?) {
        _cropSourceUri.value = uri
    }

    fun onAvatarCroppedAndSaved(savedPath: String) {
        _cropSourceUri.value = null
        _editingContact.value = _editingContact.value?.copy(photoPath = savedPath)
    }

    fun dismissPhotoCropper() {
        _cropSourceUri.value = null
    }

    fun saveContact(name: String, phoneNumber: String, photoPath: String?) {
        val current = _editingContact.value ?: return
        if (name.isBlank() || phoneNumber.isBlank()) {
            _toastMessage.value = "请填写完整的姓名与手机号"
            return
        }

        val finalPhotoPath = if (photoPath.isNullOrBlank()) {
            ImageUtils.createDefaultPlaceholder(getApplication(), name).absolutePath
        } else {
            photoPath
        }

        val updatedContact = current.copy(
            name = name.trim(),
            phoneNumber = phoneNumber.trim(),
            photoPath = finalPhotoPath
        )

        viewModelScope.launch {
            if (_isCreatingNew.value) {
                repository.insertContact(updatedContact)
                _toastMessage.value = "添加联系人成功"
            } else {
                repository.updateContact(updatedContact)
                _toastMessage.value = "保存联系人成功"
            }
            _editingContact.value = null
            _isCreatingNew.value = false
        }
    }

    // Move contact up or down in list
    fun swapContactOrder(fromIndex: Int, toIndex: Int) {
        val currentList = contacts.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val itemA = currentList[fromIndex]
            val itemB = currentList[toIndex]
            val updatedA = itemA.copy(orderIndex = itemB.orderIndex)
            val updatedB = itemB.copy(orderIndex = itemA.orderIndex)

            viewModelScope.launch {
                repository.updateContacts(listOf(updatedA, updatedB))
            }
        }
    }

    // Settings dialog
    fun openSettingsDialog() {
        _showSettingsDialog.value = true
    }

    fun dismissSettingsDialog() {
        _showSettingsDialog.value = false
    }

    fun updateGridColumns(cols: Int) {
        _gridColumns.value = cols
        appSettings.gridColumns = cols
    }

    fun updateFontSize(size: String) {
        _fontSize.value = size
        appSettings.fontSize = size
    }

    fun updateFontColor(color: String) {
        _fontColor.value = color
        appSettings.fontColor = color
    }

    fun updateDialScheme(scheme: String) {
        _dialScheme.value = scheme
        appSettings.dialScheme = scheme
    }

    fun updateHideSettingsButton(hide: Boolean) {
        _hideSettingsButton.value = hide
        appSettings.hideSettingsButton = hide
    }

    // System contacts batch import
    fun openSystemImportDialog() {
        _showSystemImportDialog.value = true
    }

    fun dismissSystemImportDialog() {
        _showSystemImportDialog.value = false
    }

    fun importSystemContacts(selectedContacts: List<Contact>) {
        viewModelScope.launch {
            val maxOrder = contacts.value.maxOfOrNull { it.orderIndex } ?: 0
            val preparedList = selectedContacts.mapIndexed { idx, item ->
                val placeholder = ImageUtils.createDefaultPlaceholder(getApplication(), item.name)
                item.copy(
                    photoPath = placeholder.absolutePath,
                    orderIndex = maxOrder + idx + 1
                )
            }
            repository.insertContacts(preparedList)
            _showSystemImportDialog.value = false
            _toastMessage.value = "已成功导入 ${preparedList.size} 位联系人"
        }
    }

    // Backup & Restore
    fun triggerBackup() {
        viewModelScope.launch {
            val file = BackupUtils.createBackup(getApplication())
            if (file != null) {
                _toastMessage.value = "备份成功！已保存至 Download 目录: ${file.name}"
            } else {
                _toastMessage.value = "备份失败，请检查卡容量或权限"
            }
        }
    }

    fun triggerRestore(zipFile: File) {
        viewModelScope.launch {
            val success = BackupUtils.restoreFromBackup(getApplication(), zipFile)
            if (success) {
                VibrationUtils.triggerVibration(getApplication(), 200)
                _toastMessage.value = "数据还原成功！"
            } else {
                _toastMessage.value = "还原失败，请校验压缩包格式"
            }
        }
    }
}
