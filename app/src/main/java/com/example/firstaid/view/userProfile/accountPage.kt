package com.example.firstaid.view.userProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.example.firstaid.viewmodel.userProfile.AccountViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.firstaid.view.components.TopBar

@Composable
fun AccountPage(
    onSelectBottom: (BottomItem) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onMyProfileClick: () -> Unit = {},
    onMyBadgesClick: () -> Unit = {},
    onContactUsClick: () -> Unit = {},
    viewModel: AccountViewModel = viewModel()
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
    
    val docId = prefs.getString("docId", null)
    val userId = prefs.getString("userId", null)
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Load user data from ViewModel
    LaunchedEffect(docId, userId) {
        viewModel.loadUserData(docId, userId)
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
            TopBar()

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(43.dp))
                
                // Profile Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Color(0xFFECECEC),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!uiState.profileImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = uiState.profileImageUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.logo),
                            placeholder = null
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(50.dp),
                            tint = Color.Black
                        )
                    }
                }
                    
                    Spacer(modifier = Modifier.width(31.dp))
                    
                    // User Info
                    Column {
                        Text(
                            text = uiState.userName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = cabin
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = uiState.userEmail,
                            fontSize = 15.sp,
                            color = Color(0xFF747474),
                            fontFamily = cabin
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                
                // Divider line
                Divider(
                    color = Color(0xFFB8B8B8),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(30.dp))
                
                // Menu Items
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // My Profile
                    MenuItem(
                        icon = Icons.Default.Person,
                        title = "My Profile",
                        onClick = onMyProfileClick
                    )
                    
                    // My Badges
                    MenuItem(
                        icon = Icons.Default.Star,
                        title = "My Badges",
                        onClick = onMyBadgesClick
                    )
                    
                    // Contact Us
                    MenuItem(
                        icon = Icons.Default.Phone,
                        title = "Contact Us",
                        onClick = onContactUsClick
                    )
                    
                    // Log Out
                    MenuItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Log Out",
                        onClick = onLogoutClick,
                        isDestructive = true
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
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
private fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val borderColor = if (isDestructive) Color.Red else colorResource(id = R.color.green_primary)
    val textColor = if (isDestructive) Color.Red else colorResource(id = R.color.green_primary)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon background circle
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        borderColor,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // Title
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontFamily = FontFamily(Font(R.font.cabin, FontWeight.Bold))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountPagePreview() {
    AccountPage()
}
