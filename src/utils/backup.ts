import { Paths, File, Directory } from 'expo-file-system';
import { shareAsync, isAvailableAsync } from 'expo-sharing';
import JSZip from 'jszip';
import type { Contact } from '../types/contact';

const BACKUP_NAME = 'FamilyContacts_Backup';

function safeName(name: string): string {
  return name.replace(/[^a-zA-Z0-9一-鿿]/g, '_') || 'unknown';
}

async function photoToBase64(uri: string): Promise<string> {
  const f = new File(uri);
  return await f.base64();
}

export async function exportBackup(contacts: Contact[]): Promise<string> {
  const zip = new JSZip();
  const hasPhotos = contacts.some((c) => c.photoPath);

  const contactsData = contacts.map((c, idx) => {
    let photoFile: string | null = null;
    if (c.photoPath) {
      const ext = c.photoPath.split('.').pop()?.split('?')[0] || 'jpg';
      photoFile = `photo_${idx}_${safeName(c.name)}.${ext}`;
    }
    return {
      name: c.name,
      phoneNumber: c.phoneNumber,
      photo: photoFile,
      orderIndex: c.orderIndex,
    };
  });

  zip.file(
    'contacts.json',
    JSON.stringify({ version: 1, exportedAt: new Date().toISOString(), contacts: contactsData }, null, 2)
  );

  if (hasPhotos) {
    const photosFolder = zip.folder('photos');
    for (let i = 0; i < contacts.length; i++) {
      const c = contacts[i];
      if (!c.photoPath) continue;
      const ext = c.photoPath.split('.').pop()?.split('?')[0] || 'jpg';
      const photoFile = `photo_${i}_${safeName(c.name)}.${ext}`;
      try {
        const b64 = await photoToBase64(c.photoPath);
        photosFolder!.file(photoFile, b64, { base64: true });
      } catch {
        // 旧照片可能没有读取权限，跳过但继续备份
      }
    }
  }

  const zipB64 = await zip.generateAsync({ type: 'base64' });

  const backupDir = new Directory(Paths.document, 'backups');
  backupDir.create({ idempotent: true });
  const ts = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
  const zipName = `${BACKUP_NAME}_${ts}.zip`;
  const zipFile = new File(backupDir, zipName);
  zipFile.write(zipB64, { encoding: 'base64' });

  if (await isAvailableAsync()) {
    await shareAsync(zipFile.uri, {
      dialogTitle: '保存联系人备份',
      mimeType: 'application/zip',
    });
  }

  return zipFile.uri;
}

export async function pickBackupFile(): Promise<Omit<Contact, 'id'>[] | null> {
  const result = await File.pickFileAsync({ mimeTypes: ['application/zip'] });
  if (result.canceled || !result.result) return null;

  const picked = result.result;
  const zipB64 = await picked.base64();
  const zip = await JSZip.loadAsync(zipB64, { base64: true });

  const jsonEntry = zip.file('contacts.json');
  if (!jsonEntry) throw new Error('备份文件中找不到 contacts.json');

  const jsonText = await jsonEntry.async('text');
  const parsed = JSON.parse(jsonText);
  if (!parsed.contacts || !Array.isArray(parsed.contacts)) {
    throw new Error('备份文件格式不正确');
  }

  const photosDir = new Directory(Paths.document, 'photos');
  photosDir.create({ idempotent: true });

  const contacts: Omit<Contact, 'id'>[] = [];
  for (let i = 0; i < parsed.contacts.length; i++) {
    const c = parsed.contacts[i];
    let photoPath: string | null = null;

    if (c.photo) {
      const photoEntry = zip.file(`photos/${c.photo}`);
      if (photoEntry) {
        const photoB64 = await photoEntry.async('base64');
        const dest = new File(photosDir, c.photo);
        dest.write(photoB64, { encoding: 'base64' });
        photoPath = dest.uri;
      }
    }

    contacts.push({
      name: c.name || '未知',
      phoneNumber: c.phoneNumber || '',
      photoPath,
      orderIndex: c.orderIndex ?? i,
    });
  }

  return contacts;
}
