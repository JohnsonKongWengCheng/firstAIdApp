package com.example.firstaid.view.building

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ExamPage(
    onBackClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {},
    onTopicClick: (String) -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    
    var selectedTab by remember { mutableStateOf("Exam") }
    var examTopics by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var firstAidTitles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var examsLoaded by remember { mutableStateOf(false) }
    var titlesLoaded by remember { mutableStateOf(false) }
    
    val db = FirebaseFirestore.getInstance()
    
    // Fetch First_Aid titles and Exam data from Firebase
    LaunchedEffect(Unit) {
        try {
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
                    if (examsLoaded) isLoading = false
                }
                .addOnFailureListener { e ->
                    errorMessage = e.localizedMessage ?: "Failed to load first aid titles"
                    isLoading = false
                }

            db.collection("Exam")
                .get()
                .addOnSuccessListener { documents ->
                    val allExams = documents.mapNotNull { doc ->
                        mapOf(
                            "examId" to (doc.getString("examId") ?: doc.id),
                            "description" to (doc.getString("description") ?: ""),
                            "firstAidId" to (doc.getString("firstAidId") ?: "")
                        )
                    }
                    
                    // Filter exams that have questions
                    if (allExams.isNotEmpty()) {
                        val examIds = allExams.map { it["examId"] as String }
                        android.util.Log.d("ExamPage", "Checking questions for ${examIds.size} exams: $examIds")
                        db.collection("Question")
                            .whereIn("examId", examIds)
                            .get()
                            .addOnSuccessListener { questionDocs ->
                                val examsWithQuestions = questionDocs.documents.mapNotNull { doc ->
                                    doc.getString("examId")
                                }.toSet()
                                android.util.Log.d("ExamPage", "Found questions for exams: $examsWithQuestions")
                                
                                examTopics = allExams.filter { exam ->
                                    val hasQuestions = examsWithQuestions.contains(exam["examId"])
                                    android.util.Log.d("ExamPage", "Exam ${exam["examId"]} has questions: $hasQuestions")
                                    hasQuestions
                                }
                                android.util.Log.d("ExamPage", "Filtered to ${examTopics.size} exams with questions")
                                examsLoaded = true
                                if (titlesLoaded) isLoading = false
                            }
                            .addOnFailureListener { e ->
                                // If question query fails, show all exams (fallback)
                                examTopics = allExams
                                examsLoaded = true
                                if (titlesLoaded) isLoading = false
                            }
                    } else {
                        examTopics = allExams
                        examsLoaded = true
                        if (titlesLoaded) isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage = e.localizedMessage ?: "Failed to load exams"
                    isLoading = false
                }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to load data"
            isLoading = false
        }
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
                title = "Exam",
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
                if (selectedTab == "Exam") {
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
                            items(examTopics) { exam ->
                                val firstAidId = exam["firstAidId"] as? String ?: ""
                                val title = firstAidTitles[firstAidId] ?: firstAidId
                                ExamTopicCard(
                                    title = title,
                                    onClick = { onTopicClick(exam["examId"] as String) },
                                    fontFamily = cabin
                                )
                            }
                        }
                    }
                } else {
                    // Learn content - placeholder for now
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Learn content coming soon",
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
private fun ExamTopicCard(
    title: String,
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

            // No icon for exam topics
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExamPagePreview() {
    ExamPage()
}
