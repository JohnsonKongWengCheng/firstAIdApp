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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstaid.viewmodel.userProfile.LoginViewModel

@Composable
fun LoginPage(
    redirectTo: String? = null,
    onLoginSuccess: () -> Unit = {},
    onSignupClick: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val alfaslabone = FontFamily(
        Font(R.font.alfaslabone_regular, FontWeight.Bold)
    )
    val cabin = FontFamily(
        Font(R.font.cabin, FontWeight.Bold)
    )
    val mateSC = FontFamily(
        Font(R.font.matesc_regular, FontWeight.Bold)
    )

    val brandGreen = colorResource(id = R.color.green_primary)

    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    
    val redirectMessage = remember(redirectTo) {
        when (redirectTo) {
            "learn" -> "Log in to continue learning modules."
            "account" -> "Log in to view your account."
            "ai" -> "Log in to access the AI helper."
            null, "" -> null
            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Fixed TopBar
        Spacer(modifier = Modifier.height(60.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState) // Scrollable content
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

            redirectMessage?.let { message ->
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontFamily = cabin,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

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
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                placeholder = { Text("Enter Email Address", color = Color(0xFFAAAAAA)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.emailError != null,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFECecec),
                    unfocusedContainerColor = Color(0xFFECecec),
                    disabledContainerColor = Color(0xFFECecec),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black,
                    errorContainerColor = Color(0xFFECecec)
                )
            )

            if (uiState.emailError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = uiState.emailError ?: "",
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
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                placeholder = { Text("Enter Password", color = Color(0xFFAAAAAA)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.passwordError != null,
                shape = RoundedCornerShape(10.dp),
                visualTransformation = if (uiState.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (uiState.showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
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
                    cursorColor = Color.Black,
                    errorContainerColor = Color(0xFFECecec)
                )
            )

            if (uiState.passwordError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = uiState.passwordError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        viewModel.resetPassword(
                            onSuccess = {},
                            onFailure = {}
                        )
                    }
                ) {
                    if (uiState.isResettingPassword) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Forgot Password?",
                            color = brandGreen,
                            fontFamily = cabin,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            uiState.forgotPasswordMessage?.let { message ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        color = androidx.compose.ui.graphics.Color(uiState.forgotPasswordMessageColor.toInt()),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }
            }

            uiState.verificationMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        color = androidx.compose.ui.graphics.Color(uiState.verificationMessageColor.toInt()),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                TextButton(
                    onClick = {
                        viewModel.resendVerificationEmail(
                            onSuccess = {},
                            onFailure = {}
                        )
                    }
                ) {
                    if (uiState.isResendingVerification) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Resend Verification Email",
                            color = brandGreen,
                            fontFamily = cabin,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            uiState.errorMessage?.let { error ->
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
                    viewModel.login(
                        onSuccess = { docId, userId ->
                            // Persist session
                            val prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            db.collection("User").document(docId).get()
                                .addOnSuccessListener { doc ->
                                    val name = doc.getString("name") ?: ""
                                    val userEmail = doc.getString("email") ?: uiState.email
                                    val editor = prefs.edit()
                                    editor.putString("docId", docId)
                                    editor.putString("userId", userId)
                                    editor.putString("name", name)
                                    editor.putString("email", userEmail)
                                    editor.putBoolean("isAdmin", false)
                                    editor.apply()
                                    onLoginSuccess()
                                }
                        },
                        onFailure = {}
                    )
                },
                modifier = Modifier
                    .width(209.dp)
                    .height(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!uiState.isLoading && uiState.emailError == null && uiState.passwordError == null)
                        colorResource(id = R.color.green_primary)
                    else
                        Color.Gray
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = !uiState.isLoading
                        && uiState.email.isNotBlank()
                        && uiState.password.isNotBlank()
                        && uiState.emailError == null
                        && uiState.passwordError == null
            ) {
                if (uiState.isLoading) {
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