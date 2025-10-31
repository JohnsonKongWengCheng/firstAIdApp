package com.example.firstaid.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstaid.R
import com.example.firstaid.view.components.TopBar
import com.example.firstaid.view.components.TopBarWithBack
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.imePadding

@Composable
fun AddTopicPage(
    onBackClick: () -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val db = remember { FirebaseFirestore.getInstance() }
    var titleState by remember { mutableStateOf(TextFieldValue("")) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var titleExists by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }
    var isFormatInvalid by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    // Validate title (letters and spaces only) and existence in Firebase (case-insensitive)
    LaunchedEffect(titleState.text) {
        val title = titleState.text.trim()
        if (title.isNotEmpty()) {
            isValidating = true
            validationError = ""
            titleExists = false
            isFormatInvalid = false
            
            // Format validation: allow only letters and spaces
            val isOnlyLettersAndSpaces = title.all { it.isLetter() || it.isWhitespace() }
            if (!isOnlyLettersAndSpaces) {
                isFormatInvalid = true
                validationError = "Only letters and spaces are allowed"
                isValidating = false
                return@LaunchedEffect
            }

            // Get all documents and check case-insensitively
            db.collection("First_Aid")
                .get()
                .addOnSuccessListener { documents ->
                    val existingTitles = documents.documents.mapNotNull { doc ->
                        doc.getString("title")?.trim()
                    }

                    // Check if any existing title matches (case-insensitive)
                    titleExists = existingTitles.any { existingTitle ->
                        existingTitle.equals(title, ignoreCase = true)
                    }

                    if (titleExists) {
                        validationError = "This topic title already exists"
                    }
                    isValidating = false
                }
                .addOnFailureListener { exception ->
                    validationError = "Error checking title availability"
                    isValidating = false
                }
        } else {
            titleExists = false
            validationError = ""
            isValidating = false
            isFormatInvalid = false
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
                    value = titleState,
                    onValueChange = { titleState = it },
                    placeholder = { Text("Enter First Aid Topic Title here..", color = Color(0xFFAAAAAA)) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = if (titleExists || isFormatInvalid) Color.Red else Color.Transparent,
                        focusedBorderColor = if (titleExists || isFormatInvalid) Color.Red else colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                        unfocusedContainerColor = Color(0xFFECF0EC),
                        focusedContainerColor = Color(0xFFE6F3E6)
                    ),
                    isError = titleExists || isFormatInvalid
                )
                
                // Error message display
                if (validationError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = validationError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontFamily = cabin
                    )
                }
                
                // Validation loading indicator
                if (isValidating && titleState.text.trim().isNotEmpty()) {
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
                    if (!isSaving && titleState.text.isNotBlank() && !titleExists && !isValidating) {
                        isSaving = true
                        // Create document with generated id; store both firstAidId and title
                        val docRef = db.collection("First_Aid").document()
                        val data = hashMapOf(
                            "firstAidId" to docRef.id,
                            "title" to titleState.text.trim()
                        )
                        docRef.set(data)
                            .addOnSuccessListener {
                                showSuccess = true
                                scope.launch {
                                    kotlinx.coroutines.delay(1000)
                                    showSuccess = false
                                    onBackClick()
                                }
                            }
                            .addOnFailureListener {
                                isSaving = false
                            }
                    }
                },
                enabled = !isSaving && titleState.text.isNotBlank() && !titleExists && !isValidating && !isFormatInvalid,
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
                Text(text = if (isSaving) "Saving..." else "Confirm", color = Color.White, fontFamily = cabin)
            }
        }

        // Success overlay
        if (showSuccess) {
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

