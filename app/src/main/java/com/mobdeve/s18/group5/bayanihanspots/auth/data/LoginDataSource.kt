package com.mobdeve.s18.group5.bayanihanspots.auth.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore
import com.mobdeve.s18.group5.bayanihanspots.auth.data.model.LoggedInUser
import kotlinx.coroutines.tasks.await
import java.io.IOException

class LoginDataSource{
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore
    private lateinit var displayName: String

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(username, password).await()
            val user = authResult.user
            if (user == null) {
                throw IOException("User is null after successful login.")
            }
            val uid = user.uid
            val email = user.email ?: ""
            val document = db.collection("users").document(uid).get().await()
            val firstname = document.getString("firstname") ?: ""
            val lastname = document.getString("lastname") ?: ""
            displayName = "$firstname $lastname"
            val customUsername = document.getString("username") ?: ""
            val image = document.getString("imageUri") ?: ""

            val loggedInUser = LoggedInUser(uid, email, displayName, customUsername, image)
            Result.Success(loggedInUser)
        } catch (e: Exception) {
            Result.Error(IOException("Error logging in", e))
        }
    }
    suspend fun register(email: String, password: String, firstname: String, lastname: String, username: String): Result<LoggedInUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user!!

            val displayName = "$firstname $lastname"
            val profileUpdates = userProfileChangeRequest {
                this.displayName = displayName
            }
            user.updateProfile(profileUpdates).await()
            val userDocument = hashMapOf(
                "firstname" to firstname,
                "lastname" to lastname,
                "username" to username,
                "email" to email
            )

            db.collection("users").document(user.uid)
                .set(userDocument)
                .await()

            Result.Success(LoggedInUser(user.uid, email, displayName, username, ""))
        } catch (e: Exception) {
            Result.Error(IOException("Error registering new user", e))
        }
    }
    suspend fun loginWithGoogleToken(idToken: String): Result<LoggedInUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user!!
            val displayName = user.displayName ?: "User"
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false

            val username = (user.email?.split("@")?.firstOrNull() ?: "user_${user.uid.take(5)}")
            if (isNewUser) {
                val nameParts = displayName.split(" ")
                val firstname = nameParts.firstOrNull() ?: "User"
                val lastname = nameParts.drop(1).joinToString(" ")
                val userDocument = hashMapOf(
                    "firstname" to firstname,
                    "lastname" to lastname,
                    "username" to username,
                    "email" to user.email
                )
                db.collection("users").document(user.uid)
                    .set(userDocument)
                    .await()
            }
            val email = user.email ?: ""
            Result.Success(LoggedInUser(user.uid, email, displayName, username, ""))
        } catch (e: Exception) {
            Result.Error(IOException("Error logging in with Google", e))
        }
    }

    fun logout(){
        auth.signOut()
    }
}