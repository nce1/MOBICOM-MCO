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

    private val _loginForm = MutableLiveData(LoginFormState(
        usernameError = null,
        passwordError = null,
        isDataValid = false,
        usernameTouched = false,
        passwordTouched = false
    ))
    val loginFormState: LiveData<LoginFormState> = _loginForm
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(username: String, password: String){
        _isLoading.value = true
        viewModelScope.launch {
            val result = loginRepository.login(username, password)

            if (result is Result.Success) {
                _loginResult.value =
                    LoginResult(success = LoggedInUserView(result.data.displayName, result.data.username, result.data.email, result.data.imageUri))
            } else {
                val errorMessage = (result as? Result.Error)?.exception?.message ?: "Login failed"
                _loginResult.value = LoginResult(errorString = errorMessage)
            }
            _isLoading.value = false
        }
    }
    fun loginWithGoogleToken(idToken: String){
        _isLoading.value = true
        viewModelScope.launch {
            val result = loginRepository.loginWithGoogleToken(idToken)
            if (result is Result.Success) {
                _loginResult.value =
                    LoginResult(success = LoggedInUserView(result.data.displayName, result.data.username, result.data.email, result.data.imageUri))
            } else {
                _loginResult.value = LoginResult(error = R.string.login_failed)
            }
            _isLoading.value = false
        }
    }
    fun loginDataChanged(username: String, password: String) {
        val form = _loginForm.value ?: return
        val isUserValid = isUserNameValid(username)
        val isPassValid = isPasswordValid(password)
        _loginForm.value = form.copy(
            isDataValid = isUserValid && isPassValid
        )
    }
    fun usernameFocusLost(username: String) {
        val form = _loginForm.value ?: return
        val isValid = isUserNameValid(username)

        _loginForm.value = form.copy(
            usernameTouched = true,
            usernameError = if (isValid) null else R.string.invalid_email
        )
    }

    fun passwordFocusLost(password: String) {
        val form = _loginForm.value ?: return
        val isValid = isPasswordValid(password)

        _loginForm.value = form.copy(
            passwordTouched = true,
            passwordError = if (isValid) null else R.string.invalid_password
        )
    }
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(username).matches()
    }
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8
    }
}