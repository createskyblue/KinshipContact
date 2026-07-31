// 用内存模拟 AsyncStorage
const mockStore = new Map<string, string>();

jest.mock('@react-native-async-storage/async-storage', () => ({
  getItem: jest.fn((key: string) => Promise.resolve(mockStore.get(key) ?? null)),
  setItem: jest.fn((key: string, value: string) => {
    mockStore.set(key, value);
    return Promise.resolve();
  }),
  removeItem: jest.fn((key: string) => {
    mockStore.delete(key);
    return Promise.resolve();
  }),
}));

import * as storage from '../utils/storage';

beforeEach(() => {
  mockStore.clear();
});

describe('storage', () => {
  it('getAllContacts 空数据库返回 []', async () => {
    const contacts = await storage.getAllContacts();
    expect(contacts).toEqual([]);
  });

  it('insertContact 插入并返回带 id 的联系人', async () => {
    const c = await storage.insertContact({
      name: '张大爷',
      phoneNumber: '13800138000',
      photoPath: null,
      orderIndex: 0,
    });
    expect(c.id).toBe(1);
    expect(c.name).toBe('张大爷');
    expect(c.phoneNumber).toBe('13800138000');
  });

  it('getAllContacts 返回排序后的联系人', async () => {
    await storage.insertContact({ name: 'B', phoneNumber: '2', photoPath: null, orderIndex: 2 });
    await storage.insertContact({ name: 'A', phoneNumber: '1', photoPath: null, orderIndex: 1 });

    const contacts = await storage.getAllContacts();
    expect(contacts).toHaveLength(2);
    expect(contacts[0].orderIndex).toBe(1); // 按 orderIndex 升序
    expect(contacts[0].name).toBe('A');
    expect(contacts[1].orderIndex).toBe(2);
    expect(contacts[1].name).toBe('B');
  });

  it('updateContact 更新联系人字段', async () => {
    const c = await storage.insertContact({ name: '老王', phoneNumber: '111', photoPath: null, orderIndex: 0 });
    await storage.updateContact({ ...c, name: '老王改', phoneNumber: '222' });

    const contacts = await storage.getAllContacts();
    expect(contacts[0].name).toBe('老王改');
    expect(contacts[0].phoneNumber).toBe('222');
  });

  it('updateContacts 批量更新', async () => {
    const a = await storage.insertContact({ name: 'A', phoneNumber: '1', photoPath: null, orderIndex: 1 });
    const b = await storage.insertContact({ name: 'B', phoneNumber: '2', photoPath: null, orderIndex: 2 });

    // 交换 orderIndex
    await storage.updateContacts([
      { ...a, orderIndex: 2 },
      { ...b, orderIndex: 1 },
    ]);

    const contacts = await storage.getAllContacts();
    expect(contacts[0].name).toBe('B'); // orderIndex 1 → B 在前面
    expect(contacts[1].name).toBe('A'); // orderIndex 2 → A 在后面
  });

  it('deleteContact 删除联系人', async () => {
    const c = await storage.insertContact({ name: '老王', phoneNumber: '111', photoPath: null, orderIndex: 0 });
    expect(await storage.getContactCount()).toBe(1);

    await storage.deleteContact(c.id);
    expect(await storage.getContactCount()).toBe(0);
  });

  it('deleteAllContacts 清空所有联系人', async () => {
    await storage.insertContact({ name: 'A', phoneNumber: '1', photoPath: null, orderIndex: 0 });
    await storage.insertContact({ name: 'B', phoneNumber: '2', photoPath: null, orderIndex: 1 });

    await storage.deleteAllContacts();
    expect(await storage.getContactCount()).toBe(0);
  });

  it('insertContacts 批量插入', async () => {
    await storage.insertContacts([
      { name: 'A', phoneNumber: '1', photoPath: null, orderIndex: 1 },
      { name: 'B', phoneNumber: '2', photoPath: null, orderIndex: 2 },
    ]);
    expect(await storage.getContactCount()).toBe(2);
  });
});
