package com.braille.braisee.ui.detail

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.braille.braisee.data.learn.Module

class DetailViewModel : ViewModel() {

    private val _module = MutableLiveData<Module?>()
    val module: LiveData<Module?> get() = _module

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    var webViewState: Bundle? = null
    var isWebViewInitialized: Boolean = false

    fun setModule(module: Module?) {
        if (module == null) {
            _error.value = "Module tidak ditemukan!"
        } else {
            _error.value = null
            _module.value = module
        }
    }

    fun clearError() {
        _error.value = null
    }
}
