package com.mobdeve.s18.group5.bayanihanspots.auth.ui.signup

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobdeve.s18.group5.bayanihanspots.R
import com.mobdeve.s18.group5.bayanihanspots.auth.data.LoginRepository
import com.mobdeve.s18.group5.bayanihanspots.auth.data.Result
import com.mobdeve.s18.group5.bayanihanspots.auth.ui.login.LoggedInUserView
import com.mobdeve.s18.group5.bayanihanspots.auth.ui.login.LoginResult
import kotlinx.coroutines.launch

class SignupViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    private val _signUpForm = MutableLiveData(SignUpFormState(
        emailError = null,
        passwordError = null,
        confirmPasswordError = null,
        firstnameError = null,
        lastnameError = null,
        isDataValid = false,
        firstnameTouched = false,
        lastnameTouched = false,
        emailTouched = false,
        usernameTouched = false,
        passwordTouched = false,
        cPasswordTouched = false
    ))
    val signUpFormState: LiveData<SignUpFormState> = _signUpForm
    private val _signUpResult = MutableLiveData<LoginResult>()
    val signUpResult: LiveData<LoginResult> = _signUpResult
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(email: String, password: String, firstname: String, lastname: String, username: String){
        _isLoading.value = true
        viewModelScope.launch{
            val result = loginRepository.register(email, password, firstname, lastname, username)
            if (result is Result.Success){
                _signUpResult.value =
                    LoginResult(success = LoggedInUserView(result.data.displayName, result.data.username, result.data.email, result.data.imageUri))
            } else{
                _signUpResult.value = LoginResult(error = R.string.signup_failed)
            }
            _isLoading.value = false
        }
    }
    fun loginWithGoogleToken(idToken: String){
        _isLoading.value = true
        viewModelScope.launch{
            val result = loginRepository.loginWithGoogleToken(idToken)
            if (result is Result.Success){
                _signUpResult.value =
                    LoginResult(success = LoggedInUserView(result.data.displayName, result.data.username, result.data.email, result.data.imageUri))
            } else{
                val errorMessage = (result as? Result.Error)?.exception?.message ?: "Login failed"
                _signUpResult.value = LoginResult(errorString = errorMessage)
            }
            _isLoading.value = false
        }
    }
    fun signUpDataChanged(email: String, firstname: String, lastname: String, username: String, password: String, confirmPass: String){
        val form = _signUpForm.value ?: return
        val isEmailValid = isEmailValid(email)
        val isUsernameValid = isUsernameValid(username)
        val isPasswordValid = isPasswordValid(password)
        val isCPasswordValid = isConfirmPasswordValid(password, confirmPass)
        val isFirstValid = isFirstnameValid(firstname)
        val isLastValid = isLastnameValid(lastname)
        _signUpForm.value = form.copy(isDataValid = isEmailValid && isUsernameValid && isPasswordValid && isCPasswordValid && isFirstValid && isLastValid)
    }
    fun emailFocusLost(email: String){
        val form = _signUpForm.value ?: return
        val isValid = isEmailValid(email)
        _signUpForm.value = form.copy(emailTouched = true, emailError = if (isValid) null else R.string.invalid_email)
    }
    fun usernameFocusLost(username: String){
        val form = _signUpForm.value ?: return
        val isValid = isUsernameValid(username)
        _signUpForm.value = form.copy(usernameTouched = true, usernameError = if (isValid) null else R.string.invalid_username)
    }
    fun passwordFocusLost(password: String){
        val form = _signUpForm.value ?: return
        val isValid = isPasswordValid(password)
        _signUpForm.value = form.copy(passwordTouched = true, passwordError = if (isValid) null else R.string.invalid_password)
    }
    fun cPasswordFocusLost(password: String, cPassword: String){
        val form = _signUpForm.value ?: return
        val isValid = isConfirmPasswordValid(password, cPassword)
        _signUpForm.value = form.copy(cPasswordTouched = true, confirmPasswordError = if (isValid) null else R.string.mismatch_password)
    }
    fun firstnameFocusLost(firstname: String){
        val form = _signUpForm.value ?: return
        val isValid = isFirstnameValid(firstname)
        _signUpForm.value = form.copy(firstnameTouched = true, firstnameError = if (isValid) null else R.string.invalid_first)
    }
    fun lastnameFocusLost(lastname: String){
        val form = _signUpForm.value ?: return
        val isValid = isLastnameValid(lastname)
        _signUpForm.value = form.copy(lastnameTouched = true, lastnameError = if (isValid) null else R.string.invalid_last)
    }
    private fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 7
    }
    private fun isConfirmPasswordValid(password: String, confirmPass: String): Boolean {
        return password == confirmPass
    }
    private fun isFirstnameValid(firstname: String): Boolean {
        return firstname.isNotBlank()
    }
    private fun isLastnameValid(lastname: String): Boolean {
        return lastname.isNotBlank()
    }
    private fun isUsernameValid(username: String): Boolean {
        return username.isNotBlank() && username.length > 5
    }
}