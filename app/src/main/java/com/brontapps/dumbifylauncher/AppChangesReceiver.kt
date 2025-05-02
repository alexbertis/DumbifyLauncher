package com.brontapps.dumbifylauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AppChangesReceiver(var onReceived: () -> Unit) : BroadcastReceiver() {

    private val allowedActions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        setOf(Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REMOVED, Intent.ACTION_PACKAGE_REPLACED, Intent.ACTION_PACKAGES_SUSPENDED, Intent.ACTION_PACKAGES_UNSUSPENDED)
    } else {
        setOf(Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REMOVED, Intent.ACTION_PACKAGE_REPLACED)
    }

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if (intent.action !in allowedActions) {
            return
        }
        onReceived()
    }
}