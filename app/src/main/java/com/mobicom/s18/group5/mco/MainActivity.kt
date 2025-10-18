package com.mobicom.s18.group5.mco

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.s18.group5.mco.databinding.ActivityMainBinding
import com.mobicom.s18.group5.mco.ui.theme.McoTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            print("Horse");
        } else{
            print("Camel")
            // Do Toast
        }
    }

    private val signUpLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            print("Horse");
        } else{
            print("Camel")
            // Do Toast
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.viewBinding.root)

        val button: Button = findViewById(R.id.programButton)
        button.setOnClickListener {
            val intent = Intent(this, ProgramsLayoutActivity::class.java)
            startActivity(intent)
        }

        // Test Login
        this.viewBinding.btnLogin.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            loginLauncher.launch(intent)
        }
        this.viewBinding.button.setOnClickListener{
            val intent = Intent(this, SpotsMapsActivity::class.java)
            signUpLauncher.launch(intent)
        }




    }
}