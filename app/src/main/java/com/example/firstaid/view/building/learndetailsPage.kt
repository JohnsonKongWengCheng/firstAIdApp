package com.example.firstaid.view.building

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstaid.R
import com.example.firstaid.view.components.BottomBar
import com.example.firstaid.view.components.BottomItem
import com.example.firstaid.view.components.TopBarWithBack
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import android.util.Log

private data class LearningContent(
    val id: String,
    val learningId: String,
    val firstAidId: String,
    val title: String,
    val content: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val stepNumber: Int = 1
)

@Composable
fun LearnDetailsPage(
    learningId: String,
    onBackClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var firstAidTitle by remember { mutableStateOf("") }
    var learningContent by remember { mutableStateOf<List<LearningContent>>(emptyList()) }
    var isCompleted by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    
    // Get current user ID from SharedPreferences
    val prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("userId", null)

    // Fetch Learning data and related content
    LaunchedEffect(learningId) {
        try {
            if (learningId.isBlank()) {
                errorMessage = "Invalid Learning ID: empty"
                isLoading = false
                return@LaunchedEffect
            }

            // Get Learning data
            db.collection("Learning")
                .whereEqualTo("learningId", learningId)
                .get()
                .addOnSuccessListener { learningDocs ->
                    if (learningDocs.documents.isNotEmpty()) {
                        val learningDoc = learningDocs.documents.first()
                        val firstAidId = learningDoc.getString("firstAidId") ?: ""

                        // Get First_Aid title
                        db.collection("First_Aid")
                            .whereEqualTo("firstAidId", firstAidId)
                            .get()
                            .addOnSuccessListener { firstAidDocs ->
                                if (firstAidDocs.documents.isNotEmpty()) {
                                    val firstAidDoc = firstAidDocs.documents.first()
                                    firstAidTitle = firstAidDoc.getString("title") ?: ""
                                }
                            }

                        // Get Content data for this learning
                        db.collection("Content")
                            .whereEqualTo("learningId", learningId)
                            .get()
                            .addOnSuccessListener { contentDocs ->
                                val contents = contentDocs.documents.mapNotNull { doc ->
                                    LearningContent(
                                        id = doc.id,
                                        learningId = doc.getString("learningId") ?: "",
                                        firstAidId = firstAidId,
                                        title = doc.getString("title") ?: "",
                                        content = doc.getString("content") ?: "",
                                        description = doc.getString("description"),
                                        imageUrl = doc.getString("imageUrl"),
                                        stepNumber = (doc.getLong("stepNumber")?.toInt()) ?: 1
                                    )
                                }.sortedBy { it.stepNumber }

                                learningContent = contents
                                
                                // Update learning progress to "Started" when user opens learning material (only if not already completed)
                                if (currentUserId != null) {
                                    db.collection("Learning_Progress")
                                        .whereEqualTo("userId", currentUserId)
                                        .whereEqualTo("learningId", learningId)
                                        .get()
                                        .addOnSuccessListener { progressDocs ->
                                            if (progressDocs.documents.isNotEmpty()) {
                                                val progressDoc = progressDocs.documents.first()
                                                val currentStatus = progressDoc.getString("status") ?: "Pending"
                                                
                                                // Check if already completed
                                                isCompleted = currentStatus == "Completed"
                                                
                                                // Only update to "Started" if not already "Completed"
                                                if (currentStatus != "Completed") {
                                                    progressDoc.reference.update("status", "Started")
                                                        .addOnSuccessListener {
                                                            android.util.Log.d("LearnDetails", "Learning progress updated to Started")
                                                        }
                                                        .addOnFailureListener { e ->
                                                            android.util.Log.e("LearnDetails", "Failed to update learning progress: ${e.localizedMessage}")
                                                        }
                                                } else {
                                                    android.util.Log.d("LearnDetails", "Learning material already completed, skipping status update")
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            android.util.Log.e("LearnDetails", "Failed to fetch learning progress: ${e.localizedMessage}")
                                        }
                                }
                                
                                isLoading = false
                            }
                            .addOnFailureListener { e ->
                                errorMessage = e.localizedMessage ?: "Failed to load content"
                                isLoading = false
                            }
                    } else {
                        errorMessage = "No learning data found"
                        isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage = e.localizedMessage ?: "Failed to load learning data"
                    isLoading = false
                }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to load data"
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with back button
            TopBarWithBack(
                title = firstAidTitle.ifEmpty { "Learn Details" },
                onBackClick = onBackClick
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green_primary))
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            } else if (learningContent.isNotEmpty()) {
                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 100.dp) // Space for bottom bar
                ) {
                    // Learning content items
                    learningContent.forEachIndexed { index, content ->
                        LearningContentCard(
                            content = content,
                            index = index + 1,
                            context = context,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                    
                    // Complete button (only show if not already completed)
                    if (learningContent.isNotEmpty() && !isCompleted) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                // Update learning progress to "Completed"
                                if (currentUserId != null) {
                                    db.collection("Learning_Progress")
                                        .whereEqualTo("userId", currentUserId)
                                        .whereEqualTo("learningId", learningId)
                                        .get()
                                        .addOnSuccessListener { progressDocs ->
                                            if (progressDocs.documents.isNotEmpty()) {
                                                val progressDoc = progressDocs.documents.first()
                                                progressDoc.reference.update("status", "Completed")
                                                    .addOnSuccessListener {
                                                        android.util.Log.d("LearnDetails", "Learning progress updated to Completed for learningId: $learningId")
                                                        isCompleted = true // Update local state
                                                        showSuccessMessage = true
                                                        // Show success message for 1 second, then navigate
                                                        coroutineScope.launch {
                                                            kotlinx.coroutines.delay(1000)
                                                            showSuccessMessage = false
                                                            onBackClick() // Navigate back to learn page
                                                        }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        android.util.Log.e("LearnDetails", "Failed to update learning progress: ${e.localizedMessage}")
                                                    }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            android.util.Log.e("LearnDetails", "Failed to fetch learning progress: ${e.localizedMessage}")
                                        }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(50.dp)
                                .shadow(4.dp, RoundedCornerShape(10.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.green_primary)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Mark as Complete",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Success message
                        if (showSuccessMessage) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.green_primary).copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Success",
                                        tint = colorResource(id = R.color.green_primary),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Learning Material Completed Successfully!",
                                        color = colorResource(id = R.color.green_primary),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = cabin
                                    )
                                }
                            }
                        }
                    } else if (learningContent.isNotEmpty() && isCompleted) {
                        // Show completion message if already completed
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.green_primary).copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = colorResource(id = R.color.green_primary),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Learning Material Completed",
                                    color = colorResource(id = R.color.green_primary),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No learning content available",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Bottom Bar
        BottomBar(
            selected = BottomItem.LEARN,
            onSelected = onSelectBottom,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun LearningContentCard(
    content: LearningContent,
    index: Int,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Step number and title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$index.",
                    color = Color(0xFF4DB648), // Green color
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = content.title,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content text
            Text(
                text = content.content,
                color = Color.Black,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            // Description if available
            content.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            // Image if available - display as complete image like firstaiddetailsPage
            if (!content.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val imageRequest = ImageRequest.Builder(context)
                        .data(content.imageUrl)
                        .crossfade(true)
                        .build()
                    
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "Step illustration",
                        modifier = Modifier
                            .width(266.dp)
                            .height(236.dp),
                        contentScale = ContentScale.Crop,
                        onError = { error ->
                            Log.e("LearnDetails", "Error loading image from '${content.imageUrl}': ${error.result.throwable.message}")
                        },
                        onSuccess = {
                            Log.d("LearnDetails", "Image loaded successfully from: ${content.imageUrl}")
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LearnDetailsPreview() {
    LearnDetailsPage(
        learningId = "sample_learning_id",
        onBackClick = {},
        onSelectBottom = {}
    )
}