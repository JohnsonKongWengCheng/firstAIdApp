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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstaid.R
import com.example.firstaid.view.components.BottomBar
import com.example.firstaid.view.components.BottomItem
import com.example.firstaid.view.components.TopBarWithBack
import com.example.firstaid.model.building.Question
import com.example.firstaid.viewmodel.building.ExamDetailsViewModel
import kotlinx.coroutines.delay

@Composable
fun ExamDetailsPage(
    examId: String,
    onBackClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {},
    onSubmitClick: () -> Unit = {},
    viewModel: ExamDetailsViewModel? = null
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE) }
    val currentUserId = remember { sharedPreferences.getString("userId", null) }
    
    val actualViewModel: ExamDetailsViewModel = viewModel ?: androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ExamDetailsViewModel(examId, currentUserId) as T
            }
        }
    )
    
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val uiState by actualViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // Handle success message navigation
    LaunchedEffect(uiState.showSuccessMessage) {
        if (uiState.showSuccessMessage) {
            delay(1000)
            actualViewModel.dismissSuccessMessage()
            onSubmitClick()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with back button
            TopBarWithBack(
                title = uiState.firstAidTitle.ifEmpty { "Exam" },
                onBackClick = onBackClick
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green_primary))
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            } else if (uiState.shuffledQuestions.isNotEmpty()) {
                // Scrollable exam content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 100.dp) // Space for bottom bar
                ) {
                    // Questions
                    uiState.shuffledQuestions.forEachIndexed { questionIndex, question ->
                        QuestionCard(
                            question = question,
                            questionNumber = questionIndex + 1,
                            selectedAnswer = uiState.selectedAnswers[question.questionId],
                            onAnswerSelected = { answer ->
                                actualViewModel.onAnswerSelected(question.questionId, answer)
                            },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }

                    // Submit Button - Only show if not already passed
                    if (!uiState.isAlreadyPassed) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                actualViewModel.submitExam(onSubmitClick)
                            },
                            enabled = uiState.allQuestionsAnswered,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(50.dp)
                                .then(if (uiState.allQuestionsAnswered) Modifier.shadow(4.dp, RoundedCornerShape(10.dp)) else Modifier),
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
                        if (uiState.showSuccessMessage) {
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
        
        // Try again overlay message for non-perfect score
        if (uiState.showTryAgainMessage) {
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
                                actualViewModel.dismissTryAgainMessage()
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
