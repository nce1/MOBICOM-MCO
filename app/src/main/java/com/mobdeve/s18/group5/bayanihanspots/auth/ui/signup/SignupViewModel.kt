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
    private val _signUpForm = MutableLiveData<SignUpFormState>()
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
    fun signUpDataChanged(email: String, firstname: String, lastname: String, username: String, password: String, confirmPass: String){
        if (!isEmailValid(email)){
            _signUpForm.value = SignUpFormState(emailError = R.string.invalid_field)
        } else if (!isFirstnameValid(firstname)){
            _signUpForm.value = SignUpFormState(firstnameError = R.string.invalid_field)
        } else if (!isLastnameValid(lastname)){
            _signUpForm.value = SignUpFormState(lastnameError = R.string.invalid_field)
        } else if (!isUsernameValid(username)){
            _signUpForm.value = SignUpFormState(usernameError = R.string.invalid_field)
        } else if (!isPasswordValid(password)){
            _signUpForm.value = SignUpFormState(passwordError = R.string.invalid_password)
        } else if (!isConfirmPasswordValid(password, confirmPass)){
            _signUpForm.value = SignUpFormState(confirmPasswordError = R.string.mismatch_password)
        } else{
            _signUpForm.value = SignUpFormState(isDataValid = true)
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