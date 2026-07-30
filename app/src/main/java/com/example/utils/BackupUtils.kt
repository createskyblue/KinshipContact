package com.example.utils

import android.content.Context
import android.os.Environment
import com.example.data.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupUtils {

    /**
     * Creates a ZIP file containing the SQLite database and all avatar images in Download folder.
     */
    fun createBackup(context: Context): File? {
        return try {
            val dbFile = context.getDatabasePath("family_contacts_db")
            val avatarDir = File(context.filesDir, "avatars")

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val backupZip = File(downloadDir, "FamilyContacts_Backup_$timeStamp.zip")

            ZipOutputStream(FileOutputStream(backupZip)).use { zos ->
                // Add DB file
                if (dbFile.exists()) {
                    addFileToZip(zos, dbFile, "family_contacts_db")
                }

                // Add Avatars folder
                if (avatarDir.exists() && avatarDir.isDirectory) {
                    avatarDir.listFiles()?.forEach { avatarFile ->
                        if (avatarFile.isFile) {
                            addFileToZip(zos, avatarFile, "avatars/" + avatarFile.name)
                        }
                    }
                }
            }

            backupZip
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addFileToZip(zos: ZipOutputStream, file: File, zipPath: String) {
        FileInputStream(file).use { fis ->
            val entry = ZipEntry(zipPath)
            zos.putNextEntry(entry)
            val buffer = ByteArray(1024)
            var length: Int
            while (fis.read(buffer).also { length = it } > 0) {
                zos.write(buffer, 0, length)
            }
            zos.closeEntry()
        }
    }

    /**
     * Restores database and avatar files from a backup ZIP file.
     */
    fun restoreFromBackup(context: Context, zipFile: File): Boolean {
        return try {
            // Close active DB connection before overwrite
            val appDb = AppDatabase.getDatabase(context)
            if (appDb.isOpen) {
                appDb.close()
            }

            val targetDb = context.getDatabasePath("family_contacts_db")
            val avatarDir = File(context.filesDir, "avatars")
            if (!avatarDir.exists()) avatarDir.mkdirs()

            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    if (entryName == "family_contacts_db") {
                        FileOutputStream(targetDb).use { fos ->
                            zis.copyTo(fos)
                        }
                    } else if (entryName.startsWith("avatars/")) {
                        val fileName = entryName.removePrefix("avatars/")
                        if (fileName.isNotEmpty()) {
                            val outFile = File(avatarDir, fileName)
                            FileOutputStream(outFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
