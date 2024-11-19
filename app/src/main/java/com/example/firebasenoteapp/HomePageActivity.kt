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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebasenoteapp.login.and.signup.BACKGROUND_COLOUR
import com.example.firebasenoteapp.ui.theme.FirebaseNoteAppTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class HomePageActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirebaseNoteAppTheme {
                val bundleUserName = intent.getBundleExtra("KEY_BUNDLE_USERNAME")!!
                val userName = bundleUserName.getString("KEY_USERNAME")!!
                HomePage(db, appViewModel, userName)
            }
        }
    }
}


@Composable
fun HomePage(db: FirebaseFirestore, appViewModel: AppViewModel, userName: String) {
    val context = LocalContext.current

    // This is how we force back button to go to a new activity.
    BackHandler(
        onBack = {
            launchLoginActivity(context)
        }
    )

    // Create a state to hold the notes
    val notesToDisplay = remember { mutableStateListOf<NoteDisplay>() }

    LaunchedEffect(userName) {
        db.collection("users")
            .document(userName)
            .collection("notes")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val noteId = document.id
                    var noteTitle = document.getString("title")!!
                    var noteContent = document.getString("content")!!

                    noteTitle = noteTitle.trimStart().trimEnd()
                    if (noteTitle == "") {
                        noteTitle = "<No Title>"
                    }

                    noteContent = noteContent.trimStart().trimEnd()
                    if (noteContent == "") {
                        noteContent = "<No Content>"
                    }

                    val noteToAdd = NoteDisplay(id = noteId, title = noteTitle, content = noteContent)
                    notesToDisplay.add(noteToAdd)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("HomePageActivity", "Error getting documents: ", exception)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOUR)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Username displayed at the top.
        Text(
            text = "@${userName}'s notes",
            fontSize = 27.sp,
            color = Color.White,
            modifier = Modifier
                .padding(top = 5.dp, bottom = 5.dp)
        )

        // Display list of available notes for this user.
        NoteList(noteDisplays = notesToDisplay, modifier = Modifier.weight(1f), userName)

        // Button to add a note.
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10),
            onClick = {
                launchAddNoteActivity(context, userName)
            },
            modifier = Modifier
                .padding(bottom = 5.dp)
        ) {
            Text(text = "Add Note", fontSize = 18.sp)
        }
    }
}


@Composable
fun SingleNote(noteDisplay: NoteDisplay, userName: String) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable {
                launchEditNoteActivity(
                    context,
                    noteDisplay.id,
                    noteDisplay.title,
                    noteDisplay.content,
                    userName
                )
            },
        shape = RoundedCornerShape(10),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Note titles are displayed here.
            Text(
                text = noteDisplay.title,
                fontSize = 27.sp,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}


@Composable
fun NoteList(noteDisplays: List<NoteDisplay>, modifier: Modifier = Modifier, userName: String) {
    LazyColumn(modifier = modifier) {
        items(noteDisplays){ noteDisplay: NoteDisplay ->
            SingleNote(noteDisplay = noteDisplay, userName = userName)
        }
    }
}


private fun launchLoginActivity(context: Context) {
    val intent = Intent(context, LoginActivity::class.java)
    context.startActivity(intent)
}


private fun launchAddNoteActivity(context: Context, userName: String) {
    val intent = Intent(context, AddNoteActivity::class.java)
    val bundle = Bundle()

    bundle.putString("KEY_USERNAME", userName)
    intent.putExtra("KEY_BUNDLE_USERNAME", bundle)
    context.startActivity(intent)
}


private fun launchEditNoteActivity(context: Context, noteId: String, noteTitle: String, noteContent: String, userName: String) {
    val intent = Intent(context, EditNoteActivity::class.java)
    val bundle = Bundle()

    bundle.putString("KEY_NOTE_ID", noteId)
    bundle.putString("KEY_NOTE_TITLE", noteTitle)
    bundle.putString("KEY_NOTE_CONTENT", noteContent)
    bundle.putString("KEY_USERNAME", userName)
    intent.putExtra("KEY_BUNDLE_NOTE", bundle)
    context.startActivity(intent)
}