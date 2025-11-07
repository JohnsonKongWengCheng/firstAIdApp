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
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import java.util.UUID
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton

private data class TopicRef(val id: String, val title: String)
private data class ModuleRef(val id: String, val title: String)

private fun uploadStepImages(
    learningId: String,
    steps: List<EditableStep>,
    onComplete: (Map<Int, String?>) -> Unit
) {
    val storage = FirebaseStorage.getInstance()
    val imageUrls = mutableMapOf<Int, String?>()
    var uploadsCompleted = 0
    
    // Find steps with new images to upload
    val stepsToUpload = steps.mapIndexedNotNull { index, step ->
        if (step.imageUri != null) {
            Pair(index, step.imageUri!!)
        } else {
            imageUrls[index] = step.imageUrl // Keep existing URL
            null
        }
    }
    
    if (stepsToUpload.isEmpty()) {
        // No images to upload, return existing URLs
        onComplete(imageUrls)
        return
    }
    
    // Upload new images to Firebase Storage
    stepsToUpload.forEach { (index, uri) ->
        val fileName = "content_${learningId}_step${index + 1}_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("content_images/$fileName")
        
        Log.d("EditModule", "Uploading image for step ${index + 1} to: $fileName")
        
        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    imageUrls[index] = imageUrl
                    uploadsCompleted++
                    Log.d("EditModule", "Image uploaded successfully for step ${index + 1}: $imageUrl")
                    
                    // Check if all uploads are complete
                    if (uploadsCompleted == stepsToUpload.size) {
                        onComplete(imageUrls)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditModule", "Failed to get download URL for step ${index + 1}: ${e.message}")
                    // Continue without image if upload fails
                    imageUrls[index] = steps[index].imageUrl // Keep existing
                    uploadsCompleted++
                    if (uploadsCompleted == stepsToUpload.size) {
                        onComplete(imageUrls)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditModule", "Failed to upload image for step ${index + 1}: ${e.message}")
                // Continue without image if upload fails
                imageUrls[index] = steps[index].imageUrl // Keep existing
                uploadsCompleted++
                if (uploadsCompleted == stepsToUpload.size) {
                    onComplete(imageUrls)
                }
            }
    }
}

private data class EditableStep(
    val id: String?,
    var title: TextFieldValue,
    var content: TextFieldValue,
    var description: TextFieldValue,
    var stepNumber: Int,
    var imageUrl: String? = null, // Current image URL from Firestore
    var imageUri: Uri? = null // New image URI to upload
)

@Composable
fun EditModulePage(
    onBackClick: () -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Dropdown data
    var topics by remember { mutableStateOf<List<TopicRef>>(emptyList()) }
    var modules by remember { mutableStateOf<List<ModuleRef>>(emptyList()) }
    var selectedTopic by remember { mutableStateOf<TopicRef?>(null) }
    var selectedModule by remember { mutableStateOf<ModuleRef?>(null) }
    var topicExpanded by remember { mutableStateOf(false) }
    var noTopicsAvailable by remember { mutableStateOf(false) }
    var moduleDescription by remember { mutableStateOf(TextFieldValue("")) }

    // Steps
    var steps by remember { mutableStateOf<List<EditableStep>>(emptyList()) }
    var originalSteps by remember { mutableStateOf<List<EditableStep>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Load topics that have modules
    LaunchedEffect(Unit) {
        // First get all first aid topics
        db.collection("First_Aid").get()
            .addOnSuccessListener { firstAidResult ->
                // Then get all modules to see which topics have modules
                db.collection("Learning")
                    .get()
                    .addOnSuccessListener { moduleResult ->
                        val topicsWithModules = moduleResult.documents.mapNotNull { moduleDoc ->
                            moduleDoc.getString("firstAidId")
                        }.toSet()
                        
                        // Filter to only show topics that have modules
                        topics = firstAidResult.documents.mapNotNull { doc ->
                            val firstAidId = doc.getString("firstAidId") ?: doc.id
                            val title = doc.getString("title") ?: return@mapNotNull null
                            if (topicsWithModules.contains(firstAidId)) {
                                TopicRef(id = firstAidId, title = title)
                            } else null
                        }.sortedBy { it.title }
                        
                        // Check if no topics are available
                        noTopicsAvailable = topics.isEmpty()
                        isLoading = false
                    }
                    .addOnFailureListener { exception ->
                        // If module collection fails, show all topics
                        topics = firstAidResult.documents.map { d ->
                            TopicRef(id = d.getString("firstAidId") ?: d.id, title = d.getString("title") ?: d.id)
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

    // Load modules when topic selected
    LaunchedEffect(selectedTopic) {
        selectedModule = null
        steps = emptyList()
        moduleDescription = TextFieldValue("")
        if (selectedTopic != null) {
            db.collection("Learning").whereEqualTo("firstAidId", selectedTopic!!.id).get()
                .addOnSuccessListener { qs ->
                    modules = qs.documents.map { d ->
                        // Learning only contains: learningId, firstAidId, description
                        val learningId = d.getString("learningId") ?: d.id
                        val description = d.getString("description") ?: learningId
                        ModuleRef(id = learningId, title = description)
                    }.sortedBy { it.title }
                    // Auto-select the only module (or the first one)
                    selectedModule = modules.firstOrNull()
                }
        }
    }

    // Load steps and description when module selected
    LaunchedEffect(selectedModule) {
        steps = emptyList()
        originalSteps = emptyList()
        moduleDescription = TextFieldValue("")
        val mod = selectedModule ?: return@LaunchedEffect
        
        // Load Learning document to get description
        db.collection("Learning").whereEqualTo("learningId", mod.id).get()
            .addOnSuccessListener { learningDocs ->
                if (learningDocs.documents.isNotEmpty()) {
                    val learningDoc = learningDocs.documents.first()
                    val description = learningDoc.getString("description") ?: ""
                    moduleDescription = TextFieldValue(description)
                }
            }
        
        // Load Content documents for steps
        db.collection("Content").whereEqualTo("learningId", mod.id).get()
            .addOnSuccessListener { qs ->
                val list = qs.documents.mapIndexed { index, d ->
                    EditableStep(
                        id = d.id,
                        title = TextFieldValue(d.getString("title") ?: ""),
                        content = TextFieldValue(d.getString("content") ?: ""),
                        description = TextFieldValue(d.getString("description") ?: ""),
                        stepNumber = (d.getLong("stepNumber")?.toInt()) ?: (index + 1),
                        imageUrl = d.getString("imageUrl") // Load existing image URL
                    )
                }.sortedBy { it.stepNumber }
                steps = list.ifEmpty { listOf(EditableStep(null, TextFieldValue(""), TextFieldValue(""), TextFieldValue(""), 1)) }
                originalSteps = list // Store original steps for comparison
            }
    }

    fun addEmptyStep() {
        val next = (steps.maxOfOrNull { it.stepNumber } ?: 0) + 1
        steps = steps + EditableStep(null, TextFieldValue(""), TextFieldValue(""), TextFieldValue(""), next, null)
    }

    fun removeStep(stepToRemove: EditableStep) {
        steps = steps.filter { it != stepToRemove }
    }

    // Validation function to check if all steps have valid data
    fun areAllStepsValid(): Boolean {
        return steps.isNotEmpty() && steps.all { step ->
            step.title.text.trim().isNotBlank() &&
            step.content.text.trim().isNotBlank() &&
            step.description.text.trim().isNotBlank()
        }
    }

    // Function to find steps that were removed (exist in original but not in current)
    fun getRemovedSteps(): List<EditableStep> {
        val currentStepIds = steps.mapNotNull { it.id }.toSet()
        return originalSteps.filter { it.id != null && !currentStepIds.contains(it.id) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarWithBack(title = "Edit Module", onBackClick = onBackClick)

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
                        .padding(bottom = 120.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Topic dropdown
                    Text(text = "First Aid Topic Title", fontSize = 16.sp, color = Color.Black, fontFamily = cabin)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .background(if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC), RoundedCornerShape(10.dp))
                            .clickable { if (!noTopicsAvailable) topicExpanded = true },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                        DropdownMenu(expanded = topicExpanded && !noTopicsAvailable, onDismissRequest = { topicExpanded = false }) {
                            topics.forEach { item ->
                                DropdownMenuItem(text = { Text(item.title) }, onClick = {
                                    selectedTopic = item
                                    topicExpanded = false
                                })
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
                                    text = "No first aid topics have modules yet. You cannot edit modules at this time.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF856404),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Module (auto-selected): show read-only field
                    if (selectedModule != null) {
                        // Module Description field
                        Text(text = "Module Description", fontSize = 16.sp, color = Color.Black, fontFamily = cabin)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = moduleDescription,
                            onValueChange = { moduleDescription = it },
                            placeholder = { Text("Enter module description", color = Color(0xFFAAAAAA)) },
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Steps editor (once module selected)
                    if (selectedModule != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Step ${index + 1}", fontSize = 16.sp, color = Color.Black, fontFamily = cabin)
                                
                                // Remove Step button (show if there are 2 or more steps)
                                if (steps.size > 1) {
                                    Button(
                                        onClick = { removeStep(step) },
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
                            Text(text = "Title:", fontSize = 16.sp, color = Color.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = step.title,
                                onValueChange = { steps = steps.toMutableList().apply { this[index] = this[index].copy(title = it) } },
                                placeholder = { Text("Enter step title", color = Color(0xFFAAAAAA)) },
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
                                value = step.content,
                                onValueChange = { steps = steps.toMutableList().apply { this[index] = this[index].copy(content = it) } },
                                placeholder = { Text("Enter step content", color = Color(0xFFAAAAAA)) },
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
                                value = step.description,
                                onValueChange = { steps = steps.toMutableList().apply { this[index] = this[index].copy(description = it) } },
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
                            val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                                if (uri != null) {
                                    steps = steps.toMutableList().apply {
                                        this[index] = this[index].copy(imageUri = uri)
                                    }
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
                                when {
                                    step.imageUri != null -> {
                                        // Show newly selected image (content URI)
                                        val imageRequest = ImageRequest.Builder(context)
                                            .data(step.imageUri)
                                            .build()
                                        AsyncImage(model = imageRequest, contentDescription = null, modifier = Modifier.matchParentSize())
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                .clickable {
                                                    steps = steps.toMutableList().apply {
                                                        this[index] = this[index].copy(imageUri = null, imageUrl = null)
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
                                    }
                                    !step.imageUrl.isNullOrBlank() -> {
                                        // Show existing image from Firestore (Firebase Storage URL)
                                        AsyncImage(model = step.imageUrl, contentDescription = null, modifier = Modifier.matchParentSize())
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                .clickable {
                                                    steps = steps.toMutableList().apply {
                                                        this[index] = this[index].copy(imageUri = null, imageUrl = null)
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
                                    }
                                    else -> {
                                        // Show placeholder
                                        Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                            Text(text = "+", color = Color(0xFF757575), fontSize = 24.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Divider(color = Color(0xFFB8B8B8), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        Button(
                            onClick = { addEmptyStep() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6F3E6)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Add Step", color = colorResource(id = R.color.green_primary))
                        }
                    }
                }
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
                    val module = selectedModule
                    if (!isSaving && module != null) {
                        isSaving = true
                        
                        // First, upload new images to Firebase Storage
                        uploadStepImages(module.id, steps) { imageUrls ->
                            // After uploads complete, save to Firestore
                            // First get Learning document reference, then proceed with batch
                            db.collection("Learning").whereEqualTo("learningId", module.id).limit(1).get()
                                .addOnSuccessListener { learningDocs ->
                                    val batch = db.batch()
                                    
                                    // Update Learning document with new description
                                    if (learningDocs.documents.isNotEmpty()) {
                                        val learningDocRef = learningDocs.documents.first().reference
                                        batch.update(learningDocRef, "description", moduleDescription.text.trim())
                                    }
                                    
                                    // Delete removed steps
                                    val removedSteps = getRemovedSteps()
                                    removedSteps.forEach { removedStep ->
                                        if (removedStep.id != null) {
                                            batch.delete(db.collection("Content").document(removedStep.id))
                                        }
                                    }
                                    
                                    // Save/update current steps
                                    steps.forEachIndexed { idx, s ->
                                        val docRef = if (s.id != null) db.collection("Content").document(s.id) else db.collection("Content").document()
                                        
                                        // Use new image URL if uploaded, otherwise keep existing
                                        val finalImageUrl = imageUrls[idx] ?: s.imageUrl
                                        
                                        val data = hashMapOf(
                                            "learningId" to module.id,
                                            "title" to s.title.text.trim(),
                                            "content" to s.content.text.trim(),
                                            "description" to s.description.text.trim(),
                                            "stepNumber" to (idx + 1),
                                            "imageUrl" to finalImageUrl
                                        )
                                        batch.set(docRef, data, com.google.firebase.firestore.SetOptions.merge())
                                    }
                                    
                                    batch.commit()
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
                                            Log.e("EditModule", "Failed to save content: ${it.message}")
                                        }
                                }
                                .addOnFailureListener { 
                                    isSaving = false
                                    Log.e("EditModule", "Failed to load Learning document: ${it.message}")
                                }
                        }
                    }
                },
                enabled = !isSaving && selectedModule != null && !noTopicsAvailable && 
                    moduleDescription.text.isNotBlank() && areAllStepsValid(),
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
                            text = "Module Updated Successfully!",
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