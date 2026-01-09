package com.example.firstaid.view.userProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.firstaid.R
import com.example.firstaid.view.components.BottomBar
import com.example.firstaid.view.components.BottomItem
import com.example.firstaid.view.components.TopBarWithBack
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstaid.viewmodel.userProfile.MyBadgesViewModel
import com.example.firstaid.model.userProfile.BadgeData

@Composable
fun MyBadgesPage(
    onBackClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {},
    viewModel: MyBadgesViewModel = viewModel()
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
    val currentUserId = sharedPreferences.getString("userId", null)
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Load badges from ViewModel
    LaunchedEffect(currentUserId) {
        viewModel.loadBadges(currentUserId)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopBarWithBack(
                title = "My Badges",
                onBackClick = onBackClick
            )

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {

            Spacer(modifier = Modifier.height(41.dp))
            
            // Badges List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.green_primary)
                    )
                }
            } else {
                if (uiState.badges.isEmpty()) {
                    // Show message when user has no badges
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.StarBorder,
                                contentDescription = "No badges",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Badges Yet",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = cabin
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Complete exams to earn your first badge!",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontFamily = cabin,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.badges) { badge ->
                            BadgeCard(
                                badge = badge,
                                fontFamily = cabin
                            )
                        }
                    }
                }
            }
            }
        }
        
        // Bottom Bar - positioned at bottom
        BottomBar(
            selected = BottomItem.ACCOUNT,
            onSelected = onSelectBottom,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun BadgeCard(
    badge: BadgeData,
    fontFamily: FontFamily
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(4.dp, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            colorResource(id = R.color.green_primary)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(19.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Badge Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        if (badge.isEarned) colorResource(id = R.color.green_primary) else Color(0xFFE0E0E0),
                        RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (badge.isEarned) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Badge",
                    modifier = Modifier.size(30.dp),
                    tint = if (badge.isEarned) Color.White else Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Badge Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = badge.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = fontFamily
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = badge.description,
                    fontSize = 17.sp,
                    color = Color.Black,
                    fontFamily = fontFamily,
                    lineHeight = 20.sp
                )
                
                if (badge.isEarned && badge.earnedDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Earned on ${badge.earnedDate}",
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.green_primary),
                        fontFamily = fontFamily
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyBadgesPagePreview() {
    MyBadgesPage()
}
