package com.example.firstaid.view.userProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstaid.R

@Composable
fun LoginSignupPage(
    onLoginClick: () -> Unit = {},
    onSignupClick: () -> Unit = {}
) {
    val mateSC = FontFamily(
        Font(R.font.matesc_regular, FontWeight.Bold)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        //Background Image
        Image(
            painter = painterResource(id = R.drawable.cprstickman),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(870.dp)
                .offset(y = 47.dp)
                .background(colorResource(id = R.color.green_primary).copy(alpha = 0.7f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(200.dp))

            //logo
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .shadow(4.dp, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            //app name
            Box { Image(painter = painterResource(id = R.drawable.appname), contentDescription = null) }

            Spacer(modifier = Modifier.height(20.dp))

            //log in button
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .width(209.dp)
                    .height(72.dp)
                    .shadow(8.dp, RoundedCornerShape(10.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Log In", fontSize = 25.sp, fontFamily = mateSC, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(25.dp))

            //sign up button
            Button(
                onClick = onSignupClick,
                modifier = Modifier
                    .width(209.dp)
                    .height(72.dp)
                    .shadow(8.dp, RoundedCornerShape(10.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Sign Up", fontSize = 25.sp, fontFamily = mateSC, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginSignupPreview() {
    LoginSignupPage()
}