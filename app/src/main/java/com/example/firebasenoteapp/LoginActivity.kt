// References
/* ----------------------------------------------------------------------------------------- */
// 1. Below link helped with sensing whether keyboard is open or not for un-focus effect.
// https://stackoverflow.com/questions/68847559/how-can-i-detect-keyboard-opening-and-closing-in-jetpack-compose
/* ----------------------------------------------------------------------------------------- */

package com.example.firebasenoteapp

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.firebasenoteapp.login.and.signup.BACKGROUND_COLOUR
import com.example.firebasenoteapp.login.and.signup.KeyboardAware
import com.example.firebasenoteapp.login.and.signup.MainTitle
import com.example.firebasenoteapp.login.and.signup.keyboardAsState
import com.example.firebasenoteapp.login.and.signup.loginOrSignupInput
import com.example.firebasenoteapp.ui.theme.FirebaseNoteAppTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// NOTE: Next thing to add is that we show error messages as actual text.

class LoginActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // I now remember the reason this is needed is because it is tied
            // to the un-focus effect if the keyboard is closed. Explains why without
            // this line, you can only type one character at a time.
            WindowCompat.setDecorFitsSystemWindows(window, false)
            FirebaseNoteAppTheme {
                KeyboardAware {
                    LoginPage(db, appViewModel)
                }
            }
        }
    }
}


@Composable
fun LoginPage(db: FirebaseFirestore, appViewModel: AppViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOUR),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top third
        MainTitle(
            textInput = "Cloud Note App",
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        // Middle third
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error message in case anything goes wrong with login, or good message otherwise.
            if (appViewModel.loginState == 0) {
                Text(text = appViewModel.loginMessage, color = Color.Green)
            } else if (appViewModel.loginState == 1) {
                Text(text = appViewModel.loginMessage, color = Color.Red)
            }

            // Text field inputs.
            Column {
                appViewModel.username = loginOrSignupInput(textInput = "Username")
                appViewModel.password = loginOrSignupInput(textInput = "Password")
            }

            // Log In button
            LogInButton(db, appViewModel)

            // Sign up link
            Text(
                text = "Don't have an account? Sign up now!",
                textDecoration = TextDecoration.Underline,
                color = Color.White,
                modifier = Modifier.clickable {
                    launchSignupActivity(context)
                }
            )
        }

        if (!isKeyboardOpen) {
            // Un-focuses any text input when the keyboard is hidden.
            focusManager.clearFocus()

            // Bottom third
            Spacer(modifier = Modifier
                .fillMaxSize()
                .weight(1f))
        }
    }
}


@Composable
fun LogInButton(db: FirebaseFirestore, appViewModel: AppViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(10),
        onClick = {
            db.collection("users")
                .get()
                .addOnSuccessListener { documents ->
                    var userDocument: QueryDocumentSnapshot? = null
                    for (document in documents) {
                        // This section checks if username supplied is in the database.
                        // If yes, it assigns it to 'userDocument'. Else, 'userDocument' is null.
                        if (document.id == appViewModel.username) {
                            userDocument = document
                        }
                    }

                    if (userDocument != null) {
                        // Username exists so we check if password is valid.
                        if (userDocument.data.containsValue(appViewModel.password)) {
                            appViewModel.loginState = 0
                            appViewModel.loginMessage = "Login successful."
                            // coroutineScope needed or else delay would not work.
                            coroutineScope.launch {
                                delay(1500L)
                                launchHomePageActivity(context, appViewModel.username)
                            }
                        } else {
                            appViewModel.loginState = 1
                            appViewModel.loginMessage = "Invalid password!"
                        }
                    } else {
                        // We do this if a username is not found (i.e. userDocument is NULL)
                        appViewModel.loginState = 1
                        appViewModel.loginMessage = "Username not found!"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        },
    ) {
        Text(text = "Log In", fontSize = 18.sp)
    }
}


private fun launchSignupActivity(context: Context) {
    val intent = Intent(context, SignupActivity::class.java)
    context.startActivity(intent)
}


private fun launchHomePageActivity(context: Context, userName: String) {
    val intent = Intent(context, HomePageActivity::class.java)
    val bundle = Bundle()

    bundle.putString("KEY_USERNAME", userName)
    intent.putExtra("KEY_BUNDLE_USERNAME", bundle)
    context.startActivity(intent)
}