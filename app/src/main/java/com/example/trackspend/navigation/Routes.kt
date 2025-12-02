package com.example.trackspend.navigation

/**
 * Centralized navigation route definitions used across the app.
 *
 * Storing all route strings in one object prevents spelling mistakes,
 * keeps NavHost setup consistent, and makes it easy to update or add routes.
 *
 * Each constant corresponds to a destination composable in the navigation graph.
 */
object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val ANALYTICS = "analytics"
    const val DETAILS = "details"
    const val STATS_SPENDING = "stats_spending"
    const val STATS_ORDERS = "stats_orders"

    const val STATS_SUMMARY = "stats_summary"

}
