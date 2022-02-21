package com.trigonated.ctwtopheadlines.ui.misc

import android.app.ActivityManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.trigonated.ctwtopheadlines.R

/**
 * Easy-to-use wrapper around the [BiometricPrompt] API.
 *
 * Usage:
 *
 * ```kotlin
 * class SomeFragment : Fragment() {
 *  private lateinit var biometricPrompt: SimpleBiometricPrompt
 *
 *   override fun onCreateView(
 *      inflater: LayoutInflater,
 *      container: ViewGroup?,
 *      savedInstanceState: Bundle?
 *      ): View {
 *          biometricPrompt = SimpleBiometricPrompt(this)
 *          biometricPrompt.onAuthenticationCompleted = { result ->
                // Do something
            }
 *      }
 *
 *      ...
 *
 *      biometricPrompt.show()
 *  }
 * ```
 */
class SimpleBiometricPrompt(private val fragment: Fragment) {
    /** Callback for the prompt. */
    var onAuthenticationCompleted: ((result: BiometricPromptResult) -> Unit)? = null
    /** The prompt. */
    private val biometricPrompt: BiometricPrompt = BiometricPrompt(
        fragment,
        ContextCompat.getMainExecutor(fragment.requireContext()),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onAuthenticationCompleted?.invoke(BiometricPromptResult.FAILED)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onAuthenticationCompleted?.invoke(BiometricPromptResult.FAILED)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthenticationCompleted?.invoke(BiometricPromptResult.SUCCESS)
            }
        }
    )

    /** Show the prompt. Use [onAuthenticationCompleted] to listen for results. */
    fun show() {
        val promptInfo = createPromptInfo()
        if (isBiometricAuthenticationAvailable()) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            onAuthenticationCompleted?.invoke(BiometricPromptResult.UNSUPPORTED)
        }
    }

    /** Gets whether the hardware is available **and** configured. */
    private fun isBiometricAuthenticationAvailable(): Boolean {
        // Disable if running on tests
        if (TestUtils.isOnInstrumentedTest()) return false
        // Check requirements
        return BiometricManager.from(fragment.requireContext())
            .canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(fragment.getString(R.string.biometric_prompt_title))
            .setDescription(fragment.getString(R.string.biometric_prompt_description))
            .setConfirmationRequired(false)
            .setNegativeButtonText(fragment.getString(R.string.biometric_prompt_negative))
            .build()
    }

    enum class BiometricPromptResult {
        SUCCESS,
        FAILED,
        UNSUPPORTED
    }
}