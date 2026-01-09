package com.example.firstaid.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
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

private data class TopicItem(val id: String, val title: String)

@Composable
fun EditTopicPage(
    onBackClick: () -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val db = remember { FirebaseFirestore.getInstance() }
    var topics by remember { mutableStateOf<List<TopicItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTopic by remember { mutableStateOf<TopicItem?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf(TextFieldValue("")) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var isFormatInvalid by remember { mutableStateOf(false) }
    var isSameTitle by remember { mutableStateOf(false) }
    var isWhitespaceOnly by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Validate new title format and difference from current title
    LaunchedEffect(newTitle.text, selectedTopic) {
        val rawTitle = newTitle.text
        val title = rawTitle.trim()
        val currentTopic = selectedTopic
        
        // First check for whitespace-only input (even if trimmed is empty)
        if (rawTitle.isNotEmpty() && rawTitle.isBlank()) {
            isWhitespaceOnly = true
            isFormatInvalid = false
            isSameTitle = false
            validationError = "First Aid Topic Title should not be empty"
            return@LaunchedEffect
        }
        
        // Reset whitespace flag if not whitespace-only
        isWhitespaceOnly = false
        
        if (title.isNotEmpty() && currentTopic != null) {
            isFormatInvalid = false
            isSameTitle = false
            validationError = ""

            // Format validation: allow only letters and spaces
            val isOnlyLettersAndSpaces = title.all { it.isLetter() || it.isWhitespace() }
            if (!isOnlyLettersAndSpaces) {
                isFormatInvalid = true
                validationError = "Only letters and spaces are allowed"
                return@LaunchedEffect
            }

            // Check if new title is exactly the same as current title (case-sensitive)
            // This allows capitalization changes but prevents exact duplicates
            val isExactSame = title == currentTopic.title
            if (isExactSame) {
                isSameTitle = true
                validationError = "New title must be different from current title"
            }
        } else {
            isFormatInvalid = false
            isSameTitle = false
            validationError = ""
        }
    }

    LaunchedEffect(Unit) {
        db.collection("First_Aid").get()
            .addOnSuccessListener { qs ->
                topics = qs.documents.map { d ->
                    TopicItem(id = d.getString("firstAidId") ?: d.id, title = d.getString("title") ?: d.id)
                }.sortedBy { it.title }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarWithBack(title = "Edit Topic", onBackClick = onBackClick)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green_primary))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 100.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Dropdown label
                    Text(
                        text = "First Aid Topic Title",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontFamily = cabin
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom dropdown styled as a rounded field with placeholder and chevron
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .background(Color(0xFFECF0EC), RoundedCornerShape(10.dp))
                            .clickable { expanded = true },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedTopic?.title ?: "Choose the First Aid Topic Title",
                                color = if (selectedTopic == null) Color(0xFFAAAAAA) else Color.Black,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.Black
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            topics.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.title) },
                                    onClick = {
                                        selectedTopic = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedTopic != null) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "New First Aid Topic Title",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val whitespaceError = isWhitespaceOnly
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("Enter First Aid Topic Title here..", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = if (isFormatInvalid || isSameTitle || whitespaceError) Color.Red else Color.Transparent,
                                focusedBorderColor = if (isFormatInvalid || isSameTitle || whitespaceError) Color.Red else colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                errorBorderColor = Color.Red,
                                unfocusedContainerColor = Color(0xFFECF0EC),
                                focusedContainerColor = Color(0xFFE6F3E6)
                            ),
                            isError = isFormatInvalid || isSameTitle || whitespaceError
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
                    }
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
                    val sel = selectedTopic
                    if (!isSaving && sel != null && newTitle.text.isNotBlank()) {
                        isSaving = true
                        db.collection("First_Aid")
                            .whereEqualTo("firstAidId", sel.id)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { qs ->
                                val doc = qs.documents.firstOrNull()
                                if (doc != null) {
                                    doc.reference.update("title", newTitle.text.trim())
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
                                } else {
                                    isSaving = false
                                }
                            }
                            .addOnFailureListener {
                                isSaving = false
                            }
                    }
                },
                enabled = !isSaving && selectedTopic != null && newTitle.text.isNotBlank() && !isFormatInvalid && !isSameTitle && !isWhitespaceOnly,
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
                            text = "Topic Updated Successfully!",
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