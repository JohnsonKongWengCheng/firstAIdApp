package com.example.firstaid.view.admin

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstaid.R
import com.example.firstaid.view.components.TopBarWithBack
import com.example.firstaid.viewmodel.admin.AdminMainViewModel

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
    viewModel: AdminMainViewModel = viewModel()
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
    val docId = prefs.getString("docId", null)
    val userId = prefs.getString("userId", null)

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(docId, userId) {
        viewModel.checkAdminStatus(userId, docId)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
    BackHandler(onBack = onBackClick)

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(
            title = "Admin",
            onBackClick = onBackClick
        )

            if (uiState.isChecking) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green_primary))
                }
            } else if (!uiState.isAdmin) {
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
                                onPrimaryClick = { viewModel.checkTopicsWithoutModules(onAddModule) },
                                onSecondaryClick = { viewModel.checkTopicsWithModules(onEditModule) },
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
                                onPrimaryClick = { viewModel.checkTopicsWithoutExams(onAddExam) },
                                onSecondaryClick = { viewModel.checkTopicsWithExams(onEditExam) },
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
                                onPrimaryClick = { viewModel.checkTopicsWithoutBadges(onAddBadge) },
                                onSecondaryClick = onEditBadge,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
        
        // Dialog to show when no topics without badges are available
        if (uiState.showNoTopicsDialog) {
            Dialog(onDismissRequest = { viewModel.dismissNoTopicsDialog() }) {
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
                            onClick = { viewModel.dismissNoTopicsDialog() },
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
        if (uiState.showNoExamTopicsDialog) {
            Dialog(onDismissRequest = { viewModel.dismissNoExamTopicsDialog() }) {
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
                            onClick = { viewModel.dismissNoExamTopicsDialog() },
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
        if (uiState.showNoTopicsForEditDialog) {
            Dialog(onDismissRequest = { viewModel.dismissNoTopicsForEditDialog() }) {
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
                            onClick = { viewModel.dismissNoTopicsForEditDialog() },
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
        if (uiState.showNoModuleTopicsDialog) {
            Dialog(onDismissRequest = { viewModel.dismissNoModuleTopicsDialog() }) {
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
                            onClick = { viewModel.dismissNoModuleTopicsDialog() },
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
        if (uiState.showNoTopicsForEditModuleDialog) {
            Dialog(onDismissRequest = { viewModel.dismissNoTopicsForEditModuleDialog() }) {
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
                            onClick = { viewModel.dismissNoTopicsForEditModuleDialog() },
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

