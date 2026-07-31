import { Linking, Alert, PermissionsAndroid, Platform } from 'react-native';
import * as IntentLauncher from 'expo-intent-launcher';
import type { DialScheme } from '../types/contact';

async function requestCallPermission(): Promise<boolean> {
  if (Platform.OS !== 'android') return false;

  try {
    const granted = await PermissionsAndroid.request(
      'android.permission.CALL_PHONE' as any,
      {
        title: '拨号权限',
        message: '需要拨号权限才能直接拨打联系人电话',
        buttonPositive: '允许',
        buttonNegative: '拒绝',
      }
    );
    return granted === 'granted';
  } catch {
    return false;
  }
}

export async function dialNumber(
  phoneNumber: string,
  scheme: DialScheme
): Promise<void> {
  const clean = phoneNumber.replace(/\s/g, '').replace(/-/g, '');

  try {
    if (scheme === 'CALL') {
      // 先请求 CALL_PHONE 权限
      const hasPermission = await requestCallPermission();
      if (hasPermission) {
        await IntentLauncher.startActivityAsync(
          'android.intent.action.CALL',
          { data: `tel:${clean}` }
        );
        return;
      }
      // 权限被拒，降级到拨号盘
    }
    // 打开拨号盘
    await Linking.openURL(`tel:${clean}`);
  } catch (error) {
    try {
      await Linking.openURL(`tel:${clean}`);
    } catch {
      Alert.alert('拨号失败', '无法发起拨号，请检查号码是否正确');
    }
  }
}
