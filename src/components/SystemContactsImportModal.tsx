import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  Modal,
  TouchableOpacity,
  FlatList,
  StyleSheet,
  ActivityIndicator,
  Alert,
} from 'react-native';
import * as Contacts from 'expo-contacts/legacy';
import type { Contact } from '../types/contact';

interface Props {
  visible: boolean;
  onImport: (contacts: Contact[]) => void;
  onDismiss: () => void;
}

interface SystemContactItem {
  name: string;
  phoneNumber: string;
  selected: boolean;
}

export function SystemContactsImportModal({
  visible,
  onImport,
  onDismiss,
}: Props) {
  const [loading, setLoading] = useState(false);
  const [systemContacts, setSystemContacts] = useState<SystemContactItem[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (visible) {
      loadContacts();
    }
  }, [visible]);

  const loadContacts = async () => {
    setLoading(true);
    setError(null);
    try {
      const { status } = await Contacts.requestPermissionsAsync();
      if (status !== 'granted') {
        setError('需要通讯录读取权限');
        setLoading(false);
        return;
      }

      const { data } = await Contacts.getContactsAsync({
        fields: [Contacts.Fields.Name, Contacts.Fields.PhoneNumbers],
      });

      const items: SystemContactItem[] = [];
      for (const c of data) {
        if (c.name && c.phoneNumbers?.length) {
          for (const p of c.phoneNumbers) {
            if (p.number) {
              items.push({
                name: c.name,
                phoneNumber: p.number.replace(/\s/g, '').replace(/-/g, ''),
                selected: false,
              });
            }
          }
        }
      }

      setSystemContacts(items);
    } catch (e: any) {
      setError('读取通讯录失败');
    } finally {
      setLoading(false);
    }
  };

  const toggleSelect = (index: number) => {
    setSystemContacts((prev) =>
      prev.map((c, i) => (i === index ? { ...c, selected: !c.selected } : c))
    );
  };

  const selectedCount = systemContacts.filter((c) => c.selected).length;

  const handleImport = () => {
    const selected = systemContacts.filter((c) => c.selected);
    if (selected.length === 0) {
      Alert.alert('提示', '请至少选择一个联系人');
      return;
    }
    const contacts: Contact[] = selected.map((c, idx) => ({
      id: 0, // will be assigned by storage
      name: c.name,
      phoneNumber: c.phoneNumber,
      photoPath: null,
      orderIndex: idx,
    }));
    onImport(contacts);
  };

  return (
    <Modal visible={visible} transparent animationType="slide">
      <View style={styles.overlay}>
        <View style={styles.dialog}>
          <Text style={styles.title}>从通讯录导入</Text>

          {loading ? (
            <View style={styles.center}>
              <ActivityIndicator size="large" color="#D32F2F" />
              <Text style={styles.loadingText}>读取通讯录...</Text>
              <TouchableOpacity style={styles.cancelBtnSmall} onPress={onDismiss}>
                <Text style={styles.cancelText}>取消</Text>
              </TouchableOpacity>
            </View>
          ) : error ? (
            <View style={styles.center}>
              <Text style={styles.errorText}>{error}</Text>
              <TouchableOpacity style={styles.cancelBtnSmall} onPress={onDismiss}>
                <Text style={styles.cancelText}>关闭</Text>
              </TouchableOpacity>
            </View>
          ) : (
            <>
              <FlatList
                data={systemContacts}
                keyExtractor={(_, i) => String(i)}
                style={styles.list}
                renderItem={({ item, index }) => (
                  <TouchableOpacity
                    style={[
                      styles.contactItem,
                      item.selected && styles.contactItemSelected,
                    ]}
                    onPress={() => toggleSelect(index)}
                    activeOpacity={0.7}
                  >
                    <View style={styles.checkbox}>
                      {item.selected && <Text style={styles.checkmark}>✓</Text>}
                    </View>
                    <View style={styles.contactInfo}>
                      <Text style={styles.contactName}>{item.name}</Text>
                      <Text style={styles.contactPhone}>{item.phoneNumber}</Text>
                    </View>
                  </TouchableOpacity>
                )}
              />

              <View style={styles.footer}>
                <Text style={styles.count}>已选 {selectedCount} 位</Text>
                <View style={styles.footerBtns}>
                  <TouchableOpacity
                    style={styles.cancelBtn}
                    onPress={onDismiss}
                    activeOpacity={0.7}
                  >
                    <Text style={styles.cancelText}>取消</Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    style={styles.importBtn}
                    onPress={handleImport}
                    activeOpacity={0.7}
                  >
                    <Text style={styles.importText}>导入</Text>
                  </TouchableOpacity>
                </View>
              </View>
            </>
          )}
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 16,
  },
  dialog: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 20,
    width: '100%',
    maxWidth: 400,
    maxHeight: '85%',
  },
  title: {
    fontSize: 20,
    fontWeight: '700',
    color: '#212121',
    textAlign: 'center',
    marginBottom: 16,
  },
  center: {
    alignItems: 'center',
    paddingVertical: 40,
  },
  loadingText: {
    fontSize: 16,
    color: '#757575',
    marginTop: 12,
  },
  errorText: {
    fontSize: 16,
    color: '#D32F2F',
  },
  list: {
    maxHeight: 400,
  },
  contactItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    borderRadius: 10,
    marginBottom: 4,
    backgroundColor: '#FAFAFA',
  },
  contactItemSelected: {
    backgroundColor: '#E3F2FD',
  },
  checkbox: {
    width: 24,
    height: 24,
    borderRadius: 4,
    borderWidth: 2,
    borderColor: '#BDBDBD',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  checkmark: {
    color: '#D32F2F',
    fontSize: 16,
    fontWeight: '700',
  },
  contactInfo: {
    flex: 1,
  },
  contactName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#212121',
  },
  contactPhone: {
    fontSize: 14,
    color: '#757575',
    marginTop: 2,
  },
  footer: {
    marginTop: 16,
  },
  count: {
    fontSize: 14,
    color: '#616161',
    textAlign: 'center',
    marginBottom: 10,
  },
  footerBtns: {
    flexDirection: 'row',
    gap: 10,
  },
  cancelBtn: {
    flex: 1,
    backgroundColor: '#E0E0E0',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  cancelBtnSmall: {
    backgroundColor: '#E0E0E0',
    borderRadius: 10,
    paddingVertical: 10,
    paddingHorizontal: 24,
    alignItems: 'center',
    marginTop: 16,
  },
  cancelText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#616161',
  },
  importBtn: {
    flex: 1,
    backgroundColor: '#D32F2F',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  importText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
  },
});
