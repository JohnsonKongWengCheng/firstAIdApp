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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.SharedPreferences
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class for badge
data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val isEarned: Boolean = false,
    val earnedDate: String? = null
)

@Composable
fun MyBadgesPage(
    onBackClick: () -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
    val currentUserId = sharedPreferences.getString("userId", null)
    val db = FirebaseFirestore.getInstance()
    
    // State for badges
    var badges by remember { mutableStateOf<List<Badge>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var allBadges by remember { mutableStateOf<List<Badge>>(emptyList()) }
    var userBadges by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Load badges from Firebase
    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            // Load all badges from Badge table
            db.collection("Badge")
                .get()
                .addOnSuccessListener { badgeDocuments ->
                    val badgeList = badgeDocuments.mapNotNull { doc ->
                        Badge(
                            id = doc.getString("badgeId") ?: doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: ""
                        )
                    }
                    allBadges = badgeList
                    
                    // Load user's earned badges from User_Badge table
                    db.collection("User_Badge")
                        .whereEqualTo("userId", currentUserId)
                        .get()
                        .addOnSuccessListener { userBadgeDocuments ->
                            val earnedBadgeIds = userBadgeDocuments.mapNotNull { doc ->
                                doc.getString("badgeId")
                            }
                            userBadges = earnedBadgeIds
                            
                            android.util.Log.d("MyBadgesPage", "Current userId: $currentUserId")
                            android.util.Log.d("MyBadgesPage", "Found ${userBadgeDocuments.size()} earned badges")
                            android.util.Log.d("MyBadgesPage", "Earned badge IDs: $earnedBadgeIds")
                            
                            // Only show badges that the user has actually earned
                            badges = userBadgeDocuments.mapNotNull { userBadgeDoc ->
                                val badgeId = userBadgeDoc.getString("badgeId")
                                val earnedDate = userBadgeDoc.getTimestamp("earnedDate")?.toDate()
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                
                                // Find the badge details from the badge list
                                val badgeDetails = badgeList.find { it.id == badgeId }
                                if (badgeDetails != null) {
                                    Badge(
                                        id = badgeDetails.id,
                                        title = badgeDetails.title,
                                        description = badgeDetails.description,
                                        isEarned = true,
                                        earnedDate = earnedDate?.let { dateFormat.format(it) }
                                    )
                                } else {
                                    null
                                }
                            }
                            isLoading = false
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("MyBadgesPage", "Failed to load user badges: ${e.localizedMessage}")
                            badges = emptyList() // Show no badges if failed to load user badges
                            isLoading = false
                        }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("MyBadgesPage", "Failed to load badges: ${e.localizedMessage}")
                    isLoading = false
                }
        } else {
            android.util.Log.e("MyBadgesPage", "No current user ID found")
            badges = emptyList() // Show no badges if no user ID
            isLoading = false
        }
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.green_primary)
                    )
                }
            } else {
                if (badges.isEmpty()) {
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
                        items(badges) { badge ->
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
    badge: Badge,
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
