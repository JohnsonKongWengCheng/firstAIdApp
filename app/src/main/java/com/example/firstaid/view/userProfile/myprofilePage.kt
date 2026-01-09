package com.example.firstaid.view.userProfile

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.firstaid.R
import com.example.firstaid.view.components.BottomBar
import com.example.firstaid.view.components.BottomItem
import com.example.firstaid.view.components.TopBarWithBack
import com.example.firstaid.viewmodel.userProfile.MyProfileViewModel

@Composable
fun MyProfilePage(
    onBackClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {},
    onSaveClick: () -> Unit = onBackClick,
    viewModel: MyProfileViewModel = viewModel()
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    val uiState by viewModel.uiState.collectAsState()

    val sessionDocId = prefs.getString("docId", null)
    val sessionUserId = prefs.getString("userId", null)

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // Load user data from ViewModel
    LaunchedEffect(Unit) {
        viewModel.loadProfile(sessionDocId, sessionUserId)
    }

    // Handle navigation after success
    LaunchedEffect(uiState.showSuccessMessage) {
        if (uiState.showSuccessMessage) {
            kotlinx.coroutines.delay(1500)
            onSaveClick()
            viewModel.onSuccessMessageShown()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .clickable { keyboardController?.hide() }
        ) {
            // Top Bar
            TopBarWithBack(
                title = "My Profile",
                onBackClick = onBackClick
            )
            
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                
                // Loading indicator
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.green_primary)
                        )
                    }
                }
                
                // Profile Picture Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                Color(0xFFECECEC),
                                CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = if (uiState.isUploadingImage) Color(0xFF2E7D32) else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                if (!uiState.isUploadingImage) {
                                    imagePickerLauncher.launch("image/*")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isUploadingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = Color(0xFF2E7D32),
                                strokeWidth = 3.dp
                            )
                        } else if (uiState.selectedImageUri != null) {
                            // Show selected image (not yet saved)
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uiState.selectedImageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Selected Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                placeholder = null
                            )
                        } else if (uiState.profileImageUrl.isNotEmpty()) {
                            // Show saved profile image
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uiState.profileImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                placeholder = null
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(60.dp),
                                tint = Color.Gray
                            )
                        }

                        // Red "X" overlay to remove image when one exists
                        if (!uiState.isUploadingImage && (uiState.selectedImageUri != null || uiState.profileImageUrl.isNotEmpty())) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(22.dp)
                                    .background(Color.White, CircleShape)
                                    .border(1.dp, Color.Red, CircleShape)
                                    .clickable { viewModel.removeImage() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove image",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                
                
                // Username Field
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = { newValue -> viewModel.onUsernameChange(newValue) },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.usernameError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.green_primary),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorBorderColor = Color.Red,
                        errorTextColor = Color.Red
                    ),
                    textStyle = TextStyle(fontFamily = cabin, color = Color.Black),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    )
                )

                if (uiState.usernameError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.usernameError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontFamily = cabin
                    )
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                
                // Save Button
                Button(
                    onClick = {
                        val docId = prefs.getString("docId", "")
                        viewModel.saveProfile(docId, sessionUserId)
                    },
                    enabled = uiState.hasChanges && !uiState.isUploadingImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.hasChanges && !uiState.isUploadingImage)
                            colorResource(id = R.color.green_primary) 
                        else Color.Gray,
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (uiState.isUploadingImage) "Uploading..." else "Save",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = cabin
                    )
                }
                
                // Add bottom padding to ensure content doesn't get hidden behind BottomBar
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // Fixed Bottom Bar at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BottomBar(
                selected = BottomItem.ACCOUNT,
                onSelected = onSelectBottom
            )
        }
        
        // Success message overlay
        if (uiState.showSuccessMessage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = colorResource(id = R.color.green_primary),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Profile Updated Successfully!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = cabin,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your changes have been saved",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontFamily = cabin,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyProfilePagePreview() {
    MyProfilePage()
}