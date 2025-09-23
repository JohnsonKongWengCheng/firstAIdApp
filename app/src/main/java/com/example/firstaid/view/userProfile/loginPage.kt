package com.example.firstaid.view.userProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import com.example.firstaid.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.AuthResult

@Composable
fun LoginPage(
    redirectTo: String? = null,
    onLoginSuccess: () -> Unit = {},
    onSignupClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val alfaslabone = FontFamily(
        Font(R.font.alfaslabone_regular, FontWeight.Bold)
    )
    val cabin = FontFamily(
        Font(R.font.cabin, FontWeight.Bold)
    )
    val mateSC = FontFamily(
        Font(R.font.matesc_regular, FontWeight.Bold)
    )

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Fixed TopBar
        Spacer(modifier = Modifier.height(60.dp))
        
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = 48.dp)
                .clickable { keyboardController?.hide() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.size(118.dp).clip(RoundedCornerShape(10.dp)).border(
                    width = 1.dp,
                    color = colorResource(id = R.color.green_primary),
                    shape = RoundedCornerShape(10.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Log in your account",
                fontSize = 28.sp,
                fontFamily = alfaslabone,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Email Address",
                    fontSize = 16.sp,
                    fontFamily = cabin,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = if (emailRegex.matches(it)) null else "Invalid email format"
                    errorMessage = null
                },
                placeholder = { Text("Enter Email Address", color = Color(0xFFAAAAAA)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFECecec),
                    unfocusedContainerColor = Color(0xFFECecec),
                    disabledContainerColor = Color(0xFFECecec),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                )
            )

            if (emailError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = emailError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Password",
                    fontSize = 16.sp,
                    fontFamily = cabin,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = when {
                        it.isBlank() -> "Password cannot be empty"
                        it.contains(" ") -> "Password cannot contain spaces"
                        it.length < 6 -> "Password must be at least 6 characters"
                        else -> null
                    }
                    errorMessage = null
                },
                placeholder = { Text("Enter Password", color = Color(0xFFAAAAAA)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFECecec),
                    unfocusedContainerColor = Color(0xFFECecec),
                    disabledContainerColor = Color(0xFFECecec),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                )
            )

            if (passwordError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = passwordError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let { error ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

    Button(
                onClick = {
            isLoading = true
            errorMessage = null
            if (emailError == null && passwordError == null && email.isNotBlank() && password.isNotBlank()) {
                // First verify against Firestore User table
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("User")
                    .whereEqualTo("email", email)
                    .whereEqualTo("password", password)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            val doc = snapshot.documents.first()
                            val docId = doc.id
                            val userId = doc.getString("userId") ?: docId
                            val name = doc.getString("name") ?: ""
                            val userEmail = doc.getString("email") ?: email
                            
                            // Try to sign in to Firebase Auth for Storage access
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener { authResult: AuthResult ->
                                    val firebaseUser = authResult.user
                                    android.util.Log.d("Login", "Successfully signed in to Firebase Auth: ${firebaseUser?.uid}")
                                    
                                    // Store session data
                                    val prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                                    val editor = prefs.edit()
                                    editor.putString("docId", docId)
                                    editor.putString("userId", userId)
                                    editor.putString("name", name)
                                    editor.putString("email", userEmail)
                                    editor.apply()
                                    
                                    // Mark user as logged in in Firestore
                                    db.collection("User").document(docId).update("login", true)
                                    isLoading = false
                                    onLoginSuccess()
                                }
                                .addOnFailureListener { authError: Exception ->
                                    // If Firebase Auth fails, create a new Firebase Auth user
                                    android.util.Log.d("Login", "Firebase Auth sign-in failed, creating new user: ${authError.localizedMessage}")
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnSuccessListener { authResult: AuthResult ->
                                            val firebaseUser = authResult.user
                                            android.util.Log.d("Login", "Successfully created Firebase Auth user: ${firebaseUser?.uid}")
                                            
                                            // Update Firestore with the new Firebase Auth UID
                                            db.collection("User").document(docId)
                                                .update("userId", firebaseUser?.uid ?: userId)
                                                .addOnSuccessListener {
                                                    // Store session data
                                                    val prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                                                    val editor = prefs.edit()
                                                    editor.putString("docId", docId)
                                                    editor.putString("userId", firebaseUser?.uid ?: userId)
                                                    editor.putString("name", name)
                                                    editor.putString("email", userEmail)
                                                    editor.apply()
                                                    
                                                    // Mark user as logged in in Firestore
                                                    db.collection("User").document(docId).update("login", true)
                                                    isLoading = false
                                                    onLoginSuccess()
                                                }
                                                .addOnFailureListener { updateError ->
                                                    isLoading = false
                                                    errorMessage = "Failed to update user ID: ${updateError.localizedMessage}"
                                                }
                                        }
                                        .addOnFailureListener { createError: Exception ->
                                            isLoading = false
                                            errorMessage = "Failed to create Firebase Auth user: ${createError.localizedMessage}"
                                        }
                                }
                        } else {
                            isLoading = false
                            errorMessage = "Invalid email or password"
                        }
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = e.localizedMessage ?: "Login failed"
                    }
            } else {
                isLoading = false
                errorMessage = "Please fix the errors above"
            }
                },
                modifier = Modifier
                    .width(209.dp)
                    .height(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isLoading && emailError == null && passwordError == null)
                        colorResource(id = R.color.green_primary)
                    else
                        Color.Gray
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoading
                        && email.isNotBlank()
                        && password.isNotBlank()
                        && emailError == null
                        && passwordError == null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Log In",
                        fontSize = 30.sp,
                        fontFamily = mateSC,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don’t have an account? ",
                    color = Color.Black,
                    fontFamily = cabin,
                    fontSize = 16.sp
                )
                TextButton(
                    onClick = onSignupClick
                ) {
                    Text(
                        text = "Sign Up",
                        color = colorResource(id = R.color.green_primary),
                        fontSize = 16.sp,
                        fontFamily = cabin,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    LoginPage()
}