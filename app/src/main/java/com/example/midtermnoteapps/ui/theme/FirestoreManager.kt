package com.example.midtermnoteapps.ui.theme

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    fun addNote(title: String, description: String, fileUrl: String, callback: (Boolean) -> Unit) {
        val note = hashMapOf("title" to title, "description" to description, "fileUrl" to fileUrl)
        db.collection("notes").add(note).addOnCompleteListener {
            callback(it.isSuccessful)
        }
    }

    fun updateNote(noteId: String, title: String, description: String, fileUrl: String, callback: (Boolean) -> Unit) {
        val note = hashMapOf("title" to title, "description" to description, "fileUrl" to fileUrl)
        db.collection("notes").document(noteId).set(note).addOnCompleteListener {
            callback(it.isSuccessful)
        }
    }

    fun deleteNote(noteId: String, callback: (Boolean) -> Unit) {
        db.collection("notes").document(noteId).delete().addOnCompleteListener {
            callback(it.isSuccessful)
        }
    }
}
