package com.example.firebasenoteapp

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebasenoteapp.login.and.signup.BACKGROUND_COLOUR
import com.example.firebasenoteapp.ui.theme.FirebaseNoteAppTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class AddNoteActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirebaseNoteAppTheme {
                val bundleUserName = intent.getBundleExtra("KEY_BUNDLE_USERNAME")!!
                val userName = bundleUserName.getString("KEY_USERNAME")!!
                NoteInputScreen(db, appViewModel, userName)
            }
        }
    }
}


@Composable
fun NoteInputScreen(db: FirebaseFirestore, appViewModel: AppViewModel, userName: String) {
    val context = LocalContext.current

    var noteTitleInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    // This is how we force back button to go to a new activity.
    BackHandler(
        onBack = {
            launchHomePageActivity(context, userName)
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOUR),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Note title input
        TextField(
            value = noteTitleInput,
            onValueChange = { noteTitleInput = it },
            colors = TextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                unfocusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,

                focusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            placeholder = { Text(text = "Title", fontSize = 30.sp) },
            textStyle = TextStyle(fontSize = 30.sp),
            modifier = Modifier
                .fillMaxWidth()
        )

        // Actual note taking input
        TextField(
            value = noteInput,
            onValueChange = { noteInput = it },
            colors = TextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                unfocusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,

                focusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            placeholder = { Text(text = "Note", fontSize = 20.sp) },
            textStyle = TextStyle(fontSize = 20.sp),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Discard note button
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xffff5703),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10),
                onClick = {
                    // Just go back to home page
                    launchHomePageActivity(context, userName)
                },
                modifier = Modifier.padding(bottom = 5.dp)
            ) {
                Text(text = "Discard Note", fontSize = 18.sp)
            }

            // Save Note button
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10),
                onClick = {
                    // Store note title and note content as data.
                    appViewModel.noteTitle = noteTitleInput
                    appViewModel.noteContent = noteInput

                    val note = hashMapOf(
                        "title" to appViewModel.noteTitle,
                        "content" to appViewModel.noteContent
                    )

                    // We add a note to the cloud database.
                    db.collection("users").document(userName)
                        .collection("notes").add(note)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }

                    // Once we added stuff we just exit this screen back to the homepage.
                    launchHomePageActivity(context, userName)
                },
                modifier = Modifier.padding(bottom = 5.dp)
            ) {
                Text(text = "Save Note", fontSize = 18.sp)
            }
        }
    }
}


private fun launchHomePageActivity(context: Context, userName: String) {
    val intent = Intent(context, HomePageActivity::class.java)
    val bundle = Bundle()

    bundle.putString("KEY_USERNAME", userName)
    intent.putExtra("KEY_BUNDLE_USERNAME", bundle)
    context.startActivity(intent)
}