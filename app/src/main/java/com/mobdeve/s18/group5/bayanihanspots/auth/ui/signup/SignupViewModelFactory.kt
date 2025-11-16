package com.mobdeve.s18.group5.bayanihanspots.auth.ui.signup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobdeve.s18.group5.bayanihanspots.auth.data.LoginDataSource
import com.mobdeve.s18.group5.bayanihanspots.auth.data.LoginRepository

class SignupViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignupViewModel::class.java)) {
            return SignupViewModel(
                loginRepository = LoginRepository(
                    dataSource = LoginDataSource(),
                    context
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}