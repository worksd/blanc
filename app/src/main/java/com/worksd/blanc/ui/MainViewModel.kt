package com.worksd.blanc.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.worksd.blanc.data.BootInfoResponse
import com.worksd.blanc.data.BottomMenuResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
): ViewModel() {

    private val gson = Gson()

    private val _mainRoute: MutableStateFlow<String> = MutableStateFlow("")
    val mainRoute: StateFlow<String> = _mainRoute

    private val _bottomMenuList: MutableStateFlow<List<BottomMenuResponse>> = MutableStateFlow(listOf())
    val bottomMenuList: StateFlow<List<BottomMenuResponse>> = _bottomMenuList

    private val _currentSelectedIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    val currentSelectedIndex: StateFlow<Int> = _currentSelectedIndex

    private val _replace: MutableSharedFlow<String> = MutableSharedFlow()
    val replace: SharedFlow<String> = _replace

    private val _push: MutableSharedFlow<String> = MutableSharedFlow()
    val push: SharedFlow<String> = _push

    private val _clearAndPush: MutableSharedFlow<String> = MutableSharedFlow()
    val clearAndPush: SharedFlow<String> = _clearAndPush

    private val _back: MutableSharedFlow<Unit> = MutableSharedFlow()
    val back: SharedFlow<Unit> = _back



    fun startConnectTimer() {
        viewModelScope.launch {
            delay(5000L)

        }
    }

    fun setBottomMenuList(list: List<BottomMenuResponse>) {
        viewModelScope.launch(Dispatchers.Main) {
            _bottomMenuList.emit(list)
        }
    }

    fun selectBottomMenu(index: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            _currentSelectedIndex.emit(index)
        }
    }

    fun navigate(route: String) {
        viewModelScope.launch {
            _replace.emit(route)
        }
    }

    fun push(route: String) {
        viewModelScope.launch {
            _push.emit(route)
        }
    }

    fun clearAndPush(route: String) {
        viewModelScope.launch {
            _clearAndPush.emit(route)
        }
    }

    fun back() {
        viewModelScope.launch {
            _back.emit(Unit)
        }
    }

    fun setBootInfo(bootInfoResponse: String) {
        viewModelScope.launch {
            try {
                val bootInfo = gson.fromJson(bootInfoResponse, BootInfoResponse::class.java)
                Log.d("WebAppInterface", bootInfo.toString())
                _bottomMenuList.emit(bootInfo.bottomMenuList)
                _mainRoute.emit(bootInfo.route)
            } catch (e: Throwable) {
                Log.d("WebAppInterface", "error invoked = " + e.message.orEmpty())
            }
        }
    }
}