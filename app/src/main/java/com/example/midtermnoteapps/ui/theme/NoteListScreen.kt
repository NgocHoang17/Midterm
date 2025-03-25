package com.example.midtermnoteapps.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObjects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(navController: NavController, firestore: FirebaseFirestore?) {
    var notes by remember { mutableStateOf(emptyList<Note>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Lấy dữ liệu từ Firestore
    LaunchedEffect(Unit) {
        if (firestore == null) {
            errorMessage = "Lỗi hệ thống: Firebase chưa được khởi tạo!"
            return@LaunchedEffect
        }

        listenerRegistration = firestore.collection("notes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage = "Lỗi tải ghi chú: ${error.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    notes = snapshot.toObjects<Note>().mapIndexed { index, note ->
                        note.copy(id = snapshot.documents[index].id)
                    }
                }
            }
    }

    // Hủy listener khi không cần thiết (tránh rò rỉ bộ nhớ)
    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
        }
    }

    Scaffold(
        topBar = {
            // Thanh TopAppBar được cải tiến
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = "Logo", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Notes App",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFB0BEC5), // Màu nền tím đậm
                    titleContentColor = Color.Black // Màu chữ trắng
                ),
                modifier = Modifier.heightIn(min = 56.dp, max = 85.dp), // Chiều cao thanh AppBar
                // Sử dụng `contentPadding` để tạo khoảng cách hợp lý cho chữ
                //contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
            )
        },
        floatingActionButton = {
            // Nút thêm ghi chú
            FloatingActionButton(
                onClick = { navController.navigate("add_note") },
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm Ghi Chú")
            }
        },
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Hiển thị lỗi (nếu có)
                    errorMessage?.let {
                        Text(it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    // Danh sách ghi chú
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(notes, key = { it.id }) { note ->
                            NoteItem(note = note, onClick = {
                                navController.navigate("edit_note/${note.id}")
                            })
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun NoteItem(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(note.title, fontWeight = FontWeight.Bold)
            Text(note.description, modifier = Modifier.padding(top = 4.dp))

            // Hiển thị ảnh nếu có
            if (note.fileUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(note.fileUrl),
                    contentDescription = "Ảnh ghi chú",
                    modifier = Modifier
                        .size(120.dp)
                        .clipToBounds()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNoteListScreen() {
    val navController = rememberNavController()
    NoteListScreen(navController, null) // Không truyền Firestore thật trong preview
}
