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
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.imePadding

private data class ExamTopicRef(val id: String, val title: String)

@Composable
fun AddExamPage(
    onBackClick: () -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var topics by remember { mutableStateOf<List<ExamTopicRef>>(emptyList()) }
    var selectedTopic by remember { mutableStateOf<ExamTopicRef?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var noTopicsAvailable by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf(TextFieldValue("")) }

    // Dynamic questions management
    data class QuestionData(
        val id: Int,
        var question: TextFieldValue,
        var correctAnswer: TextFieldValue,
        var option1: TextFieldValue,
        var option2: TextFieldValue,
        var option3: TextFieldValue
    )
    
    var questions by remember { mutableStateOf(listOf(QuestionData(1, TextFieldValue(""), TextFieldValue(""), TextFieldValue(""), TextFieldValue(""), TextFieldValue("")))) }
    var nextQuestionId by remember { mutableStateOf(2) }

    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Enable Confirm only when topic chosen, description filled, and all questions have required fields
    val canConfirm by remember {
        derivedStateOf {
            selectedTopic != null &&
            description.text.isNotBlank() &&
            questions.all { question ->
                question.question.text.isNotBlank() &&
                question.correctAnswer.text.isNotBlank() &&
                (question.option1.text.isNotBlank() || question.option2.text.isNotBlank() || question.option3.text.isNotBlank())
            }
        }
    }

    LaunchedEffect(Unit) {
        // First get all first aid topics
        db.collection("First_Aid").get()
            .addOnSuccessListener { firstAidResult ->
                // Then get all exams to see which topics already have exams
                db.collection("Exam")
                    .get()
                    .addOnSuccessListener { examResult ->
                        val topicsWithExams = examResult.documents.mapNotNull { examDoc ->
                            examDoc.getString("firstAidId")
                        }.toSet()
                        
                        // Filter out topics that already have exams
                        topics = firstAidResult.documents.mapNotNull { doc ->
                            val firstAidId = doc.getString("firstAidId") ?: doc.id
                            if (!topicsWithExams.contains(firstAidId)) {
                                ExamTopicRef(id = firstAidId, title = doc.getString("title") ?: doc.id)
                            } else null
                        }.sortedBy { it.title }
                        
                        // Check if no topics are available
                        noTopicsAvailable = topics.isEmpty()
                        isLoading = false
                    }
                    .addOnFailureListener { exception ->
                        // If exam collection fails, show all topics
                        topics = firstAidResult.documents.map { d ->
                            ExamTopicRef(id = d.getString("firstAidId") ?: d.id, title = d.getString("title") ?: d.id)
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
            TopBarWithBack(title = "Add Exam", onBackClick = onBackClick)

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
                            .clickable { if (!noTopicsAvailable) expanded = true },
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
                        DropdownMenu(expanded = expanded && !noTopicsAvailable, onDismissRequest = { expanded = false }) {
                            topics.forEach { item ->
                                DropdownMenuItem(text = { Text(item.title) }, onClick = {
                                    selectedTopic = item
                                    expanded = false
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
                                    text = "All first aid topics already have exams. You cannot add new exams at this time.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF856404),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text(text = "Description:", fontSize = 16.sp, color = if (noTopicsAvailable) Color.Gray else Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (!noTopicsAvailable) description = it },
                        enabled = !noTopicsAvailable,
                        placeholder = { Text("Enter the description here..", color = Color(0xFFAAAAAA)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                            unfocusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC),
                            focusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFE6F3E6),
                            disabledContainerColor = Color(0xFFF5F5F5),
                            disabledTextColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dynamic questions rendering
                    questions.forEachIndexed { index, question ->
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
                                text = "Question ${index + 1}",
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontFamily = cabin
                            )
                            
                            // Remove Question button (show if there are 2 or more questions)
                            if (questions.size > 1) {
                                Button(
                                    onClick = {
                                        if (!noTopicsAvailable) {
                                            questions = questions.filter { it.id != question.id }
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
                        
                        Text(text = "Question:", fontSize = 16.sp, color = if (noTopicsAvailable) Color.Gray else Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = question.question,
                            onValueChange = { newValue ->
                                if (!noTopicsAvailable) {
                                    questions = questions.map { 
                                        if (it.id == question.id) it.copy(question = newValue) else it 
                                    }
                                }
                            },
                            enabled = !noTopicsAvailable,
                            placeholder = { Text("Enter question here..", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                unfocusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC),
                                focusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFE6F3E6),
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Correct Answer:", fontSize = 16.sp, color = if (noTopicsAvailable) Color.Gray else Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = question.correctAnswer,
                            onValueChange = { newValue ->
                                if (!noTopicsAvailable) {
                                    questions = questions.map { 
                                        if (it.id == question.id) it.copy(correctAnswer = newValue) else it 
                                    }
                                }
                            },
                            enabled = !noTopicsAvailable,
                            placeholder = { Text("Enter correct answer..", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                unfocusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC),
                                focusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFE6F3E6),
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Other option 1:", fontSize = 16.sp, color = if (noTopicsAvailable) Color.Gray else Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = question.option1,
                            onValueChange = { newValue ->
                                if (!noTopicsAvailable) {
                                    questions = questions.map { 
                                        if (it.id == question.id) it.copy(option1 = newValue) else it 
                                    }
                                }
                            },
                            enabled = !noTopicsAvailable,
                            placeholder = { Text("Enter option 1", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                unfocusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC),
                                focusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFE6F3E6),
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Other option 2:", fontSize = 16.sp, color = if (noTopicsAvailable) Color.Gray else Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = question.option2,
                            onValueChange = { newValue ->
                                if (!noTopicsAvailable) {
                                    questions = questions.map { 
                                        if (it.id == question.id) it.copy(option2 = newValue) else it 
                                    }
                                }
                            },
                            enabled = !noTopicsAvailable,
                            placeholder = { Text("Enter option 2", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                unfocusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC),
                                focusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFE6F3E6),
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Other option 3:", fontSize = 16.sp, color = if (noTopicsAvailable) Color.Gray else Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = question.option3,
                            onValueChange = { newValue ->
                                if (!noTopicsAvailable) {
                                    questions = questions.map { 
                                        if (it.id == question.id) it.copy(option3 = newValue) else it 
                                    }
                                }
                            },
                            enabled = !noTopicsAvailable,
                            placeholder = { Text("Enter option 3", color = Color(0xFFAAAAAA)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                unfocusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFECF0EC),
                                focusedContainerColor = if (noTopicsAvailable) Color(0xFFF5F5F5) else Color(0xFFE6F3E6),
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Gray
                            )
                        )
                    }

                    // Add Question button
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (!noTopicsAvailable) {
                                questions = questions + QuestionData(
                                    id = nextQuestionId,
                                    question = TextFieldValue(""),
                                    correctAnswer = TextFieldValue(""),
                                    option1 = TextFieldValue(""),
                                    option2 = TextFieldValue(""),
                                    option3 = TextFieldValue("")
                                )
                                nextQuestionId++
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
                        Text(text = "Add Question", color = colorResource(id = R.color.green_primary))
                    }

                    Spacer(modifier = Modifier.height(120.dp))
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
                    val topic = selectedTopic
                    if (!isSaving && canConfirm && topic != null) {
                        isSaving = true
                        // Create Exam
                        val examRef = db.collection("Exam").document()
                        val exam = hashMapOf(
                            "examId" to examRef.id,
                            "firstAidId" to topic.id,
                            "description" to description.text.trim()
                        )
                        examRef.set(exam)
                            .addOnSuccessListener {
                                // Create questions from dynamic questions list
                                val batch = db.batch()
                                fun enqueueQuestion(qText: String, corr: String, o1: String?, o2: String?, o3: String?) {
                                    val ref = db.collection("Question").document()
                                    val others = listOfNotNull(o1?.takeIf { it.isNotBlank() }?.trim(), o2?.takeIf { it.isNotBlank() }?.trim(), o3?.takeIf { it.isNotBlank() }?.trim())
                                    val data = hashMapOf(
                                        "questionId" to ref.id,
                                        "examId" to examRef.id,
                                        "question" to qText.trim(),
                                        "correctAnswer" to corr.trim(),
                                        "otherOptions" to others
                                    )
                                    batch.set(ref, data)
                                }
                                
                                // Add all questions from the dynamic list
                                questions.forEach { question ->
                                    enqueueQuestion(
                                        question.question.text,
                                        question.correctAnswer.text,
                                        question.option1.text,
                                        question.option2.text,
                                        question.option3.text
                                    )
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
                                    .addOnFailureListener { isSaving = false }
                            }
                            .addOnFailureListener { isSaving = false }
                    }
                },
                enabled = !isSaving && canConfirm && !noTopicsAvailable,
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
                            text = "Exam Added Successfully!",
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