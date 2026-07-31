import React, { createContext, useContext, useReducer, useEffect, useCallback, useRef } from 'react';
import { Alert } from 'react-native';
import type { Contact, AppSettings, FontSize, DialScheme } from '../types/contact';
import { DEFAULT_SETTINGS } from '../types/contact';
import * as storage from '../utils/storage';
const { loadSettings, saveSetting } = storage;
import { dialNumber } from '../utils/dialer';
import { exportBackup, pickBackupFile } from '../utils/backup';

// ── State ──

interface AppState {
  contacts: Contact[];
  settings: AppSettings;
  isAdminMode: boolean;
  isCallLocked: boolean;
  pendingCallContact: Contact | null;
  showPasswordDialog: boolean;
  passwordError: boolean;
  showSettingsDialog: boolean;
  showSystemImportDialog: boolean;
  showPermissionGuide: boolean;
  editingContact: Contact | null;
  isCreatingNew: boolean;
  deleteCandidateContact: Contact | null;
  toastMessage: string | null;
}

const initialState: AppState = {
  contacts: [],
  settings: DEFAULT_SETTINGS,
  isAdminMode: false,
  isCallLocked: false,
  pendingCallContact: null,
  showPasswordDialog: false,
  passwordError: false,
  showSettingsDialog: false,
  showSystemImportDialog: false,
  showPermissionGuide: false,
  editingContact: null,
  isCreatingNew: false,
  deleteCandidateContact: null,
  toastMessage: null,
};

// ── Actions ──

type Action =
  | { type: 'SET_CONTACTS'; contacts: Contact[] }
  | { type: 'SET_SETTINGS'; settings: AppSettings }
  | { type: 'SET_ADMIN_MODE'; on: boolean }
  | { type: 'SET_CALL_LOCKED'; on: boolean }
  | { type: 'SHOW_CALL_CONFIRM'; contact: Contact }
  | { type: 'DISMISS_CALL_CONFIRM' }
  | { type: 'OPEN_PASSWORD_DIALOG' }
  | { type: 'DISMISS_PASSWORD_DIALOG' }
  | { type: 'SET_PASSWORD_ERROR'; error: boolean }
  | { type: 'OPEN_SETTINGS' }
  | { type: 'DISMISS_SETTINGS' }
  | { type: 'OPEN_SYSTEM_IMPORT' }
  | { type: 'DISMISS_SYSTEM_IMPORT' }
  | { type: 'OPEN_PERMISSION_GUIDE' }
  | { type: 'DISMISS_PERMISSION_GUIDE' }
  | { type: 'OPEN_EDIT'; contact: Contact | null; isNew: boolean }
  | { type: 'DISMISS_EDIT' }
  | { type: 'OPEN_DELETE_CONFIRM'; contact: Contact }
  | { type: 'DISMISS_DELETE_CONFIRM' }
  | { type: 'SET_TOAST'; message: string | null }
  | { type: 'UPDATE_SETTING'; key: keyof AppSettings; value: any };

