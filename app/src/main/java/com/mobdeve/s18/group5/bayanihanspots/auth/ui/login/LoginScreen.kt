package com.mobdeve.s18.group5.bayanihanspots.auth.ui.login

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobdeve.s18.group5.bayanihanspots.R

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit, onSignupClick: () -> Unit, onForgotPasswordClick: () -> Unit){
    val formState by viewModel.loginFormState.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val loginResult by viewModel.loginResult.observeAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailHadFocus by remember { mutableStateOf(false) }
    var passwordHadFocus by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(loginResult){
        val result = loginResult ?: return@LaunchedEffect
        if (result.success != null){
            onLoginSuccess()
        }
        result.error?.let{ Toast.makeText(context, context.getString(it), Toast.LENGTH_SHORT).show() }
        result.errorString?.let{ Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_auth),
            contentDescription = "Auth Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {

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
                    text = "Welcome Back",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Don't have an account?",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    TextButton(
                        onClick = onSignupClick,
                        modifier = Modifier.padding(start = 4.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Sign Up", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        isError = formState?.usernameTouched == true && formState?.usernameError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused){
                                    emailHadFocus = true
                                } else if (emailHadFocus){
                                    viewModel.usernameFocusLost(email)
                                    viewModel.loginDataChanged(email, password)
                                }
                            }
                    )
                    if (formState?.usernameTouched == true){
                        formState?.usernameError?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                    }
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        isError = formState?.passwordTouched == true && formState?.passwordError != null,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    passwordHadFocus = true
                                } else if (passwordHadFocus) {
                                    viewModel.passwordFocusLost(password)
                                    viewModel.loginDataChanged(email, password)
                                }
                            }
                    )
                    if (formState?.passwordTouched == true){
                        formState?.passwordError?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = onForgotPasswordClick,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            viewModel.login(email.trim(), password)
                        },
                        enabled = formState?.isDataValid == true && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Log In", fontSize = 16.sp)
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