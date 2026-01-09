package com.example.firstaid.view.firstAid

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstaid.R
import com.example.firstaid.view.components.TopBarWithBack
import com.example.firstaid.view.components.BottomBar
import com.example.firstaid.view.components.BottomItem
import com.example.firstaid.viewmodel.firstAid.FirstAidDetailsViewModel
import android.app.Application
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import android.util.Log

@Composable
fun FirstAidDetailsPage(
    firstAidId: String,
    viewModel: FirstAidDetailsViewModel = viewModel(
        factory = FirstAidDetailsViewModel.Factory(
            LocalContext.current.applicationContext as Application,
            firstAidId
        )
    ),
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()

    // Auto play voice once per step
    LaunchedEffect(uiState.currentStepIndex, uiState.isTtsInitialized, uiState.contentItems) {
        if (uiState.isTtsInitialized && uiState.contentItems.isNotEmpty()) {
            if (!uiState.autoPlayedSteps.contains(uiState.currentStepIndex)) {
                val textToSpeak = uiState.contentItems[uiState.currentStepIndex].content
                viewModel.playTextToSpeech()
                viewModel.markStepAsAutoPlayed(uiState.currentStepIndex)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarWithBack(
                title = uiState.firstAidTitle.ifEmpty { "First Aid Details" },
                onBackClick = onBackClick
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green_primary))
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.errorMessage ?: "", color = Color.Red)
                }
            } else if (uiState.contentItems.isNotEmpty()) {
                val currentContent = uiState.currentContent ?: return@Column

                Spacer(modifier = Modifier.height(24.dp))

                // Call 999 button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(51.dp)
                        .padding(horizontal = 24.dp)
                        .shadow(4.dp, RoundedCornerShape(10.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:999")
                            }
                            context.startActivity(intent)
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.phone),
                                contentDescription = null
                            )
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Call 999",
                                    color = Color.White,
                                    fontSize = 30.sp,
                                    fontFamily = cabin,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Image(
                                painter = painterResource(id = R.drawable.ambulance),
                                contentDescription = null
                            )
                        }
                    }
                }

                // Content title
                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    Text(
                        text = (uiState.currentStepIndex + 1).toString() + ".",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Text(
                        text = currentContent.title,
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Content text
                Text(
                    text = currentContent.content,
                    color = Color.Black,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                // Illustration - only show if image URL exists
                val imageUrl = currentContent.imageUrl
                Log.d("FirstAidDetails", "Rendering step ${uiState.currentStepIndex + 1} - imageUrl: '$imageUrl', isNullOrBlank: ${imageUrl.isNullOrBlank()}")
                
                if (!imageUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        // Use ImageRequest.Builder for proper handling of content URIs and HTTP URLs
                        val imageRequest = ImageRequest.Builder(context)
                            .data(imageUrl)
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
                                Log.e("FirstAidDetails", "Error loading image from '$imageUrl': ${error.result.throwable.message}")
                            },
                            onSuccess = {
                                Log.d("FirstAidDetails", "Image loaded successfully from: $imageUrl")
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sound button
                Button(
                    onClick = {
                        viewModel.playTextToSpeech()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(46.dp)
                        .shadow(4.dp, RoundedCornerShape(10.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.green_primary),
                        disabledContainerColor = colorResource(id = R.color.green_primary)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = true // Always enabled, but TTS functionality depends on initialization
                ) {
                    if (uiState.isTtsInitialized) {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = "Play",
                            tint = Color.White
                        )
                    } else {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.9f))

                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!uiState.isFirstStep) {
                        Button(
                            onClick = { viewModel.onPreviousStep() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .shadow(4.dp, RoundedCornerShape(10.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.green_primary)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )
                                Text(
                                    text = "Back",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }

                    if (uiState.isLastStep) {
                        Button(
                            onClick = onCompleteClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .shadow(4.dp, RoundedCornerShape(10.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.green_primary)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Complete", color = Color.White, fontSize = 15.sp)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.onNextStep() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .shadow(4.dp, RoundedCornerShape(10.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.green_primary)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Next",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Bar
            BottomBar(
                selected = BottomItem.FIRST_AID,
                onSelected = onSelectBottom
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FirstAidDetailsPreview() {
    FirstAidDetailsPage(
        firstAidId = "sample_id",
        onBackClick = {},
        onCompleteClick = {}
    )
}