function reducer(state: AppState, action: Action): AppState {
  switch (action.type) {
    case 'SET_CONTACTS':
      return { ...state, contacts: action.contacts };
    case 'SET_SETTINGS':
      return { ...state, settings: action.settings };
    case 'SET_ADMIN_MODE':
      return { ...state, isAdminMode: action.on };
    case 'SET_CALL_LOCKED':
      return { ...state, isCallLocked: action.on };
    case 'SHOW_CALL_CONFIRM':
      return { ...state, isCallLocked: true, pendingCallContact: action.contact };
    case 'DISMISS_CALL_CONFIRM':
      return { ...state, isCallLocked: false, pendingCallContact: null };
    case 'OPEN_PASSWORD_DIALOG':
      return { ...state, showPasswordDialog: true, passwordError: false };
    case 'DISMISS_PASSWORD_DIALOG':
      return { ...state, showPasswordDialog: false, passwordError: false };
    case 'SET_PASSWORD_ERROR':
      return { ...state, passwordError: action.error };
    case 'OPEN_SETTINGS':
      return { ...state, showSettingsDialog: true };
    case 'DISMISS_SETTINGS':
      return { ...state, showSettingsDialog: false };
    case 'OPEN_SYSTEM_IMPORT':
      return { ...state, showSystemImportDialog: true };
    case 'DISMISS_SYSTEM_IMPORT':
      return { ...state, showSystemImportDialog: false };
    case 'OPEN_PERMISSION_GUIDE':
      return { ...state, showPermissionGuide: true };
    case 'DISMISS_PERMISSION_GUIDE':
      return { ...state, showPermissionGuide: false };
    case 'OPEN_EDIT':
      return { ...state, editingContact: action.contact, isCreatingNew: action.isNew };
    case 'DISMISS_EDIT':
      return { ...state, editingContact: null, isCreatingNew: false };
    case 'OPEN_DELETE_CONFIRM':
      return { ...state, deleteCandidateContact: action.contact };
    case 'DISMISS_DELETE_CONFIRM':
      return { ...state, deleteCandidateContact: null };
    case 'SET_TOAST':
      return { ...state, toastMessage: action.message };
    case 'UPDATE_SETTING':
      return {
        ...state,
        settings: { ...state.settings, [action.key]: action.value },
      };
    default:
      return state;
  }
}

// ── Context ──

interface AppContextType {
  state: AppState;
  dispatch: React.Dispatch<Action>;
  // Business logic actions
  refreshContacts: () => Promise<void>;
  onContactPress: (contact: Contact) => void;
  onCallConfirmed: () => void;
  onCallDismissed: () => void;
  verifyPassword: (input: string) => void;
  enterAdminMode: () => void;
  exitAdminMode: () => void;
  saveContact: (name: string, phone: string, photoPath: string | null) => Promise<void>;
  confirmDeleteContact: () => Promise<void>;
  importSystemContacts: (contacts: Contact[]) => Promise<void>;
  updateSetting: <K extends keyof AppSettings>(key: K, value: AppSettings[K]) => Promise<void>;
  doBackup: () => Promise<void>;
  doRestore: () => Promise<void>;
  addContact: () => void;
  editContact: (contact: Contact) => void;
  onMoveUp: (index: number) => void;
  onMoveDown: (index: number) => void;
  showToast: (msg: string) => void;
}

const AppContext = createContext<AppContextType | null>(null);

