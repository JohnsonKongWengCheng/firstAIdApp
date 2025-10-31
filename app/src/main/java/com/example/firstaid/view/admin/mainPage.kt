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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
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
    var showNoTopicsDialog by remember { mutableStateOf(false) }
    var showNoExamTopicsDialog by remember { mutableStateOf(false) }
    var showNoTopicsForEditDialog by remember { mutableStateOf(false) }
    var showNoModuleTopicsDialog by remember { mutableStateOf(false) }
    var showNoTopicsForEditModuleDialog by remember { mutableStateOf(false) }

    // Function to check if there are topics without badges
    fun checkTopicsWithoutBadges() {
        // First get all first aid topics
        db.collection("First_Aid")
            .get()
            .addOnSuccessListener { firstAidResult ->
                // Then get all badges to see which topics already have badges
                db.collection("Badge")
                    .get()
                    .addOnSuccessListener { badgeResult ->
                        val topicsWithBadges = badgeResult.documents.mapNotNull { badgeDoc ->
                            badgeDoc.getString("firstAidId")
                        }.toSet()
                        
                        // Check if there are topics without badges
                        val topicsWithoutBadges = firstAidResult.documents.any { doc ->
                            val firstAidId = doc.getString("firstAidId") ?: doc.id
                            !topicsWithBadges.contains(firstAidId)
                        }
                        
                        if (topicsWithoutBadges) {
                            // There are topics without badges, allow navigation
                            onAddBadge()
                        } else {
                            // No topics without badges, show dialog
                            showNoTopicsDialog = true
                        }
                    }
                    .addOnFailureListener { exception ->
                        // If badge collection fails, allow navigation (fallback)
                        onAddBadge()
                    }
            }
            .addOnFailureListener { exception ->
                // If first aid collection fails, allow navigation (fallback)
                onAddBadge()
            }
    }

    // Function to check if there are topics without exams (for Add Exam)
    fun checkTopicsWithoutExams() {
        // First get all first aid topics
        db.collection("First_Aid")
            .get()
            .addOnSuccessListener { firstAidResult ->
                // Then get all exams to see which topics already have exams
                db.collection("Exam")
                    .get()
                    .addOnSuccessListener { examResult ->
                        val topicsWithExams = examResult.documents.mapNotNull { examDoc ->
                            examDoc.getString("firstAidId")
                        }.toSet()
                        
                        // Check if there are topics without exams
                        val topicsWithoutExams = firstAidResult.documents.any { doc ->
                            val firstAidId = doc.getString("firstAidId") ?: doc.id
                            !topicsWithExams.contains(firstAidId)
                        }
                        
                        if (topicsWithoutExams) {
                            // There are topics without exams, allow navigation
                            onAddExam()
                        } else {
                            // No topics without exams, show dialog
                            showNoExamTopicsDialog = true
                        }
                    }
                    .addOnFailureListener { exception ->
                        // If exam collection fails, allow navigation (fallback)
                        onAddExam()
                    }
            }
            .addOnFailureListener { exception ->
                // If first aid collection fails, allow navigation (fallback)
                onAddExam()
            }
    }

    // Function to check if there are topics with exams (for Edit Exam)
    fun checkTopicsWithExams() {
        // First get all first aid topics
        db.collection("First_Aid")
            .get()
            .addOnSuccessListener { firstAidResult ->
                // Then get all exams to see which topics have exams
                db.collection("Exam")
                    .get()
                    .addOnSuccessListener { examResult ->
                        val topicsWithExams = examResult.documents.mapNotNull { examDoc ->
                            examDoc.getString("firstAidId")
                        }.toSet()
                        
                        // Check if there are topics with exams
                        val topicsWithExamsExist = firstAidResult.documents.any { doc ->
                            val firstAidId = doc.getString("firstAidId") ?: doc.id
                            topicsWithExams.contains(firstAidId)
                        }
                        
                        if (topicsWithExamsExist) {
                            // There are topics with exams, allow navigation
                            onEditExam()
                        } else {
                            // No topics with exams, show dialog
                            showNoTopicsForEditDialog = true
                        }
                    }
                    .addOnFailureListener { exception ->
                        // If exam collection fails, allow navigation (fallback)
                        onEditExam()
                    }
            }
            .addOnFailureListener { exception ->
                // If first aid collection fails, allow navigation (fallback)
                onEditExam()
            }
    }

    // Function to check if there are topics without modules (for Add Module)
    fun checkTopicsWithoutModules() {
        // First get all first aid topics
        db.collection("First_Aid")
            .get()
            .addOnSuccessListener { firstAidResult ->
                // Then get all modules to see which topics already have modules
                db.collection("Learning")
                    .get()
                    .addOnSuccessListener { moduleResult ->
                        val topicsWithModules = moduleResult.documents.mapNotNull { moduleDoc ->
                            moduleDoc.getString("firstAidId")
                        }.toSet()
                        
                        // Check if there are topics without modules
                        val topicsWithoutModules = firstAidResult.documents.any { doc ->
                            val firstAidId = doc.getString("firstAidId") ?: doc.id
                            !topicsWithModules.contains(firstAidId)
                        }
                        
                        if (topicsWithoutModules) {
                            // There are topics without modules, allow navigation
                            onAddModule()
                        } else {
                            // No topics without modules, show dialog
                            showNoModuleTopicsDialog = true
                        }
                    }
                    .addOnFailureListener { exception ->
                        // If module collection fails, allow navigation (fallback)
                        onAddModule()
                    }
            }
            .addOnFailureListener { exception ->
                // If first aid collection fails, allow navigation (fallback)
                onAddModule()
            }
    }

    // Function to check if there are topics with modules (for Edit Module)
    fun checkTopicsWithModules() {
        // First get all first aid topics
        db.collection("First_Aid")
            .get()
            .addOnSuccessListener { firstAidResult ->
                // Then get all modules to see which topics have modules
                db.collection("Learning")
                    .get()
                    .addOnSuccessListener { moduleResult ->
                        val topicsWithModules = moduleResult.documents.mapNotNull { moduleDoc ->
                            moduleDoc.getString("firstAidId")
                        }.toSet()
                        
                        // Check if there are topics with modules
                        val topicsWithModulesExist = firstAidResult.documents.any { doc ->
                            val firstAidId = doc.getString("firstAidId") ?: doc.id
                            topicsWithModules.contains(firstAidId)
                        }
                        
                        if (topicsWithModulesExist) {
                            // There are topics with modules, allow navigation
                            onEditModule()
                        } else {
                            // No topics with modules, show dialog
                            showNoTopicsForEditModuleDialog = true
                        }
                    }
                    .addOnFailureListener { exception ->
                        // If module collection fails, allow navigation (fallback)
                        onEditModule()
                    }
            }
            .addOnFailureListener { exception ->
                // If first aid collection fails, allow navigation (fallback)
                onEditModule()
            }
    }

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
                                onPrimaryClick = { checkTopicsWithoutModules() },
                                onSecondaryClick = { checkTopicsWithModules() },
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
                                onPrimaryClick = { checkTopicsWithoutExams() },
                                onSecondaryClick = { checkTopicsWithExams() },
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
                                onPrimaryClick = { checkTopicsWithoutBadges() },
                                onSecondaryClick = onEditBadge,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
        
        // Dialog to show when no topics without badges are available
        if (showNoTopicsDialog) {
            Dialog(onDismissRequest = { showNoTopicsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Topics Available",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF856404),
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "All first aid topics already have badges. You cannot add new badges at this time.",
                            fontSize = 14.sp,
                            color = Color(0xFF856404),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showNoTopicsDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.green_primary)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("OK", color = Color.White, fontFamily = cabin)
                        }
                    }
                }
            }
        }
        
        // Dialog to show when no topics without exams are available (for Add Exam)
        if (showNoExamTopicsDialog) {
            Dialog(onDismissRequest = { showNoExamTopicsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Topics Available",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF856404),
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "All first aid topics already have exams. You cannot add new exams at this time.",
                            fontSize = 14.sp,
                            color = Color(0xFF856404),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showNoExamTopicsDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.green_primary)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("OK", color = Color.White, fontFamily = cabin)
                        }
                    }
                }
            }
        }
        
        // Dialog to show when no topics with exams are available (for Edit Exam)
        if (showNoTopicsForEditDialog) {
            Dialog(onDismissRequest = { showNoTopicsForEditDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Topics Available",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF856404),
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No first aid topics have exams yet. You cannot edit exams at this time.",
                            fontSize = 14.sp,
                            color = Color(0xFF856404),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showNoTopicsForEditDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.green_primary)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("OK", color = Color.White, fontFamily = cabin)
                        }
                    }
                }
            }
        }
        
        // Dialog to show when no topics without modules are available (for Add Module)
        if (showNoModuleTopicsDialog) {
            Dialog(onDismissRequest = { showNoModuleTopicsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Topics Available",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF856404),
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "All first aid topics already have modules. You cannot add new modules at this time.",
                            fontSize = 14.sp,
                            color = Color(0xFF856404),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showNoModuleTopicsDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.green_primary)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("OK", color = Color.White, fontFamily = cabin)
                        }
                    }
                }
            }
        }
        
        // Dialog to show when no topics with modules are available (for Edit Module)
        if (showNoTopicsForEditModuleDialog) {
            Dialog(onDismissRequest = { showNoTopicsForEditModuleDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Topics Available",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF856404),
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No first aid topics have modules yet. You cannot edit modules at this time.",
                            fontSize = 14.sp,
                            color = Color(0xFF856404),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showNoTopicsForEditModuleDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.green_primary)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("OK", color = Color.White, fontFamily = cabin)
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

