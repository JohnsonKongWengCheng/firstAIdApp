package com.example.firstaid.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopBar() {
    Spacer(modifier = Modifier.height(35.dp))
}

@Composable
fun TopBarWithBack(
    title: String,
    onBackClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(35.dp))
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Title centered
            Text(
                text = title,
                fontSize = 27.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF4DB648)
            )

            // Back button aligned to start
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart).size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF4DB648),
                    modifier = Modifier.size(24.dp)
                )
            }
        }


        // Divider line
        Divider(
            color = Color(0xFFB8B8B8), // Gray color from Figma
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 11.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TopBarPreview() {
    TopBarWithBack(
        title = "",
        onBackClick = {}
    )
}