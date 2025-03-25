package com.example.midtermnoteapps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.midtermnoteapps.ui.theme.AddEditNoteScreen
import com.example.midtermnoteapps.ui.theme.LoginScreen
import com.example.midtermnoteapps.ui.theme.MidTermNoteAppsTheme
import com.example.midtermnoteapps.ui.theme.NoteListScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dataconnect.LogLevel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MidTermNoteAppsTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val currentUser = auth.currentUser
    val startDestination = if (currentUser != null) "note_list" else "login"

    NavHost(navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController, auth, firestore) } // Sửa ở đây
        composable("note_list") { NoteListScreen(navController, firestore) }
        composable("add_note") { AddEditNoteScreen(navController, firestore, storage) }
        composable("edit_note/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            AddEditNoteScreen(navController, firestore, storage, noteId)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewNoteListScreen() {
    val navController = rememberNavController()
    MidTermNoteAppsTheme {
        NoteListScreen(navController, firestore = null)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddEditNoteScreen() {
    val navController = rememberNavController()
    MidTermNoteAppsTheme {
        AddEditNoteScreen(navController, firestore = null, storage = null)
    }
}
