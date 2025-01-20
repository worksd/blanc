package com.worksd.blanc.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worksd.blanc.data.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
): ViewModel() {

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoading: SharedFlow<Boolean> = _isLoading

    private val _isConnectFail: MutableSharedFlow<Unit> = MutableSharedFlow()
    val isConnectFail: SharedFlow<Unit> = _isConnectFail

    private val _onKakaoLoginSuccess: MutableSharedFlow<String> = MutableSharedFlow()
    val onKakaoLoginSuccess: SharedFlow<String> = _onKakaoLoginSuccess

    private val _onGoogleLoginSuccess: MutableSharedFlow<String> = MutableSharedFlow()
    val onGoogleLoginSuccess: SharedFlow<String> = _onGoogleLoginSuccess

    private val _errorInvoked: MutableSharedFlow<Throwable> = MutableSharedFlow()
    val errorInvoked: SharedFlow<Throwable> = _errorInvoked

    fun kakaoLogin(context: Context) {
        viewModelScope.launch {
            runCatching {
                Log.d("WebAppInterface", "kakaoLogin")
                loginRepository.kakaoLogin(context)
            }.onSuccess {
                _onKakaoLoginSuccess.emit(it)
            }.onFailure {
                _errorInvoked.emit(it)
            }
        }
    }

    fun googleLogin(context: Context, serverClientId: String, nonce: String) {
        viewModelScope.launch {
            runCatching {
                loginRepository.googleLogin(context, serverClientId, nonce)
            }.onSuccess {
                _onGoogleLoginSuccess.emit(it)
            }.onFailure {
                _errorInvoked.emit(it)
            }
        }
    }

    fun setLoading(isLoading: Boolean) {
        viewModelScope.launch {
            _isLoading.emit(true)
        }
    }

    fun onConnectFail() {
        viewModelScope.launch {
            if (_isLoading.value) {
                _isConnectFail.emit(Unit)
                _isLoading.emit(false)
            }
        }
    }


}