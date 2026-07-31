import React from 'react';
import {
  View,
  Text,
  Modal,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
} from 'react-native';
import type { FontSize, DialScheme } from '../types/contact';

interface Props {
  visible: boolean;
  gridColumns: number;
  fontSize: FontSize;
  dialScheme: DialScheme;
  onGridColumnsChange: (n: number) => void;
  onFontSizeChange: (s: FontSize) => void;
  onDialSchemeChange: (s: DialScheme) => void;
  onImportSystem: () => void;
  onBackup: () => void;
  onRestore: () => void;
  onDismiss: () => void;
}

const COL_OPTIONS = [1, 2, 3, 4];
const SIZE_OPTIONS: { key: FontSize; label: string }[] = [
  { key: 'small', label: '小' },
  { key: 'medium', label: '中' },
  { key: 'large', label: '大' },
  { key: 'xlarge', label: '超大' },
];

export function SettingsModal({
  visible,
  gridColumns,
  fontSize,
  dialScheme,
  onGridColumnsChange,
  onFontSizeChange,
  onDialSchemeChange,
  onImportSystem,
  onBackup,
  onRestore,
  onDismiss,
}: Props) {
  return (
    <Modal visible={visible} animationType="fade">
      <View style={styles.container}>
        {/* 顶栏 */}
        <View style={styles.topBar}>
          <Text style={styles.topTitle}>设置</Text>
          <TouchableOpacity style={styles.closeBtn} onPress={onDismiss}>
            <Text style={styles.closeBtnText}>✕</Text>
          </TouchableOpacity>
        </View>

        <ScrollView style={styles.content}>
          {/* Grid columns */}
          <Text style={styles.sectionTitle}>每行列数</Text>
          <View style={styles.optionRow}>
            {COL_OPTIONS.map((n) => (
              <TouchableOpacity
                key={n}
                style={[styles.option, gridColumns === n && styles.optionActive]}
                onPress={() => onGridColumnsChange(n)}
              >
                <Text
                  style={[
                    styles.optionText,
                    gridColumns === n && styles.optionTextActive,
                  ]}
                >
                  {n}列
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          {/* Font size */}
          <Text style={styles.sectionTitle}>字号</Text>
          <View style={styles.optionRow}>
            {SIZE_OPTIONS.map((s) => (
              <TouchableOpacity
                key={s.key}
                style={[styles.option, fontSize === s.key && styles.optionActive]}
                onPress={() => onFontSizeChange(s.key)}
              >
                <Text
                  style={[
                    styles.optionText,
                    fontSize === s.key && styles.optionTextActive,
                  ]}
                >
                  {s.label}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          {/* Dial scheme */}
          <Text style={styles.sectionTitle}>拨号方案</Text>
          <View style={styles.optionRow}>
            <TouchableOpacity
              style={[styles.option, dialScheme === 'DIAL' && styles.optionActive]}
              onPress={() => onDialSchemeChange('DIAL')}
            >
              <Text
                style={[
                  styles.optionText,
                  dialScheme === 'DIAL' && styles.optionTextActive,
                ]}
              >
                拨号盘
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.option, dialScheme === 'CALL' && styles.optionActive]}
              onPress={() => onDialSchemeChange('CALL')}
            >
              <Text
                style={[
                  styles.optionText,
                  dialScheme === 'CALL' && styles.optionTextActive,
                ]}
              >
                直接拨打
              </Text>
            </TouchableOpacity>
          </View>

          {/* Action buttons */}
          <View style={styles.actions}>
            <TouchableOpacity
              style={styles.actionBtn}
              onPress={onImportSystem}
              activeOpacity={0.7}
            >
              <Text style={styles.actionText}>从通讯录导入</Text>
            </TouchableOpacity>

{/* 备份还原功能不稳定，暂时隐藏
            <View style={styles.actionRow}>
              <TouchableOpacity
                style={[styles.actionBtn, styles.actionHalf]}
                onPress={onBackup}
                activeOpacity={0.7}
              >
                <Text style={styles.actionText}>备份</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.actionBtn, styles.actionHalf]}
                onPress={onRestore}
                activeOpacity={0.7}
              >
                <Text style={styles.actionText}>还原</Text>
              </TouchableOpacity>
            </View>
            */}
          </View>
        </ScrollView>
      </View>
    </Modal>
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
    backgroundColor: '#D32F2F',
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  topTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  closeBtn: {
    width: 44,
    height: 44,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 22,
  },
  closeBtnText: {
    fontSize: 22,
    color: '#FFFFFF',
    fontWeight: '600',
  },
  content: {
    flex: 1,
    padding: 20,
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: '#424242',
    marginTop: 16,
    marginBottom: 8,
  },
  optionRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
  },
  option: {
    backgroundColor: '#FFFFFF',
    borderRadius: 10,
    paddingHorizontal: 20,
    paddingVertical: 12,
    elevation: 1,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  optionActive: {
    backgroundColor: '#D32F2F',
  },
  optionText: {
    fontSize: 15,
    color: '#424242',
    fontWeight: '500',
  },
  optionTextActive: {
    color: '#FFFFFF',
  },
  actions: {
    marginTop: 28,
    gap: 12,
  },
  actionBtn: {
    backgroundColor: '#D32F2F',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  actionHalf: {
    flex: 1,
  },
  actionRow: {
    flexDirection: 'row',
    gap: 12,
  },
  actionText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
  },
});
