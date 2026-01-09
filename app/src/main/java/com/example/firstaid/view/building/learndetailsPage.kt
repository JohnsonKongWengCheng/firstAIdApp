package com.example.firstaid.view.building

import androidx.compose.foundation.background
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import android.util.Log
import com.example.firstaid.R
import com.example.firstaid.view.components.BottomBar
import com.example.firstaid.view.components.BottomItem
import com.example.firstaid.view.components.TopBarWithBack
import com.example.firstaid.model.building.LearningContent
import com.example.firstaid.viewmodel.building.LearnDetailsViewModel
import kotlinx.coroutines.delay

@Composable
fun LearnDetailsPage(
    learningId: String,
    onBackClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {},
    viewModel: LearnDetailsViewModel? = null
) {
    val context = LocalContext.current
    val prefs = remember(learningId) { context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE) }
    val currentUserId = remember(learningId) { prefs.getString("userId", null) }
    
    val actualViewModel: LearnDetailsViewModel = viewModel ?: androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return LearnDetailsViewModel(learningId, currentUserId) as T
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
            onBackClick()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with back button
            TopBarWithBack(
                title = uiState.firstAidTitle.ifEmpty { "Learn Details" },
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
            } else if (uiState.learningContent.isNotEmpty()) {
                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 100.dp) // Space for bottom bar
                ) {
                    // Learning content items
                    uiState.learningContent.forEachIndexed { index, content ->
                        LearningContentCard(
                            content = content,
                            index = index + 1,
                            context = context,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }

                    // Complete button (only show if not already completed)
                    if (uiState.learningContent.isNotEmpty() && !uiState.isCompleted) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                actualViewModel.markAsCompleted(onBackClick)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(50.dp)
                                .shadow(4.dp, RoundedCornerShape(10.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.green_primary)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Mark as Complete",
                                color = Color.White,
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
                                        text = "Learning Material Completed Successfully!",
                                        color = colorResource(id = R.color.green_primary),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = cabin
                                    )
                                }
                            }
                        }
                    } else if (uiState.learningContent.isNotEmpty() && uiState.isCompleted) {
                        // Show completion message if already completed
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
                                    contentDescription = "Completed",
                                    tint = colorResource(id = R.color.green_primary),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Learning Material Completed",
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
                        text = "No learning content available",
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
    }
}

@Composable
private fun LearningContentCard(
    content: LearningContent,
    index: Int,
    context: android.content.Context,
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
            // Step number and title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$index.",
                    color = Color(0xFF4DB648), // Green color
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = content.title,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content text
            Text(
                text = content.content,
                color = Color.Black,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            // Description if available
            content.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            // Image if available
            if (!content.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val imageRequest = ImageRequest.Builder(context)
                        .data(content.imageUrl)
                        .crossfade(true)
                        .build()

                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "Step illustration",
                        modifier = Modifier
                            .width(266.dp)
                            .height(236.dp),
                        contentScale = ContentScale.Crop,
                        onError = { error ->
                            Log.e("LearnDetails", "Error loading image from '${content.imageUrl}': ${error.result.throwable.message}")
                        },
                        onSuccess = {
                            Log.d("LearnDetails", "Image loaded successfully from: ${content.imageUrl}")
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LearnDetailsPreview() {
    LearnDetailsPage(
        learningId = "sample_learning_id",
        onBackClick = {},
        onSelectBottom = {}
    )
}
