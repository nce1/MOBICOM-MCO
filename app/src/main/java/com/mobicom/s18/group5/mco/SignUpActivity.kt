package com.mobicom.s18.group5.mco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mobicom.s18.group5.mco.databinding.ActivitySignUpBinding
import com.mobicom.s18.group5.mco.ui.theme.McoTheme

class SignUpActivity : ComponentActivity() {
    private lateinit var viewBinding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.viewBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(this.viewBinding.root)
    }
}