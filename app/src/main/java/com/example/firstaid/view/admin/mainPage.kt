package com.example.firstaid.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstaid.R
import com.example.firstaid.view.components.TopBar
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AdminMainPage(
    onBackClick: () -> Unit = {},
    onAddTopic: () -> Unit = {},
    onEditTopic: () -> Unit = {},
    onAddModule: () -> Unit = {},
    onEditModule: () -> Unit = {},
    onAddExam: () -> Unit = {},
    onEditExam: () -> Unit = {},
    onAddBadge: () -> Unit = {},
    onEditBadge: () -> Unit = {},
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
    val docId = prefs.getString("docId", null)
    val userId = prefs.getString("userId", null)
    val db = FirebaseFirestore.getInstance()

    var isChecking by remember { mutableStateOf(true) }
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(docId, userId) {
        // Gate: allow only admin. Primary check: presence in Admin table (userId). Fallback: role/isAdmin on User.
        fun handleDenied() {
            isAdmin = false
            isChecking = false
        }
        try {
            val uid = userId
            if (uid != null) {
                // Primary: check Admin collection
                db.collection("Admin").whereEqualTo("userId", uid).limit(1).get()
                    .addOnSuccessListener { qs ->
                        if (!qs.isEmpty) {
                            isAdmin = true
                            isChecking = false
                        } else {
                            // Fallback to User flags
                            if (docId != null) {
                                db.collection("User").document(docId).get()
                                    .addOnSuccessListener { snap ->
                                        val role = snap.getString("role")
                                        val flag = snap.getBoolean("isAdmin") ?: false
                                        isAdmin = role == "admin" || flag
                                        isChecking = false
                                    }
                                    .addOnFailureListener { handleDenied() }
                            } else {
                                handleDenied()
                            }
                        }
                    }
                    .addOnFailureListener { handleDenied() }
            } else if (docId != null) {
                // If no userId stored, fallback check on User flags
                db.collection("User").document(docId).get()
                    .addOnSuccessListener { snap ->
                        val role = snap.getString("role")
                        val flag = snap.getBoolean("isAdmin") ?: false
                        isAdmin = role == "admin" || flag
                        isChecking = false
                    }
                    .addOnFailureListener { handleDenied() }
            } else {
                handleDenied()
            }
        } catch (e: Exception) {
            handleDenied()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar()

            Divider(color = Color.Black, thickness = 1.dp)

            if (isChecking) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green_primary))
                }
            } else if (!isAdmin) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Admins Only",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontFamily = cabin
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You don't have permission to access this page.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontFamily = cabin
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.green_primary)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Go Back", color = Color.White, fontFamily = cabin)
                            }
                        }
                    }
                }
            } else {
                // Admin content UI: 2x2 quadrants (Top-Left, Top-Right, Bottom-Left, Bottom-Right)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Top row (two quadrants) with a vertical divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            AdminQuadrant(
                                title = "First Aid Topic",
                                primaryText = "Add New Topic",
                                secondaryText = "Edit Current Topic",
                                onPrimaryClick = onAddTopic,
                                onSecondaryClick = onEditTopic,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Vertical divider between left and right
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(Color(0xFFD9D9D9))
                        )

                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            AdminQuadrant(
                                title = "First Aid Module",
                                primaryText = "Add New Module",
                                secondaryText = "Edit Current Module",
                                onPrimaryClick = onAddModule,
                                onSecondaryClick = onEditModule,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Horizontal divider between top and bottom rows
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFD9D9D9))
                    )

                    // Bottom row (two quadrants) with a vertical divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            AdminQuadrant(
                                title = "First Aid Exam",
                                primaryText = "Add New Exam",
                                secondaryText = "Edit Current Exam",
                                onPrimaryClick = onAddExam,
                                onSecondaryClick = onEditExam,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Vertical divider between left and right
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(Color(0xFFD9D9D9))
                        )

                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            AdminQuadrant(
                                title = "First Aid Badge",
                                primaryText = "Add New Badge",
                                secondaryText = "Edit Current Badge",
                                onPrimaryClick = onAddBadge,
                                onSecondaryClick = onEditBadge,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(43.dp)
            .shadow(4.dp, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.green_primary)),
        shape = RoundedCornerShape(10.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.cabin, FontWeight.Bold))
            )
        }
    }
}

@Composable
private fun AdminQuadrant(
    title: String,
    primaryText: String,
    secondaryText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = colorResource(id = R.color.green_primary),
                fontFamily = FontFamily(Font(R.font.cabin, FontWeight.Bold))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AdminActionButton(text = primaryText, onClick = onPrimaryClick, modifier = Modifier.fillMaxWidth())
                AdminActionButton(text = secondaryText, onClick = onSecondaryClick, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

