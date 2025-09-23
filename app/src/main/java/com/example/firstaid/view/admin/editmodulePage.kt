package com.example.firstaid.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstaid.R
import com.example.firstaid.view.components.TopBarWithBack
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.imePadding

private data class TopicRef(val id: String, val title: String)
private data class ModuleRef(val id: String, val title: String)
private data class EditableStep(
    val id: String?,
    var title: TextFieldValue,
    var content: TextFieldValue,
    var description: TextFieldValue,
    var stepNumber: Int
)

@Composable
fun EditModulePage(
    onBackClick: () -> Unit = {}
) {
    val cabin = FontFamily(Font(R.font.cabin, FontWeight.Bold))
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    // Dropdown data
    var topics by remember { mutableStateOf<List<TopicRef>>(emptyList()) }
    var modules by remember { mutableStateOf<List<ModuleRef>>(emptyList()) }
    var selectedTopic by remember { mutableStateOf<TopicRef?>(null) }
    var selectedModule by remember { mutableStateOf<ModuleRef?>(null) }
    var topicExpanded by remember { mutableStateOf(false) }

    // Steps
    var steps by remember { mutableStateOf<List<EditableStep>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Load topics
    LaunchedEffect(Unit) {
        db.collection("First_Aid").get()
            .addOnSuccessListener { qs ->
                topics = qs.documents.map { d ->
                    TopicRef(id = d.getString("firstAidId") ?: d.id, title = d.getString("title") ?: d.id)
                }.sortedBy { it.title }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    // Load modules when topic selected
    LaunchedEffect(selectedTopic) {
        selectedModule = null
        steps = emptyList()
        if (selectedTopic != null) {
            db.collection("Learning").whereEqualTo("firstAidId", selectedTopic!!.id).get()
                .addOnSuccessListener { qs ->
                    modules = qs.documents.map { d ->
                        ModuleRef(id = d.getString("learningId") ?: d.id, title = d.getString("title") ?: d.id)
                    }.sortedBy { it.title }
                    // Auto-select the only module (or the first one)
                    selectedModule = modules.firstOrNull()
                }
        }
    }

    // Load steps when module selected
    LaunchedEffect(selectedModule) {
        steps = emptyList()
        val mod = selectedModule ?: return@LaunchedEffect
        db.collection("Content").whereEqualTo("learningId", mod.id).get()
            .addOnSuccessListener { qs ->
                val list = qs.documents.mapIndexed { index, d ->
                    EditableStep(
                        id = d.id,
                        title = TextFieldValue(d.getString("title") ?: ""),
                        content = TextFieldValue(d.getString("content") ?: ""),
                        description = TextFieldValue(d.getString("description") ?: ""),
                        stepNumber = (d.getLong("stepNumber")?.toInt()) ?: (index + 1)
                    )
                }.sortedBy { it.stepNumber }
                steps = list.ifEmpty { listOf(EditableStep(null, TextFieldValue(""), TextFieldValue(""), TextFieldValue(""), 1)) }
            }
    }

    fun addEmptyStep() {
        val next = (steps.maxOfOrNull { it.stepNumber } ?: 0) + 1
        steps = steps + EditableStep(null, TextFieldValue(""), TextFieldValue(""), TextFieldValue(""), next)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarWithBack(title = "Edit Module", onBackClick = onBackClick)

            Divider(color = Color(0xFFB8B8B8), thickness = 1.dp, modifier = Modifier.padding(horizontal = 11.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green_primary))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 120.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Topic dropdown
                    Text(text = "First Aid Topic Title", fontSize = 16.sp, color = Color.Black, fontFamily = cabin)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .background(Color(0xFFECF0EC), RoundedCornerShape(10.dp))
                            .clickable { topicExpanded = true },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedTopic?.title ?: "Choose the First Aid Topic Title",
                                color = if (selectedTopic == null) Color(0xFFAAAAAA) else Color.Black,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.Black)
                        }
                        DropdownMenu(expanded = topicExpanded, onDismissRequest = { topicExpanded = false }) {
                            topics.forEach { item ->
                                DropdownMenuItem(text = { Text(item.title) }, onClick = {
                                    selectedTopic = item
                                    topicExpanded = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Module (auto-selected): show read-only field
                    if (selectedModule != null) {
                        Text(text = "Module", fontSize = 16.sp, color = Color.Black, fontFamily = cabin)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .background(Color(0xFFECF0EC), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedModule?.title ?: "",
                                    color = Color.Black,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Steps editor (once module selected)
                    if (selectedModule != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        steps.forEachIndexed { index, step ->
                            Text(text = "Step ${index + 1}", fontSize = 16.sp, color = Color.Black, fontFamily = cabin)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Title:", fontSize = 16.sp, color = Color.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = step.title,
                                onValueChange = { steps = steps.toMutableList().apply { this[index] = this[index].copy(title = it) } },
                                placeholder = { Text("Enter step title", color = Color(0xFFAAAAAA)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                    unfocusedContainerColor = Color(0xFFECF0EC),
                                    focusedContainerColor = Color(0xFFE6F3E6)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Content:", fontSize = 16.sp, color = Color.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = step.content,
                                onValueChange = { steps = steps.toMutableList().apply { this[index] = this[index].copy(content = it) } },
                                placeholder = { Text("Enter step content", color = Color(0xFFAAAAAA)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                    unfocusedContainerColor = Color(0xFFECF0EC),
                                    focusedContainerColor = Color(0xFFE6F3E6)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Description:", fontSize = 16.sp, color = Color.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = step.description,
                                onValueChange = { steps = steps.toMutableList().apply { this[index] = this[index].copy(description = it) } },
                                placeholder = { Text("Enter the description here..", color = Color(0xFFAAAAAA)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = colorResource(id = R.color.green_primary).copy(alpha = 0.4f),
                                    unfocusedContainerColor = Color(0xFFECF0EC),
                                    focusedContainerColor = Color(0xFFE6F3E6)
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            Divider(color = Color(0xFFB8B8B8), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        Button(
                            onClick = { addEmptyStep() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6F3E6)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Add Step", color = colorResource(id = R.color.green_primary))
                        }
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        // Bottom Confirm Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .imePadding()
                .padding(bottom = 16.dp)
        ) {
            Divider(color = Color(0xFFB8B8B8), thickness = 1.dp, modifier = Modifier.padding(horizontal = 11.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val module = selectedModule
                    if (!isSaving && module != null) {
                        isSaving = true
                        // Save steps to Content (update existing by id, add new for null id)
                        val batch = db.batch()
                        steps.forEachIndexed { idx, s ->
                            val docRef = if (s.id != null) db.collection("Content").document(s.id) else db.collection("Content").document()
                            val data = hashMapOf(
                                "learningId" to module.id,
                                "title" to s.title.text.trim(),
                                "content" to s.content.text.trim(),
                                "description" to s.description.text.trim(),
                                "stepNumber" to (idx + 1)
                            )
                            batch.set(docRef, data, com.google.firebase.firestore.SetOptions.merge())
                        }
                        batch.commit()
                            .addOnSuccessListener {
                                showSuccess = true
                                scope.launch {
                                    kotlinx.coroutines.delay(1000)
                                    showSuccess = false
                                    onBackClick()
                                }
                            }
                            .addOnFailureListener { isSaving = false }
                    }
                },
                enabled = !isSaving && selectedModule != null,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.green_primary),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = if (isSaving) "Saving..." else "Confirm", color = Color.White, fontFamily = cabin)
            }
        }

        // Success overlay
        if (showSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
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
                            .padding(32.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Success",
                            tint = colorResource(id = R.color.green_primary),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Module Updated Successfully!",
                            color = colorResource(id = R.color.green_primary),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = cabin
                        )
                    }
                }
            }
        }
    }
}