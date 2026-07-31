import AsyncStorage from '@react-native-async-storage/async-storage';
import type { Contact, AppSettings } from '../types/contact';
import { DEFAULT_SETTINGS } from '../types/contact';

// ── Keys ──

const KEY_CONTACTS = 'contacts_data';
const KEY_SETTINGS = 'app_settings';

// ── Contacts CRUD ──

export async function getAllContacts(): Promise<Contact[]> {
  const raw = await AsyncStorage.getItem(KEY_CONTACTS);
  if (!raw) return [];
  try {
    const contacts: Contact[] = JSON.parse(raw);
    return contacts.sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0));
  } catch {
    return [];
  }
}

async function saveContacts(contacts: Contact[]): Promise<void> {
  await AsyncStorage.setItem(KEY_CONTACTS, JSON.stringify(contacts));
}

export async function insertContact(contact: Omit<Contact, 'id'>): Promise<Contact> {
  const contacts = await getAllContacts();
  const maxId = contacts.reduce((max, c) => Math.max(max, c.id), 0);
  const newContact: Contact = { ...contact, id: maxId + 1 };
  contacts.push(newContact);
  await saveContacts(contacts);
  return newContact;
}

export async function insertContacts(list: Omit<Contact, 'id'>[]): Promise<void> {
  const contacts = await getAllContacts();
  let maxId = contacts.reduce((max, c) => Math.max(max, c.id), 0);
  for (const c of list) {
    maxId++;
    contacts.push({ ...c, id: maxId });
  }
  await saveContacts(contacts);
}

export async function updateContact(updated: Contact): Promise<void> {
  const contacts = await getAllContacts();
  const idx = contacts.findIndex((c) => c.id === updated.id);
  if (idx >= 0) {
    contacts[idx] = updated;
    await saveContacts(contacts);
  }
}

export async function updateContacts(updates: Contact[]): Promise<void> {
  const contacts = await getAllContacts();
  for (const u of updates) {
    const idx = contacts.findIndex((c) => c.id === u.id);
    if (idx >= 0) contacts[idx] = u;
  }
  await saveContacts(contacts);
}

export async function deleteContact(id: number): Promise<void> {
  const contacts = await getAllContacts();
  await saveContacts(contacts.filter((c) => c.id !== id));
}

export async function deleteAllContacts(): Promise<void> {
  await AsyncStorage.removeItem(KEY_CONTACTS);
}

export async function getContactCount(): Promise<number> {
  const contacts = await getAllContacts();
  return contacts.length;
}

// ── Settings ──

export async function loadSettings(): Promise<AppSettings> {
  const raw = await AsyncStorage.getItem(KEY_SETTINGS);
  if (!raw) return { ...DEFAULT_SETTINGS };
  try {
    return { ...DEFAULT_SETTINGS, ...JSON.parse(raw) };
  } catch {
    return { ...DEFAULT_SETTINGS };
  }
}

export async function saveSetting<K extends keyof AppSettings>(
  key: K,
  value: AppSettings[K]
): Promise<void> {
  const settings = await loadSettings();
  settings[key] = value;
  await AsyncStorage.setItem(KEY_SETTINGS, JSON.stringify(settings));
}
