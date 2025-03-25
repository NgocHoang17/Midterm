package com.example.midtermnoteapps.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController, auth: FirebaseAuth, firestore: FirebaseFirestore) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userInfo by remember { mutableStateOf<User?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (isRegistering) {
                registerUser(auth, firestore, email, password) { success, message ->
                    errorMessage = message
                    if (success) navController.navigate("note_list")
                }
            } else {
                loginUser(auth, firestore, email, password) { success, user, message ->
                    errorMessage = message
                    if (success) {
                        userInfo = user
                        navController.navigate("note_list")
                    }
                }
            }
        }) {
            Text(if (isRegistering) "Đăng ký" else "Đăng nhập")
        }

        TextButton(onClick = { isRegistering = !isRegistering }) {
            Text(if (isRegistering) "Bạn đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color.Red)
        }
    }
}

fun loginUser(auth: FirebaseAuth, firestore: FirebaseFirestore, email: String, password: String, onComplete: (Boolean, User?, String?) -> Unit) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = task.result?.user?.uid
                if (userId != null) {
                    fetchUserData(userId, firestore) { user ->
                        onComplete(true, user, null)
                    }
                } else {
                    onComplete(false, null, "Lỗi: Không tìm thấy thông tin người dùng!")
                }
            } else {
                onComplete(false, null, "Đăng nhập thất bại! Kiểm tra lại email và mật khẩu.")
            }
        }
}

fun registerUser(auth: FirebaseAuth, firestore: FirebaseFirestore, email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = task.result?.user?.uid
                if (userId != null) {
                    val newUser = User(userId, email.substringBefore("@"), email)
                    firestore.collection("users").document(userId).set(newUser)
                        .addOnSuccessListener {
                            onComplete(true, "Đăng ký thành công!")
                        }
                        .addOnFailureListener {
                            onComplete(false, "Lỗi: Không thể lưu thông tin người dùng.")
                        }
                }
            } else {
                onComplete(false, "Đăng ký thất bại! Kiểm tra lại email và mật khẩu.")
            }
        }
}

fun fetchUserData(userId: String, firestore: FirebaseFirestore, onComplete: (User?) -> Unit) {
    firestore.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                onComplete(user)
            } else {
                onComplete(null)
            }
        }
        .addOnFailureListener {
            onComplete(null)
        }
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = ""
)