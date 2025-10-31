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
import com.example.firstaid.service.ApiService
import android.util.Log
import org.json.JSONObject
import org.json.JSONArray

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
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Warm up the API service when the page loads
    LaunchedEffect(Unit) {
        val apiService = ApiService(context)
        apiService.warmUpService()
    }

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
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLoading || scenarioText.isEmpty()) 
                            Color.Gray else colorResource(id = R.color.green_primary)
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { 
                                if (scenarioText.isNotEmpty() && !isLoading) {
                                    isLoading = true
                                    errorMessage = ""
                                    possibleInjuries = ""
                                    
                                    val apiService = ApiService(context)
                                    apiService.extractKeywords(scenarioText, object : ApiService.ApiCallback {
                                        override fun onSuccess(response: String) {
                                            try {
                                                val json = JSONObject(response)
                                                val keywordsJson = json.optJSONArray("keywords") ?: JSONArray()
                                                val keywords = MutableList(keywordsJson.length()) { idx ->
                                                    keywordsJson.optString(idx)
                                                }.filter { it.isNotBlank() }

                                                if (keywords.isEmpty()) {
                                                    isLoading = false
                                                    possibleInjuries = "No keywords detected."
                                                    return
                                                }

											apiService.predictInjury(keywords, object : ApiService.ApiCallback {
												override fun onSuccess(response: String) {
													isLoading = false
													try {
														val mlJson = JSONObject(response)
														val predictedAny = mlJson.opt("predicted_injury")
														val predictedCode = predictedAny?.toString()?.toIntOrNull()
														possibleInjuries = if (predictedCode != null) mapPredictedInjury(predictedCode) else (predictedAny?.toString() ?: response)
													} catch (e: Exception) {
														possibleInjuries = response
													}
													Log.d("AiPage", "ML Success: $response")
												}

                                                    override fun onError(error: String) {
                                                        isLoading = false
                                                        errorMessage = error
                                                        Log.e("AiPage", "ML Error: $error")
                                                    }
                                                })
                                            } catch (e: Exception) {
                                                isLoading = false
                                                errorMessage = "Failed to parse keywords."
                                            }
                                        }
                                        
                                        override fun onError(error: String) {
                                            isLoading = false
                                            errorMessage = error
                                            Log.e("AiPage", "API Error: $error")
                                        }
                                    })
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLoading) "Analyzing..." else "Submit",
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
                        when {
                            isLoading -> {
                                Text(
                                    text = "Analyzing scenario...",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 15.sp,
                                    fontFamily = cabin,
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                )
                            }
                            errorMessage.isNotEmpty() -> {
                                Column {
                                    Text(
                                        text = "Error: $errorMessage",
                                        color = Color.Red,
                                        fontSize = 15.sp,
                                        fontFamily = cabin
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tap Submit to try again",
                                        color = Color(0xFF666666),
                                        fontSize = 12.sp,
                                        fontFamily = cabin,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    )
                                }
                            }
                            possibleInjuries.isNotEmpty() -> {
                                Text(
                                    text = possibleInjuries,
                                    color = Color.Black,
                                    fontSize = 15.sp,
                                    fontFamily = cabin
                                )
                            }
                            else -> {
                                Text(
                                    text = "AI analysis will appear here...",
                                    color = Color(0xFF848484),
                                    fontSize = 15.sp,
                                    fontFamily = cabin,
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                )
                            }
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

private fun mapPredictedInjury(code: Int): String {
    return when (code) {
        41 -> "Ingestion"
        42 -> "Aspiration"
        46 -> "Burns, Electrical"
        47 -> "Burns, Not Specified"
        48 -> "Burns, Scald"
        49 -> "Burns, Chemical"
        50 -> "Amputaion"
        51 -> "Burns, Thermal"
        52 -> "Concussions"
        53 -> "Contusions, Abrasions"
        54 -> "Crushing"
        55 -> "Dislocation"
        56 -> "Foreign Body"
        57 -> "Fracture"
        58 -> "Hematoma"
        59 -> "Laceration"
        60 -> "Dental Injury"
        61 -> "Nerve Damage"
        62 -> "Internal Organ Injury"
        63 -> "Puncture"
        64 -> "Strain, Sprain"
        65 -> "Anoxia"
        66 -> "Hemorrhage"
        67 -> "Electric Shock"
        68 -> "Poisoning"
        69 -> "Submersion"
        71 -> "Other/Not Stated"
        72 -> "Avulsion"
        73 -> "Burns, Radiation"
        74 -> "Dermatitis, Conjunctivitis"
        else -> "Unknown injury ($code)"
    }
}

@Preview(showBackground = true)
@Composable
fun AiPagePreview() {
    AiPage()
}