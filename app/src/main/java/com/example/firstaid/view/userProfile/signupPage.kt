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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpPage(
    onSignupSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val mateSC = FontFamily(
        Font(R.font.matesc_regular, FontWeight.Bold)
    )
    val cabin = FontFamily(
        Font(R.font.cabin, FontWeight.Bold)
    )
    val alfaslabone = FontFamily(
        Font(R.font.alfaslabone_regular, FontWeight.Bold)
    )

    var emailError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    val usernameRegex = "^[A-Za-z0-9]{8,}$".toRegex()
    val keyboardController = LocalSoftwareKeyboardController.current

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
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 48.dp)
                .clickable { keyboardController?.hide() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
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
                text = "Create your account",
                fontSize = 28.sp,
                fontFamily = alfaslabone,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Email Address",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = { newEmail ->
                    email = newEmail
                    emailError = if (emailRegex.matches(newEmail)) null else "Invalid email format"
                    errorMessage = null
                },
                placeholder = { Text("Enter Email Address", color = Color(0xFFAAAAAA)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = emailError != null,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFECecec),
                    unfocusedContainerColor = Color(0xFFECecec),
                    disabledContainerColor = Color(0xFFECecec),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black,
                    errorContainerColor = Color(0xFFECecec),
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
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Username",
                    fontSize = 16.sp,
                    fontFamily = cabin,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = if (usernameRegex.matches(it)) null else "At least 8 characters (letters or digits only)"
                    errorMessage = null
                },
                placeholder = { Text("Enter Username", color = Color(0xFFAAAAAA)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = usernameError != null,
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
            if (usernameError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        usernameError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                    confirmPasswordError = if (confirmPassword == password) null else "Passwords do not match"
                    errorMessage = null
                },
                placeholder = { Text("Enter Password", color = Color(0xFFAAAAAA)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = passwordError != null,
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
                        passwordError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Confirm Password",
                    fontSize = 16.sp,
                    fontFamily = cabin,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = if (it == password) null else "Confirmed password must match Password"
                    errorMessage = null
                },
                placeholder = { Text("Confirm Password", color = Color(0xFFAAAAAA)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = confirmPasswordError != null,
                shape = RoundedCornerShape(10.dp),
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
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
            if (confirmPasswordError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        confirmPasswordError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show Firebase or validation error
            errorMessage?.let { err ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = err,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (emailError != null || usernameError != null || passwordError != null || confirmPasswordError != null) {
                        errorMessage = "Please fix the errors above"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null

                    val auth = FirebaseAuth.getInstance()
                    val firestore = FirebaseFirestore.getInstance()

                    auth.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val uid = auth.currentUser?.uid
                                if (uid == null) {
                                    isLoading = false
                                    errorMessage = "Signup succeeded but no user id found"
                                    return@addOnCompleteListener
                                }
                                val userData = hashMapOf(
                                    "userId" to uid,
                                    "email" to email.trim(),
                                    "name" to username.trim(),
                                    "password" to password,
                                    "login" to false,
                                    "createdAt" to System.currentTimeMillis()
                                )
                                firestore.collection("User").add(userData)
                                    .addOnSuccessListener { userDocRef ->
                                        // Create both learning and exam progress records
                                        val batch = firestore.batch()
                                        
                                        // Create learning progress records for all learning materials
                                        firestore.collection("Learning").get()
                                            .addOnSuccessListener { learningDocs ->
                                                learningDocs.documents.forEach { learningDoc ->
                                                    val learningId = learningDoc.getString("learningId") ?: learningDoc.id
                                                    val progressData = hashMapOf(
                                                        "userId" to uid,
                                                        "learningId" to learningId,
                                                        "status" to "Pending"
                                                    )
                                                    val progressRef = firestore.collection("Learning_Progress").document()
                                                    batch.set(progressRef, progressData)
                                                }
                                                
                                                // Create exam progress records for all exams
                                                firestore.collection("Exam").get()
                                                    .addOnSuccessListener { examDocs ->
                                                        examDocs.documents.forEach { examDoc ->
                                                            val examId = examDoc.getString("examId") ?: examDoc.id
                                                            val examProgressData = hashMapOf(
                                                                "userId" to uid,
                                                                "examId" to examId,
                                                                "status" to "Pending",
                                                                "score" to 0
                                                            )
                                                            val examProgressRef = firestore.collection("Exam_Progress").document()
                                                            batch.set(examProgressRef, examProgressData)
                                                        }
                                                        
                                                        // Commit all progress records
                                                        batch.commit()
                                                            .addOnSuccessListener {
                                                                isLoading = false
                                                                onSignupSuccess()
                                                            }
                                                            .addOnFailureListener { e ->
                                                                isLoading = false
                                                                errorMessage = "User created but failed to initialize progress: ${e.localizedMessage}"
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        isLoading = false
                                                        errorMessage = "User created but failed to load exams: ${e.localizedMessage}"
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                errorMessage = "User created but failed to load learning materials: ${e.localizedMessage}"
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Failed to save user"
                                    }
                            } else {
                                isLoading = false
                                errorMessage = authTask.exception?.localizedMessage ?: "Signup failed"
                            }
                        }
                },
                modifier = Modifier
                    .width(209.dp)
                    .height(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.green_primary)
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoading
                        && email.isNotBlank()
                        && username.isNotBlank()
                        && password.isNotBlank()
                        && confirmPassword.isNotBlank()
                        && emailError == null
                        && usernameError == null
                        && passwordError == null
                        && confirmPasswordError == null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Sign Up",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mateSC,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Have an account? ",
                    color = Color.Black,
                    fontFamily = cabin,
                    fontSize = 16.sp
                )
                TextButton(
                    onClick = onLoginClick
                ) {
                    Text(
                        text = "Log In",
                        color = colorResource(id = R.color.green_primary),
                        fontFamily = cabin,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpPreview() {
    SignUpPage()
}