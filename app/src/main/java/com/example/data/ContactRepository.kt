package com.example.data

import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {
    val allContacts: Flow<List<Contact>> = contactDao.getAllContactsFlow()

    suspend fun getContactCount(): Int = contactDao.getContactCount()

    suspend fun insertContact(contact: Contact): Long = contactDao.insertContact(contact)

    suspend fun insertContacts(contacts: List<Contact>) = contactDao.insertContacts(contacts)

    suspend fun updateContact(contact: Contact) = contactDao.updateContact(contact)

    suspend fun updateContacts(contacts: List<Contact>) = contactDao.updateContacts(contacts)

    suspend fun deleteContact(contact: Contact) = contactDao.deleteContact(contact)

    suspend fun deleteContactById(id: Long) = contactDao.deleteContactById(id)

    suspend fun getAllContactsSync(): List<Contact> = contactDao.getAllContactsSync()

    suspend fun deleteAll() = contactDao.deleteAll()
}
