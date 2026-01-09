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
import com.example.firstaid.viewmodel.firstAid.FirstAidListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FirstAidListPage(
    viewModel: FirstAidListViewModel = viewModel(),
    onItemClick: (String) -> Unit = {},
    onSelectBottom: (BottomItem) -> Unit = {}
) {
    val context = LocalContext.current
    val cabin = FontFamily(
        Font(R.font.cabin, FontWeight.Bold)
    )

    val uiState by viewModel.uiState.collectAsState()

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
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorResource(id = R.color.green_primary))
                    }
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
                else -> {
                    uiState.items.forEachIndexed { index, item ->
                        FirstAidItem(
                            title = item.title,
                            onClick = { 
                                println("DEBUG: FirstAidListPage - Clicked item.id: '${item.id}'")
                                onItemClick(item.id) 
                            }
                        )
                        if (index != uiState.items.lastIndex) {
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

