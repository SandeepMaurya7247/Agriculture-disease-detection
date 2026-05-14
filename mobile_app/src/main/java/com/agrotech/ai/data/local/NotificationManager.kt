package com.agrotech.ai.data.local

import com.agrotech.ai.data.model.AppNotification
import com.agrotech.ai.data.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationManager {
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    fun addNotification(notification: AppNotification) {
        _notifications.value = listOf(notification) + _notifications.value
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }

    // Overload for legacy calls
    fun addNotification(title: String, message: String) {
        val notification = AppNotification(
            title = title,
            message = message,
            type = "SYSTEM"
        )
        addNotification(notification)
    }

    fun markAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        _unreadCount.value = _notifications.value.count { !it.isRead }
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
