package com.mobdeve.s18.group5.bayanihanspots.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobdeve.s18.group5.bayanihanspots.auth.data.LoginDataSource
import com.mobdeve.s18.group5.bayanihanspots.auth.data.LoginRepository

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                loginRepository = LoginRepository(
                    dataSource = LoginDataSource(),
                    context = context.applicationContext
                ),
                context = context.applicationContext
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}