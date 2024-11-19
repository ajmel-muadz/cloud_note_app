package com.example.firebasenoteapp

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebasenoteapp.login.and.signup.BACKGROUND_COLOUR
import com.example.firebasenoteapp.ui.theme.FirebaseNoteAppTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class EditNoteActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirebaseNoteAppTheme {
                val bundleNote = intent.getBundleExtra("KEY_BUNDLE_NOTE")!!
                NoteInputScreen(db, appViewModel, bundleNote)
            }
        }
    }
}


@Composable
fun NoteInputScreen(db: FirebaseFirestore, appViewModel: AppViewModel, bundleNote: Bundle) {
    val context = LocalContext.current

    val noteId = bundleNote.getString("KEY_NOTE_ID")!!
    val noteTitle = bundleNote.getString("KEY_NOTE_TITLE")!!
    val noteContent = bundleNote.getString("KEY_NOTE_CONTENT")!!
    val userName = bundleNote.getString("KEY_USERNAME")!!

    // Basically we are setting the last saved text to the edit inputs,
    // hence why mutableStateOf(noteTitle) is not mutableStateOf(""). Same for noteInput variable.
    var noteTitleInput by remember { mutableStateOf(noteTitle) }
    var noteInput by remember { mutableStateOf(noteContent) }

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
            // Delete note button
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10),
                onClick = {
                    // We delete the note we do not want anymore.
                    db.collection("users").document(userName)
                        .collection("notes").document(noteId)
                        .delete()
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!")
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e)
                        }

                    // Once we click the delete button we just exit back to home page.
                    launchHomePageActivity(context, userName)
                },
                modifier = Modifier.padding(bottom = 5.dp)
            ) {
                Text(text = "Delete Note", fontSize = 18.sp)
            }

            // Save Note button
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10),
                onClick = {
                    appViewModel.noteTitle = noteTitleInput
                    appViewModel.noteContent = noteInput

                    val note = hashMapOf(
                        "title" to appViewModel.noteTitle,
                        "content" to appViewModel.noteContent
                    )

                    // We edit the correct note in the database.
                    db.collection("users").document(userName)
                        .collection("notes").document(noteId)
                        .set(note)
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!")
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)
                        }

                    // Once we edited the note we just exit back to the home page.
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