export function AppProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(reducer, initialState);
  const toastTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // ── Initialization ──
  useEffect(() => {
    (async () => {
      // Load settings
      const settings = await loadSettings();
      dispatch({ type: 'SET_SETTINGS', settings });

      // Load contacts
      const contacts = await storage.getAllContacts();
      dispatch({ type: 'SET_CONTACTS', contacts });

      // First launch → show permission guide
      if (!settings.isPermissionGuided) {
        dispatch({ type: 'OPEN_PERMISSION_GUIDE' });
      }

      // Insert sample contacts if empty
      if (contacts.length === 0) {
        const samples: Omit<Contact, 'id'>[] = [
          { name: '张大爷', phoneNumber: '10086', photoPath: null, orderIndex: 0 },
          { name: '李奶奶', phoneNumber: '10010', photoPath: null, orderIndex: 1 },
          { name: '王阿姨', phoneNumber: '10000', photoPath: null, orderIndex: 2 },
        ];
        await storage.insertContacts(samples);
        const refreshed = await storage.getAllContacts();
        dispatch({ type: 'SET_CONTACTS', contacts: refreshed });
      }
    })();
  }, []);

  const refreshContacts = useCallback(async () => {
    const contacts = await storage.getAllContacts();
    dispatch({ type: 'SET_CONTACTS', contacts });
  }, []);

  const showToast = useCallback(
    (msg: string) => {
      dispatch({ type: 'SET_TOAST', message: msg });
      if (toastTimerRef.current) clearTimeout(toastTimerRef.current);
      toastTimerRef.current = setTimeout(() => {
        dispatch({ type: 'SET_TOAST', message: null });
      }, 2000);
    },
    []
  );

  // ── Contact press handler ──
  const onContactPress = useCallback(
    (contact: Contact) => {
      if (state.isCallLocked) return;
      if (state.isAdminMode) {
        // Admin mode: edit
        dispatch({ type: 'OPEN_EDIT', contact, isNew: false });
      } else {
        // Normal mode: call confirmation
        dispatch({ type: 'SHOW_CALL_CONFIRM', contact });
      }
    },
    [state.isAdminMode, state.isCallLocked]
  );

  const onCallConfirmed = useCallback(() => {
    if (state.pendingCallContact) {
      dialNumber(state.pendingCallContact.phoneNumber, state.settings.dialScheme);
    }
    dispatch({ type: 'DISMISS_CALL_CONFIRM' });
  }, [state.pendingCallContact, state.settings.dialScheme]);

  const onCallDismissed = useCallback(() => {
    dispatch({ type: 'DISMISS_CALL_CONFIRM' });
  }, []);

  // ── Password / Admin mode ──
  const verifyPassword = useCallback(
    (input: string) => {
      if (input === state.settings.adminPassword) {
        dispatch({ type: 'DISMISS_PASSWORD_DIALOG' });
        dispatch({ type: 'SET_ADMIN_MODE', on: true });
        showToast('已进入管理模式');
      } else {
        dispatch({ type: 'SET_PASSWORD_ERROR', error: true });
      }
    },
    [state.settings.adminPassword, showToast]
  );

  const enterAdminMode = useCallback(() => {
    dispatch({ type: 'OPEN_PASSWORD_DIALOG' });
  }, []);

  const exitAdminMode = useCallback(() => {
    dispatch({ type: 'SET_ADMIN_MODE', on: false });
    dispatch({ type: 'DISMISS_SETTINGS' });
    showToast('已退出管理模式');
  }, [showToast]);

  // ── CRUD ──
  const saveContact = useCallback(
    async (name: string, phone: string, photoPath: string | null) => {
      if (!name.trim() || !phone.trim()) {
        showToast('请填写完整的姓名和手机号');
        return;
      }

      if (state.isCreatingNew) {
        const maxOrder = state.contacts.reduce((max, c) => Math.max(max, c.orderIndex), 0);
        await storage.insertContact({
          name: name.trim(),
          phoneNumber: phone.trim(),
          photoPath: photoPath || null,
          orderIndex: maxOrder + 1,
        });
        showToast('添加联系人成功');
      } else if (state.editingContact) {
        await storage.updateContact({
          ...state.editingContact,
          name: name.trim(),
          phoneNumber: phone.trim(),
          photoPath: photoPath || state.editingContact.photoPath,
        });
        showToast('保存联系人成功');
      }

      dispatch({ type: 'DISMISS_EDIT' });
      await refreshContacts();
    },
    [state.isCreatingNew, state.editingContact, state.contacts, refreshContacts, showToast]
  );

  const confirmDeleteContact = useCallback(async () => {
    if (!state.deleteCandidateContact) return;
    await storage.deleteContact(state.deleteCandidateContact.id);
    dispatch({ type: 'DISMISS_DELETE_CONFIRM' });
    showToast(`已删除联系人: ${state.deleteCandidateContact.name}`);
    await refreshContacts();
  }, [state.deleteCandidateContact, refreshContacts, showToast]);

  const addContact = useCallback(() => {
    const maxOrder = state.contacts.reduce((max, c) => Math.max(max, c.orderIndex), 0);
    dispatch({
      type: 'OPEN_EDIT',
      contact: {
        id: 0,
        name: '',
        phoneNumber: '',
        photoPath: null,
        orderIndex: maxOrder + 1,
      },
      isNew: true,
    });
  }, [state.contacts]);

  const editContact = useCallback((contact: Contact) => {
    dispatch({ type: 'OPEN_EDIT', contact, isNew: false });
  }, []);

  // ── Ordering ──
  const onMoveUp = useCallback(
    async (index: number) => {
      if (index <= 0) return;
      const list = [...state.contacts];
      const a = list[index - 1];
      const b = list[index];
      const updatedA = { ...a, orderIndex: b.orderIndex };
      const updatedB = { ...b, orderIndex: a.orderIndex };
      await storage.updateContacts([updatedA, updatedB]);
      await refreshContacts();
    },
    [state.contacts, refreshContacts]
  );

  const onMoveDown = useCallback(
    async (index: number) => {
      if (index >= state.contacts.length - 1) return;
      const list = [...state.contacts];
      const a = list[index];
      const b = list[index + 1];
      const updatedA = { ...a, orderIndex: b.orderIndex };
      const updatedB = { ...b, orderIndex: a.orderIndex };
      await storage.updateContacts([updatedA, updatedB]);
      await refreshContacts();
    },
    [state.contacts, refreshContacts]
  );

  // ── System contacts import ──
  const importSystemContacts = useCallback(
    async (contacts: Contact[]) => {
      const maxOrder = state.contacts.reduce((max, c) => Math.max(max, c.orderIndex), 0);
      const toInsert = contacts.map((c, idx) => ({
        name: c.name,
        phoneNumber: c.phoneNumber,
        photoPath: c.photoPath,
        orderIndex: maxOrder + idx + 1,
      }));
      await storage.insertContacts(toInsert);
      dispatch({ type: 'DISMISS_SYSTEM_IMPORT' });
      showToast(`已成功导入 ${toInsert.length} 位联系人`);
      await refreshContacts();
    },
    [state.contacts, refreshContacts, showToast]
  );

  // ── Settings ──
  const updateSetting = useCallback(
    async <K extends keyof AppSettings>(key: K, value: AppSettings[K]) => {
      dispatch({ type: 'UPDATE_SETTING', key, value });
      await saveSetting(key, value);
    },
    []
  );

  // ── Backup / Restore ──
  const doBackup = useCallback(async () => {
    dispatch({ type: 'DISMISS_SETTINGS' });
    await new Promise((r) => setTimeout(r, 300));
    try {
      if (state.contacts.length === 0) {
        showToast('没有联系人可以备份');
        return;
      }
      showToast('正在备份...');
      const path = await exportBackup(state.contacts);
      showToast(`备份完成: ${path}`);
    } catch (e: any) {
      showToast(`备份失败: ${e?.message || String(e)}`);
    }
  }, [state.contacts, showToast]);

  const doRestore = useCallback(async () => {
    dispatch({ type: 'DISMISS_SETTINGS' });
    await new Promise((r) => setTimeout(r, 300));
    // 还原会覆盖现有数据，先确认
    Alert.alert(
      '确认还原',
      '还原备份将覆盖当前所有联系人数据，确定继续吗？',
      [
        { text: '取消', style: 'cancel' },
        {
          text: '确定还原',
          style: 'destructive',
          onPress: async () => {
            try {
              const contacts = await pickBackupFile();
              if (!contacts) return;
              await storage.deleteAllContacts();
              await storage.insertContacts(contacts);
              showToast(`数据还原成功，已导入 ${contacts.length} 位联系人`);
              await refreshContacts();
            } catch (e: any) {
              showToast(`还原失败: ${e.message}`);
            }
          },
        },
      ]
    );
  }, [refreshContacts, showToast]);

  return (
    <AppContext.Provider
      value={{
        state,
        dispatch,
        refreshContacts,
        onContactPress,
        onCallConfirmed,
        onCallDismissed,
        verifyPassword,
        enterAdminMode,
        exitAdminMode,
        saveContact,
        confirmDeleteContact,
        importSystemContacts,
        updateSetting,
        doBackup,
        doRestore,
        addContact,
        editContact,
        onMoveUp,
        onMoveDown,
        showToast,
      }}
    >
      {children}
    </AppContext.Provider>
  );
}

export function useAppContext(): AppContextType {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error('useAppContext must be within AppProvider');
  return ctx;
}
