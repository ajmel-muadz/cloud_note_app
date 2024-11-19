package com.example.firebasenoteapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AppViewModel : ViewModel() {
    /* Username and password related stuff. */
    /* ------------------------------------ */
    var username by mutableStateOf("")
    var password by mutableStateOf("")

    var newUsername by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    /* ------------------------------------ */

    var loginMessage by mutableStateOf("")
    var loginState by mutableIntStateOf(-1)  // Tracks whether good or bad message.
    var signupMessage by mutableStateOf("")
    var signupState by mutableIntStateOf(-1)  // Tracks whether good or bad message.

    var noteTitle by mutableStateOf("")
    var noteContent by mutableStateOf("")
}