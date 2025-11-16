package com.mobdeve.s18.group5.bayanihanspots.ui.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.mobdeve.s18.group5.bayanihanspots.auth.data.LoginRepository
import com.mobdeve.s18.group5.bayanihanspots.auth.data.model.LoggedInUser

class ProfileViewModel(private val loginRepository: LoginRepository, private val context: Context) : ViewModel() {
    private val _profile = MutableLiveData<LoggedInUser>()
    val profile: LiveData<LoggedInUser> = _profile
    private val _completedCount = MutableLiveData<String>("14")
    val completedCount: LiveData<String> = _completedCount

    private val _activeCount = MutableLiveData<String>("06")
    val activeCount: LiveData<String> = _activeCount

    private val _pendingCount = MutableLiveData<String>("25")
    val pendingCount: LiveData<String> = _pendingCount

    private val _logoutComplete = MutableLiveData<Boolean>()
    val logoutComplete: LiveData<Boolean> = _logoutComplete

    init {
        loadProfileFromCache()
    }

    private fun loadProfileFromCache() {
        val prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        val userJson = prefs.getString("USER_KEY", null)

        if (userJson != null) {
            val gson = Gson()
            val user = gson.fromJson(userJson, LoggedInUser::class.java)
            _profile.value = user
        }
    }
    fun signOut() {
        loginRepository.logout()
        _logoutComplete.value = true
    }
}