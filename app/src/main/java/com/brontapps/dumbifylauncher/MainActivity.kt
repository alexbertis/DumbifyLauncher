package com.brontapps.dumbifylauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
        Column(modifier = modifier.verticalScroll(rememberScrollState())) {
            for (app in apps) {
                AppEntry(name = app.name)
            }
        }
    }
}

@Composable
fun AppEntry(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row (modifier = modifier.fillMaxWidth()){
        Surface(onClick = { Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show() }, modifier = modifier.fillMaxWidth(), color = Color.Black) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    modifier = modifier.padding(16.dp),
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DumbifyLauncherTheme {
        AppEntry("Android")
    }
}


fun getApps(pManager: PackageManager): ArrayList<AppInfo> {
    val appsList = ArrayList<AppInfo>()
    val i = Intent(Intent.ACTION_MAIN, null)
    i.addCategory(Intent.CATEGORY_LAUNCHER)
    val allApps: List<ResolveInfo> = pManager.queryIntentActivities(i, PackageManager.MATCH_ALL)

    for (ri in allApps) {
        val app = AppInfo(name = ri.loadLabel(pManager).toString(), packageName = ri.activityInfo.packageName)
        Log.i(" Log package ", app.packageName)
        appsList.add(app)
    }
    appsList.sortBy { appInfo -> appInfo.name }
    return appsList
}