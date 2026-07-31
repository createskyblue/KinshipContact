export interface Contact {
  id: number;
  name: string;
  phoneNumber: string;
  photoPath: string | null;
  orderIndex: number;
}

export type FontSize = 'small' | 'medium' | 'large' | 'xlarge';

export type FontColor = 'dark' | 'light' | 'red' | 'blue' | 'green';

export type DialScheme = 'DIAL' | 'CALL';

export interface AppSettings {
  gridColumns: number;
  fontSize: FontSize;
  fontColor: FontColor; // 保留兼容旧数据，不再在设置中展示
  dialScheme: DialScheme;
  hideSettingsButton: boolean; // 始终为 true，不再展示按钮
  adminPassword: string;
  isPermissionGuided: boolean;
}

export const DEFAULT_SETTINGS: AppSettings = {
  gridColumns: 2,
  fontSize: 'large',
  fontColor: 'dark',
  dialScheme: 'DIAL',
  hideSettingsButton: true,
  adminPassword: '123',
  isPermissionGuided: false,
};

// 大号字体，适合中文 2-4 字一行显示
export const FONT_SIZE_MAP: Record<FontSize, number> = {
  small: 18,
  medium: 24,
  large: 30,
  xlarge: 38,
};

export const FONT_COLOR_MAP: Record<FontColor, string> = {
  dark: '#212121',
  light: '#FFFFFF',
  red: '#D32F2F',
  blue: '#D32F2F',
  green: '#2E7D32',
};
