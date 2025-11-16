package com.mobdeve.s18.group5.bayanihanspots.auth

import android.content.Context
import android.util.Log
import com.mobdeve.s18.group5.bayanihanspots.R
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class GoogleAuthHelper(private val context: Context, private val auth: FirebaseAuth = Firebase.auth) {
    private val credentialManager = CredentialManager.create(context)
    private val serverClientId = context.getString(R.string.default_web_client_id)

    private suspend fun firebaseAuthWithGoogle(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).await()
    }
}