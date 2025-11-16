package com.mobdeve.s18.group5.bayanihanspots.auth.ui.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mobdeve.s18.group5.bayanihanspots.MainActivity
import com.mobdeve.s18.group5.bayanihanspots.R
import com.mobdeve.s18.group5.bayanihanspots.auth.ui.login.LoggedInUserView
import com.mobdeve.s18.group5.bayanihanspots.databinding.FragmentSignupBinding
import kotlinx.coroutines.launch

class SignupFragment : Fragment(){
    private lateinit var signupViewModel: SignupViewModel
    private var _binding: FragmentSignupBinding? = null
    private lateinit var credentialManager: CredentialManager
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        credentialManager = CredentialManager.create(requireContext())

        signupViewModel = ViewModelProvider(this, SignupViewModelFactory(requireContext()))
            .get(SignupViewModel::class.java)

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }

        val emailEditText = binding.email
        val firstnameEditText = binding.firstname
        val lastnameEditText = binding.lastname
        val usernameSignupEditText = binding.usernameSignup
        val passwordEditText = binding.password
        val confirmPasswordEditText = binding.passwordConfirm
        val signUpButton = binding.signup
        val loadingProgressBar = binding.loading

        val googleSignInButton = binding.googleSignInButton

        signupViewModel.signUpFormState.observe(
            viewLifecycleOwner,
            Observer { signUpFormState ->
                if (signUpFormState == null) {
                    return@Observer
                }
                signUpButton.isEnabled = signUpFormState.isDataValid
                signUpFormState.emailError?.let { emailEditText.error = getString(it) }
                signUpFormState.firstnameError?.let { firstnameEditText.error = getString(it) }
                signUpFormState.lastnameError?.let { lastnameEditText.error = getString(it) }
                signUpFormState.usernameError?.let { usernameSignupEditText.error = getString(it) }
                signUpFormState.passwordError?.let { passwordEditText.error = getString(it) }
                signUpFormState.confirmPasswordError?.let { confirmPasswordEditText.error = getString(it) }
            })
        signupViewModel.signUpResult.observe(
            viewLifecycleOwner,
            Observer { signUpResult ->
                signUpResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                signUpResult.error?.let {
                    showSignUpFailed(it)
                }
                signUpResult.success?.let {
                    updateUiWithUser(it)
                }
            })
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                signupViewModel.signUpDataChanged(
                    emailEditText.text.toString(),
                    firstnameEditText.text.toString(),
                    lastnameEditText.text.toString(),
                    usernameSignupEditText.text.toString(),
                    passwordEditText.text.toString(),
                    confirmPasswordEditText.text.toString()
                )
            }
        }
        emailEditText.addTextChangedListener(afterTextChangedListener)
        firstnameEditText.addTextChangedListener(afterTextChangedListener)
        lastnameEditText.addTextChangedListener(afterTextChangedListener)
        usernameSignupEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        confirmPasswordEditText.addTextChangedListener(afterTextChangedListener)

        signUpButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            signupViewModel.register(
                emailEditText.text.toString(),
                passwordEditText.text.toString(),
                firstnameEditText.text.toString(),
                lastnameEditText.text.toString(),
                usernameSignupEditText.text.toString()
            )
        }
        googleSignInButton.setOnClickListener{
            loadingProgressBar.visibility = View.VISIBLE

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Show all Google accounts
                .setServerClientId(getString(R.string.default_web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(requireContext(), request)
                    val credential = result.credential

                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                        signupViewModel.loginWithGoogleToken(googleIdToken.idToken)
                    } else {
                        Log.w("SignUpFragment", "Credential is not of type Google ID!")
                        loadingProgressBar.visibility = View.GONE
                        showSignUpFailed(R.string.login_failed)
                    }
                } catch (e: Exception) {
                    Log.e("SignUpFragment", "Google Sign-in failed", e)
                    loadingProgressBar.visibility = View.GONE
                    showSignUpFailed(R.string.login_failed)
                }
            }
        }
    }
    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + " " + model.displayName
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()

        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
    private fun showSignUpFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}