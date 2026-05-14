package com.agrotech.ai.data.local

import com.agrotech.ai.data.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object HistoryManager {
    private val _historyItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyItems: StateFlow<List<HistoryItem>> = _historyItems.asStateFlow()

    fun addHistoryItem(item: HistoryItem) {
        _historyItems.value = listOf(item) + _historyItems.value
    }

    fun clearHistory() {
        _historyItems.value = emptyList()
    }
}
