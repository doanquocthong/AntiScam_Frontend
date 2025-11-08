package com.example.antiscam.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.example.antiscam.data.model.Contact
import kotlin.math.absoluteValue

class ContactRepository(private val context: Context) {

    // Hàm helper để generate màu avatar dựa trên phoneNumber
    private fun generateAvatarColor(phoneNumber: String): Int {
        val colors = listOf(
            0xFF3D3B8E,
            0xFF2C5F2D,
            0xFF7B3F00,
            0xFF5A189A,
            0xFF1E6091,
            0xFF9A031E,
            0xFF364F6B
        )
        val colorIndex = phoneNumber.hashCode().absoluteValue % colors.size
        return colors[colorIndex].toInt()
    }

    fun getAllContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx)
                val phone = it.getString(phoneIdx)
                val avatarColor = generateAvatarColor(phone)
                contacts.add(Contact(name, phone, avatarColor))
            }
        }
        return contacts
    }
}