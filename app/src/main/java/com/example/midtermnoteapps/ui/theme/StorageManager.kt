package com.example.midtermnoteapps.ui.theme

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object StorageManager {
    private val storage = FirebaseStorage.getInstance().reference

    fun uploadFile(fileUri: Uri, callback: (String?) -> Unit) {
        val fileRef = storage.child("uploads/${fileUri.lastPathSegment}")
        fileRef.putFile(fileUri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString()) // Trả về link file
            }
        }.addOnFailureListener {
            callback(null)
        }
    }
}
