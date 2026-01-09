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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstaid.R
import com.example.firstaid.view.components.BottomBar
import com.example.firstaid.view.components.BottomItem
import com.example.firstaid.view.components.TopBarWithBack
import com.example.firstaid.viewmodel.building.ExamPageViewModel

@Composable
fun ExamPage(
    onBackClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    viewModel: ExamPageViewModel = viewModel()
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val uiState by viewModel.uiState.collectAsState()

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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.onTabSelected("Learn") }
                    ) {
                        Text(
                            text = "Learn",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.selectedTab == "Learn") colorResource(id = R.color.green_primary) else Color(0xFFAAAAAA),
                            fontFamily = cabin
                        )
                        if (uiState.selectedTab == "Learn") {
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.onTabSelected("Exam") }
                    ) {
                        Text(
                            text = "Exam",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.selectedTab == "Exam") colorResource(id = R.color.green_primary) else Color(0xFFAAAAAA),
                            fontFamily = cabin
                        )
                        if (uiState.selectedTab == "Exam") {
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
                if (uiState.selectedTab == "Exam") {
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
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.sortedExamTopics) { exam ->
                                val title = uiState.firstAidTitles[exam.firstAidId] ?: exam.firstAidId
                                ExamTopicCard(
                                    title = title,
                                    onClick = { onTopicClick(exam.examId) },
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
