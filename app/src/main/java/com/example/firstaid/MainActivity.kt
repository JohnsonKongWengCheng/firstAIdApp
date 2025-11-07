package com.example.firstaid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import com.example.firstaid.ui.theme.FirstAIdTheme
import com.example.firstaid.view.firstAid.FirstAidDetailsPage
import com.example.firstaid.view.firstAid.FirstAidListPage
import com.example.firstaid.view.building.LearnExamPage
import com.example.firstaid.view.building.LearnDetailsPage
import com.example.firstaid.view.building.ExamDetailsPage
import com.example.firstaid.view.userProfile.AccountPage
import com.example.firstaid.view.userProfile.ContactUsPage
import com.example.firstaid.view.userProfile.LoginPage
import com.example.firstaid.view.userProfile.LoginSignupPage
import com.example.firstaid.view.userProfile.MyBadgesPage
import com.example.firstaid.view.userProfile.MyProfilePage
import com.example.firstaid.view.userProfile.SignUpPage
import com.example.firstaid.view.ai.AiPage
import com.example.firstaid.view.admin.AdminMainPage
import com.example.firstaid.view.admin.AddTopicPage
import com.example.firstaid.view.admin.EditTopicPage
import com.example.firstaid.view.admin.AddModulePage
import com.example.firstaid.view.admin.EditModulePage
import com.example.firstaid.view.admin.AddExamPage
import com.example.firstaid.view.admin.EditExamPage
import com.example.firstaid.view.admin.AddBadgePage
import com.example.firstaid.view.admin.EditBadgePage

