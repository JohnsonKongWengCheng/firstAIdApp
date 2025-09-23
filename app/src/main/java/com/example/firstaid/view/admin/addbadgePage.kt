package com.example.firstaid.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.firstaid.view.components.TopBarWithBack
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class BadgeTopicItem(
    val firstAidId: String,
    val title: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBadgePage(
    onBackClick: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    
    // State variables
    var topics by remember { mutableStateOf<List<BadgeTopicItem>>(emptyList()) }
    var selectedTopic by remember { mutableStateOf<BadgeTopicItem?>(null) }
    var showTopicDropdown by remember { mutableStateOf(false) }
    var badgeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    
    // Load topics from Firebase
    LaunchedEffect(Unit) {
        firestore.collection("First_Aid")
            .get()
            .addOnSuccessListener { result ->
                topics = result.documents.mapNotNull { doc ->
                    BadgeTopicItem(
                        firstAidId = doc.getString("firstAidId") ?: doc.id,
                        title = doc.getString("title") ?: doc.id
                    )
                }
            }
    }
    
    // Check if all fields are filled
    val canConfirm = selectedTopic != null && 
                    badgeName.isNotBlank() && 
                    description.isNotBlank()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Top Bar
            TopBarWithBack(
                title = "Add Badge",
                onBackClick = onBackClick
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // First Aid Topic Title
                Text(
                    text = "First Aid Topic Title",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Topic Dropdown
                Box {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTopicDropdown = true },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECECEC)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedTopic?.title ?: "Choose the First Aid Topic Title",
                                fontSize = 16.sp,
                                color = if (selectedTopic != null) Color.Black else Color(0xFFAAAAAA)
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color.Black
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showTopicDropdown,
                        onDismissRequest = { showTopicDropdown = false }
                    ) {
                        topics.forEach { topic ->
                            DropdownMenuItem(
                                text = { Text(topic.title) },
                                onClick = {
                                    selectedTopic = topic
                                    showTopicDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Badge Name
                Text(
                    text = "Badge Name:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = badgeName,
                    onValueChange = { badgeName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter Badge Name..") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFECECEC),
                        unfocusedContainerColor = Color(0xFFECECEC),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                Text(
                    text = "Description:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter the description here..") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFECECEC),
                        unfocusedContainerColor = Color(0xFFECECEC),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(100.dp)) // Extra space for bottom button
            }
        }

        // Bottom Confirm Bar
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
                    if (canConfirm && selectedTopic != null) {
                        loading = true
                        
                        // Generate badge ID
                        val badgeId = "B${System.currentTimeMillis().toString().takeLast(4)}"
                        
                        // Create badge data
                        val badgeData = hashMapOf(
                            "badgeId" to badgeId,
                            "firstAidId" to selectedTopic!!.firstAidId,
                            "name" to badgeName.trim(),
                            "description" to description.trim()
                        )
                        
                        // Save to Firebase
                        firestore.collection("Badge")
                            .document(badgeId)
                            .set(badgeData)
                            .addOnSuccessListener {
                                loading = false
                                showSuccessDialog = true
                                scope.launch {
                                    kotlinx.coroutines.delay(1000)
                                    showSuccessDialog = false
                                    onBackClick()
                                }
                            }
                            .addOnFailureListener {
                                loading = false
                                // Handle error if needed
                            }
                    }
                },
                enabled = canConfirm && !loading,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4DB648),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (loading) "Saving..." else "Confirm", 
                    color = Color.White
                )
            }
        }

        // Loading indicator
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4DB648))
            }
        }

        // Success dialog
        if (showSuccessDialog) {
            Dialog(
                onDismissRequest = { },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Badge Added Successfully!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4DB648)
                        )
                    }
                }
            }
        }
    }
}