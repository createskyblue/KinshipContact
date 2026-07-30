package com.example.utils

import android.content.Context
import android.provider.ContactsContract
import com.example.data.Contact

object SystemContactsUtils {

    /**
     * Reads name and phone number from system contacts provider.
     */
    fun getSystemContacts(context: Context): List<Contact> {
        val resultList = mutableListOf<Contact>()
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        try {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                var index = 0
                while (cursor.moveToNext()) {
                    val name = if (nameIndex >= 0) cursor.getString(nameIndex) else "未知"
                    val number = if (numberIndex >= 0) cursor.getString(numberIndex) else ""
                    if (!name.isNullOrBlank() && !number.isNullOrBlank()) {
                        resultList.add(
                            Contact(
                                name = name,
                                phoneNumber = number.replace(" ", "").replace("-", ""),
                                photoPath = null,
                                orderIndex = index++
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return resultList
    }
}
