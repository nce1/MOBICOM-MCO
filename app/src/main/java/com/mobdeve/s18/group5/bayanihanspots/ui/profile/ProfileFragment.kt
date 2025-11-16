package com.mobdeve.s18.group5.bayanihanspots.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.fragment.app.viewModels
import com.mobdeve.s18.group5.bayanihanspots.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState:Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.profile.observe(viewLifecycleOwner, Observer { user ->
            binding.profileName.text = user.displayName
            binding.subtitleUsername.text = "@${user.username}"
            binding.profileEmail.text = user.email
        })
        binding.logoutButton.setOnClickListener {
            viewModel.signOut()
        }
        viewModel.logoutComplete.observe(viewLifecycleOwner, Observer { hasLoggedOut ->
            if (hasLoggedOut) {
                Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show()
                //findNavController().navigate(R.id.action_profile_to_login)
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}