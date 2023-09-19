package com.usgs.functions


fun getTimeAgo(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val elapsedTimeMillis = currentTime - timestamp

    val seconds = elapsedTimeMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val years = days / 365

    return when {
        years > 0 -> "$years years ago"
        days > 0 -> "$days days ago"
        hours > 0 -> "$hours hours ago"
        minutes > 0 -> "$minutes minutes ago"
        else -> "$seconds seconds ago"
    }
}
