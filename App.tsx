import React, { useCallback, useRef } from 'react';
import {
  View,
  Text,
  StatusBar,
  StyleSheet,
  Dimensions,
  TouchableOpacity,
  FlatList,
  Platform,
} from 'react-native';
import { AppProvider, useAppContext } from './src/store/AppContext';
import { ContactCard } from './src/components/ContactCard';
import { CallConfirmModal } from './src/components/CallConfirmModal';
import { ContactEditModal } from './src/components/ContactEditModal';
import { DeleteConfirmModal } from './src/components/DeleteConfirmModal';
import { SettingsModal } from './src/components/SettingsModal';
import { SystemContactsImportModal } from './src/components/SystemContactsImportModal';
import { PermissionGuideModal } from './src/components/PermissionGuideModal';

const SCREEN_WIDTH = Dimensions.get('window').width;

function HomeScreen() {
  const ctx = useAppContext();
  const { state } = ctx;
  const {
    contacts,
    settings,
    isAdminMode,
    isCallLocked,
    pendingCallContact,
    showSettingsDialog,
    showSystemImportDialog,
    showPermissionGuide,
    editingContact,
    isCreatingNew,
    deleteCandidateContact,
    toastMessage,
  } = state;

  const numColumns = settings.gridColumns;

  // 20-tap counter for hidden admin entry
  const tapCountRef = useRef(0);
  const firstTapRef = useRef(0);
  const tapTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const handleTitlePress = () => {
    const now = Date.now();
    // Reset if > 60 seconds since first tap
    if (now - firstTapRef.current > 60000) {
      tapCountRef.current = 0;
      firstTapRef.current = now;
    }
    if (tapCountRef.current === 0) {
      firstTapRef.current = now;
    }
    tapCountRef.current++;
    // Clear old timer
    if (tapTimerRef.current) clearTimeout(tapTimerRef.current);
    // Auto-reset after 60s of inactivity
    tapTimerRef.current = setTimeout(() => {
      tapCountRef.current = 0;
    }, 60000);

    if (tapCountRef.current >= 20) {
      tapCountRef.current = 0;
      if (!isAdminMode) {
        ctx.dispatch({ type: 'SET_ADMIN_MODE', on: true });
      }
    }
  };

  const renderContact = useCallback(
    ({ item, index }: { item: any; index: number }) => (
      <ContactCard
        contact={item}
        gridColumns={numColumns}
        fontSize={settings.fontSize}
        isAdminMode={isAdminMode}
        canMoveUp={index > 0}
        canMoveDown={index < contacts.length - 1}
        onPress={() => ctx.onContactPress(item)}
        onMoveUp={() => ctx.onMoveUp(index)}
        onMoveDown={() => ctx.onMoveDown(index)}
      />
    ),
    [numColumns, settings.fontSize, isAdminMode, contacts.length, ctx.onContactPress, ctx.onMoveUp, ctx.onMoveDown]
  );

  return (
    <View style={[
      styles.container,
      { paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) : 0 }
    ]}>
      <StatusBar
        backgroundColor="#D32F2F"
        barStyle="light-content"
      />

      {/* ── Top Bar ── */}
      <View
        style={[
          styles.topBar,
          { backgroundColor: '#D32F2F' },
        ]}
      >
        <TouchableOpacity
          onPress={handleTitlePress}
          activeOpacity={1}
          style={styles.titleArea}
        >
          <Text style={styles.topTitle}>
            {isAdminMode ? '亲情联系人(管理模式)' : '亲情联系人'}
          </Text>
        </TouchableOpacity>

        {isAdminMode && (
          <View style={styles.topActions}>
            <TouchableOpacity
              style={styles.topBtn}
              onPress={() => ctx.dispatch({ type: 'OPEN_SETTINGS' })}
            >
              <Text style={styles.topBtnText}>设置</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.topBtn} onPress={ctx.exitAdminMode}>
              <Text style={styles.topBtnText}>退出</Text>
            </TouchableOpacity>
          </View>
        )}
      </View>

      {/* ── Admin action bar ── */}
      {isAdminMode && (
        <View style={styles.adminBar}>
          <TouchableOpacity
            style={styles.adminActionBtn}
            onPress={ctx.addContact}
            activeOpacity={0.7}
          >
            <Text style={styles.adminActionText}>＋ 添加联系人</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.adminActionBtn, styles.adminActionSecondary]}
            onPress={() => ctx.dispatch({ type: 'OPEN_SYSTEM_IMPORT' })}
            activeOpacity={0.7}
          >
            <Text style={styles.adminActionSecondaryText}>从通讯录导入</Text>
          </TouchableOpacity>
        </View>
      )}

      {/* ── Contact Grid ── */}
      {contacts.length === 0 ? (
        <View style={styles.emptyState}>
          <Text style={styles.emptyIcon}>📇</Text>
          <Text style={styles.emptyTitle}>暂无联系人卡片</Text>
          {isAdminMode && (
            <TouchableOpacity
              style={styles.emptyAddBtn}
              onPress={ctx.addContact}
              activeOpacity={0.7}
            >
              <Text style={styles.emptyAddBtnText}>添加联系人</Text>
            </TouchableOpacity>
          )}
        </View>
      ) : (
        <FlatList
          data={contacts}
          renderItem={renderContact}
          keyExtractor={(item) => String(item.id)}
          numColumns={numColumns}
          key={numColumns}
          contentContainerStyle={styles.grid}
          columnWrapperStyle={numColumns > 1 ? { gap: 8 } : undefined}
          showsVerticalScrollIndicator={false}
        />
      )}

      {/* ── Toast ── */}
      {toastMessage && (
        <View style={styles.toast}>
          <Text style={styles.toastText}>{toastMessage}</Text>
        </View>
      )}

      {/* ── Modals ── */}
      <CallConfirmModal
        visible={!!pendingCallContact && !isAdminMode}
        contact={pendingCallContact!}
        onConfirm={ctx.onCallConfirmed}
        onDismiss={ctx.onCallDismissed}
      />

      <ContactEditModal
        visible={!!editingContact}
        contact={editingContact!}
        isCreatingNew={isCreatingNew}
        onSave={ctx.saveContact}
        onDelete={() => {
          if (editingContact) {
            ctx.dispatch({ type: 'DISMISS_EDIT' });
            ctx.dispatch({ type: 'OPEN_DELETE_CONFIRM', contact: editingContact });
          }
        }}
        onDismiss={() => ctx.dispatch({ type: 'DISMISS_EDIT' })}
      />

      <DeleteConfirmModal
        visible={!!deleteCandidateContact}
        contact={deleteCandidateContact!}
        onConfirm={ctx.confirmDeleteContact}
        onDismiss={() => ctx.dispatch({ type: 'DISMISS_DELETE_CONFIRM' })}
      />

      <SettingsModal
        visible={showSettingsDialog}
        gridColumns={settings.gridColumns}
        fontSize={settings.fontSize}
        dialScheme={settings.dialScheme}
        onGridColumnsChange={(n) => ctx.updateSetting('gridColumns', n)}
        onFontSizeChange={(s) => ctx.updateSetting('fontSize', s)}
        onDialSchemeChange={(s) => ctx.updateSetting('dialScheme', s)}
        onImportSystem={() => {
          ctx.dispatch({ type: 'DISMISS_SETTINGS' });
          setTimeout(() => ctx.dispatch({ type: 'OPEN_SYSTEM_IMPORT' }), 300);
        }}
        onBackup={ctx.doBackup}
        onRestore={ctx.doRestore}
        onDismiss={() => ctx.dispatch({ type: 'DISMISS_SETTINGS' })}
      />

      <SystemContactsImportModal
        visible={showSystemImportDialog}
        onImport={ctx.importSystemContacts}
        onDismiss={() => ctx.dispatch({ type: 'DISMISS_SYSTEM_IMPORT' })}
      />

      <PermissionGuideModal
        visible={showPermissionGuide}
        onRequestPermissions={() => {
          ctx.dispatch({ type: 'DISMISS_PERMISSION_GUIDE' });
          ctx.updateSetting('isPermissionGuided', true);
        }}
      />
    </View>
  );
}

