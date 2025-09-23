package com.example.firstaid.view.building

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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


@Composable
fun LearnPage(
    onBackClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {},
    onTopicClick: (String) -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    
    var selectedTab by remember { mutableStateOf("Learn") }
    var learnTopics by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var firstAidTitles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var learningProgress by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var learningLoaded by remember { mutableStateOf(false) }
    var titlesLoaded by remember { mutableStateOf(false) }
    var progressLoaded by remember { mutableStateOf(false) }
    
    val db = FirebaseFirestore.getInstance()
    
    // Get current user ID from SharedPreferences
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("userId", null)

    // Function to refresh learning progress
    fun refreshLearningProgress() {
        if (currentUserId != null) {
            android.util.Log.d("LearnPage", "Refreshing learning progress for userId: $currentUserId")
            db.collection("Learning_Progress")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener { progressDocs ->
                    android.util.Log.d("LearnPage", "Refreshed - Found ${progressDocs.documents.size} progress documents")
                    val progressMap = progressDocs.documents.associate { doc ->
                        val learningId = doc.getString("learningId") ?: ""
                        val status = doc.getString("status") ?: "Pending"
                        android.util.Log.d("LearnPage", "Refreshed - learningId: $learningId, status: $status")
                        learningId to status
                    }
                    learningProgress = progressMap
                    android.util.Log.d("LearnPage", "Refreshed learning progress map: $learningProgress")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("LearnPage", "Failed to refresh learning progress: ${e.localizedMessage}")
                }
        }
    }
    
    // Fetch First_Aid titles, Learning data, and Learning Progress from Firebase
    LaunchedEffect(Unit) {
        try {
            // Fetch First_Aid titles
            db.collection("First_Aid")
                .get()
                .addOnSuccessListener { documents ->
                    val map = documents.associate { doc ->
                        val id = doc.getString("firstAidId") ?: doc.id
                        val title = doc.getString("title") ?: ""
                        id to title
                    }
                    firstAidTitles = map
                    titlesLoaded = true
                    if (learningLoaded && progressLoaded) isLoading = false
                }
                .addOnFailureListener { e ->
                    errorMessage = e.localizedMessage ?: "Failed to load first aid titles"
                    isLoading = false
                }

            // Fetch Learning data
            db.collection("Learning")
                .get()
                .addOnSuccessListener { documents ->
                    val learnings = documents.mapNotNull { doc ->
                        mapOf(
                            "learningId" to (doc.getString("learningId") ?: doc.id),
                            "firstAidId" to (doc.getString("firstAidId") ?: ""),
                            "isCompleted" to false
                        )
                    }
                    learnTopics = learnings
                    learningLoaded = true
                    if (titlesLoaded && progressLoaded) isLoading = false
                }
                .addOnFailureListener { e ->
                    errorMessage = e.localizedMessage ?: "Failed to load learning topics"
                    isLoading = false
                }

            // Fetch Learning Progress for current user
            if (currentUserId != null) {
                android.util.Log.d("LearnPage", "Fetching learning progress for userId: $currentUserId")
                db.collection("Learning_Progress")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener { progressDocs ->
                        android.util.Log.d("LearnPage", "Found ${progressDocs.documents.size} progress documents")
                        val progressMap = progressDocs.documents.associate { doc ->
                            val learningId = doc.getString("learningId") ?: ""
                            val status = doc.getString("status") ?: "Pending"
                            android.util.Log.d("LearnPage", "Progress - learningId: $learningId, status: $status")
                            learningId to status
                        }
                        learningProgress = progressMap
                        android.util.Log.d("LearnPage", "Learning progress map: $learningProgress")
                        progressLoaded = true
                        if (titlesLoaded && learningLoaded) isLoading = false
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("LearnPage", "Failed to load learning progress: ${e.localizedMessage}")
                        progressLoaded = true
                        if (titlesLoaded && learningLoaded) isLoading = false
                    }
            } else {
                android.util.Log.w("LearnPage", "No current user ID found")
                progressLoaded = true
                if (titlesLoaded && learningLoaded) isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to load data"
            isLoading = false
        }
    }
    
    // Refresh learning progress when page becomes visible again
    DisposableEffect(Unit) {
        onDispose {
            // This will be called when the composable is disposed
        }
    }
    
    // Add a LaunchedEffect to refresh progress when the page is recomposed
    LaunchedEffect(Unit) {
        // Refresh progress data
        refreshLearningProgress()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopBarWithBack(
                title = "Learn",
                onBackClick = onBackClick
            )
            
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(29.dp))
                
                // Title
                Text(
                    text = "First Aid Building",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.green_primary),
                    fontFamily = cabin,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(62.dp))
                
                // Divider line
                Divider(
                    color = Color(0xFFB8B8B8),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(18.dp))
                
                // Tab Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Learn Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Learn",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == "Learn") colorResource(id = R.color.green_primary) else Color(0xFFAAAAAA),
                            fontFamily = cabin
                        )
                        if (selectedTab == "Learn") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(3.dp)
                                    .background(colorResource(id = R.color.green_primary), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                    
                    // Exam Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Exam",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == "Exam") colorResource(id = R.color.green_primary) else Color(0xFFAAAAAA),
                            fontFamily = cabin
                        )
                        if (selectedTab == "Exam") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(3.dp)
                                    .background(colorResource(id = R.color.green_primary), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(35.dp))
                
                // Content based on selected tab
                if (selectedTab == "Learn") {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colorResource(id = R.color.green_primary)
                            )
                        }
                    } else if (errorMessage != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage ?: "An error occurred",
                                color = Color.Red,
                                fontFamily = cabin
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(learnTopics) { learning ->
                                val firstAidId = learning["firstAidId"] as? String ?: ""
                                val learningId = learning["learningId"] as? String ?: ""
                                val title = firstAidTitles[firstAidId] ?: firstAidId
                                val status = learningProgress[learningId] ?: "Pending"
                                val isCompleted = status == "Completed"
                                android.util.Log.d("LearnPage", "Rendering card - learningId: $learningId, status: $status, isCompleted: $isCompleted")
                                LearnTopicCard(
                                    title = title,
                                    isCompleted = isCompleted,
                                    onClick = { onTopicClick(learningId) },
                                    fontFamily = cabin
                                )
                            }
                        }
                    }
                } else {
                    // Exam content - placeholder for now
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Exam content coming soon",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontFamily = cabin
                        )
                    }
                }
            }
        }
        
        // Bottom Bar - positioned at bottom
        BottomBar(
            selected = BottomItem.LEARN,
            onSelected = onSelectBottom,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun LearnTopicCard(
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
    fontFamily: FontFamily
) {
    android.util.Log.d("LearnTopicCard", "Rendering card - title: $title, isCompleted: $isCompleted")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp)
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, colorResource(id = R.color.green_primary))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = colorResource(id = R.color.green_primary),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily
            )
            
            if (isCompleted) {
                android.util.Log.d("LearnTopicCard", "Showing check icon for: $title")
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = colorResource(id = R.color.green_primary),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                android.util.Log.d("LearnTopicCard", "Not showing check icon for: $title (isCompleted: $isCompleted)")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LearnPagePreview() {
    LearnPage()
}
