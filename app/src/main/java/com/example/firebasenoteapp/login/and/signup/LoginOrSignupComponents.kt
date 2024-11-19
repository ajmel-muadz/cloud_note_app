// References
/* ----------------------------------------------------------------------------------------- */
// 1. Below link helped with the KeyboardAware composable so keyboard does not block content.
// https://medium.com/@mark.frelih_9464/how-to-handle-automatic-content-resizing-when-keyboard-is-visible-in-jetpack-compose-1c76e0e17c57
/* ----------------------------------------------------------------------------------------- */

package com.example.firebasenoteapp.login.and.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp

val BACKGROUND_COLOUR = Color(0xff2b2b2b)


@Composable
fun MainTitle(textInput: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App title.
        Text(
            text = textInput,
            fontSize = 40.sp,
            color = Color.White
        )
    }
}


@Composable
fun loginOrSignupInput(textInput: String): String {
    val labelColour: Color

    var input by remember { mutableStateOf("") }
    labelColour = if (input == "") {
        Color.Gray
    } else {
        Color.White
    }

    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        label = { Text(text = textInput) },
        colors = TextFieldDefaults.colors(
            unfocusedTextColor = Color.White,
            unfocusedIndicatorColor = Color.White,  // Set border colour.
            unfocusedContainerColor = Color.Transparent,  // Makes actual content transparent.
            unfocusedLabelColor = labelColour,

            focusedTextColor = Color.White,
            focusedIndicatorColor = Color.White,
            focusedContainerColor = Color.Transparent,
            focusedLabelColor = labelColour
        )
    )

    return input
}


@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}


@Composable
fun KeyboardAware(
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.imePadding()) {
        content()
    }
}
