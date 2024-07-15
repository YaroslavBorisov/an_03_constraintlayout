package ru.netology.nmedia.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentLoginBinding
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.LoginViewModel

@AndroidEntryPoint
class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        val viewModel: LoginViewModel by viewModels()
        val authViewModel: AuthViewModel by viewModels()

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data), Snackbar.LENGTH_LONG
                        ).show()

                    }

                    Activity.RESULT_OK -> {
                        val uri = it.data?.data
                        viewModel.changePhoto(uri, uri?.toFile())

                    }
                }
            }


        arguments?.textArg?.let {
            binding.username.isVisible = true
            binding.confirmPassword.isVisible = true
            binding.signInButton.isVisible = false
            binding.signUpButton.isVisible = true
            binding.photoContainer.isVisible = true
            binding.buttonPanel.isVisible = true

        }

        binding.signInButton.setOnClickListener {
            viewModel.signIn(binding.login.text.toString(), binding.password.text.toString())
        }

        binding.signUpButton.setOnClickListener {
            if(binding.password.text.isNotBlank() && binding.password.text.toString() == binding.password.text.toString()) {
                viewModel.signUp(binding.username.text.toString(), binding.login.text.toString(), binding.password.text.toString())
            } else {
                Snackbar.make(binding.root, R.string.incorrect_password, Snackbar.LENGTH_SHORT).show()
            }
        }

        authViewModel.auth.observe(viewLifecycleOwner) {
            if (authViewModel.isAuthorized) {
                findNavController().navigateUp()
            }
        }

        viewModel.loginState.observe(viewLifecycleOwner) { loginState ->
            binding.loading.isVisible = loginState.loading
            if (loginState.error) {
                Snackbar.make(binding.root, R.string.error_updating, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        viewModel.resetState()
                    }
                    .show()
            }
        }


        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .compress(1024)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cameraOnly()
                .compress(1024)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.dropPhoto()
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            binding.photoContainer.isVisible = it.file != null
            binding.photo.setImageURI(it.uri)
            binding.removePhoto.isVisible = binding.photoContainer.isVisible
        }


        return binding.root
    }
}