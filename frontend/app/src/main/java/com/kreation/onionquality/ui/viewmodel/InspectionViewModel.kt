package com.kreation.onionquality.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kreation.onionquality.data.api.InspectionResultDto
import com.kreation.onionquality.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

sealed interface InspectionUiState {
    object Idle : InspectionUiState
    data class Loading(val message: String = "Uploading image & analyzing sample with Gemini Vision...") : InspectionUiState
    data class Success(val result: InspectionResultDto) : InspectionUiState
    data class Error(val message: String) : InspectionUiState
}

class InspectionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<InspectionUiState>(InspectionUiState.Idle)
    val uiState: StateFlow<InspectionUiState> = _uiState.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _batchId = MutableStateFlow("ON-2026-001")
    val batchId: StateFlow<String> = _batchId.asStateFlow()

    private val _vendorId = MutableStateFlow("")
    val vendorId: StateFlow<String> = _vendorId.asStateFlow()

    private val _variety = MutableStateFlow("")
    val variety: StateFlow<String> = _variety.asStateFlow()

    private val _sampleCount = MutableStateFlow("")
    val sampleCount: StateFlow<String> = _sampleCount.asStateFlow()

    fun setImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun setBatchId(value: String) {
        _batchId.value = value
    }

    fun setVendorId(value: String) {
        _vendorId.value = value
    }

    fun setVariety(value: String) {
        _variety.value = value
    }

    fun setSampleCount(value: String) {
        _sampleCount.value = value
    }

    fun resetState() {
        _uiState.value = InspectionUiState.Idle
    }

    fun startInspection(context: Context) {
        val uri = _selectedImageUri.value
        if (uri == null) {
            _uiState.value = InspectionUiState.Error("Please capture or select an onion sample image first.")
            return
        }

        viewModelScope.launch {
            _uiState.value = InspectionUiState.Loading("Sending image to FastAPI & Gemini backend...")

            try {
                // Prepare Multipart image body
                val imagePart = prepareImagePart(context, uri)
                if (imagePart == null) {
                    _uiState.value = InspectionUiState.Error("Unable to read selected image file.")
                    return@launch
                }

                // Prepare optional sample count & lot ID request bodies
                val countVal = _sampleCount.value.trim().toIntOrNull()
                val sampleCountPart = countVal?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val lotIdPart = _batchId.value.trim().ifEmpty { "ON-2026-DEFAULT" }.toRequestBody("text/plain".toMediaTypeOrNull())

                val apiService = RetrofitClient.getService()
                val response = apiService.inspectOnion(
                    image = imagePart,
                    sampleCount = sampleCountPart,
                    lotId = lotIdPart
                )

                if (response.isSuccessful && response.body() != null) {
                    val resultDto = response.body()!!
                    _uiState.value = InspectionUiState.Success(resultDto)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Server returned error code ${response.code()}"
                    _uiState.value = InspectionUiState.Error("Backend error: $errorMsg")
                }
            } catch (e: Exception) {
                _uiState.value = InspectionUiState.Error("Network/Server failure: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    private fun prepareImagePart(context: Context, uri: Uri): MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("onion_sample_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
