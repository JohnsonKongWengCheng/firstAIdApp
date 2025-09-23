package com.example.firstaid.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstaid.R

enum class BottomItem { FIRST_AID, AI, LEARN, ACCOUNT }

@Composable
fun BottomBar(
    selected: BottomItem,
    onSelected: (BottomItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp) // thickness of border
                    .align(Alignment.TopCenter)
                    .background(Color.LightGray) // border color
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 35.dp, top = 0.dp)
            ) {
                if (selected == BottomItem.FIRST_AID) {
                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .height(3.dp)
                            .background(Color.Black, RoundedCornerShape(2.dp))
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(
                    iconResId = R.drawable.firstaidkit,
                    label = "FIRST AID",
                    selected = selected == BottomItem.FIRST_AID,
                    onClick = { onSelected(BottomItem.FIRST_AID) }
                )
                BottomBarItem(
                    iconResId = R.drawable.robot,
                    label = "AI",
                    selected = selected == BottomItem.AI,
                    onClick = { onSelected(BottomItem.AI) }
                )
                BottomBarItem(
                    iconResId = R.drawable.book,
                    label = "LEARN",
                    selected = selected == BottomItem.LEARN,
                    onClick = { onSelected(BottomItem.LEARN) }
                )
                BottomBarItem(
                    iconResId = R.drawable.profile,
                    label = "ACCOUNT",
                    selected = selected == BottomItem.ACCOUNT,
                    onClick = { onSelected(BottomItem.ACCOUNT) }
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    iconResId: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
            .then(
                if (selected) {
                    Modifier.background(
                        Color(0xFFE8F5E8), // Light green background for selected item
                        RoundedCornerShape(8.dp)
                    ).padding(vertical = 4.dp, horizontal = 8.dp)
                } else {
                    Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = label,
            modifier = Modifier.size(35.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (selected) Color(0xFF2E7D32) else Color.Black,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BottomBarPreview() {
    BottomBar(
        selected = BottomItem.FIRST_AID, // set default selected item
        onSelected = {} // no-op click handler for preview
    )
}
