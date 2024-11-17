package com.worksd.blanc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worksd.blanc.data.BottomMenuResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {

    private val _bottomMenuList: MutableStateFlow<List<BottomMenuResponse>> = MutableStateFlow(listOf())
    val bottomMenuList: StateFlow<List<BottomMenuResponse>> = _bottomMenuList

    private val _currentSelectedIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    val currentSelectedIndex: StateFlow<Int> = _currentSelectedIndex


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
}