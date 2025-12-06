package com.mobdeve.s18.group5.bayanihanspots.auth.ui.signup

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobdeve.s18.group5.bayanihanspots.R


@Composable
fun SignupScreen(viewModel: SignupViewModel, onLoginClick: () -> Unit, onSignUpSuccess: () -> Unit) {
    val formState by viewModel.signUpFormState.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val signUpResult by viewModel.signUpResult.observeAsState()

    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstnameHadFocus by remember { mutableStateOf(false) }
    var lastnameHadFocus by remember { mutableStateOf(false) }
    var emailHadFocus by remember { mutableStateOf(false) }
    var usernameHadFocus by remember { mutableStateOf(false) }
    var passwordHadFocus by remember { mutableStateOf(false) }
    var confirmPasswordHadFocus by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(signUpResult) {
        val result = signUpResult ?: return@LaunchedEffect
        if (result.success != null) {
            onSignUpSuccess()
        }
        if (result.error != null) {
            Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
        }
    }
    Box(modifier = Modifier.fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.bg_auth),
            contentDescription = "Auth Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 96.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(
                    text = "Bayanihan Spots",
                    color = Color(0xFFD9D6D6),
                    fontSize = 34.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Get Started Now",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Already have an account?",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    TextButton(
                        onClick = onLoginClick,
                        modifier = Modifier.padding(start = 4.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Login", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = firstname,
                                onValueChange = {
                                    firstname = it
                                    viewModel.signUpDataChanged(email, it, lastname, username, password, confirmPassword)
                                },
                                label = { Text("First Name") },
                                modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        firstnameHadFocus = true
                                    } else if (firstnameHadFocus) {
                                        viewModel.firstnameFocusLost(firstname)
                                    }
                                }
                            )
                            if (formState?.firstnameTouched == true){
                                formState?.firstnameError?.let{ Text(text = stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp, top = 2.dp)) }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = lastname,
                                onValueChange = {
                                    lastname = it
                                    viewModel.signUpDataChanged(email, firstname, it, username, password, confirmPassword)
                                },
                                label = { Text("Last Name") },
                                modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        lastnameHadFocus = true
                                    } else if (lastnameHadFocus) {
                                        viewModel.lastnameFocusLost(lastname)
                                    }
                                }
                            )
                            if (formState?.lastnameTouched == true) {
                                formState?.lastnameError?.let { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp, top = 2.dp)) }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.signUpDataChanged(it, firstname, lastname, username, password, confirmPassword)
                        },
                        label = { Text("Email") },
                        isError = formState?.emailTouched == true && formState?.emailError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    emailHadFocus = true
                                } else if (emailHadFocus) {
                                    viewModel.emailFocusLost(email)
                                }
                            }
                    )
                    if (formState?.emailTouched == true){
                        formState?.emailError?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            viewModel.signUpDataChanged(email, firstname, lastname, it, password, confirmPassword)
                        },
                        label = { Text("Username") },
                        isError = formState?.usernameTouched == true && formState?.usernameError != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    usernameHadFocus = true
                                } else if (usernameHadFocus) {
                                    viewModel.usernameFocusLost(username)
                                }
                            }
                    )
                    if (formState?.usernameTouched == true){
                        formState?.usernameError?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.signUpDataChanged(email, firstname, lastname, username, it, confirmPassword)
                        },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = formState?.passwordTouched == true && formState?.passwordError != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    passwordHadFocus = true
                                } else if (passwordHadFocus) {
                                    viewModel.passwordFocusLost(password)
                                }
                            }
                    )
                    if (formState?.passwordTouched == true){
                        formState?.passwordError?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            viewModel.signUpDataChanged(email, firstname, lastname, username, password, it)
                        },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = formState?.cPasswordTouched == true && formState?.confirmPasswordError != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    confirmPasswordHadFocus = true
                                } else if (confirmPasswordHadFocus) {
                                    viewModel.cPasswordFocusLost(password, confirmPassword)
                                }
                            }
                    )
                    if (formState?.cPasswordTouched == true){
                        formState?.confirmPasswordError?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            viewModel.register(
                                email, password, firstname, lastname, username
                            )
                        },
                        enabled = formState?.isDataValid == true && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sign up", fontSize = 16.sp)
                    }

                }
            }
        }
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}