import React from 'react';
import { View, Text, Modal, TouchableOpacity, StyleSheet } from 'react-native';

interface Props {
  visible: boolean;
  onRequestPermissions: () => void;
}

export function PermissionGuideModal({
  visible,
  onRequestPermissions,
}: Props) {
  return (
    <Modal visible={visible} transparent animationType="fade">
      <View style={styles.overlay}>
        <View style={styles.dialog}>
          <Text style={styles.title}>权限说明</Text>

          <View style={styles.permissionList}>
            <View style={styles.permissionItem}>
              <Text style={styles.icon}>📞</Text>
              <View style={styles.permissionText}>
                <Text style={styles.permissionTitle}>拨打电话</Text>
                <Text style={styles.permissionDesc}>
                  用于直接拨打联系人电话
                </Text>
              </View>
            </View>

            <View style={styles.permissionItem}>
              <Text style={styles.icon}>📇</Text>
              <View style={styles.permissionText}>
                <Text style={styles.permissionTitle}>读取通讯录</Text>
                <Text style={styles.permissionDesc}>
                  用于从系统通讯录导入联系人
                </Text>
              </View>
            </View>
          </View>

          <TouchableOpacity
            style={styles.confirmBtn}
            onPress={onRequestPermissions}
            activeOpacity={0.7}
          >
            <Text style={styles.confirmText}>开始授权</Text>
          </TouchableOpacity>
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
    padding: 32,
  },
  dialog: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 28,
    width: '100%',
    maxWidth: 340,
  },
  title: {
    fontSize: 20,
    fontWeight: '700',
    color: '#212121',
    textAlign: 'center',
    marginBottom: 20,
  },
  permissionList: {
    gap: 16,
    marginBottom: 24,
  },
  permissionItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  icon: {
    fontSize: 28,
  },
  permissionText: {
    flex: 1,
  },
  permissionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#212121',
  },
  permissionDesc: {
    fontSize: 14,
    color: '#757575',
    marginTop: 2,
  },
  confirmBtn: {
    backgroundColor: '#D32F2F',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  confirmText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#FFFFFF',
  },
});
