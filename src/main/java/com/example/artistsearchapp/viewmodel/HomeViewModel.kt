package com.example.artistsearchapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.artistsearchapp.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class HomeViewModel (
    private val api: ApiService
): ViewModel() {
    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent

    private val _deleteEvent = MutableStateFlow(false)
    val deleteEvent: StateFlow<Boolean> = _deleteEvent

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _logoutEvent.value = true
    }

    fun deleteAccount(context: Context) {
        viewModelScope.launch {
            try {
                val response = api.deleteAccount()
                if (response.isSuccessful) {
                    val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()
                    _deleteEvent.value = true
                } else {
                    // You can log or handle error here if needed
                }
            } catch (e: Exception) {
                // Optional: handle exception (e.g., network error)
            }
        }
    }


    fun clearEvents() {
        _logoutEvent.value = false
        _deleteEvent.value = false
    }


}
