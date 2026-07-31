import React from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Dimensions,
  Image,
} from 'react-native';
import type { Contact, FontSize } from '../types/contact';
import { FONT_SIZE_MAP } from '../types/contact';

const SCREEN_WIDTH = Dimensions.get('window').width;

interface Props {
  contact: Contact;
  gridColumns: number;
  fontSize: FontSize;
  isAdminMode: boolean;
  canMoveUp: boolean;
  canMoveDown: boolean;
  onPress: () => void;
  onMoveUp: () => void;
  onMoveDown: () => void;
}

export function ContactCard({
  contact,
  gridColumns,
  fontSize,
  isAdminMode,
  canMoveUp,
  canMoveDown,
  onPress,
  onMoveUp,
  onMoveDown,
}: Props) {
  const cardWidth = (SCREEN_WIDTH - 24 - (gridColumns - 1) * 8) / gridColumns;
  const photoHeight = cardWidth * 1.05;
  const textSize = FONT_SIZE_MAP[fontSize];

  return (
    <View style={[styles.wrapper, { width: cardWidth }]}>
      {isAdminMode && (
        <View style={styles.orderRow}>
          <TouchableOpacity
            onPress={onMoveUp}
            disabled={!canMoveUp}
            style={[styles.orderBtn, !canMoveUp && styles.orderBtnDisabled]}
          >
            <Text style={styles.orderBtnText}>▲</Text>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={onMoveDown}
            disabled={!canMoveDown}
            style={[styles.orderBtn, !canMoveDown && styles.orderBtnDisabled]}
          >
            <Text style={styles.orderBtnText}>▼</Text>
          </TouchableOpacity>
        </View>
      )}

      <TouchableOpacity
        style={styles.card}
        onPress={onPress}
        activeOpacity={0.7}
      >
        {/* 上半部分：头像 */}
        {contact.photoPath ? (
          <Image
            source={{ uri: contact.photoPath }}
            style={[styles.photo, { width: cardWidth, height: photoHeight }]}
            resizeMode="cover"
          />
        ) : (
          <View style={[styles.photoPlaceholder, { width: cardWidth, height: photoHeight }]}>
            <Text style={styles.photoPlaceholderText}>
              {contact.name ? contact.name.charAt(0).toUpperCase() : '?'}
            </Text>
          </View>
        )}

        {/* 下半部分：姓名 + 电话 */}
        <View style={styles.infoArea}>
          <Text
            style={[styles.name, { fontSize: textSize }]}
            numberOfLines={1}
            ellipsizeMode="tail"
          >
            {contact.name || '未命名'}
          </Text>
          <Text style={styles.phone} numberOfLines={1} ellipsizeMode="tail">
            {contact.phoneNumber}
          </Text>
        </View>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    marginBottom: 12,
  },
  orderRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 8,
    marginBottom: 4,
  },
  orderBtn: {
    backgroundColor: '#D32F2F',
    paddingHorizontal: 10,
    paddingVertical: 2,
    borderRadius: 4,
  },
  orderBtnDisabled: {
    backgroundColor: '#BDBDBD',
  },
  orderBtnText: {
    color: '#FFFFFF',
    fontSize: 12,
  },
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    overflow: 'hidden',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.15,
    shadowRadius: 3,
  },
  photo: {
    // width/height set dynamically
  },
  photoPlaceholder: {
    backgroundColor: '#D32F2F',
    justifyContent: 'center',
    alignItems: 'center',
  },
  photoPlaceholderText: {
    color: '#FFFFFF',
    fontSize: 56,
    fontWeight: '700',
  },
  infoArea: {
    paddingVertical: 14,
    paddingHorizontal: 8,
    alignItems: 'center',
  },
  name: {
    fontWeight: '700',
    color: '#212121',
    textAlign: 'center',
    marginBottom: 4,
  },
  phone: {
    fontSize: 14,
    color: '#757575',
    textAlign: 'center',
  },
});
