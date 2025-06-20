package com.project.applicationa

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.project.applicationa.ui.theme.ApplicationaTheme
import com.project.applicationa.utils.CryptoHelper
import com.project.applicationa.utils.CryptoHelper.performEncryption


private const val TAG = "ClientApp"

class MainActivity : ComponentActivity() {
    var encryptionService: IEncryptionService? = null
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            encryptionService = IEncryptionService.Stub.asInterface(p1)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            encryptionService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApplicationaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RenderInfoScreen(
                        modifier = Modifier.padding(innerPadding),
                        encryptionService = encryptionService
                    )
                }
            }
        }
        bindToService()
    }

    private fun bindToService() {
        val intent = Intent("com.project.applicationb.SecureEncryptionService")
        intent.setPackage("com.project.applicationb")
        Log.d(TAG, "Binding to service")
        val result = bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Log.d(TAG, "Binding result: $result")
        Log.d(TAG, "IntefaceName: ${IEncryptionService::class.java.name}")

    }

}

private


@Composable
fun RenderInfoScreen(modifier: Modifier = Modifier, encryptionService: IEncryptionService?) {
    var result by remember { mutableStateOf(NO_RESPONSE) }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = HEADING,
            modifier = modifier
        )
        Text(
            text = "Current thread:${Thread.currentThread().name} \n" +
                    "Current id:${Thread.currentThread().id} \n" +
                    "current pid:${android.os.Process.myPid()} \n"
        )
        Text(text = "$RESPONSE $result")
        Button(onClick = {
            Log.d(
                TAG, "Sending secure message in thread:${Thread.currentThread().name} \n" +
                        "id:${Thread.currentThread().id} \n" +
                        "pid:${android.os.Process.myPid()} \n"
            )

            val response = try {
                encryptionService?.processEncrypted(MESSAGE.performEncryption()) ?: byteArrayOf()
            } catch (e: Exception) {
                byteArrayOf()
            }
            val decryptedResponse = CryptoHelper.decrypt(response)
            result = decryptedResponse
            Log.d(TAG, "Response: $response")
            result = response.toString()
        }) {
            Text(text = SEND_SECURE_MESSAGE)
        }
    }


}

