package com.example.firstaid.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.firstaid.view.components.TopBarWithBack
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class ExamTopicItem(
    val firstAidId: String,
    val title: String
)

data class ExamData(
    val examId: String,
    val firstAidId: String,
    val description: String
)

data class QuestionData(
    val questionId: String,
    val examId: String,
    val question: String,
    val correctAnswer: String,
    val otherOption1: String,
    val otherOption2: String,
    val otherOption3: String
)

data class EditableQuestion(
    val questionId: String,
    var question: String,
    var correctAnswer: String,
    var otherOption1: String,
    var otherOption2: String,
    var otherOption3: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExamPage(
    onBackClick: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // State variables
    var topics by remember { mutableStateOf<List<ExamTopicItem>>(emptyList()) }
    var selectedTopic by remember { mutableStateOf<ExamTopicItem?>(null) }
    var selectedExam by remember { mutableStateOf<ExamData?>(null) }
    var questions by remember { mutableStateOf<List<QuestionData>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showTopicDropdown by remember { mutableStateOf(false) }

    // Form fields
    var description by remember { mutableStateOf("") }
    var editableQuestions by remember { mutableStateOf<List<EditableQuestion>>(emptyList()) }

    // Load topics on first composition
    LaunchedEffect(Unit) {
        loading = true
        firestore.collection("First_Aid").get()
            .addOnSuccessListener { snapshot ->
                topics = snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val firstAidId = doc.getString("firstAidId") ?: doc.id
                    ExamTopicItem(
                        firstAidId = firstAidId,
                        title = title
                    )
                }
                loading = false
            }
            .addOnFailureListener {
                loading = false
            }
    }

    // Load exam and questions when topic is selected
    LaunchedEffect(selectedTopic) {
        if (selectedTopic != null) {
            loading = true
            // Reset previous data
            editableQuestions = emptyList()
            questions = emptyList()
            description = ""
            selectedExam = null
            
            // Get the first exam for this topic
            firestore.collection("Exam")
                .whereEqualTo("firstAidId", selectedTopic!!.firstAidId)
                .limit(1)
                .get()
                .addOnSuccessListener { examSnapshot ->
                    if (!examSnapshot.isEmpty) {
                        val examDoc = examSnapshot.documents.first()
                        selectedExam = ExamData(
                            examId = examDoc.getString("examId") ?: examDoc.id,
                            firstAidId = examDoc.getString("firstAidId") ?: "",
                            description = examDoc.getString("description") ?: ""
                        )
                        description = selectedExam!!.description

                        // Load questions for this exam
                        firestore.collection("Question")
                            .whereEqualTo("examId", selectedExam!!.examId)
                            .get()
                            .addOnSuccessListener { questionsSnapshot ->
                                questions = questionsSnapshot.documents.mapNotNull { qDoc ->
                                    val otherOptions = qDoc.get("otherOptions") as? List<String> ?: emptyList()
                                    QuestionData(
                                        questionId = qDoc.id,
                                        examId = qDoc.getString("examId") ?: "",
                                        question = qDoc.getString("question") ?: "",
                                        correctAnswer = qDoc.getString("correctAnswer") ?: "",
                                        otherOption1 = otherOptions.getOrNull(0) ?: "",
                                        otherOption2 = otherOptions.getOrNull(1) ?: "",
                                        otherOption3 = otherOptions.getOrNull(2) ?: ""
                                    )
                                }

                                // Convert to editable questions
                                editableQuestions = questions.map { q ->
                                    EditableQuestion(
                                        questionId = q.questionId,
                                        question = q.question,
                                        correctAnswer = q.correctAnswer,
                                        otherOption1 = q.otherOption1,
                                        otherOption2 = q.otherOption2,
                                        otherOption3 = q.otherOption3
                                    )
                                }
                                loading = false
                            }
                            .addOnFailureListener {
                                loading = false
                            }
                    } else {
                        loading = false
                    }
                }
                .addOnFailureListener {
                    loading = false
                }
        } else {
            // Reset data when no topic is selected
            editableQuestions = emptyList()
            questions = emptyList()
            description = ""
            selectedExam = null
        }
    }

    // Check if any changes have been made
    val hasChanges by remember {
        derivedStateOf {
            if (selectedExam == null) return@derivedStateOf false
            
            // Check if description changed
            val descriptionChanged = description != selectedExam!!.description
            
            // Check if any question changed or if there are new questions
            val questionsChanged = editableQuestions.any { editableQ ->
                if (editableQ.questionId.startsWith("new_")) {
                    // New question - check if it has any content
                    editableQ.question.isNotBlank() ||
                    editableQ.correctAnswer.isNotBlank() ||
                    editableQ.otherOption1.isNotBlank() ||
                    editableQ.otherOption2.isNotBlank() ||
                    editableQ.otherOption3.isNotBlank()
                } else {
                    // Existing question - check if it changed
                    val originalQ = questions.find { it.questionId == editableQ.questionId }
                    originalQ != null && (
                        editableQ.question != originalQ.question ||
                        editableQ.correctAnswer != originalQ.correctAnswer ||
                        editableQ.otherOption1 != originalQ.otherOption1 ||
                        editableQ.otherOption2 != originalQ.otherOption2 ||
                        editableQ.otherOption3 != originalQ.otherOption3
                    )
                }
            }
            
            descriptionChanged || questionsChanged
        }
    }

    // Validation
    val canConfirm by remember {
        derivedStateOf {
            selectedTopic != null &&
            description.isNotBlank() &&
            editableQuestions.isNotEmpty() &&
            editableQuestions.all { q ->
                q.question.isNotBlank() &&
                q.correctAnswer.isNotBlank() &&
                (q.otherOption1.isNotBlank() || q.otherOption2.isNotBlank() || q.otherOption3.isNotBlank())
            } &&
            hasChanges
        }
    }

    // Function to update questions sequentially
    fun updateQuestionsSequentially(index: Int) {
        if (index >= editableQuestions.size) {
            showSuccessDialog = true
            return
        }
        
        val question = editableQuestions[index]
        val otherOptions = listOfNotNull(
            question.otherOption1.takeIf { it.isNotBlank() },
            question.otherOption2.takeIf { it.isNotBlank() },
            question.otherOption3.takeIf { it.isNotBlank() }
        )
        
        // Check if this is a new question (temporary ID) or existing question
        if (question.questionId.startsWith("new_")) {
            // Create new question
            val newQuestionRef = firestore.collection("Question").document()
            val newQuestionData = hashMapOf(
                "questionId" to newQuestionRef.id,
                "examId" to selectedExam!!.examId,
                "question" to question.question,
                "correctAnswer" to question.correctAnswer,
                "otherOptions" to otherOptions
            )
            
            newQuestionRef.set(newQuestionData)
                .addOnSuccessListener {
                    updateQuestionsSequentially(index + 1)
                }
        } else {
            // Update existing question
            firestore.collection("Question")
                .document(question.questionId)
                .update(
                    mapOf(
                        "question" to question.question,
                        "correctAnswer" to question.correctAnswer,
                        "otherOptions" to otherOptions
                    )
                )
                .addOnSuccessListener {
                    updateQuestionsSequentially(index + 1)
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Top Bar
            TopBarWithBack(
                title = "Edit Exam",
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
                                text = selectedTopic?.title ?: "Select Topic",
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

                Spacer(modifier = Modifier.height(16.dp))

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    enabled = selectedTopic != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (selectedTopic != null) Color(0xFFECECEC) else Color(0xFFF5F5F5),
                        unfocusedContainerColor = if (selectedTopic != null) Color(0xFFECECEC) else Color(0xFFF5F5F5),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledContainerColor = Color(0xFFF5F5F5),
                        disabledTextColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))


                // Dynamic Questions Section
                editableQuestions.forEachIndexed { index, question ->
                    Text(
                        text = "Question ${index + 1}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Question Text
                    Text(
                        text = "Question:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = question.question,
                        onValueChange = { question.question = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        placeholder = { Text("Enter question text") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFECECEC),
                            unfocusedContainerColor = Color(0xFFECECEC),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Correct Answer
                    Text(
                        text = "Correct Answer:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = question.correctAnswer,
                        onValueChange = { question.correctAnswer = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter correct answer") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFECECEC),
                            unfocusedContainerColor = Color(0xFFECECEC),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Other Options
                    Text(
                        text = "Other option 1:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = question.otherOption1,
                        onValueChange = { question.otherOption1 = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter option 1") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFECECEC),
                            unfocusedContainerColor = Color(0xFFECECEC),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Other option 2:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = question.otherOption2,
                        onValueChange = { question.otherOption2 = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter option 2") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFECECEC),
                            unfocusedContainerColor = Color(0xFFECECEC),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Other option 3:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = question.otherOption3,
                        onValueChange = { question.otherOption3 = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter option 3") },
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
                    
                    // Add separator line between questions (except for the last question)
                    if (index < editableQuestions.size - 1) {
                        Divider(
                            color = Color(0xFFB8B8B8),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
                
                // Add Question Button (only show if topic is selected)
                if (selectedTopic != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            // Add a new empty question
                            val newQuestion = EditableQuestion(
                                questionId = "new_${System.currentTimeMillis()}", // Temporary ID for new questions
                                question = "",
                                correctAnswer = "",
                                otherOption1 = "",
                                otherOption2 = "",
                                otherOption3 = ""
                            )
                            editableQuestions = editableQuestions + newQuestion
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6F3E6)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Add Question", color = Color(0xFF4DB648))
                    }
                }
                
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
                    if (canConfirm && selectedExam != null) {
                        // Update exam description
                        firestore.collection("Exam")
                            .document(selectedExam!!.examId)
                            .update("description", description)
                            .addOnSuccessListener {
                                // Update all questions
                                updateQuestionsSequentially(0)
                            }
                    }
                },
                enabled = canConfirm,
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
                Text(text = "Confirm", color = Color.White)
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4DB648)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Exam Updated Successfully!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Auto dismiss and navigate back
            LaunchedEffect(showSuccessDialog) {
                kotlinx.coroutines.delay(1000)
                showSuccessDialog = false
                onBackClick()
            }
        }
    }
}
