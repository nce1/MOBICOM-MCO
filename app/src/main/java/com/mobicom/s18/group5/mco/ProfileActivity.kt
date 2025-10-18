package com.mobicom.s18.group5.mco

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val profileGroup = findViewById<Group>(R.id.profileGroup)
        val loggedOutGroup = findViewById<Group>(R.id.loggedOutGroup)

        // TODO: Replace with actual login check
        val isLoggedIn = false

        if (isLoggedIn) {
            profileGroup.visibility = View.VISIBLE
            loggedOutGroup.visibility = View.GONE
        } else {
            profileGroup.visibility = View.GONE
            loggedOutGroup.visibility = View.VISIBLE
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
