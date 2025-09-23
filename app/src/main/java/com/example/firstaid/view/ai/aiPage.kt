package com.example.firstaid.view.ai

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstaid.R
import com.example.firstaid.view.components.BottomBar
import com.example.firstaid.view.components.BottomItem
import com.example.firstaid.view.components.TopBar
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop

@Composable
fun AiPage(
    onSelectBottom: (BottomItem) -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    
    var scenarioText by remember { mutableStateOf("") }
    var possibleInjuries by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    // Speech Recognition Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!spokenText.isNullOrEmpty()) {
                    scenarioText = spokenText[0]
                }
            }
            android.app.Activity.RESULT_CANCELED -> {
                // Handle cancellation silently
            }
        }
        isListening = false
    }

    // Auto-stop listening after 10 seconds
    LaunchedEffect(isListening) {
        if (isListening) {
            kotlinx.coroutines.delay(10000) // 10 seconds timeout
            isListening = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .clickable { keyboardController?.hide() }
        ) {
            // Fixed TopBar
            Spacer(modifier = Modifier.height(20.dp))
            TopBar()
            Spacer(modifier = Modifier.height(20.dp))
            
            // Content
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            
                //call ambulance button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(51.dp)
                        .shadow(4.dp, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:999")
                                }
                                context.startActivity(intent)
                            }
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
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Scenario Input Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECECEC)),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, Color(0xFFCDCDCD))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(15.dp)
                    ) {
                        BasicTextField(
                            value = scenarioText,
                            onValueChange = { scenarioText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = if (isListening) Color(0xFF4CAF50) else Color.Black,
                                fontSize = 15.sp,
                                fontFamily = cabin
                            ),
                            decorationBox = { innerTextField ->
                                if (scenarioText.isEmpty()) {
                                    Text(
                                        text = if (isListening) "Listening... Speak now..." else "Write the scenarios here...",
                                        color = if (isListening) Color(0xFF4CAF50) else Color(0xFF848484),
                                        fontSize = 15.sp,
                                        fontFamily = cabin
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(5.dp))
                
                // Microphone Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .shadow(4.dp, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isListening) Color.Red else colorResource(id = R.color.green_primary)
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { 
                                if (!isListening) {
                                    startSpeechRecognition(context, speechLauncher)
                                    isListening = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop Recording" else "Record Voice",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            if (isListening) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Listening...",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontFamily = cabin,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(5.dp))
                
                // Submit Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .shadow(4.dp, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.green_primary)),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { 
                                // TODO: Implement submit functionality
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Submit",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = cabin,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Possible Injuries Section
                Text(
                    text = "Possible Injuries/Medical Condition :",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontFamily = cabin,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(5.dp))
                
                // Injuries Display Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, Color(0xFFCDCDCD))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(15.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (possibleInjuries.isEmpty()) {
                            Text(
                                text = "AI analysis will appear here...",
                                color = Color(0xFF848484),
                                fontSize = 15.sp,
                                fontFamily = cabin,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        } else {
                            Text(
                                text = possibleInjuries,
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontFamily = cabin
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))
                
                // View First Aid Guidance Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .shadow(4.dp, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.green_primary)),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { 
                                // TODO: Implement view guidance functionality
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View First Aid Guidance",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = cabin,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Add bottom padding to ensure content doesn't get hidden behind BottomBar
            Spacer(modifier = Modifier.height(80.dp))
        }
        
        // Fixed BottomBar at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BottomBar(selected = BottomItem.AI, onSelected = onSelectBottom)
        }
    }
}

fun startSpeechRecognition(
    context: android.content.Context,
    launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        return
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the emergency situation...")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    try {
        launcher.launch(intent)
    } catch (e: Exception) {
        // Handle error silently
    }
}

@Preview(showBackground = true)
@Composable
fun AiPagePreview() {
    AiPage()
}