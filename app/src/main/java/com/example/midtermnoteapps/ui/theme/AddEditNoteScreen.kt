package com.example.midtermnoteapps.ui.theme

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@Composable
fun AddEditNoteScreen(
    navController: NavController,
    firestore: FirebaseFirestore?,
    storage: FirebaseStorage?,
    noteId: String? = null
) {
    // Kiểm tra firestore không null trước khi dùng
    if (firestore == null) {
        Log.e("Firestore", "Firestore chưa được khởi tạo!")
        return
    }

    // State lưu trữ dữ liệu
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val fileUrl = remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Bộ chọn ảnh
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        uri?.let {
            if (storage != null) {
                val storageRef = storage.reference.child("images/${UUID.randomUUID()}")
                storageRef.putFile(it)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) task.exception?.let { throw it }
                        storageRef.downloadUrl
                    }.addOnSuccessListener { url ->
                        fileUrl.value = url.toString()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Storage", "Lỗi khi tải ảnh lên", e)
                    }
            } else {
                Log.e("Storage", "Firebase Storage chưa được khởi tạo!")
            }
        }
    }

    // Nếu đang chỉnh sửa, lấy dữ liệu từ Firestore
    LaunchedEffect(noteId) {
        if (noteId != null) {
            firestore.collection("notes").document(noteId).get()
                .addOnSuccessListener { doc ->
                    title.value = doc.getString("title") ?: ""
                    description.value = doc.getString("description") ?: ""
                    fileUrl.value = doc.getString("fileUrl") ?: ""
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Lỗi khi lấy dữ liệu", e)
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Tiêu đề") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Nội dung") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Hiển thị ảnh chọn từ bộ nhớ
        imageUri?.let {
            Card(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Ảnh đã chọn",
                    modifier = Modifier.height(150.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Hiển thị ảnh đã lưu trong Firestore (nếu có)
        if (fileUrl.value.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(fileUrl.value),
                    contentDescription = "Ảnh từ Firebase",
                    modifier = Modifier.height(150.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = { imageLauncher.launch("image/*") }) {
            Text("Chọn ảnh")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Thêm hoặc cập nhật ghi chú
        Button(onClick = {
            val note = hashMapOf(
                "title" to title.value,
                "description" to description.value,
                "fileUrl" to fileUrl.value
            )

            if (noteId == null) {
                // Thêm mới ghi chú
                firestore.collection("notes")
                    .add(note)
                    .addOnSuccessListener {
                        navController.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Lỗi khi thêm ghi chú", e)
                    }
            } else {
                // Cập nhật ghi chú hiện có
                firestore.collection("notes").document(noteId)
                    .set(note)
                    .addOnSuccessListener {
                        navController.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Lỗi khi cập nhật ghi chú", e)
                    }
            }
        }) {
            Text(if (noteId == null) "Thêm Ghi Chú" else "Cập Nhật")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Nút Xóa (chỉ hiển thị khi đang chỉnh sửa)
        if (noteId != null) {
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Xóa Ghi Chú")
            }
        }

        // Hộp thoại xác nhận xóa
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa ghi chú này không?") },
                confirmButton = {
                    Button(onClick = {
                        if (noteId != null) {
                            firestore.collection("notes").document(noteId).delete()
                                .addOnSuccessListener {
                                    navController.popBackStack()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Lỗi khi xóa ghi chú", e)
                                }
                        }
                    }) {
                        Text("Xóa")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddEditNoteScreen() {
    val navController = rememberNavController()

    // Tạo một giá trị giả lập để xem trước
    val fakeFirestore = FirebaseFirestore.getInstance() // Không sử dụng thật khi preview
    val fakeStorage = FirebaseStorage.getInstance() // Không sử dụng thật khi preview

    MidTermNoteAppsTheme {
        AddEditNoteScreen(
            navController = navController,
            firestore = fakeFirestore, // Truyền giá trị giả lập
            storage = fakeStorage, // Truyền giá trị giả lập
            noteId = null // Xem trước chế độ thêm mới
        )
    }
}
