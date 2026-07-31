import { DEFAULT_SETTINGS, FONT_SIZE_MAP } from '../types/contact';

describe('DEFAULT_SETTINGS', () => {
  it('默认每行列数为 2', () => {
    expect(DEFAULT_SETTINGS.gridColumns).toBe(2);
  });

  it('默认字号为 large', () => {
    expect(DEFAULT_SETTINGS.fontSize).toBe('large');
  });

  it('默认隐藏设置按钮为 true', () => {
    expect(DEFAULT_SETTINGS.hideSettingsButton).toBe(true);
  });

  it('默认密码为 123', () => {
    expect(DEFAULT_SETTINGS.adminPassword).toBe('123');
  });

  it('默认拨号方案为拨号盘', () => {
    expect(DEFAULT_SETTINGS.dialScheme).toBe('DIAL');
  });
});

describe('FONT_SIZE_MAP', () => {
  it('字号从 small 到 xlarge 递增', () => {
    expect(FONT_SIZE_MAP.small).toBeLessThan(FONT_SIZE_MAP.medium);
    expect(FONT_SIZE_MAP.medium).toBeLessThan(FONT_SIZE_MAP.large);
    expect(FONT_SIZE_MAP.large).toBeLessThan(FONT_SIZE_MAP.xlarge);
  });

  it('large 字号 >= 28，老人看得清', () => {
    expect(FONT_SIZE_MAP.large).toBeGreaterThanOrEqual(28);
  });
});