export default function App() {
  return (
    <AppProvider>
      <HomeScreen />
    </AppProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 14,
    minHeight: 56,
  },
  titleArea: {
    flex: 1,
  },
  topTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  topActions: {
    flexDirection: 'row',
    gap: 8,
  },
  topBtn: {
    paddingHorizontal: 14,
    paddingVertical: 10,
    minWidth: 44,
    minHeight: 44,
    justifyContent: 'center',
    alignItems: 'center',
  },
  topBtnText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  adminBar: {
    flexDirection: 'row',
    padding: 12,
    gap: 10,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E0E0E0',
  },
  adminActionBtn: {
    flex: 1,
    backgroundColor: '#D32F2F',
    borderRadius: 10,
    paddingVertical: 12,
    alignItems: 'center',
  },
  adminActionText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  adminActionSecondary: {
    backgroundColor: '#F5F5F5',
    borderWidth: 1,
    borderColor: '#D32F2F',
  },
  adminActionSecondaryText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#D32F2F',
  },
  grid: {
    padding: 12,
    gap: 12,
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 32,
  },
  emptyIcon: {
    fontSize: 64,
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#9E9E9E',
  },
  emptyAddBtn: {
    marginTop: 16,
    backgroundColor: '#D32F2F',
    borderRadius: 12,
    paddingVertical: 14,
    paddingHorizontal: 32,
  },
  emptyAddBtnText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  toast: {
    position: 'absolute',
    bottom: 40,
    left: 32,
    right: 32,
    backgroundColor: '#323232',
    borderRadius: 10,
    paddingVertical: 12,
    paddingHorizontal: 20,
    alignItems: 'center',
    elevation: 6,
  },
  toastText: {
    fontSize: 15,
    color: '#FFFFFF',
  },
});
