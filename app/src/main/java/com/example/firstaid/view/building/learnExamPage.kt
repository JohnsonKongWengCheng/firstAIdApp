package com.example.firstaid.view.building

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.firstaid.view.components.TopBar
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun LearnExamPage(
    onSelectBottom: (BottomItem) -> Unit = {},
    onLearnTopicClick: (String) -> Unit = {},
    onExamTopicClick: (String) -> Unit = {}
) {
    var refreshTrigger by remember { mutableStateOf(0) }
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    
    var selectedTab by remember { mutableStateOf("Learn") }
    var learnTopics by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var examTopics by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var firstAidTitles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var learningProgress by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var examProgress by remember { mutableStateOf<Map<String, Map<String, Any>>>(emptyMap()) }
    var learnLoaded by remember { mutableStateOf(false) }
    var examLoaded by remember { mutableStateOf(false) }
    var titlesLoaded by remember { mutableStateOf(false) }
    var progressLoaded by remember { mutableStateOf(false) }
    var examProgressLoaded by remember { mutableStateOf(false) }
    var showUnavailableMessage by remember { mutableStateOf(false) }
    var unavailableExamTitle by remember { mutableStateOf("") }
    
    val db = FirebaseFirestore.getInstance()
    
    // Get current user ID from SharedPreferences
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("userId", null)
    
    android.util.Log.d("LearnExamPage", "Current userId from SharedPreferences: $currentUserId")

    // Fetch First_Aid titles, Learning and Exam data from Firebase
    LaunchedEffect(Unit) {
        try {
            // First_Aid titles
            db.collection("First_Aid")
                .get()
                .addOnSuccessListener { docs ->
                    val map = docs.associate { d ->
                        val id = d.getString("firstAidId") ?: d.id
                        val title = d.getString("title") ?: ""
                        id to title
                    }
                    firstAidTitles = map
                    titlesLoaded = true
                    if (learnLoaded && examLoaded) isLoading = false
                }
                .addOnFailureListener { e ->
                    errorMessage = e.localizedMessage ?: "Failed to load first aid titles"
                    isLoading = false
                }

            // Learning
            db.collection("Learning")
                .get()
                .addOnSuccessListener { learningDocs ->
                    val learnings = learningDocs.mapNotNull { doc ->
                        mapOf(
                            "learningId" to (doc.getString("learningId") ?: doc.id),
                            "firstAidId" to (doc.getString("firstAidId") ?: ""),
                            "isCompleted" to false
                        )
                    }
                    learnTopics = learnings
                    learnLoaded = true
                    if (titlesLoaded && examLoaded) isLoading = false
                }
                .addOnFailureListener { e ->
                    errorMessage = e.localizedMessage ?: "Failed to load learning topics"
                    isLoading = false
                }

            // Exam
            db.collection("Exam")
                .get()
                .addOnSuccessListener { examDocs ->
                    val allExams = examDocs.mapNotNull { doc ->
                        mapOf(
                            "examId" to (doc.getString("examId") ?: doc.id),
                            "description" to (doc.getString("description") ?: ""),
                            "firstAidId" to (doc.getString("firstAidId") ?: "")
                        )
                    }
                    
                    // Filter exams that have questions
                    if (allExams.isNotEmpty()) {
                        val examIds = allExams.map { it["examId"] as String }
                        android.util.Log.d("LearnExamPage", "Checking questions for ${examIds.size} exams: $examIds")
                        db.collection("Question")
                            .whereIn("examId", examIds)
                            .get()
                            .addOnSuccessListener { questionDocs ->
                                val examsWithQuestions = questionDocs.documents.mapNotNull { doc ->
                                    doc.getString("examId")
                                }.toSet()
                                android.util.Log.d("LearnExamPage", "Found questions for exams: $examsWithQuestions")
                                
                                examTopics = allExams.filter { exam ->
                                    val hasQuestions = examsWithQuestions.contains(exam["examId"])
                                    android.util.Log.d("LearnExamPage", "Exam ${exam["examId"]} has questions: $hasQuestions")
                                    hasQuestions
                                }
                                android.util.Log.d("LearnExamPage", "Filtered to ${examTopics.size} exams with questions")
                                examLoaded = true
                                if (titlesLoaded && learnLoaded) isLoading = false
                            }
                            .addOnFailureListener { e ->
                                // If question query fails, show all exams (fallback)
                                examTopics = allExams
                                examLoaded = true
                                if (titlesLoaded && learnLoaded) isLoading = false
                            }
                    } else {
                        examTopics = allExams
                        examLoaded = true
                        if (titlesLoaded && learnLoaded) isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage = e.localizedMessage ?: "Failed to load exams"
                    isLoading = false
                }

            // Fetch Learning Progress for current user
            if (currentUserId != null) {
                android.util.Log.d("LearnExamPage", "Fetching learning progress for userId: $currentUserId")
                db.collection("Learning_Progress")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener { progressDocs ->
                        android.util.Log.d("LearnExamPage", "Found ${progressDocs.documents.size} progress documents")
                        val progressMap = progressDocs.documents.associate { doc ->
                            val learningId = doc.getString("learningId") ?: ""
                            val status = doc.getString("status") ?: "Pending"
                            android.util.Log.d("LearnExamPage", "Progress - learningId: $learningId, status: $status")
                            learningId to status
                        }
                        learningProgress = progressMap
                        android.util.Log.d("LearnExamPage", "Learning progress map: $learningProgress")
                        progressLoaded = true
                        if (titlesLoaded && learnLoaded && examLoaded) isLoading = false
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("LearnExamPage", "Failed to load learning progress: ${e.localizedMessage}")
                        progressLoaded = true
                        if (titlesLoaded && learnLoaded && examLoaded) isLoading = false
                    }
            } else {
                android.util.Log.w("LearnExamPage", "No current user ID found")
                progressLoaded = true
                if (titlesLoaded && learnLoaded && examLoaded) isLoading = false
            }

            // Fetch Exam Progress for current user
            if (currentUserId != null) {
                android.util.Log.d("LearnExamPage", "Fetching exam progress for userId: $currentUserId")
                db.collection("Exam_Progress")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener { progressDocs ->
                        android.util.Log.d("LearnExamPage", "Found ${progressDocs.documents.size} exam progress documents")
                        val progressMap = progressDocs.documents.associate { doc ->
                            val examId = doc.getString("examId") ?: ""
                            val status = doc.getString("status") ?: "Pending"
                            val score = doc.getLong("score")?.toInt() ?: 0
                            examId to mapOf(
                                "status" to status,
                                "score" to score
                            )
                        }
                        examProgress = progressMap
                        android.util.Log.d("LearnExamPage", "Exam progress map: $examProgress")
                        examProgressLoaded = true
                        if (titlesLoaded && learnLoaded && examLoaded && progressLoaded) isLoading = false
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("LearnExamPage", "Failed to load exam progress: ${e.localizedMessage}")
                        examProgressLoaded = true
                        if (titlesLoaded && learnLoaded && examLoaded && progressLoaded) isLoading = false
                    }
            } else {
                android.util.Log.w("LearnExamPage", "No current user ID found for exam progress")
                examProgressLoaded = true
                if (titlesLoaded && learnLoaded && examLoaded && progressLoaded) isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to load data"
            isLoading = false
        }
    }
    
    // Refresh learning progress when page becomes visible again
    LaunchedEffect(refreshTrigger) {
        if (currentUserId != null && refreshTrigger > 0) {
            android.util.Log.d("LearnExamPage", "Refreshing learning progress for userId: $currentUserId")
            db.collection("Learning_Progress")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener { progressDocs ->
                    android.util.Log.d("LearnExamPage", "Refreshed - Found ${progressDocs.documents.size} progress documents")
                    val progressMap = progressDocs.documents.associate { doc ->
                        val learningId = doc.getString("learningId") ?: ""
                        val status = doc.getString("status") ?: "Pending"
                        android.util.Log.d("LearnExamPage", "Refreshed - learningId: $learningId, status: $status")
                        learningId to status
                    }
                    learningProgress = progressMap
                    android.util.Log.d("LearnExamPage", "Refreshed learning progress map: $learningProgress")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("LearnExamPage", "Failed to refresh learning progress: ${e.localizedMessage}")
                }
            
            // Also refresh exam progress
            android.util.Log.d("LearnExamPage", "Refreshing exam progress for userId: $currentUserId")
            db.collection("Exam_Progress")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener { progressDocs ->
                    android.util.Log.d("LearnExamPage", "Refreshed - Found ${progressDocs.documents.size} exam progress documents")
                    val progressMap = progressDocs.documents.associate { doc ->
                        val examId = doc.getString("examId") ?: ""
                        val status = doc.getString("status") ?: "Pending"
                        val score = doc.getLong("score")?.toInt() ?: 0
                        android.util.Log.d("LearnExamPage", "Refreshed - examId: $examId, status: $status, score: $score")
                        examId to mapOf(
                            "status" to status,
                            "score" to score
                        )
                    }
                    examProgress = progressMap
                    android.util.Log.d("LearnExamPage", "Refreshed exam progress map: $examProgress")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("LearnExamPage", "Failed to refresh exam progress: ${e.localizedMessage}")
                }
        }
    }
    
    // Add a simple refresh mechanism using DisposableEffect
    DisposableEffect(Unit) {
        onDispose {
            // This will be called when the composable is disposed
        }
    }
    
    // Add a LaunchedEffect to refresh progress data periodically
    LaunchedEffect(Unit) {
        // Refresh progress data when the page is first loaded
        refreshTrigger++
    }
    
    // Add a LaunchedEffect that triggers when the page becomes visible
    LaunchedEffect(Unit) {
        // This will trigger a refresh when the composable is recomposed
        refreshTrigger++
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        TopBar()
        
        // Main Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(29.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "First Aid Building",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF4DB648) // Green color from Figma
                    )
                }

                // Divider line
                Divider(
                    color = Color(0xFFB8B8B8), // Gray color from Figma
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 11.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                
                // Tab Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Learn Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedTab = "Learn" }
                    ) {
                        Text(
                            text = "Learn",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == "Learn") colorResource(id = R.color.green_primary) else Color(0xFFAAAAAA),
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(3.dp)
                                .background(
                                    if (selectedTab == "Learn") colorResource(id = R.color.green_primary) else Color.Transparent,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                    
                    // Exam Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedTab = "Exam" }
                    ) {
                        Text(
                            text = "Exam",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == "Exam") colorResource(id = R.color.green_primary) else Color(0xFFAAAAAA),
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(3.dp)
                                .background(
                                    if (selectedTab == "Exam") colorResource(id = R.color.green_primary) else Color.Transparent,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }

                Divider(
                    color = Color(0xFFB8B8B8), // Gray color from Figma
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )
                
                Spacer(modifier = Modifier.height(35.dp))
                
                // Content based on selected tab
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
                } else if (selectedTab == "Learn") {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 20.dp)
                    ) {
                        items(learnTopics) { learning ->
                            val firstAidId = learning["firstAidId"] as? String ?: ""
                            val learningId = learning["learningId"] as? String ?: ""
                            val title = firstAidTitles[firstAidId] ?: firstAidId
                            val status = learningProgress[learningId] ?: "Pending"
                            val isCompleted = status == "Completed"
                            android.util.Log.d("LearnExamPage", "Rendering card - learningId: $learningId, status: $status, isCompleted: $isCompleted")
                            LearnTopicCard(
                                title = title,
                                isCompleted = isCompleted,
                                onClick = { onLearnTopicClick(learningId) },
                                fontFamily = cabin
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp) // Reduced bottom padding
                    ) {
                        items(examTopics) { exam ->
                            val firstAidId = exam["firstAidId"] as? String ?: ""
                            val examId = exam["examId"] as? String ?: ""
                            val title = firstAidTitles[firstAidId] ?: firstAidId
                            
                            // Check if learning material for this specific topic is completed
                            // Find the learning material that matches this exam's firstAidId
                            val matchingLearning = learnTopics.find { learning ->
                                (learning["firstAidId"] as? String) == firstAidId
                            }
                            val learningId = matchingLearning?.get("learningId") as? String ?: ""
                            val learningStatus = learningProgress[learningId] ?: "Pending"
                            val isLearningCompleted = learningStatus == "Completed"
                            
                            // Get exam progress status
                            val examProgressData = examProgress[examId]
                            val examStatus = examProgressData?.get("status") as? String ?: "Pending"
                            val isExamPassed = examStatus == "Passed"
                            
                            // Exam is available if learning is completed
                            val isExamAvailable = isLearningCompleted
                            
                            android.util.Log.d("LearnExamPage", "Rendering exam card - examId: $examId, learningCompleted: $isLearningCompleted, examStatus: $examStatus, isAvailable: $isExamAvailable")
                            
                            ExamTopicCard(
                                title = title,
                                isAvailable = isExamAvailable,
                                isPassed = isExamPassed,
                                onClick = { 
                                    if (isExamAvailable) {
                                        onExamTopicClick(examId)
                                    }
                                },
                                onUnavailableClick = { examTitle ->
                                    unavailableExamTitle = examTitle
                                    showUnavailableMessage = true
                                },
                                fontFamily = cabin
                            )
                        }
                    }
                }
            }
        
        // Bottom Bar
        BottomBar(
            selected = BottomItem.LEARN,
            onSelected = onSelectBottom
        )
        
        // Unavailable exam message dialog
        if (showUnavailableMessage) {
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
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Exam Not Available",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = cabin
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "You should complete the learning material for $unavailableExamTitle before taking the exam.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontFamily = cabin,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = { 
                                showUnavailableMessage = false
                                unavailableExamTitle = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.green_primary)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "OK",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = cabin
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearnTopicCard(
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
    fontFamily: FontFamily
) {
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
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = colorResource(id = R.color.green_primary),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ExamTopicCard(
    title: String,
    isAvailable: Boolean = true,
    isPassed: Boolean = false,
    onClick: () -> Unit,
    onUnavailableClick: (String) -> Unit = {},
    fontFamily: FontFamily
) {
    val cardColor = if (isAvailable) Color.White else Color.Gray.copy(alpha = 0.1f)
    val textColor = if (isAvailable) colorResource(id = R.color.green_primary) else Color.Gray
    val borderColor = if (isAvailable) colorResource(id = R.color.green_primary) else Color.DarkGray
    val iconColor = if (isAvailable) colorResource(id = R.color.green_primary) else Color.Gray
    
    android.util.Log.d("ExamTopicCard", "Rendering exam card - title: $title, isAvailable: $isAvailable, isPassed: $isPassed")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp)
            .let { modifier ->
                if (isAvailable) {
                    modifier.shadow(4.dp, RoundedCornerShape(10.dp))
                } else {
                    modifier
                }
            }
            .clickable { 
                if (isAvailable) {
                    onClick()
                } else {
                    onUnavailableClick(title)
                }
            },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor)
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
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily
            )
            
            if (isPassed) {
                android.util.Log.d("ExamTopicCard", "Showing check icon for passed exam: $title")
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Passed",
                    tint = colorResource(id = R.color.green_primary),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // No icon for non-passed exams
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LearnExamPagePreview() {
    LearnExamPage()
}
