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
import com.example.firstaid.R
import com.example.firstaid.view.components.TopBarWithBack
import com.example.firstaid.view.components.BottomBar
import com.example.firstaid.view.components.BottomItem
import com.google.firebase.firestore.FirebaseFirestore
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

private data class FirstAidItem(val id: String, val title: String)
private data class LearningItem(val id: String, val learningId: String, val firstAidId: String)
private data class ContentItem(val id: String, val contentId: String, val learningId: String, val title: String, val content: String, val stepNumber: Int, val imageUrl: String? = null)

@Composable
fun FirstAidDetailsPage(
    firstAidId: String,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var firstAidTitle by remember { mutableStateOf("") }
    var learningId by remember { mutableStateOf("") }
    var contentItems by remember { mutableStateOf<List<ContentItem>>(emptyList()) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var isTtsInitialized by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var autoPlayedSteps by remember { mutableStateOf(setOf<Int>()) }

    val db = FirebaseFirestore.getInstance()

    // Initialize TTS
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                } else {
                    isTtsInitialized = true
                    Log.d("TTS", "TTS initialized successfully")
                }
            } else {
                Log.e("TTS", "TTS initialization failed")
            }
        }
    }

    // Cleanup TTS when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Fetch First_Aid title and Learning data
    LaunchedEffect(firstAidId) {
        try {
            if (firstAidId.isBlank()) {
                errorMessage = "Invalid First Aid ID: empty"
                isLoading = false
                return@LaunchedEffect
            }

            // Debug: Log the firstAidId being used
            println("DEBUG: FirstAidDetailsPage - firstAidId: '$firstAidId'")

            // Get First_Aid title by querying firstAidId field
            db.collection("First_Aid")
                .whereEqualTo("firstAidId", firstAidId)
                .get()
                .addOnSuccessListener { docs ->
                    if (docs.documents.isNotEmpty()) {
                        val doc = docs.documents.first()
                        firstAidTitle = doc.getString("title") ?: ""
                    } else {
                        errorMessage = "First Aid not found"
                        isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage = e.localizedMessage ?: "Failed to load First Aid data"
                    isLoading = false
                }

            // Get Learning data
            db.collection("Learning")
                .whereEqualTo("firstAidId", firstAidId)
                .get()
                .addOnSuccessListener { learningDocs ->
                    if (learningDocs.documents.isNotEmpty()) {
                        val learningDoc = learningDocs.documents.first()
                        learningId = learningDoc.getString("learningId") ?: ""

                        // Get Content data
                        db.collection("Content")
                            .whereEqualTo("learningId", learningId)
                            .get()
                            .addOnSuccessListener { contentDocs ->
                                val contents = contentDocs.documents.mapNotNull { doc ->
                                    val imageUrl = doc.getString("imageUrl")
                                    Log.d("FirstAidDetails", "Loaded content - stepNumber: ${doc.getLong("stepNumber")}, imageUrl: $imageUrl")
                                    ContentItem(
                                        id = doc.id,
                                        contentId = doc.getString("contentId") ?: "",
                                        learningId = doc.getString("learningId") ?: "",
                                        title = doc.getString("title") ?: "",
                                        content = doc.getString("content") ?: "",
                                        stepNumber = (doc.getLong("stepNumber")?.toInt()) ?: 1,
                                        imageUrl = imageUrl
                                    )
                                }.sortedBy { it.stepNumber }

                                contentItems = contents
                                currentStepIndex = 0
                                isLoading = false
                            }
                            .addOnFailureListener { e ->
                                errorMessage = e.localizedMessage ?: "Failed to load content"
                                isLoading = false
                            }
                    } else {
                        errorMessage = "No learning data found"
                        isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage = e.localizedMessage ?: "Failed to load learning data"
                    isLoading = false
                }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to load data"
            isLoading = false
        }
    }

    // Auto play voice once per step
    LaunchedEffect(currentStepIndex, isTtsInitialized, contentItems) {
        if (isTtsInitialized && tts != null && contentItems.isNotEmpty()) {
            if (!autoPlayedSteps.contains(currentStepIndex)) {
                val textToSpeak = contentItems[currentStepIndex].content
                tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
                autoPlayedSteps = autoPlayedSteps + currentStepIndex
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarWithBack(
                title = firstAidTitle ?: "First Aid Details",
                onBackClick = onBackClick
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green_primary))
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage ?: "", color = Color.Red)
                }
            } else if (contentItems.isNotEmpty()) {
                val currentContent = contentItems[currentStepIndex]
                val isFirstStep = currentStepIndex == 0
                val isLastStep = currentStepIndex == contentItems.size - 1

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
                        text = (currentStepIndex + 1).toString() + ".",
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
                Log.d("FirstAidDetails", "Rendering step ${currentStepIndex + 1} - imageUrl: '$imageUrl', isNullOrBlank: ${imageUrl.isNullOrBlank()}")
                
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
                        if (isTtsInitialized && tts != null) {
                            val textToSpeak = "${currentContent.content}"
                            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
                            Log.d("TTS", "Speaking: $textToSpeak")
                        } else {
                            Log.e("TTS", "TTS not initialized")
                        }
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
                    if (isTtsInitialized) {
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
                    if (!isFirstStep) {
                        Button(
                            onClick = { currentStepIndex-- },
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

                    if (isLastStep) {
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
                            onClick = { currentStepIndex++ },
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

