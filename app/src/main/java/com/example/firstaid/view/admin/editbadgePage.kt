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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstaid.R
import com.example.firstaid.view.components.TopBarWithBack
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class EditBadgeTopicItem(
    val firstAidId: String,
    val title: String
)

data class BadgeData(
    val badgeId: String,
    val firstAidId: String,
    val name: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBadgePage(
    onBackClick: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    
    // State variables
    var topics by remember { mutableStateOf<List<EditBadgeTopicItem>>(emptyList()) }
    var selectedTopic by remember { mutableStateOf<EditBadgeTopicItem?>(null) }
    var showTopicDropdown by remember { mutableStateOf(false) }
    var badges by remember { mutableStateOf<List<BadgeData>>(emptyList()) }
    var selectedBadge by remember { mutableStateOf<BadgeData?>(null) }
    var badgeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var noTopicsAvailable by remember { mutableStateOf(false) }
    
    // Original values to track changes
    var originalBadgeName by remember { mutableStateOf("") }
    var originalDescription by remember { mutableStateOf("") }
    
    // Load topics that have badges from Firebase
    LaunchedEffect(Unit) {
        // First get all first aid topics
        firestore.collection("First_Aid")
            .get()
            .addOnSuccessListener { firstAidResult ->
                // Then get all badges to see which topics have badges
                firestore.collection("Badge")
                    .get()
                    .addOnSuccessListener { badgeResult ->
                        val topicsWithBadges = badgeResult.documents.mapNotNull { badgeDoc ->
                            badgeDoc.getString("firstAidId")
                        }.toSet()
                        
                        // Filter to only show topics that have badges
                        topics = firstAidResult.documents.mapNotNull { doc ->
                            val firstAidId = doc.getString("firstAidId") ?: doc.id
                            val title = doc.getString("title") ?: return@mapNotNull null
                            if (topicsWithBadges.contains(firstAidId)) {
                                EditBadgeTopicItem(firstAidId = firstAidId, title = title)
                            } else null
                        }
                        
                        // Check if no topics are available
                        noTopicsAvailable = topics.isEmpty()
                    }
                    .addOnFailureListener { exception ->
                        // If badge collection fails, show all topics
                        topics = firstAidResult.documents.mapNotNull { doc ->
                            EditBadgeTopicItem(
                                firstAidId = doc.getString("firstAidId") ?: doc.id,
                                title = doc.getString("title") ?: doc.id
                            )
                        }
                        noTopicsAvailable = false
                    }
            }
            .addOnFailureListener { exception ->
                // Handle error if needed
                topics = emptyList()
                noTopicsAvailable = true
            }
    }
    
    // Load badges when topic is selected
    LaunchedEffect(selectedTopic) {
        if (selectedTopic != null) {
            firestore.collection("Badge")
                .whereEqualTo("firstAidId", selectedTopic!!.firstAidId)
                .get()
                .addOnSuccessListener { result ->
                    badges = result.documents.mapNotNull { doc ->
                        BadgeData(
                            badgeId = doc.id,
                            firstAidId = doc.getString("firstAidId") ?: "",
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: ""
                        )
                    }
                    
                    // Auto-select the first badge if only one exists
                    if (badges.isNotEmpty()) {
                        selectedBadge = badges.first()
                        badgeName = selectedBadge!!.name
                        description = selectedBadge!!.description
                        originalBadgeName = selectedBadge!!.name
                        originalDescription = selectedBadge!!.description
                    } else {
                        selectedBadge = null
                        badgeName = ""
                        description = ""
                        originalBadgeName = ""
                        originalDescription = ""
                    }
                }
        } else {
            badges = emptyList()
            selectedBadge = null
            badgeName = ""
            description = ""
            originalBadgeName = ""
            originalDescription = ""
        }
    }
    
    // Check if changes have been made
    val hasChanges = badgeName != originalBadgeName || description != originalDescription
    
    // Check if all fields are filled and changes have been made
    val canConfirm = selectedTopic != null && 
                    selectedBadge != null &&
                    badgeName.isNotBlank() && 
                    description.isNotBlank() &&
                    hasChanges
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Top Bar
            TopBarWithBack(
                title = "Edit Badge",
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
                            .clickable { if (!noTopicsAvailable) showTopicDropdown = true },
                        colors = CardDefaults.cardColors(containerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECECEC)),
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
                                color = if (noTopicsAvailable) Color.Gray else if (selectedTopic != null) Color.Black else Color(0xFFAAAAAA)
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = if (noTopicsAvailable) Color.Gray else Color.Black
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showTopicDropdown && !noTopicsAvailable,
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

                // Show message if no topics are available
                if (noTopicsAvailable) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Topics Available",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF856404),
                                fontFamily = cabin
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No first aid topics have badges yet. You cannot edit badges at this time.",
                                fontSize = 14.sp,
                                color = Color(0xFF856404),
                                textAlign = TextAlign.Center
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
                    enabled = selectedTopic != null && !noTopicsAvailable,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (selectedTopic != null) Color(0xFFECECEC) else Color(0xFFF5F5F5),
                        unfocusedContainerColor = if (selectedTopic != null) Color(0xFFECECEC) else Color(0xFFF5F5F5),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledContainerColor = Color(0xFFF5F5F5),
                        disabledTextColor = Color.Gray
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
                    enabled = selectedTopic != null && !noTopicsAvailable,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (selectedTopic != null) Color(0xFFECECEC) else Color(0xFFF5F5F5),
                        unfocusedContainerColor = if (selectedTopic != null) Color(0xFFECECEC) else Color(0xFFF5F5F5),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledContainerColor = Color(0xFFF5F5F5),
                        disabledTextColor = Color.Gray
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
                    if (canConfirm && selectedBadge != null) {
                        loading = true
                        
                        // Update badge data
                        val badgeData = mapOf(
                            "name" to badgeName.trim(),
                            "description" to description.trim()
                        )
                        
                        // Save to Firebase
                        firestore.collection("Badge")
                            .document(selectedBadge!!.badgeId)
                            .update(badgeData)
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
                enabled = canConfirm && !loading && !noTopicsAvailable,
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

        // Success overlay
        if (showSuccessDialog) {
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
                            text = "Badge Updated Successfully!",
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

