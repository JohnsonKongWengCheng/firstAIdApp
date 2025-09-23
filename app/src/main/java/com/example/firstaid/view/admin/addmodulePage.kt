package com.example.firstaid.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.firstaid.view.components.TopBarWithBack
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.imePadding
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import coil.compose.AsyncImage

private data class ModuleTopicItem(val id: String, val title: String)

@Composable
fun AddModulePage(
    onBackClick: () -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var topics by remember { mutableStateOf<List<ModuleTopicItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var selectedTopic by remember { mutableStateOf<ModuleTopicItem?>(null) }

    var title by remember { mutableStateOf(TextFieldValue("")) }
    var content by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Step 2 states (appear after step 1 complete)
    val isStep1Complete by remember {
        derivedStateOf { selectedTopic != null && title.text.isNotBlank() && content.text.isNotBlank() && description.text.isNotBlank() }
    }
    var title2 by remember { mutableStateOf(TextFieldValue("")) }
    var content2 by remember { mutableStateOf(TextFieldValue("")) }
    var description2 by remember { mutableStateOf(TextFieldValue("")) }
    var selectedImageUri2 by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher2 = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri2 = uri
    }

    LaunchedEffect(Unit) {
        db.collection("First_Aid").get()
            .addOnSuccessListener { qs ->
                topics = qs.documents.map { d ->
                    ModuleTopicItem(id = d.getString("firstAidId") ?: d.id, title = d.getString("title") ?: d.id)
                }.sortedBy { it.title }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarWithBack(title = "Add Module", onBackClick = onBackClick)

            Divider(color = Color(0xFFB8B8B8), thickness = 1.dp, modifier = Modifier.padding(horizontal = 11.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green_primary))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 100.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Topic dropdown
                    Text(
                        text = "First Aid Topic Title",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontFamily = cabin
                    )
                    Spacer(modifier = Modifier.height(8.dp))

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
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.Black)
                        }

                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title
                    Text(text = "First Step", fontSize = 16.sp, color = Color.Black, fontFamily = cabin)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Title:", fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Enter first step title", color = Color(0xFFAAAAAA)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                            unfocusedContainerColor = Color(0xFFECF0EC),
                            focusedContainerColor = Color(0xFFE6F3E6)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Content:", fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("Enter first step content", color = Color(0xFFAAAAAA)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                            unfocusedContainerColor = Color(0xFFECF0EC),
                            focusedContainerColor = Color(0xFFE6F3E6)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Description:", fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Enter the description here..", color = Color(0xFFAAAAAA)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                            unfocusedContainerColor = Color(0xFFECF0EC),
                            focusedContainerColor = Color(0xFFE6F3E6)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    // Image picker placeholder box (not implemented here)
                    Text(text = "Image:", fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFFECF0EC), RoundedCornerShape(10.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(model = selectedImageUri, contentDescription = null)
                        } else {
                            Text(text = "+", color = Color(0xFF757575), fontSize = 24.sp)
                        }
                    }

                    // Step 2 (appears when step 1 complete)
                    if (isStep1Complete) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = Color(0xFFB8B8B8), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Second Step", fontSize = 16.sp, color = Color.Black, fontFamily = cabin)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Title:", fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = title2,
                            onValueChange = { title2 = it },
                            placeholder = { Text("Enter second step title", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                unfocusedContainerColor = Color(0xFFECF0EC),
                                focusedContainerColor = Color(0xFFE6F3E6)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Content:", fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = content2,
                            onValueChange = { content2 = it },
                            placeholder = { Text("Enter second step content", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                unfocusedContainerColor = Color(0xFFECF0EC),
                                focusedContainerColor = Color(0xFFE6F3E6)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Description:", fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description2,
                            onValueChange = { description2 = it },
                            placeholder = { Text("Enter the description here..", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                unfocusedContainerColor = Color(0xFFECF0EC),
                                focusedContainerColor = Color(0xFFE6F3E6)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Image:", fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color(0xFFECF0EC), RoundedCornerShape(10.dp))
                                .clickable { imagePickerLauncher2.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri2 != null) {
                                AsyncImage(model = selectedImageUri2, contentDescription = null)
                            } else {
                                Text(text = "+", color = Color(0xFF757575), fontSize = 24.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        // Bottom Confirm
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .imePadding()
                .padding(bottom = 16.dp)
        ) {
            Divider(color = Color(0xFFB8B8B8), thickness = 1.dp, modifier = Modifier.padding(horizontal = 11.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val topic = selectedTopic
                    if (!isSaving && topic != null && title.text.isNotBlank() && content.text.isNotBlank()) {
                        isSaving = true
                        val docRef = db.collection("Learning").document()
                        val data = hashMapOf(
                            "learningId" to docRef.id,
                            "firstAidId" to topic.id,
                            "title" to title.text.trim(),
                            "content" to content.text.trim(),
                            "description" to description.text.trim()
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
                            .addOnFailureListener { isSaving = false }
                    }
                },
                enabled = !isSaving && selectedTopic != null && title.text.isNotBlank() && content.text.isNotBlank(),
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
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Success",
                            tint = colorResource(id = R.color.green_primary),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Module Added Successfully!",
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