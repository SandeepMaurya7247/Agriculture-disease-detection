package com.agrotech.ai.data.local

import android.content.Context
import com.agrotech.ai.data.model.HistoryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object HistoryManager {
    private val _historyItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyItems: StateFlow<List<HistoryItem>> = _historyItems.asStateFlow()
    
    private const val PREFS_NAME = "agro_history_prefs"
    private const val KEY_HISTORY = "history_list"
    private val gson = Gson()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null)
        if (json != null) {
            val type = object : TypeToken<List<HistoryItem>>() {}.type
            val savedList: List<HistoryItem> = gson.fromJson(json, type)
            _historyItems.value = savedList
        }
    }

    fun addHistoryItem(context: Context, item: HistoryItem) {
        val newList = listOf(item) + _historyItems.value
        _historyItems.value = newList
        saveToPrefs(context, newList)
    }

    private fun saveToPrefs(context: Context, list: List<HistoryItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun clearHistory(context: Context) {
        _historyItems.value = emptyList()
        saveToPrefs(context, emptyList())
    }
}
