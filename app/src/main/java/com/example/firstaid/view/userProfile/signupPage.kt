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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstaid.viewmodel.userProfile.SignUpViewModel

@Composable
fun SignUpPage(
    onSignupSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: SignUpViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
    var successMessage by remember { mutableStateOf<String?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current

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
                .verticalScroll(rememberScrollState()) // Scrollable content
                .imePadding()
                .padding(horizontal = 48.dp)
                .clickable { keyboardController?.hide() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //logo
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
                    errorContainerColor = Color(0xFFECecec),
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
                value = uiState.username,
                onValueChange = { viewModel.onUsernameChange(it) },
                placeholder = { Text("Enter Username", color = Color(0xFFAAAAAA)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.usernameError != null,
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
            if (uiState.usernameError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        uiState.usernameError ?: "",
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
                    cursorColor = Color.Black
                )
            )
            if (uiState.passwordError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        uiState.passwordError ?: "",
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
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                placeholder = { Text("Confirm Password", color = Color(0xFFAAAAAA)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.confirmPasswordError != null,
                shape = RoundedCornerShape(10.dp),
                visualTransformation = if (uiState.showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                        Icon(
                            imageVector = if (uiState.showConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
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
            if (uiState.confirmPasswordError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        uiState.confirmPasswordError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show success message
            uiState.successMessage?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = colorResource(id = R.color.green_primary),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            // Show Firebase or validation error
            uiState.errorMessage?.let { err ->
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
                    viewModel.signUp(
                        onSuccess = {
                            // Navigate to login page after a short delay
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                onSignupSuccess()
                            }, 3000)
                        },
                        onFailure = {}
                    )
                },
                modifier = Modifier
                    .width(209.dp)
                    .height(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.green_primary)
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = !uiState.isLoading
                        && uiState.email.isNotBlank()
                        && uiState.username.isNotBlank()
                        && uiState.password.isNotBlank()
                        && uiState.confirmPassword.isNotBlank()
                        && uiState.emailError == null
                        && uiState.usernameError == null
                        && uiState.passwordError == null
                        && uiState.confirmPasswordError == null
            ) {
                if (uiState.isLoading) {
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