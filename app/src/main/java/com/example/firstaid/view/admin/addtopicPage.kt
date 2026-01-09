package com.example.firstaid.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstaid.R
import com.example.firstaid.view.components.TopBarWithBack
import com.example.firstaid.viewmodel.admin.AddTopicViewModel
import kotlinx.coroutines.delay

@Composable
fun AddTopicPage(
    onBackClick: () -> Unit = {},
    viewModel: AddTopicViewModel = viewModel()
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.showSuccess) {
        if (uiState.showSuccess) {
            delay(1000)
            viewModel.dismissSuccess()
            onBackClick()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarWithBack(title = "Add Topic", onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 100.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "First Aid Topic Title",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontFamily = cabin
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    placeholder = { Text("Enter First Aid Topic Title here..", color = Color(0xFFAAAAAA)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = if (uiState.titleExists || uiState.isFormatInvalid || uiState.isWhitespaceOnly) Color.Red else Color.Transparent,
                        focusedBorderColor = if (uiState.titleExists || uiState.isFormatInvalid || uiState.isWhitespaceOnly) Color.Red else colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                        unfocusedContainerColor = Color(0xFFECF0EC),
                        focusedContainerColor = Color(0xFFE6F3E6)
                    ),
                    isError = uiState.titleExists || uiState.isFormatInvalid || uiState.isWhitespaceOnly
                )
                
                // Error message display
                if (uiState.validationError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.validationError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontFamily = cabin
                    )
                }
                
                // Validation loading indicator
                if (uiState.isValidating && uiState.title.trim().isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Checking availability...",
                        color = Color(0xFF666666),
                        fontSize = 12.sp,
                        fontFamily = cabin
                    )
                }
            }
        }

        // Bottom divider and Confirm button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .padding(bottom = 16.dp)
        ) {
            Divider(color = Color(0xFFB8B8B8), thickness = 1.dp, modifier = Modifier.padding(horizontal = 11.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    viewModel.saveTopic {
                        // Success handled in LaunchedEffect
                    }
                },
                enabled = uiState.canSave,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.green_primary),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = if (uiState.isSaving) "Saving..." else "Confirm", color = Color.White, fontFamily = cabin)
            }
        }

        // Success overlay
        if (uiState.showSuccess) {
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
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = colorResource(id = R.color.green_primary),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Topic Added Successfully!",
                            color = colorResource(id = R.color.green_primary),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = cabin
                        )
                    }
                }
            }
        }
    }
}
