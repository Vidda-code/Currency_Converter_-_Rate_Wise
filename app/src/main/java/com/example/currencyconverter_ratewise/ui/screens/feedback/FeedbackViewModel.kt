package com.example.currencyconverter_ratewise.ui.screens.feedback

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun onFeedbackChange(feedback: String) {
        _uiState.update { it.copy(feedback = feedback, error = null) }
    }

    fun submitFeedback(): Boolean {
        val feedback = _uiState.value.feedback.trim()

        if (feedback.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your feedback") }
            return false
        }

        return try {
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("kinqvidda@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Rate Wise App Feedback")
                putExtra(Intent.EXTRA_TEXT, feedback)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooserIntent = Intent.createChooser(emailIntent, "Send feedback via...")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            _uiState.update { it.copy(feedback = "", error = null) }
            true
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to open email app: ${e.message}") }
            false
        }
    }
}

data class FeedbackUiState(
    val feedback: String = "",
    val error: String? = null
)