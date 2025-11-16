package com.mobdeve.s18.group5.bayanihanspots.auth.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.mobdeve.s18.group5.bayanihanspots.auth.data.model.LoggedInUser

class LoginRepository(val dataSource: LoginDataSource, val context: Context){
    var user: LoggedInUser? = null
        private set

    suspend fun login(username: String, password: String): Result<LoggedInUser>{
        val result = dataSource.login(username, password)
        if (result is Result.Success){
            setLoggedInUser(result.data)
        }
        return result
    }
    suspend fun register(username: String, password: String, firstname: String, lastname: String, usernameSignup: String): Result<LoggedInUser> {
        val result = dataSource.register(username, password, firstname, lastname, usernameSignup)
        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }
        return result
    }
    suspend fun loginWithGoogleToken(idToken: String): Result<LoggedInUser>{
        val result = dataSource.loginWithGoogleToken(idToken)
        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }
        return result
    }

    fun logout(){
        user = null
        dataSource.logout()

        val prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        prefs.edit {remove("USER_KEY")}
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        val prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val userJson = gson.toJson(loggedInUser)
        prefs.edit{putString("USER_KEY", userJson)}
    }
}