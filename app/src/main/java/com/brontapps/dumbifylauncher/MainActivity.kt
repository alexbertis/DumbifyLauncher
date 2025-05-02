package com.brontapps.dumbifylauncher

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import com.brontapps.dumbifylauncher.ui.theme.DumbifyLauncherTheme
import java.text.Normalizer


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DumbifyLauncherTheme {
                StatefulScreen(
                    modifier = Modifier.fillMaxSize(),
                    onConfirmation = {
                        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                        startActivity(intent)
                    },
                    isLauncherDefault = Helpers.isAppLauncherDefault(this)
                )
            }
        }

    }

    private fun getApps(): List<AppInfo> {
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        val allApps: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            packageManager.queryIntentActivities(i, PackageManager.MATCH_ALL)
        } else {
            packageManager.queryIntentActivities(i, 0)
        }

        val appsList = allApps
            .map { ri ->
                AppInfo(
                    name = ri.loadLabel(packageManager).toString(),
                    packageName = ri.activityInfo.packageName
                )
            }
            .filterNot { info ->
                info.packageName == packageName
            }

        return appsList.sortedBy { appInfo ->
            Normalizer.normalize(appInfo.name.lowercase(), Normalizer.Form.NFD)
        }
    }


    @Composable
    fun StatefulScreen(
        modifier: Modifier = Modifier,
        onConfirmation: () -> Unit,
        isLauncherDefault: Boolean
    ) {
        var currentAppsList by remember { mutableStateOf(getApps()) }

        val appChangesReceiver = remember {
            AppChangesReceiver {
                // On apps changed (notified from Broadcast Receiver), get the apps again
                currentAppsList = getApps()
            }
        }
        val context = LocalContext.current
        LifecycleStartEffect(true) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addAction(Intent.ACTION_PACKAGES_SUSPENDED)
                    addAction(Intent.ACTION_PACKAGES_UNSUSPENDED)
                }
                addDataScheme("package")
            }

            ContextCompat.registerReceiver(context, appChangesReceiver, filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            onStopOrDispose { context.unregisterReceiver(appChangesReceiver) }
        }

        // A surface container using the 'background' color from the theme
        MainScreen(
            onConfirmation = onConfirmation,
            isLauncherDefault = isLauncherDefault,
            modifier = modifier,
            apps = currentAppsList
        )
    }

}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    apps: List<AppInfo>,
    onConfirmation: () -> Unit,
    isLauncherDefault: Boolean
) {
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp)) {
            for (app in apps) {
                AppEntry(appInfo = app)
            }
        }
    }

    val openAlertDialog = remember { mutableStateOf((!isLauncherDefault)) }
    when {
        openAlertDialog.value -> {
            SettingsAlertDialog(
                onDismissRequest = { openAlertDialog.value = false },
                onConfirmation = {
                    openAlertDialog.value = false
                    onConfirmation()
                },
                dialogTitle = stringResource(R.string.alert_dialog_title),
                dialogText = stringResource(R.string.alert_dialog_description)
            )
        }
    }
}

@Composable
fun AppEntry(appInfo: AppInfo, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row (modifier = modifier.fillMaxWidth()){
        Surface(onClick = {
            launchApp(appInfo.packageName, context)
        }, modifier = modifier.fillMaxWidth(), color = Color.Black) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appInfo.name,
                    modifier = modifier.padding(16.dp),
                    color = Color.White
                )
            }
        }
    }
}
@Composable
fun SettingsAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(R.string.alert_dialog_settings_proceed))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.alert_dialog_dismiss))
            }
        }
    )
}


fun launchApp(packageName: String, context: Context) {
    val launchIntent: Intent? = context.packageManager.getLaunchIntentForPackage(packageName)
    launchIntent?.let { context.startActivity(it) }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DumbifyLauncherTheme {
        AppEntry(AppInfo("Android", "com.android.yo"))
    }
}
