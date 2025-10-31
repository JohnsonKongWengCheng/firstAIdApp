package com.example.firstaid.view.firstAid

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.google.firebase.firestore.FirebaseFirestore

private data class FirstAid(val id: String, val title: String)

@Composable
fun FirstAidListPage(
    onItemClick: (String) -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {}
) {
    val context = LocalContext.current
    val cabin = FontFamily(
        Font(R.font.cabin, FontWeight.Bold)
    )

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val items = remember { mutableStateListOf<FirstAid>() }
    val topicsWithContent = remember { mutableStateSetOf<String>() }

    // Check for topics that have both Learning modules AND Content entries
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        var learningRegistration: com.google.firebase.firestore.ListenerRegistration? = null
        var firstAidRegistration: com.google.firebase.firestore.ListenerRegistration? = null
        
        // Query Learning collection to find topics with learning modules
        learningRegistration = db.collection("Learning")
            .addSnapshotListener { learningSnapshot, learningError ->
                if (learningError != null) {
                    errorMessage = learningError.localizedMessage ?: "Failed to load learning data"
                    isLoading = false
                    return@addSnapshotListener
                }
                
                val learningDocs = learningSnapshot?.documents ?: emptyList()
                val learningIds = mutableSetOf<String>()
                val firstAidIds = mutableSetOf<String>()
                
                for (doc in learningDocs) {
                    val firstAidId = (doc.get("firstAidId") as? String)?.trim()
                    val learningId = (doc.get("learningId") as? String)?.trim()
                    if (!firstAidId.isNullOrEmpty() && !learningId.isNullOrEmpty()) {
                        firstAidIds.add(firstAidId)
                        learningIds.add(learningId)
                    }
                }
                
                // Now check which learning modules have actual content
                if (learningIds.isNotEmpty()) {
                    db.collection("Content")
                        .whereIn("learningId", learningIds.toList())
                        .get()
                        .addOnSuccessListener { contentSnapshot ->
                            val contentLearningIds = contentSnapshot.documents.mapNotNull { doc ->
                                doc.getString("learningId")?.trim()
                            }.toSet()
                            
                            // Find which firstAidIds have both learning modules and content
                            topicsWithContent.clear()
                            for (doc in learningDocs) {
                                val firstAidId = (doc.get("firstAidId") as? String)?.trim()
                                val learningId = (doc.get("learningId") as? String)?.trim()
                                if (!firstAidId.isNullOrEmpty() && 
                                    !learningId.isNullOrEmpty() && 
                                    contentLearningIds.contains(learningId)) {
                                    topicsWithContent.add(firstAidId)
                                }
                            }
                            
                            // Now query First_Aid collection and filter by topics with content
                            firstAidRegistration = db.collection("First_Aid")
                                .addSnapshotListener { snapshot, e ->
                                    if (e != null) {
                                        errorMessage = e.localizedMessage ?: "Failed to load First Aid list"
                                        isLoading = false
                                        return@addSnapshotListener
                                    }
                                    
                                    val docs = snapshot?.documents ?: emptyList()
                                    items.clear()
                                    for (doc in docs) {
                                        val title = (doc.get("title") as? String)?.trim().orEmpty()
                                        val firstAidId = (doc.get("firstAidId") as? String)?.trim().orEmpty()
                                        // Use firstAidId field as the ID, fallback to document id if not available
                                        val resolvedId = if (firstAidId.isNotEmpty()) firstAidId else doc.id
                                        val resolvedTitle = if (title.isNotEmpty()) title else doc.id
                                        
                                        // Only add topics that have both learning modules and content
                                        if (topicsWithContent.contains(resolvedId)) {
                                            println("DEBUG: FirstAidListPage - doc.id: '${doc.id}', firstAidId: '$firstAidId', title: '$resolvedTitle', resolvedId: '$resolvedId'")
                                            items.add(FirstAid(id = resolvedId, title = resolvedTitle))
                                        }
                                    }
                                    errorMessage = null
                                    isLoading = false
                                }
                        }
                        .addOnFailureListener { e ->
                            errorMessage = e.localizedMessage ?: "Failed to load content data"
                            isLoading = false
                        }
                } else {
                    // No learning modules found, clear items
                    items.clear()
                    errorMessage = null
                    isLoading = false
                }
            }
        
        onDispose { 
            learningRegistration?.remove()
            firstAidRegistration?.remove()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Spacer(modifier = Modifier.height(20.dp))

        TopBar()

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.weight(1f).padding(horizontal = 24.dp)) {
            //call ambulance button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp)
                    .shadow(4.dp, RoundedCornerShape(10.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:999")
                        }
                        context.startActivity(intent)
                    },
                colors = CardDefaults.cardColors(containerColor = Color.Red),
                shape = RoundedCornerShape(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorResource(id = R.color.green_primary))
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
                else -> {
                    items.forEachIndexed { index, item ->
                        FirstAidItem(
                            title = item.title,
                            onClick = { 
                                println("DEBUG: FirstAidListPage - Clicked item.id: '${item.id}'")
                                onItemClick(item.id) 
                            }
                        )
                        if (index != items.lastIndex) {
                            Spacer(modifier = Modifier.height(13.dp))
                        }
                    }
                }
            }
        }

        BottomBar(selected = BottomItem.FIRST_AID, onSelected = onSelectBottom)
    }
}

@Composable
private fun FirstAidItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp)
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clickable { 
                println("DEBUG: FirstAidItem clicked - title: '$title'")
                onClick() 
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorResource(id = R.color.green_primary))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
            Text(text = title, color = colorResource(id = R.color.green_primary), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FirstAidListPreview() {
    FirstAidListPage()
}

