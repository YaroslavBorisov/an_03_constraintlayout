package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.viewmodel.AuthViewModel

class AppActivity : AppCompatActivity(R.layout.activity_app) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text.isNullOrBlank()) {
                return@let
//                Snackbar.make(binding.root, R.string.error_empty_content, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(android.R.string.ok) {
//                        finish()
//                    }
//                    .show()
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    })
            //Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }

        checkGoogleApiAvailability()

        val viewModel by viewModels<AuthViewModel>()

        var currentMenuProvider: MenuProvider? = null

        viewModel.auth.observe(this) {
            val isAuthorized = viewModel.isAuthorized

            currentMenuProvider?.let { removeMenuProvider(it) }

            addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.auth_menu, menu)

                    menu.setGroupVisible(R.id.authorized, isAuthorized)
                    menu.setGroupVisible(R.id.unauthorized, !isAuthorized)

                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.sign_in, R.id.sign_up -> {
                            //AppAuth.getInstance().setAuth(5, "x-token")
                            findNavController(R.id.nav_host_fragment)
                                .navigate(R.id.action_global_loginFragment, Bundle().apply {
                                    textArg =
                                        if (menuItem.itemId == R.id.sign_up) "actionSignUp" else null
                                })
                            true
                        }

                        R.id.logout -> {
                            val logoutConfirmationDialogFragment =
                                LogoutConfirmationDialogFragment()
                            logoutConfirmationDialogFragment.show(
                                supportFragmentManager,
                                LogoutConfirmationDialogFragment.TAG
                            )
                            true
                        }

                        else -> false
                    }

            }.also {
                currentMenuProvider = it
            }

            )
        }
    }


    fun viewVideo(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        ContextCompat.startActivity(this, intent, null)
    }

    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            println(it)
        }
    }
}

class LogoutConfirmationDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.logout_confirmation))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                AppAuth.getInstance().clearAuth()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create()

    companion object {
        const val TAG = "LogoutConfirmationDialog"
    }
}