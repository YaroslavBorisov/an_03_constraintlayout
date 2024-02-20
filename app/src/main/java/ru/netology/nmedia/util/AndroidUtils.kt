package ru.netology.nmedia.util

import android.text.Editable
import java.math.BigDecimal
import java.math.RoundingMode

object AndroidUtils {
    fun String.toEditable(): Editable =
        Editable.Factory.getInstance().newEditable(this)

    fun Int.format(): String {
        if (this < 1_000) return this.toString()
        if (this < 1_100) return "1K"
        if (this < 10_000) return "%2.1fK".format(
            (this.toBigDecimal().divide(BigDecimal(1000), 1, RoundingMode.DOWN).toDouble())
        )
        if (this < 1_000_000) return "%dK".format(this / 1000)
        if (this < 1_000_000_000) return "%3.1fM".format(
            (this.toBigDecimal().divide(BigDecimal(1_000_000), 1, RoundingMode.DOWN).toDouble())
        )
        return "###" // Number too big!
    }

//    fun hideKeyboard(view: View) {
//        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view.windowToken, 0)
//    }

//    fun View.focusAndShowKeyboard() {
//        /**
//         * This is to be called when the window already has focus.
//         */
//        fun View.showTheKeyboardNow() {
//            if (isFocused) {
//                post {
//                    // We still post the call, just in case we are being notified of the windows focus
//                    // but InputMethodManager didn't get properly setup yet.
//                    val imm =
//                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
//                }
//            }
//        }
//
//        requestFocus()
//        if (hasWindowFocus()) {
//            // No need to wait for the window to get focus.
//            showTheKeyboardNow()
//        } else {
//            // We need to wait until the window gets focus.
//            viewTreeObserver.addOnWindowFocusChangeListener(
//                object : ViewTreeObserver.OnWindowFocusChangeListener {
//                    override fun onWindowFocusChanged(hasFocus: Boolean) {
//                        // This notification will arrive just before the InputMethodManager gets set up.
//                        if (hasFocus) {
//                            this@focusAndShowKeyboard.showTheKeyboardNow()
//                            // It’s very important to remove this listener once we are done.
//                            viewTreeObserver.removeOnWindowFocusChangeListener(this)
//                        }
//                    }
//                })
//        }
//    }
}