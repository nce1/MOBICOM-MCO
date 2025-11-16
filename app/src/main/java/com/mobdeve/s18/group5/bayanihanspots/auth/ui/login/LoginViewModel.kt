package com.mobdeve.s18.group5.bayanihanspots.auth.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobdeve.s18.group5.bayanihanspots.R
import com.mobdeve.s18.group5.bayanihanspots.auth.data.LoginRepository
import com.mobdeve.s18.group5.bayanihanspots.auth.data.Result
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel(){

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String){
        viewModelScope.launch {
            val result = loginRepository.login(username, password)

            if (result is Result.Success) {
                _loginResult.value =
                    LoginResult(success = LoggedInUserView(result.data.displayName, result.data.username, result.data.email, result.data.imageUri))
            } else {
                _loginResult.value = LoginResult(error = R.string.login_failed)
            }
        }
    }
    fun loginWithGoogleToken(idToken: String){
        viewModelScope.launch {
            val result = loginRepository.loginWithGoogleToken(idToken)

            if (result is Result.Success) {
                _loginResult.value =
                    LoginResult(success = LoggedInUserView(result.data.displayName, result.data.username, result.data.email, result.data.imageUri))
            } else {
                _loginResult.value = LoginResult(error = R.string.login_failed)
            }
        }
    }
    fun loginDataChanged(username: String, password: String){
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 7
    }
}