package com.brontapps.dumbifylauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brontapps.dumbifylauncher.ui.theme.DumbifyLauncherTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pManager: PackageManager = this.packageManager

        setContent {
            DumbifyLauncherTheme {
                MainScreen(
                    apps = getApps(pManager),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier,
               apps: List<AppInfo> = emptyList()) {
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
            for (app in apps) {
                AppEntry(appInfo = app)
            }
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


fun getApps(pManager: PackageManager): List<AppInfo> {
    //val appsList = ArrayList<AppInfo>()
    val i = Intent(Intent.ACTION_MAIN, null)
    i.addCategory(Intent.CATEGORY_LAUNCHER)
    val allApps: List<ResolveInfo> = pManager.queryIntentActivities(i, PackageManager.MATCH_ALL)

    val appsList = allApps.map { ri -> AppInfo(name = ri.loadLabel(pManager).toString(), packageName = ri.activityInfo.packageName) }

    return appsList.sortedBy { appInfo -> appInfo.name.lowercase() }
}