@Composable
fun GateCheckAndNavigate(target: String, navController: androidx.navigation.NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(target) {
        val prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val docId = prefs.getString("docId", null)
        if (docId == null) {
            navController.navigate("login_signup?redirect=$target") { popUpTo("firstaidlist") { inclusive = false } }
            return@LaunchedEffect
        }
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("User").document(docId).get()
                .addOnSuccessListener { doc ->
                    val loggedIn = doc.getBoolean("login") == true
                    if (loggedIn) {
                        navController.navigate(target) { popUpTo("firstaidlist") { inclusive = false } }
                    } else {
                        navController.navigate("login_signup?redirect=$target") { popUpTo("firstaidlist") { inclusive = false } }
                    }
                }
                .addOnFailureListener {
                    navController.navigate("login_signup?redirect=$target") { popUpTo("firstaidlist") { inclusive = false } }
                }
        } catch (_: Exception) {
            navController.navigate("login_signup?redirect=$target") { popUpTo("firstaidlist") { inclusive = false } }
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF2E7D32))
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstAIdTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            // Simple Compose splash that shows logo then navigates
                            LaunchedEffect(Unit) {
                                delay(1000)
                                navController.navigate("firstaidlist") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = null
                                )
                            }
                        }
                        composable(
                            "login_signup",
                            arguments = listOf(navArgument("redirect") {
                                type = NavType.StringType
                                nullable = true
                            })
                        ) { backStackEntry ->
                            val redirect = backStackEntry.arguments?.getString("redirect")
                            LoginSignupPage(
                                onLoginClick = { 
                                    navController.navigate("login${if (redirect != null) "?redirect=$redirect" else ""}")
                                },
                                onSignupClick = { 
                                    navController.navigate("signup${if (redirect != null) "?redirect=$redirect" else ""}")
                                }
                            )
                        }
                        composable(
                            route = "login?redirect={redirect}",
                            arguments = listOf(navArgument("redirect") {
                                type = NavType.StringType; defaultValue = ""
                            })
                        ) { backStackEntry ->
                            val redirect = backStackEntry.arguments?.getString("redirect").orEmpty()
                            LoginPage(
                                redirectTo = redirect.ifBlank { null },
                                onLoginSuccess = {
                                    // After login, check if user is admin; if so, go to admin
                                    val context = this@MainActivity
                                    val prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                                    val userId = prefs.getString("userId", null)
                                    if (userId != null) {
                                        try {
                                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            db.collection("Admin").whereEqualTo("userId", userId).limit(1).get()
                                                .addOnSuccessListener { qs ->
                                                    if (!qs.isEmpty) {
                                                        navController.navigate("admin") { popUpTo("firstaidlist") { inclusive = false } }
                                                    } else {
                                                        val target = when (redirect) {
                                                            "learn" -> "learn"
                                                            "account" -> "account"
                                                            else -> "firstaidlist"
                                                        }
                                                        navController.navigate(target) { popUpTo("firstaidlist") { inclusive = false } }
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    val target = when (redirect) {
                                                        "learn" -> "learn"
                                                        "account" -> "account"
                                                        else -> "firstaidlist"
                                                    }
                                                    navController.navigate(target) { popUpTo("firstaidlist") { inclusive = false } }
                                                }
                                        } catch (_: Exception) {
                                            val target = when (redirect) {
                                                "learn" -> "learn"
                                                "account" -> "account"
                                                else -> "firstaidlist"
                                            }
                                            navController.navigate(target) { popUpTo("firstaidlist") { inclusive = false } }
                                        }
                                    } else {
                                        val target = when (redirect) {
                                            "learn" -> "learn"
                                            "account" -> "account"
                                            else -> "firstaidlist"
                                        }
                                        navController.navigate(target) { popUpTo("firstaidlist") { inclusive = false } }
                                    }
                                },
                                onSignupClick = { navController.navigate("signup") }
                            )
                        }
                        composable("signup") {
                            SignUpPage(
                                onSignupSuccess = {
                                    // TODO: Navigate to your app's home screen when ready
                                    navController.popBackStack("login_signup", inclusive = false)
                                },
                                onLoginClick = { navController.navigate("login") }
                            )
                        }
                        composable("ai") {
                            AiPage(
                                onSelectBottom = { bottomItem ->
                                    when (bottomItem) {
                                        com.example.firstaid.view.components.BottomItem.FIRST_AID -> {
                                            navController.navigate("firstaidlist")
                                        }
                                        com.example.firstaid.view.components.BottomItem.AI -> {
                                            // Already on AI page
                                        }
                                        com.example.firstaid.view.components.BottomItem.LEARN -> {
                                            navController.navigate("gate/learn")
                                        }
                                        com.example.firstaid.view.components.BottomItem.ACCOUNT -> {
                                            navController.navigate("gate/account")
                                        }
                                    }
                                },
                                onNavigateToFirstAid = { firstAidId ->
                                    navController.navigate("firstaiddetails/$firstAidId")
                                }
                            )
                        }
                        composable("firstaidlist") {
                            FirstAidListPage(
                                onItemClick = { firstAidId ->
                                    navController.navigate("firstaiddetails/$firstAidId")
                                },
                                onSelectBottom = { bottomItem ->
                                    when (bottomItem) {
                                        com.example.firstaid.view.components.BottomItem.FIRST_AID -> {
                                            // Already on first aid page
                                        }

                                        com.example.firstaid.view.components.BottomItem.AI -> {
                                            navController.navigate("ai")
                                        }

                                        com.example.firstaid.view.components.BottomItem.LEARN -> {
                                            navController.navigate("gate/learn")
                                        }

                                        com.example.firstaid.view.components.BottomItem.ACCOUNT -> {
                                            navController.navigate("gate/account")
                                        }
                                    }
                                }
                            )
                        }
                        composable(
                            "firstaiddetails/{firstAidId}",
                            arguments = listOf(navArgument("firstAidId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val firstAidId = backStackEntry.arguments?.getString("firstAidId") ?: ""
                            FirstAidDetailsPage(
                                firstAidId = firstAidId,
                                onBackClick = { navController.popBackStack() },
                                onCompleteClick = { navController.popBackStack() },
                                onSelectBottom = { bottomItem ->
                                    when (bottomItem) {
                                        com.example.firstaid.view.components.BottomItem.FIRST_AID -> {
                                            navController.navigate("firstaidlist")
                                        }
                                        com.example.firstaid.view.components.BottomItem.AI -> {
                                            navController.navigate("ai")
                                        }
                                        com.example.firstaid.view.components.BottomItem.LEARN -> {
                                            navController.navigate("learn")
                                        }
                                        com.example.firstaid.view.components.BottomItem.ACCOUNT -> {
                                            navController.navigate("account")
                                        }
                                    }
                                }
                            )
                        }
                        composable(
                            "learndetails/{learningId}",
                            arguments = listOf(navArgument("learningId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val learningId = backStackEntry.arguments?.getString("learningId") ?: ""
                            LearnDetailsPage(
                                learningId = learningId,
                                onBackClick = { navController.popBackStack() },
                                onSelectBottom = { bottomItem ->
                                    when (bottomItem) {
                                        com.example.firstaid.view.components.BottomItem.FIRST_AID -> {
                                            navController.navigate("firstaidlist")
                                        }
                                        com.example.firstaid.view.components.BottomItem.AI -> {
                                            navController.navigate("ai")
                                        }
                                        com.example.firstaid.view.components.BottomItem.LEARN -> {
                                            navController.navigate("learn")
                                        }
                                        com.example.firstaid.view.components.BottomItem.ACCOUNT -> {
                                            navController.navigate("account")
                                        }
                                    }
                                }
                            )
                        }
                        composable(
                            "examdetails/{examId}",
                            arguments = listOf(navArgument("examId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val examId = backStackEntry.arguments?.getString("examId") ?: ""
                            ExamDetailsPage(
                                examId = examId,
                                onBackClick = { navController.popBackStack() },
                                onSelectBottom = { bottomItem ->
                                    when (bottomItem) {
                                        com.example.firstaid.view.components.BottomItem.FIRST_AID -> {
                                            navController.navigate("firstaidlist")
                                        }
                                        com.example.firstaid.view.components.BottomItem.AI -> {
                                            navController.navigate("ai")
                                        }
                                        com.example.firstaid.view.components.BottomItem.LEARN -> {
                                            navController.navigate("learn")
                                        }
                                        com.example.firstaid.view.components.BottomItem.ACCOUNT -> {
                                            navController.navigate("account")
                                        }
                                    }
                                },
                                onSubmitClick = { 
                                    // TODO: Handle exam submission
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("account") {
                            AccountPage(
                                onSelectBottom = { bottomItem ->
                                    when (bottomItem) {
                                        com.example.firstaid.view.components.BottomItem.FIRST_AID -> {
                                            navController.navigate("firstaidlist")
                                        }

                                        com.example.firstaid.view.components.BottomItem.AI -> {
                                            navController.navigate("ai")
                                        }

                                        com.example.firstaid.view.components.BottomItem.LEARN -> {
                                            navController.navigate("learn")
                                        }

                                        com.example.firstaid.view.components.BottomItem.ACCOUNT -> {
                                            // Already on account page
                                        }
                                    }
                                },
                                onLogoutClick = {
                                    // Update login=false in Firestore and clear local session
                                    val prefs = this@MainActivity.getSharedPreferences(
                                        "user_session",
                                        android.content.Context.MODE_PRIVATE
                                    )
                                    val docId = prefs.getString("docId", null)
                                    if (docId != null) {
                                        try {
                                            val db =
                                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            db.collection("User").document(docId)
                                                .update("login", false)
                                        } catch (_: Exception) {
                                        }
                                    }
                                    prefs.edit().clear().apply()
                                    FirebaseAuth.getInstance().signOut()
                                    navController.navigate("firstaidlist") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onMyProfileClick = {
                                    navController.navigate("myprofile")
                                },
                                onMyBadgesClick = {
                                    navController.navigate("mybadges")
                                },
                                onContactUsClick = {
                                    navController.navigate("contactus")
                                }
                            )
                        }
                        composable("myprofile") {
                            MyProfilePage(
                                onBackClick = { navController.popBackStack() },
                                onSelectBottom = { bottomItem ->
                                    when (bottomItem) {
                                        com.example.firstaid.view.components.BottomItem.FIRST_AID -> {
                                            navController.navigate("firstaidlist")
                                        }

                                        com.example.firstaid.view.components.BottomItem.AI -> {
                                            navController.navigate("ai")
                                        }

                                        com.example.firstaid.view.components.BottomItem.LEARN -> {
                                            navController.navigate("learn")
                                        }

                                        com.example.firstaid.view.components.BottomItem.ACCOUNT -> {
                                            navController.navigate("account")
                                        }
                                    }
                                },
                                onSaveClick = {
                                    navController.popBackStack() // return to Account page
                                }
                            )
                        }
                        composable("mybadges") {
                            MyBadgesPage(
                                onBackClick = { navController.popBackStack() },
                                onSelectBottom = { bottomItem ->
                                    when (bottomItem) {
                                        com.example.firstaid.view.components.BottomItem.FIRST_AID -> {
                                            navController.navigate("firstaidlist")
                                        }

                                        com.example.firstaid.view.components.BottomItem.AI -> {
                                            navController.navigate("ai")
                                        }

                                        com.example.firstaid.view.components.BottomItem.LEARN -> {
                                            navController.navigate("learn")
                                        }

                                        com.example.firstaid.view.components.BottomItem.ACCOUNT -> {
                                            navController.navigate("account")
                                        }
                                    }
                                }
                            )
                        }
                        composable("contactus") {
                            ContactUsPage(
                                onBackClick = { navController.popBackStack() },
                                onSelectBottom = { bottomItem ->
                                    when (bottomItem) {
                                        com.example.firstaid.view.components.BottomItem.FIRST_AID -> {
                                            navController.navigate("firstaidlist")
                                        }

                                        com.example.firstaid.view.components.BottomItem.AI -> {
                                            navController.navigate("ai")
                                        }

                                        com.example.firstaid.view.components.BottomItem.LEARN -> {
                                            navController.navigate("learn")
                                        }

                                        com.example.firstaid.view.components.BottomItem.ACCOUNT -> {
                                            navController.navigate("account")
                                        }
                                    }
                                }
                            )
                        }
                        composable("learn") {
                            LearnExamPage(
                                onSelectBottom = { bottomItem ->
                                    when (bottomItem) {
                                        com.example.firstaid.view.components.BottomItem.FIRST_AID -> {
                                            navController.navigate("firstaidlist")
                                        }

                                        com.example.firstaid.view.components.BottomItem.AI -> {
                                            navController.navigate("ai")
                                        }

                                        com.example.firstaid.view.components.BottomItem.LEARN -> {
                                            // Already on learn page
                                        }

                                        com.example.firstaid.view.components.BottomItem.ACCOUNT -> {
                                            navController.navigate("account")
                                        }
                                    }
                                },
                                onLearnTopicClick = { topicId ->
                                    navController.navigate("learndetails/$topicId")
                                },
                                onExamTopicClick = { topicId ->
                                    navController.navigate("examdetails/$topicId")
                                }
                            )
                        }
                        composable("admin") {
                            AdminMainPage(
                                onBackClick = { navController.popBackStack() },
                                onAddTopic = { navController.navigate("admin_add_topic") },
                                onEditTopic = { navController.navigate("admin_edit_topic") },
                                onAddModule = { navController.navigate("admin_add_module") },
                                onEditModule = { navController.navigate("admin_edit_module") },
                                onAddExam = { navController.navigate("admin_add_exam") },
                                onEditExam = { navController.navigate("admin_edit_exam") },
                                onAddBadge = { navController.navigate("admin_add_badge") },
                                onEditBadge = { navController.navigate("admin_edit_badge") }
                            )
                        }
                        composable("admin_add_topic") {
                            AddTopicPage(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("admin_edit_topic") {
                            EditTopicPage(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("admin_add_module") {
                            AddModulePage(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("admin_edit_module") {
                            EditModulePage(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("admin_add_exam") {
                            AddExamPage(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("admin_edit_exam") {
                            EditExamPage(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("admin_add_badge") {
                            AddBadgePage(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("admin_edit_badge") {
                            EditBadgePage(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        // Gate route to verify Firestore User.login flag before navigation
                        composable(
                            route = "gate/{target}",
                            arguments = listOf(navArgument("target") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val target = backStackEntry.arguments?.getString("target").orEmpty()
                            GateCheckAndNavigate(target = target, navController = navController)
                        }
                    }
                }
            }
        }
    }
}


