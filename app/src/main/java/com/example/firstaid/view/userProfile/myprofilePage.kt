package com.example.firstaid.view.userProfile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.firstaid.R
import com.example.firstaid.view.components.BottomBar
import com.example.firstaid.view.components.BottomItem
import com.example.firstaid.view.components.TopBarWithBack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun MyProfilePage(
    onBackClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    var username by remember { mutableStateOf("") }
    var originalUsername by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var profileImageUrl by remember { mutableStateOf("") }
    var originalProfileImageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var tempProfileImageUrl by remember { mutableStateOf("") }
    
    val currentUser = auth.currentUser
    val sessionDocId = prefs.getString("docId", null)
    val sessionUserId = prefs.getString("userId", null)
    
    // Check if there are any changes
    val hasChanges = username != originalUsername || selectedImageUri != null
    
    // Function to save username
    fun saveUsername(docId: String) {
        val userData = mapOf(
            "name" to username
        )
        
        db.collection("User").document(docId)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                // Update original values to reflect saved state
                originalUsername = username
                selectedImageUri = null
                showSuccessMessage = true
                coroutineScope.launch {
                    delay(1500)
                    onBackClick()
                }
            }
            .addOnFailureListener { e ->
                // Handle error silently
            }
    }
    
    // Function to upload image to Firebase Storage
    fun uploadImageToFirebase(uri: Uri, onComplete: (Boolean) -> Unit = {}) {
        isUploadingImage = true
        val fileName = "profile_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("profile_images/$fileName")
        Log.d("ProfileImage", "Starting upload with fileName: $fileName")
        
        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    
                    // Update Volunteer collection with new profile image URL
                    val imageUserId = sessionUserId ?: currentUser?.uid
                    Log.d("ProfileImage", "Using userId for upload: $imageUserId (sessionUserId: $sessionUserId, currentUser: ${currentUser?.uid})")
                    if (imageUserId != null) {
                        val volunteerData = mapOf(
                            "profileImageUrl" to imageUrl
                        )
                        
                        db.collection("Volunteer")
                            .whereEqualTo("userId", imageUserId)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    // Update existing volunteer record
                                    val volunteerDoc = querySnapshot.documents[0]
                                    volunteerDoc.reference.update(volunteerData)
                                        .addOnSuccessListener {
                                            profileImageUrl = imageUrl
                                            originalProfileImageUrl = imageUrl
                                            isUploadingImage = false
                                            Log.d("ProfileImage", "Existing volunteer record updated with URL: $imageUrl")
                                            onComplete(true)
                                        }
                                        .addOnFailureListener { e ->
                                            isUploadingImage = false
                                        }
                                } else {
                                    // Create new volunteer record
                                    val newVolunteerData = mapOf(
                                        "volunteerId" to "V${System.currentTimeMillis()}",
                                        "userId" to imageUserId,
                                        "profileImageUrl" to imageUrl
                                    )
                                    
                                    db.collection("Volunteer")
                                        .add(newVolunteerData)
                                        .addOnSuccessListener {
                                            profileImageUrl = imageUrl
                                            originalProfileImageUrl = imageUrl
                                            isUploadingImage = false
                                            Log.d("ProfileImage", "New volunteer record created with URL: $imageUrl")
                                            onComplete(true)
                                        }
                                        .addOnFailureListener { e ->
                                            isUploadingImage = false
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                isUploadingImage = false
                                onComplete(false)
                            }
                    } else {
                        isUploadingImage = false
                        onComplete(false)
                    }
                }
            }
            .addOnFailureListener { e ->
                isUploadingImage = false
                onComplete(false)
            }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Log.d("ProfileImage", "Image selected, will upload when Save is clicked: $it")
        }
    }
    
    // Load user data from Firestore
    LaunchedEffect(Unit) {
        val docId = sessionDocId
        val userId = sessionUserId
        val firebaseUserId = currentUser?.uid
        
        // Set initial username from Firebase Auth
        if (currentUser != null) {
            username = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "User"
            originalUsername = username
        }
        
        // Try to load from Firestore using docId first, then fallback to userId search
        if (docId != null && docId.isNotEmpty()) {
            // Load username from User collection
            db.collection("User")
                .document(docId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firestoreName = document.getString("name")
                        username = firestoreName ?: username
                        originalUsername = username
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    isLoading = false
                }
            
            // Load profile image from Volunteer collection
            val imageUserId = userId ?: firebaseUserId
            if (imageUserId != null) {
                db.collection("Volunteer")
                    .whereEqualTo("userId", imageUserId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val volunteerDoc = querySnapshot.documents[0]
                            profileImageUrl = volunteerDoc.getString("profileImageUrl") ?: ""
                            originalProfileImageUrl = profileImageUrl
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle error silently
                    }
            }
        } else if (firebaseUserId != null) {
            // Fallback: Search for user data using Firebase Auth UID
            // Search for user in User collection by userId field
            db.collection("User")
                .whereEqualTo("userId", firebaseUserId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val userDoc = querySnapshot.documents[0]
                        val firestoreName = userDoc.getString("name")
                        username = firestoreName ?: username
                        originalUsername = username
                        
                        // Store the docId for future use
                        val docId = userDoc.id
                        prefs.edit().putString("docId", docId).apply()
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    isLoading = false
                }
            
            // Load profile image from Volunteer collection using Firebase Auth UID
            db.collection("Volunteer")
                .whereEqualTo("userId", firebaseUserId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val volunteerDoc = querySnapshot.documents[0]
                        profileImageUrl = volunteerDoc.getString("profileImageUrl") ?: ""
                        originalProfileImageUrl = profileImageUrl
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error silently
                }
        } else {
            isLoading = false
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
            // Top Bar
            TopBarWithBack(
                title = "My Profile",
                onBackClick = onBackClick
            )
            
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                
                // Loading indicator
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.green_primary)
                        )
                    }
                }
                
                // Profile Picture Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                Color(0xFFECECEC),
                                CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = if (isUploadingImage) Color(0xFF2E7D32) else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { 
                                if (!isUploadingImage) {
                                    imagePickerLauncher.launch("image/*")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploadingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = Color(0xFF2E7D32),
                                strokeWidth = 3.dp
                            )
                        } else if (selectedImageUri != null) {
                            // Show selected image (not yet saved)
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(selectedImageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Selected Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                placeholder = null
                            )
                        } else if (profileImageUrl.isNotEmpty()) {
                            // Show saved profile image
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profileImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                placeholder = null
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(60.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                
                
                // Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { newValue -> username = newValue },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.green_primary),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    textStyle = TextStyle(fontFamily = cabin, color = Color.Black),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    )
                )
                
                Spacer(modifier = Modifier.height(30.dp))
                
                // Save Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val docId = prefs.getString("docId", "")
                            
                            if (docId != null && docId.isNotEmpty()) {
                                // If there's a selected image, upload it first
                                if (selectedImageUri != null) {
                                    isUploadingImage = true
                                    uploadImageToFirebase(selectedImageUri!!) { success ->
                                        if (success) {
                                            // Image uploaded successfully, now save username
                                            saveUsername(docId)
                                        } else {
                                            isUploadingImage = false
                                        }
                                    }
                                } else {
                                    // No image to upload, just save username
                                    saveUsername(docId)
                                }
                            }
                        }
                    },
                    enabled = hasChanges && !isUploadingImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasChanges && !isUploadingImage) 
                            colorResource(id = R.color.green_primary) 
                        else Color.Gray,
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isUploadingImage) "Uploading..." else "Save",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = cabin
                    )
                }
                
                // Add bottom padding to ensure content doesn't get hidden behind BottomBar
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // Fixed Bottom Bar at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BottomBar(
                selected = BottomItem.ACCOUNT,
                onSelected = onSelectBottom
            )
        }
        
        // Success message overlay
        if (showSuccessMessage) {
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
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = colorResource(id = R.color.green_primary),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Profile Updated Successfully!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = cabin,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your changes have been saved",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontFamily = cabin,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyProfilePagePreview() {
    MyProfilePage()
}