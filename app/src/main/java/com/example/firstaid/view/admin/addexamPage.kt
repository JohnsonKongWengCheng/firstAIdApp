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

    var description by remember { mutableStateOf(TextFieldValue("")) }
    var question by remember { mutableStateOf(TextFieldValue("")) }
    var correctAnswer by remember { mutableStateOf(TextFieldValue("")) }
    var option1 by remember { mutableStateOf(TextFieldValue("")) }
    var option2 by remember { mutableStateOf(TextFieldValue("")) }
    var option3 by remember { mutableStateOf(TextFieldValue("")) }

    // Second question fields (appear after Q1 completed)
    val isQ1Complete by remember {
        derivedStateOf {
            question.text.isNotBlank() &&
            correctAnswer.text.isNotBlank() &&
            (option1.text.isNotBlank() || option2.text.isNotBlank() || option3.text.isNotBlank())
        }
    }
    var question2 by remember { mutableStateOf(TextFieldValue("")) }
    var correctAnswer2 by remember { mutableStateOf(TextFieldValue("")) }
    var option1_2 by remember { mutableStateOf(TextFieldValue("")) }
    var option2_2 by remember { mutableStateOf(TextFieldValue("")) }
    var option3_2 by remember { mutableStateOf(TextFieldValue("")) }

    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Enable Confirm only when topic chosen, description, question, correct answer filled,
    // and at least one other option is provided
    val hasAtLeastOneOtherOption by remember {
        derivedStateOf { option1.text.isNotBlank() || option2.text.isNotBlank() || option3.text.isNotBlank() }
    }
    val hasAtLeastOneOtherOptionQ2 by remember {
        derivedStateOf { option1_2.text.isNotBlank() || option2_2.text.isNotBlank() || option3_2.text.isNotBlank() }
    }
    val isQ2Valid by remember {
        derivedStateOf { question2.text.isNotBlank() && correctAnswer2.text.isNotBlank() && hasAtLeastOneOtherOptionQ2 }
    }
    val canConfirm by remember {
        derivedStateOf {
            selectedTopic != null &&
            description.text.isNotBlank() &&
            question.text.isNotBlank() &&
            correctAnswer.text.isNotBlank() &&
            hasAtLeastOneOtherOption
        }
    }

    LaunchedEffect(Unit) {
        db.collection("First_Aid").get()
            .addOnSuccessListener { qs ->
                topics = qs.documents.map { d ->
                    ExamTopicRef(id = d.getString("firstAidId") ?: d.id, title = d.getString("title") ?: d.id)
                }.sortedBy { it.title }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
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
                            .background(Color(0xFFECF0EC), RoundedCornerShape(10.dp))
                            .clickable { expanded = true },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedTopic?.title ?: "Choose the First Aid Topic Title",
                                color = if (selectedTopic == null) Color(0xFFAAAAAA) else Color.Black,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.Black)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            topics.forEach { item ->
                                DropdownMenuItem(text = { Text(item.title) }, onClick = {
                                    selectedTopic = item
                                    expanded = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text(text = "Description:", fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // First Question
                    Text(text = "First Question", fontSize = 16.sp, color = Color.Black, fontFamily = cabin)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Question:", fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        placeholder = { Text("Enter question here..", color = Color(0xFFAAAAAA)) },
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
                    Text(text = "Correct Answer:", fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = correctAnswer,
                        onValueChange = { correctAnswer = it },
                        placeholder = { Text("Enter correct answer..", color = Color(0xFFAAAAAA)) },
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
                    Text(text = "Other option 1:", fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = option1,
                        onValueChange = { option1 = it },
                        placeholder = { Text("Enter option 1", color = Color(0xFFAAAAAA)) },
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
                    Text(text = "Other option 2:", fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = option2,
                        onValueChange = { option2 = it },
                        placeholder = { Text("Enter option 2", color = Color(0xFFAAAAAA)) },
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
                    Text(text = "Other option 3:", fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = option3,
                        onValueChange = { option3 = it },
                        placeholder = { Text("Enter option 3", color = Color(0xFFAAAAAA)) },
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

                    // Second question appears when first is complete
                    if (isQ1Complete) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = Color(0xFFB8B8B8), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Second Question", fontSize = 16.sp, color = Color.Black, fontFamily = cabin)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Question:", fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = question2,
                            onValueChange = { question2 = it },
                            placeholder = { Text("Enter question here..", color = Color(0xFFAAAAAA)) },
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
                        Text(text = "Correct Answer:", fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = correctAnswer2,
                            onValueChange = { correctAnswer2 = it },
                            placeholder = { Text("Enter correct answer..", color = Color(0xFFAAAAAA)) },
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
                        Text(text = "Other option 1:", fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = option1_2,
                            onValueChange = { option1_2 = it },
                            placeholder = { Text("Enter option 1", color = Color(0xFFAAAAAA)) },
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
                        Text(text = "Other option 2:", fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = option2_2,
                            onValueChange = { option2_2 = it },
                            placeholder = { Text("Enter option 2", color = Color(0xFFAAAAAA)) },
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
                        Text(text = "Other option 3:", fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = option3_2,
                            onValueChange = { option3_2 = it },
                            placeholder = { Text("Enter option 3", color = Color(0xFFAAAAAA)) },
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
                                // Create questions (1 and 2 if provided)
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
                                enqueueQuestion(question.text, correctAnswer.text, option1.text, option2.text, option3.text)
                                if (isQ1Complete && question2.text.isNotBlank() && correctAnswer2.text.isNotBlank()) {
                                    enqueueQuestion(question2.text, correctAnswer2.text, option1_2.text, option2_2.text, option3_2.text)
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
                enabled = !isSaving && canConfirm,
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