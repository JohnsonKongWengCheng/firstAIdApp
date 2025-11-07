package com.example.firstaid.view.building

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class Question(
    val id: String,
    val questionId: String,
    val examId: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)

@Composable
fun ExamDetailsPage(
    examId: String,
    onBackClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
    val currentUserId = sharedPreferences.getString("userId", null)
    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    // Function to award badge when user passes exam
    fun awardBadgeForExam(examId: String, userId: String) {
        android.util.Log.d("BadgeAward", "=== STARTING BADGE AWARD PROCESS ===")
        android.util.Log.d("BadgeAward", "Attempting to award badge for examId: $examId, userId: $userId")
        if (userId.isNotEmpty()) {
            // First, get the firstAidId from the exam
            android.util.Log.d("BadgeAward", "Looking up exam with examId: $examId")
            db.collection("Exam")
                .whereEqualTo("examId", examId)
                .get()
                .addOnSuccessListener { examDocs ->
                    android.util.Log.d("BadgeAward", "Exam query returned ${examDocs.documents.size} documents")
                    if (!examDocs.isEmpty) {
                        val examDoc = examDocs.documents[0]
                        val firstAidId = examDoc.getString("firstAidId")
                        android.util.Log.d("BadgeAward", "Found firstAidId: $firstAidId for examId: $examId")
                        
                        if (firstAidId != null) {
                            // Find the badge associated with this firstAidId
                            android.util.Log.d("BadgeAward", "Looking up badge with firstAidId: $firstAidId")
                            db.collection("Badge")
                                .whereEqualTo("firstAidId", firstAidId)
                                .get()
                                .addOnSuccessListener { badgeDocuments ->
                                    android.util.Log.d("BadgeAward", "Badge query returned ${badgeDocuments.documents.size} documents")
                                    if (!badgeDocuments.isEmpty) {
                                        val badgeDoc = badgeDocuments.documents[0]
                                        val badgeId = badgeDoc.getString("badgeId") ?: badgeDoc.id
                                        android.util.Log.d("BadgeAward", "Found badge: $badgeId for firstAidId: $firstAidId")
                                        
                                        // Check if user already has this badge
                                        db.collection("User_Badge")
                                            .whereEqualTo("userId", userId)
                                            .whereEqualTo("badgeId", badgeId)
                                            .get()
                                            .addOnSuccessListener { existingBadges ->
                                                if (existingBadges.isEmpty) {
                                                    // Award the badge
                                                    android.util.Log.d("BadgeAward", "User doesn't have this badge yet, creating User_Badge record...")
                                                    val userBadgeData = hashMapOf(
                                                        "userId" to userId,
                                                        "badgeId" to badgeId,
                                                        "earnedDate" to com.google.firebase.Timestamp.now(),
                                                        "examId" to examId,
                                                        "firstAidId" to firstAidId
                                                    )
                                                    
                                                    android.util.Log.d("BadgeAward", "User_Badge data: $userBadgeData")
                                                    
                                                    db.collection("User_Badge")
                                                        .add(userBadgeData)
                                                        .addOnSuccessListener { docRef ->
                                                            android.util.Log.d("BadgeAward", "=== BADGE AWARDED SUCCESSFULLY ===")
                                                            android.util.Log.d("BadgeAward", "Badge awarded successfully: $badgeId")
                                                            android.util.Log.d("BadgeAward", "Document ID: ${docRef.id}")
                                                        }
                                                        .addOnFailureListener { e ->
                                                            android.util.Log.e("BadgeAward", "=== FAILED TO AWARD BADGE ===")
                                                            android.util.Log.e("BadgeAward", "Failed to award badge: ${e.localizedMessage}")
                                                            android.util.Log.e("BadgeAward", "Error: ${e.message}")
                                                        }
                                                } else {
                                                    android.util.Log.d("BadgeAward", "User already has this badge: $badgeId")
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                android.util.Log.e("BadgeAward", "Failed to check existing badges: ${e.localizedMessage}")
                                            }
                                    } else {
                                        android.util.Log.d("BadgeAward", "No badge found for firstAidId: $firstAidId")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    android.util.Log.e("BadgeAward", "Failed to find badge for firstAidId: ${e.localizedMessage}")
                                }
                        } else {
                            android.util.Log.d("BadgeAward", "No firstAidId found for exam: $examId")
                        }
                    } else {
                        android.util.Log.d("BadgeAward", "No exam found with examId: $examId")
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("BadgeAward", "Failed to find exam: ${e.localizedMessage}")
                }
        }
    }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var firstAidTitle by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var selectedAnswers by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var shuffledQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isAlreadyPassed by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showTryAgainMessage by remember { mutableStateOf(false) }

    // Fetch Exam data and related questions
    LaunchedEffect(examId) {
        try {
            if (examId.isBlank()) {
                errorMessage = "Invalid Exam ID: empty"
                isLoading = false
                return@LaunchedEffect
            }

            // Debug logging
            android.util.Log.d("ExamDetails", "Fetching exam data for examId: $examId")
            
            // Get Exam data
            db.collection("Exam")
                .whereEqualTo("examId", examId)
                .get()
                .addOnSuccessListener { examDocs ->
                    android.util.Log.d("ExamDetails", "Found ${examDocs.documents.size} exam documents")
                    if (examDocs.documents.isNotEmpty()) {
                        val examDoc = examDocs.documents.first()
                        val firstAidId = examDoc.getString("firstAidId") ?: ""

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

                        // Get Questions for this exam
                        android.util.Log.d("ExamDetails", "Fetching questions for examId: $examId")
                        db.collection("Question")
                            .whereEqualTo("examId", examId)
                            .get()
                            .addOnSuccessListener { questionDocs ->
                                android.util.Log.d("ExamDetails", "Found ${questionDocs.documents.size} question documents")
                                val questionList = questionDocs.documents.mapNotNull { doc ->
                                    val otherOptions = doc.get("otherOptions") as? List<String> ?: emptyList()
                                    val correctAnswer = doc.getString("correctAnswer") ?: ""
                                    
                                    // Combine otherOptions and correctAnswer into a single options list
                                    val allOptions = (otherOptions + correctAnswer).shuffled()
                                    
                                    Question(
                                        id = doc.id,
                                        questionId = doc.getString("questionId") ?: "",
                                        examId = doc.getString("examId") ?: "",
                                        questionText = doc.getString("question") ?: "",
                                        options = allOptions,
                                        correctAnswer = correctAnswer
                                    )
                                }

                                questions = questionList
                                // Shuffle the order of questions
                                shuffledQuestions = questionList.shuffled()
                                
                                // Debug logging
                                android.util.Log.d("ExamDetails", "Loaded ${questionList.size} questions for exam $examId")
                                questionList.forEachIndexed { index, question ->
                                    android.util.Log.d("ExamDetails", "Question $index: ${question.questionText}")
                                    android.util.Log.d("ExamDetails", "Options: ${question.options}")
                                }
                                
                                // Check exam progress status
                                if (currentUserId != null) {
                                    db.collection("Exam_Progress")
                                        .whereEqualTo("userId", currentUserId)
                                        .whereEqualTo("examId", examId)
                                        .get()
                                        .addOnSuccessListener { progressDocs ->
                                            if (progressDocs.documents.isNotEmpty()) {
                                                val progressDoc = progressDocs.documents.first()
                                                val currentStatus = progressDoc.getString("status") ?: "Pending"
                                                isAlreadyPassed = currentStatus == "Passed"
                                                android.util.Log.d("ExamDetails", "Exam progress status: $currentStatus, isAlreadyPassed: $isAlreadyPassed")
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            android.util.Log.e("ExamDetails", "Failed to fetch exam progress: ${e.localizedMessage}")
                                        }
                                }
                                
                                isLoading = false
                            }
                            .addOnFailureListener { e ->
                                errorMessage = e.localizedMessage ?: "Failed to load questions"
                                isLoading = false
                            }
                    } else {
                        errorMessage = "No exam data found"
                        isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage = e.localizedMessage ?: "Failed to load exam data"
                    isLoading = false
                }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to load data"
            isLoading = false
        }
    }

    // Check if all questions are answered
    val allQuestionsAnswered = shuffledQuestions.isNotEmpty() && 
        shuffledQuestions.all { question -> selectedAnswers.containsKey(question.questionId) }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with back button
            TopBarWithBack(
                title = firstAidTitle.ifEmpty { "Exam" },
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
            } else if (shuffledQuestions.isNotEmpty()) {
                // Scrollable exam content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 100.dp) // Space for bottom bar
                ) {
                    // Questions
                    shuffledQuestions.forEachIndexed { questionIndex, question ->
                        QuestionCard(
                            question = question,
                            questionNumber = questionIndex + 1,
                            selectedAnswer = selectedAnswers[question.questionId],
                            onAnswerSelected = { answer ->
                                selectedAnswers = selectedAnswers + (question.questionId to answer)
                            },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }

                    // Submit Button - Only show if not already passed
                    if (!isAlreadyPassed) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                // Calculate score and update exam progress
                                if (currentUserId != null) {
                                    var correctAnswers = 0
                                    val totalQuestions = shuffledQuestions.size
                                    
                                    android.util.Log.d("ExamDetails", "Starting exam submission - UserId: $currentUserId, ExamId: $examId")
                                    android.util.Log.d("ExamDetails", "Total questions: $totalQuestions")
                                    android.util.Log.d("ExamDetails", "Selected answers: $selectedAnswers")
                                    
                                    shuffledQuestions.forEach { question ->
                                        val selectedAnswer = selectedAnswers[question.questionId]
                                        val isCorrect = selectedAnswer == question.correctAnswer
                                        android.util.Log.d("ExamDetails", "Question: ${question.questionText}")
                                        android.util.Log.d("ExamDetails", "Selected: $selectedAnswer, Correct: ${question.correctAnswer}, IsCorrect: $isCorrect")
                                        
                                        if (isCorrect) {
                                            correctAnswers++
                                        }
                                    }
                                    
                                    val score = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0
                                    val newStatus = if (correctAnswers == totalQuestions) "Passed" else "Taken"
                                    
                                    android.util.Log.d("ExamDetails", "Exam results - Correct: $correctAnswers/$totalQuestions, Score: $score%, Status: $newStatus")
                                    
                                    db.collection("Exam_Progress")
                                        .whereEqualTo("userId", currentUserId)
                                        .whereEqualTo("examId", examId)
                                        .get()
                                        .addOnSuccessListener { progressDocs ->
                                            android.util.Log.d("ExamDetails", "Found ${progressDocs.documents.size} exam progress documents")
                                            if (progressDocs.documents.isNotEmpty()) {
                                                val progressDoc = progressDocs.documents.first()
                                                android.util.Log.d("ExamDetails", "Updating exam progress document: ${progressDoc.id}")
                                                progressDoc.reference.update(
                                                    "status", newStatus,
                                                    "score", score
                                                )
                                                    .addOnSuccessListener {
                                                        android.util.Log.d("ExamDetails", "Exam progress updated successfully - Status: $newStatus, Score: $score")
                                                        isAlreadyPassed = newStatus == "Passed"

                                                        if (newStatus == "Passed") {
                                                            // Award badge if exam is passed
                                                            if (currentUserId != null) {
                                                                android.util.Log.d("BadgeAward", "Exam passed! Awarding badge for examId: $examId")
                                                                awardBadgeForExam(examId, currentUserId)
                                                            } else {
                                                                android.util.Log.d("BadgeAward", "No userId, skipping badge award")
                                                            }

                                                            showSuccessMessage = true
                                                            // Show success message for 1 second, then navigate
                                                            coroutineScope.launch {
                                                                kotlinx.coroutines.delay(1000)
                                                                showSuccessMessage = false
                                                                onSubmitClick()
                                                            }
                                                        } else {
                                                            // Not all correct – show overlay message
                                                            showTryAgainMessage = true
                                                        }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        android.util.Log.e("ExamDetails", "Failed to update exam progress: ${e.localizedMessage}")
                                                    }
                                            } else {
                                                android.util.Log.e("ExamDetails", "No exam progress document found for userId: $currentUserId, examId: $examId")
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            android.util.Log.e("ExamDetails", "Failed to fetch exam progress: ${e.localizedMessage}")
                                        }
                                } else {
                                    android.util.Log.e("ExamDetails", "No current user ID found")
                                }
                            },
                            enabled = allQuestionsAnswered,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(50.dp)
                                .then(if (allQuestionsAnswered) Modifier.shadow(4.dp, RoundedCornerShape(10.dp)) else Modifier),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.green_primary),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                                disabledContentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Submit Exam",
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
                                        text = "Exam Submitted Successfully!",
                                        color = colorResource(id = R.color.green_primary),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = cabin
                                    )
                                }
                            }
                        }
                    } else {
                        // Show completion message if already passed
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
                                    contentDescription = "Passed",
                                    tint = colorResource(id = R.color.green_primary),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Exam Already Passed",
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
                        text = "No questions available for this exam",
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
        
        // Try again overlay message for non-perfect score - positioned absolutely at top level
        if (showTryAgainMessage) {
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
                            contentDescription = "Info",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "You didn't answer all the questions correctly. Please review and try again.",
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                showTryAgainMessage = false
                                onBackClick()
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
private fun QuestionCard(
    question: Question,
    questionNumber: Int,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit,
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
            // Question number and text
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "$questionNumber.",
                    color = Color(0xFF4DB648), // Green color
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = question.questionText,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options
            question.options.forEachIndexed { index, option ->
                val isSelected = selectedAnswer == option
                val optionLetter = ('a' + index).toString()
                
                OptionCard(
                    optionText = option,
                    optionLetter = optionLetter,
                    isSelected = isSelected,
                    onClick = { onAnswerSelected(option) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun OptionCard(
    optionText: String,
    optionLetter: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colorResource(id = R.color.green_primary) else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) colorResource(id = R.color.green_primary) else colorResource(id = R.color.green_primary)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$optionLetter.",
                color = if (isSelected) Color.White else Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = optionText,
                color = if (isSelected) Color.White else Color.Black,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExamDetailsPreview() {
    ExamDetailsPage(
        examId = "sample_exam_id",
        onBackClick = {},
        onSelectBottom = {},
        onSubmitClick = {}
    )
}