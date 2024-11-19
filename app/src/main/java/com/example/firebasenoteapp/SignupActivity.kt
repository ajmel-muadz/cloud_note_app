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
import androidx.compose.runtime.LaunchedEffect
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

class SignupActivity : ComponentActivity() {
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
                    SignupPage(db, appViewModel)
                }
            }
        }
    }
}


@Composable
fun SignupPage(db: FirebaseFirestore, appViewModel: AppViewModel) {
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
            textInput = "Create an Account",
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        // Middle third
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1.25f),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error message in case anything goes wrong with signup, or good message otherwise.
            if (appViewModel.signupState == 0) {
                Text(text = appViewModel.signupMessage, color = Color.Green)
            } else if (appViewModel.signupState == 1) {
                Text(text = appViewModel.signupMessage, color = Color.Red)
            }

            // Text field inputs
            Column {
                appViewModel.newUsername = loginOrSignupInput(textInput = "New Username")
                appViewModel.newPassword =  loginOrSignupInput(textInput = "New Password")
                appViewModel.confirmPassword =  loginOrSignupInput(textInput = "Confirm Password")
            }

            // Sign up button
            SignUpButton(db, appViewModel)

            // Log in link
            Text(
                text = "Go back to Log In page",
                textDecoration = TextDecoration.Underline,
                color = Color.White,
                modifier = Modifier.clickable {
                    launchLoginActivity(context)
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
fun SignUpButton(db: FirebaseFirestore, appViewModel: AppViewModel) {
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
                        if (document.id == appViewModel.newUsername) {
                            userDocument = document
                        }
                    }

                    if (userDocument != null) {
                        // Cannot make an account if a username already exists.
                        appViewModel.signupState = 1
                        appViewModel.signupMessage = "That username already exists!"
                    } else {
                        // We do all this if it is a new account username and confirm password matches new password.
                        if (appViewModel.confirmPassword == appViewModel.newPassword) {
                            appViewModel.signupState = 0
                            val userPassword = hashMapOf(
                                "password" to appViewModel.newPassword
                            )

                            db.collection("users").document(appViewModel.newUsername)
                                .set(userPassword)
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                            appViewModel.signupMessage = "Account successfully created."
                            // coroutineScope needed or else delay would not work.
                            coroutineScope.launch {
                                delay(1500L)
                                launchHomePageActivity(context, appViewModel.newUsername)
                            }
                        } else {
                            appViewModel.signupState = 1
                            appViewModel.signupMessage = "Passwords do not match!"
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        },
    ) {
        Text(text = "Sign Up", fontSize = 18.sp)
    }
}


private fun launchLoginActivity(context: Context) {
    val intent = Intent(context, LoginActivity::class.java)
    context.startActivity(intent)
}


private fun launchHomePageActivity(context: Context, newUsername: String) {
    val intent = Intent(context, HomePageActivity::class.java)
    val bundle = Bundle()

    bundle.putString("KEY_USERNAME", newUsername)
    intent.putExtra("KEY_BUNDLE_USERNAME", bundle)
    context.startActivity(intent)
}