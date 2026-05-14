package com.agrotech.ai.data.local

import com.agrotech.ai.data.model.AppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationManager {
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    fun addNotification(notification: AppNotification) {
        _notifications.value = listOf(notification) + _notifications.value
        _unreadCount.value += 1
    }

    fun markAsRead(id: String) {
        val current = _notifications.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1 && !current[index].isRead) {
            current[index] = current[index].copy(isRead = true)
            _notifications.value = current
            _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
        }
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        _unreadCount.value = 0
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
        _unreadCount.value = 0
    }
}
