package com.example.trackspend.util

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission


/**
 * Checks whether the device currently has an active network connection
 * with internet capability.
 *
 * <p>This method requires the {@code ACCESS_NETWORK_STATE} permission.
 * It uses {@link ConnectivityManager} and {@link NetworkCapabilities}
 * to determine whether the active network is capable of accessing the
 * internet.</p>
 *
 * <p><b>Returns:</b></p>
 * <ul>
 *   <li><b>true</b> — if an active network exists AND it reports the
 *       {@link NetworkCapabilities#NET_CAPABILITY_INTERNET} capability.</li>
 *   <li><b>false</b> — if no active network is available or the network
 *       lacks internet capability.</li>
 * </ul>
 *
 * @param context The context used to retrieve the system ConnectivityManager.
 * @return {@code true} if internet capability is available, {@code false} otherwise.
 * @throws SecurityException if the caller has not declared
 *         {@code android.permission.ACCESS_NETWORK_STATE}.
 */
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun hasInternet(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}