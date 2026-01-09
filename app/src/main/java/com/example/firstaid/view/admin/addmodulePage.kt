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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstaid.R
import com.example.firstaid.view.components.TopBarWithBack
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.imePadding
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import coil.compose.AsyncImage
import android.util.Log
import java.util.UUID
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton

private data class ModuleTopicItem(val id: String, val title: String)

private data class StepData(
    val id: Int,
    var title: TextFieldValue,
    var content: TextFieldValue,
    var description: TextFieldValue,
    var imageUri: Uri?
)

private fun createContentEntries(
    learningId: String,
    steps: List<StepData>,
    onComplete: (Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    var completedCount = 0
    val totalSteps = steps.size
    var hasError = false
    var uploadsCompleted = 0
    val imageUrls = mutableMapOf<Int, String?>()
    
    if (totalSteps == 0) {
        onComplete(true)
        return
    }
    
    // First, upload all images to Firebase Storage
    val stepsWithImages = steps.mapIndexedNotNull { index, step ->
        if (step.imageUri != null) {
            Pair(index, step.imageUri!!)
        } else {
            imageUrls[index] = null // Mark as no image
            null
        }
    }
    
    if (stepsWithImages.isEmpty()) {
        // No images to upload, proceed directly to create content entries
        createContentEntriesWithUrls(learningId, steps, imageUrls, db, onComplete)
        return
    }
    
    // Upload images to Firebase Storage
    stepsWithImages.forEach { (index, uri) ->
        val fileName = "content_${learningId}_step${index + 1}_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("content_images/$fileName")
        
        Log.d("AddModule", "Uploading image for step ${index + 1} to: $fileName")
        
        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    imageUrls[index] = imageUrl
                    uploadsCompleted++
                    Log.d("AddModule", "Image uploaded successfully for step ${index + 1}: $imageUrl")
                    
                    // Check if all uploads are complete
                    if (uploadsCompleted == stepsWithImages.size) {
                        // All images uploaded, now create content entries
                        createContentEntriesWithUrls(learningId, steps, imageUrls, db, onComplete)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AddModule", "Failed to get download URL for step ${index + 1}: ${e.message}")
                    hasError = true
                    imageUrls[index] = null // Continue without image
                    uploadsCompleted++
                    if (uploadsCompleted == stepsWithImages.size) {
                        createContentEntriesWithUrls(learningId, steps, imageUrls, db, onComplete)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AddModule", "Failed to upload image for step ${index + 1}: ${e.message}")
                hasError = true
                imageUrls[index] = null // Continue without image
                uploadsCompleted++
                if (uploadsCompleted == stepsWithImages.size) {
                    createContentEntriesWithUrls(learningId, steps, imageUrls, db, onComplete)
                }
            }
    }
}

private fun createContentEntriesWithUrls(
    learningId: String,
    steps: List<StepData>,
    imageUrls: Map<Int, String?>,
    db: FirebaseFirestore,
    onComplete: (Boolean) -> Unit
) {
    var completedCount = 0
    val totalSteps = steps.size
    var hasError = false
    
    steps.forEachIndexed { index, step ->
        val contentDocRef = db.collection("Content").document()
        val contentData = hashMapOf(
            "contentId" to contentDocRef.id,
            "learningId" to learningId,
            "title" to step.title.text.trim(),
            "content" to step.content.text.trim(),
            "description" to step.description.text.trim(),
            "stepNumber" to (index + 1),
            "imageUrl" to imageUrls[index] // Store Firebase Storage download URL
        )
        
        contentDocRef.set(contentData)
            .addOnSuccessListener {
                completedCount++
                Log.d("AddModule", "Content entry ${index + 1} created successfully")
                if (completedCount == totalSteps && !hasError) {
                    onComplete(true)
                }
            }
            .addOnFailureListener { e ->
                Log.e("AddModule", "Failed to create content entry ${index + 1}: ${e.message}")
                hasError = true
                onComplete(false)
            }
    }
}

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
    var noTopicsAvailable by remember { mutableStateOf(false) }
    var moduleDescription by remember { mutableStateOf(TextFieldValue("")) }

    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Dynamic steps management
    var steps by remember { mutableStateOf(listOf(StepData(1, TextFieldValue(""), TextFieldValue(""), TextFieldValue(""), null))) }
    var nextStepId by remember { mutableStateOf(2) }

    LaunchedEffect(Unit) {
        // First get all first aid topics
        db.collection("First_Aid").get()
            .addOnSuccessListener { firstAidResult ->
                // Then get all modules to see which topics already have modules
                db.collection("Learning")
                    .get()
                    .addOnSuccessListener { moduleResult ->
                        val topicsWithModules = moduleResult.documents.mapNotNull { moduleDoc ->
                            moduleDoc.getString("firstAidId")
                        }.toSet()
                        
                        // Filter out topics that already have modules
                        topics = firstAidResult.documents.mapNotNull { doc ->
                            val firstAidId = doc.getString("firstAidId") ?: doc.id
                            if (!topicsWithModules.contains(firstAidId)) {
                                ModuleTopicItem(id = firstAidId, title = doc.getString("title") ?: doc.id)
                            } else null
                        }.sortedBy { it.title }
                        
                        // Check if no topics are available
                        noTopicsAvailable = topics.isEmpty()
                        isLoading = false
                    }
                    .addOnFailureListener { exception ->
                        // If module collection fails, show all topics
                        topics = firstAidResult.documents.map { d ->
                            ModuleTopicItem(id = d.getString("firstAidId") ?: d.id, title = d.getString("title") ?: d.id)
                        }.sortedBy { it.title }
                        noTopicsAvailable = false
                        isLoading = false
                    }
            }
            .addOnFailureListener { exception ->
                // Handle error if needed
                topics = emptyList()
                noTopicsAvailable = true
                isLoading = false
            }
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
                            .background(if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC), RoundedCornerShape(10.dp))
                            .clickable { if (!noTopicsAvailable) expanded = true },
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
                                color = if (noTopicsAvailable) Color.Gray else if (selectedTopic == null) Color(0xFFAAAAAA) else Color.Black,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null, tint = if (noTopicsAvailable) Color.Gray else Color.Black)
                        }

                        DropdownMenu(expanded = expanded && !noTopicsAvailable, onDismissRequest = { expanded = false }) {
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
                                    text = "All first aid topics already have modules. You cannot add new modules at this time.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF856404),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (selectedTopic != null) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Module Description
                        Text(
                            text = "Module Description",
                            fontSize = 16.sp,
                            color = if (noTopicsAvailable) Color.Gray else Color.Black,
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val isDescriptionWhitespace =
                            moduleDescription.text.isNotEmpty() && moduleDescription.text.isBlank()
                        OutlinedTextField(
                            value = moduleDescription,
                            onValueChange = { newValue ->
                                if (!noTopicsAvailable) {
                                    moduleDescription = newValue
                                }
                            },
                            enabled = !noTopicsAvailable,
                            placeholder = { Text("Enter module description", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = !noTopicsAvailable && isDescriptionWhitespace,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = if (!noTopicsAvailable && isDescriptionWhitespace) Color.Red else Color.Transparent,
                                focusedBorderColor = if (!noTopicsAvailable && isDescriptionWhitespace) Color.Red else colorResource(
                                    id = R.color.green_primary
                                ).copy(alpha = 0.4f),
                                unfocusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC),
                                focusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFE6F3E6),
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Gray
                            )
                        )
                        if (!noTopicsAvailable && isDescriptionWhitespace) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Module Description should not be empty",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = cabin
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Steps
                        steps.forEachIndexed { index, step ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(color = Color(0xFFB8B8B8), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Step ${index + 1}",
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontFamily = cabin
                            )
                            
                            // Remove Step button (show if there are 2 or more steps)
                            if (steps.size > 1) {
                                Button(
                                    onClick = {
                                        if (!noTopicsAvailable) {
                                            steps = steps.filter { it.id != step.id }
                                        }
                                    },
                                    enabled = !noTopicsAvailable,
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red.copy(alpha = 0.8f)
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Remove",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = cabin
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(text = "Title:", fontSize = 16.sp, color = if (noTopicsAvailable) Color.Gray else Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        val titleWhitespace = step.title.text.isNotEmpty() && step.title.text.isBlank()
                        OutlinedTextField(
                            value = step.title,
                            onValueChange = { newValue ->
                                if (!noTopicsAvailable) {
                                    steps = steps.map { 
                                        if (it.id == step.id) it.copy(title = newValue) else it 
                                    }
                                }
                            },
                            enabled = !noTopicsAvailable,
                            placeholder = { Text("Enter step ${index + 1} title", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = !noTopicsAvailable && titleWhitespace,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = if (!noTopicsAvailable && titleWhitespace) Color.Red else Color.Transparent,
                                focusedBorderColor = if (!noTopicsAvailable && titleWhitespace) Color.Red else colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                errorBorderColor = Color.Red,
                                unfocusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC),
                                focusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFE6F3E6),
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Gray
                            )
                        )
                        if (!noTopicsAvailable && titleWhitespace) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Title should not be empty",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = cabin
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Content:", fontSize = 16.sp, color = if (noTopicsAvailable) Color.Gray else Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        val contentWhitespace = step.content.text.isNotEmpty() && step.content.text.isBlank()
                        OutlinedTextField(
                            value = step.content,
                            onValueChange = { newValue ->
                                if (!noTopicsAvailable) {
                                    steps = steps.map { 
                                        if (it.id == step.id) it.copy(content = newValue) else it 
                                    }
                                }
                            },
                            enabled = !noTopicsAvailable,
                            placeholder = { Text("Enter step ${index + 1} content", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = !noTopicsAvailable && contentWhitespace,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = if (!noTopicsAvailable && contentWhitespace) Color.Red else Color.Transparent,
                                focusedBorderColor = if (!noTopicsAvailable && contentWhitespace) Color.Red else colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                errorBorderColor = Color.Red,
                                unfocusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC),
                                focusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFE6F3E6),
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Gray
                            )
                        )
                        if (!noTopicsAvailable && contentWhitespace) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Content should not be empty",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = cabin
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Description:", fontSize = 16.sp, color = if (noTopicsAvailable) Color.Gray else Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        val descriptionWhitespace = step.description.text.isNotEmpty() && step.description.text.isBlank()
                        OutlinedTextField(
                            value = step.description,
                            onValueChange = { newValue ->
                                if (!noTopicsAvailable) {
                                    steps = steps.map { 
                                        if (it.id == step.id) it.copy(description = newValue) else it 
                                    }
                                }
                            },
                            enabled = !noTopicsAvailable,
                            placeholder = { Text("Enter the description here..", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = !noTopicsAvailable && descriptionWhitespace,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = if (!noTopicsAvailable && descriptionWhitespace) Color.Red else Color.Transparent,
                                focusedBorderColor = if (!noTopicsAvailable && descriptionWhitespace) Color.Red else colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                errorBorderColor = Color.Red,
                                unfocusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC),
                                focusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFE6F3E6),
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Gray
                            )
                        )
                        if (!noTopicsAvailable && descriptionWhitespace) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Description should not be empty",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = cabin
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Image:", fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                            steps = steps.map { 
                                if (it.id == step.id) it.copy(imageUri = uri) else it 
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color(0xFFECF0EC), RoundedCornerShape(10.dp))
                                .clickable { 
                                    imageLauncher.launch("image/*")
                                }
                        ) {
                            if (step.imageUri != null) {
                                AsyncImage(model = step.imageUri, contentDescription = null, modifier = Modifier.matchParentSize())
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                        .clickable {
                                            steps = steps.map { current ->
                                                if (current.id == step.id) current.copy(imageUri = null) else current
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                    Text(text = "+", color = Color(0xFF757575), fontSize = 24.sp)
                                }
                            }
                        }
                        }

                        // Add Step
                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = Color(0xFFB8B8B8), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (!noTopicsAvailable) {
                                    steps = steps + StepData(
                                        id = nextStepId,
                                        title = TextFieldValue(""),
                                        content = TextFieldValue(""),
                                        description = TextFieldValue(""),
                                        imageUri = null
                                    )
                                    nextStepId++
                                }
                            },
                            enabled = !noTopicsAvailable,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFE6F3E6),
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledContentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Add Step", color = colorResource(id = R.color.green_primary))
                        }

                        Spacer(modifier = Modifier.height(120.dp))
                    }
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
                    val firstStep = steps.firstOrNull()
                    if (!isSaving && topic != null && firstStep != null && 
                        firstStep.title.text.isNotBlank() && firstStep.content.text.isNotBlank() &&
                        moduleDescription.text.isNotBlank()) {
                        isSaving = true
                        val docRef = db.collection("Learning").document()
                        val learningId = docRef.id
                        val data = hashMapOf(
                            "learningId" to learningId,
                            "firstAidId" to topic.id,
                            "description" to moduleDescription.text.trim()
                        )
                        docRef.set(data)
                            .addOnSuccessListener {
                                // Create Content entries for each step
                                createContentEntries(learningId, steps) { success ->
                                    if (success) {
                                        // Create Learning_Progress for all existing users
                                        db.collection("User").get()
                                            .addOnSuccessListener { userDocs ->
                                                val batch = db.batch()
                                                userDocs.documents.forEach { userDoc ->
                                                    val userId = userDoc.getString("userId") ?: userDoc.id
                                                    val progressData = hashMapOf(
                                                        "userId" to userId,
                                                        "learningId" to learningId,
                                                        "status" to "Pending"
                                                    )
                                                    val progressRef = db.collection("Learning_Progress").document()
                                                    batch.set(progressRef, progressData)
                                                }
                                                
                                                batch.commit()
                                                    .addOnSuccessListener {
                                                        Log.d("AddModule", "Created learning progress for all users")
                                                        showSuccess = true
                                                        scope.launch {
                                                            kotlinx.coroutines.delay(1000)
                                                            showSuccess = false
                                                            onBackClick()
                                                        }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("AddModule", "Failed to create learning progress: ${e.message}")
                                                        // Still show success as module was created
                                                        showSuccess = true
                                                        scope.launch {
                                                            kotlinx.coroutines.delay(1000)
                                                            showSuccess = false
                                                            onBackClick()
                                                        }
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("AddModule", "Failed to load users: ${e.message}")
                                                // Still show success as module was created
                                                showSuccess = true
                                                scope.launch {
                                                    kotlinx.coroutines.delay(1000)
                                                    showSuccess = false
                                                    onBackClick()
                                                }
                                            }
                                    } else {
                                        isSaving = false
                                    }
                                }
                            }
                            .addOnFailureListener { 
                                isSaving = false 
                            }
                    }
                },
                enabled = !isSaving && selectedTopic != null && !noTopicsAvailable &&
                    moduleDescription.text.isNotBlank() &&
                    steps.all { step -> 
                        step.title.text.isNotBlank() && 
                        step.content.text.isNotBlank() && 
                        step.description.text.isNotBlank() 
                    },
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