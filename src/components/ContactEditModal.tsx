import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  Modal,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  Image,
  Alert,
} from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import { Paths, File, Directory } from 'expo-file-system';
import type { Contact } from '../types/contact';

interface Props {
  visible: boolean;
  contact: Contact;
  isCreatingNew: boolean;
  onSave: (name: string, phone: string, photoPath: string | null) => void;
  onDelete: () => void;
  onDismiss: () => void;
}

export function ContactEditModal({
  visible,
  contact,
  isCreatingNew,
  onSave,
  onDelete,
  onDismiss,
}: Props) {
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [photoUri, setPhotoUri] = useState<string | null>(null);

  useEffect(() => {
    if (visible) {
      setName(contact.name || '');
      setPhone(contact.phoneNumber || '');
      setPhotoUri(contact.photoPath || null);
    }
  }, [visible, contact]);

  const handlePickPhoto = async () => {
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: ['image/*'],
        copyToCacheDirectory: true,
      });

      if (!result.canceled && result.assets?.[0]) {
        const srcUri = result.assets[0].uri;
        // 把图片拷到 app 自己的文档目录，保证后续备份、显示都有权限读取
        const srcFile = new File(srcUri);
        const destDir = new Directory(Paths.document, 'photos');
        destDir.create({ idempotent: true });
        const ext = srcUri.split('.').pop()?.split('?')[0] || 'jpg';
        const destName = `avatar_${Date.now()}.${ext}`;
        const destFile = new File(destDir, destName);
        await srcFile.copy(destFile);
        setPhotoUri(destFile.uri);
      }
    } catch {
      Alert.alert('取消', '未选择头像');
    }
  };

  const handleSave = () => {
    onSave(name.trim(), phone.trim(), photoUri);
  };

  return (
    <Modal visible={visible} transparent animationType="slide">
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        style={styles.overlay}
      >
        <View style={styles.dialog}>
          <Text style={styles.title}>
            {isCreatingNew ? '添加联系人' : '编辑联系人'}
          </Text>

          {/* Avatar */}
          <TouchableOpacity
            style={styles.avatarContainer}
            onPress={handlePickPhoto}
            activeOpacity={0.7}
          >
            {photoUri ? (
              <Image source={{ uri: photoUri }} style={styles.avatar} />
            ) : (
              <View style={styles.avatarPlaceholder}>
                <Text style={styles.avatarText}>
                  {name ? name.charAt(0).toUpperCase() : '?'}
                </Text>
              </View>
            )}
            <Text style={styles.photoHint}>点击设置头像</Text>
          </TouchableOpacity>

          <ScrollView style={styles.form} keyboardShouldPersistTaps="handled">
            <Text style={styles.label}>姓名</Text>
            <TextInput
              style={styles.input}
              value={name}
              onChangeText={setName}
              placeholder="联系人姓名"
              maxLength={50}
              autoFocus
            />

            <Text style={styles.label}>手机号</Text>
            <TextInput
              style={styles.input}
              value={phone}
              onChangeText={setPhone}
              placeholder="手机号码"
              keyboardType="phone-pad"
              maxLength={20}
            />
          </ScrollView>

          <View style={styles.buttonRow}>
            {!isCreatingNew && (
              <TouchableOpacity
                style={styles.deleteBtn}
                onPress={onDelete}
                activeOpacity={0.7}
              >
                <Text style={styles.deleteText}>删除</Text>
              </TouchableOpacity>
            )}

            <TouchableOpacity
              style={styles.cancelBtn}
              onPress={onDismiss}
              activeOpacity={0.7}
            >
              <Text style={styles.cancelText}>取消</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.saveBtn}
              onPress={handleSave}
              activeOpacity={0.7}
            >
              <Text style={styles.saveText}>保存</Text>
            </TouchableOpacity>
          </View>
        </View>
      </KeyboardAvoidingView>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  dialog: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 24,
    width: '100%',
    maxWidth: 360,
  },
  title: {
    fontSize: 20,
    fontWeight: '700',
    color: '#212121',
    textAlign: 'center',
    marginBottom: 16,
  },
  avatarContainer: {
    alignItems: 'center',
    marginBottom: 16,
  },
  avatar: {
    width: 72,
    height: 72,
    borderRadius: 36,
  },
  avatarPlaceholder: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: '#D32F2F',
    justifyContent: 'center',
    alignItems: 'center',
  },
  avatarText: {
    color: '#FFFFFF',
    fontSize: 32,
    fontWeight: '600',
  },
  photoHint: {
    fontSize: 12,
    color: '#D32F2F',
    marginTop: 6,
  },
  form: {
    maxHeight: 200,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#616161',
    marginBottom: 6,
    marginTop: 8,
  },
  input: {
    borderWidth: 1,
    borderColor: '#E0E0E0',
    borderRadius: 10,
    padding: 14,
    fontSize: 18,
  },
  buttonRow: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 20,
  },
  cancelBtn: {
    flex: 1,
    backgroundColor: '#E0E0E0',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  cancelText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#616161',
  },
  saveBtn: {
    flex: 1,
    backgroundColor: '#D32F2F',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  saveText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  deleteBtn: {
    backgroundColor: '#D32F2F',
    borderRadius: 12,
    paddingVertical: 14,
    paddingHorizontal: 16,
    alignItems: 'center',
  },
  deleteText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
  },
});
