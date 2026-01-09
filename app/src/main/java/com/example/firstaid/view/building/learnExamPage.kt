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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstaid.R
import com.example.firstaid.view.components.BottomBar
import com.example.firstaid.view.components.BottomItem
import com.example.firstaid.view.components.TopBar
import com.example.firstaid.viewmodel.building.LearnExamPageViewModel

@Composable
fun LearnExamPage(
    onSelectBottom: (BottomItem) -> Unit = {},
    onLearnTopicClick: (String) -> Unit = {},
    onExamTopicClick: (String) -> Unit = {},
    viewModel: LearnExamPageViewModel? = null
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE) }
    val currentUserId = remember { prefs.getString("userId", null) }
    
    val actualViewModel: LearnExamPageViewModel = viewModel ?: androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return LearnExamPageViewModel(currentUserId) as T
            }
        }
    )
    
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val uiState by actualViewModel.uiState.collectAsState()

    var showUnavailableMessage by remember { mutableStateOf(false) }
    var unavailableExamTitle by remember { mutableStateOf("") }

    // Refresh progress when page becomes visible
    LaunchedEffect(Unit) {
        actualViewModel.refreshProgress()
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
                        modifier = Modifier.clickable { actualViewModel.onTabSelected("Learn") }
                    ) {
                        Text(
                            text = "Learn",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.selectedTab == "Learn") colorResource(id = R.color.green_primary) else Color(0xFFAAAAAA),
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(3.dp)
                                .background(
                                    if (uiState.selectedTab == "Learn") colorResource(id = R.color.green_primary) else Color.Transparent,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }

                    // Exam Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { actualViewModel.onTabSelected("Exam") }
                    ) {
                        Text(
                            text = "Exam",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.selectedTab == "Exam") colorResource(id = R.color.green_primary) else Color(0xFFAAAAAA),
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(3.dp)
                                .background(
                                    if (uiState.selectedTab == "Exam") colorResource(id = R.color.green_primary) else Color.Transparent,
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
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.green_primary)
                        )
                    }
                } else if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "An error occurred",
                            color = Color.Red,
                            fontFamily = cabin
                        )
                    }
                } else if (uiState.selectedTab == "Learn") {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 20.dp)
                    ) {
                        items(uiState.sortedLearnTopics) { learning ->
                            val title = uiState.firstAidTitles[learning.firstAidId] ?: learning.firstAidId
                            LearnTopicCard(
                                title = title,
                                isCompleted = learning.isCompleted,
                                onClick = { onLearnTopicClick(learning.learningId) },
                                fontFamily = cabin
                            )
                        }
                    }
                } else {
                    // Filter exams to only show those with learning modules
                    val examsWithLearning = remember(uiState.sortedExamTopics, uiState.sortedLearnTopics) {
                        uiState.sortedExamTopics.filter { exam ->
                            uiState.sortedLearnTopics.any { learning ->
                                learning.firstAidId == exam.firstAidId
                            }
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp) // Reduced bottom padding
                    ) {
                        items(examsWithLearning) { exam ->
                            val title = uiState.firstAidTitles[exam.firstAidId] ?: exam.firstAidId

                            // Check if learning material for this specific topic is completed
                            val matchingLearning = uiState.sortedLearnTopics.find { learning ->
                                learning.firstAidId == exam.firstAidId
                            }
                            val isLearningCompleted = matchingLearning?.isCompleted ?: false

                            // Get exam progress status
                            val examProgressData = uiState.examProgress[exam.examId]
                            val examStatus = examProgressData?.get("status") as? String ?: "Pending"
                            val isExamPassed = examStatus == "Passed"

                            // Exam is available if learning is completed
                            val isExamAvailable = isLearningCompleted

                            ExamTopicCard(
                                title = title,
                                isAvailable = isExamAvailable,
                                isPassed = isExamPassed,
                                onClick = {
                                    if (isExamAvailable) {
                                        onExamTopicClick(exam.examId)
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
        }

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
                            textAlign = TextAlign.Center
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
