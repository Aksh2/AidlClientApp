package com.project.applicationa

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import   android.content.ServiceConnection
import android.os.Bundle
import android.os.DeadObjectException
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.project.applicationa.utils.CryptoHelper.encrypt
import com.project.applicationb.IEncryptionService


private const val TAG = "ClientApp"

class MainActivity : ComponentActivity() {
    var encryptionService: IEncryptionService? = null
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.d(
                TAG, "Service connected: $p0 " +
                        "${p1?.isBinderAlive}" + "${p1?.pingBinder()}"
            )
            encryptionService = IEncryptionService.Stub.asInterface(p1)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.d(TAG, "Service disconnected: $p0")

            encryptionService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val intent = Intent(IEncryptionService::class.java.name)
        intent.setPackage("com.project.applicationb")
        Log.d(TAG, "Binding to service")
        val result = bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Log.d(TAG, "Binding result: $result")
        Log.d(TAG, "IntefaceName: ${IEncryptionService::class.java.name}")

    }

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
                try {
                    var encryptedData = MESSAGE.encrypt()
                    Log.d(TAG, "Encrypted Request: $encryptedData")
                    val response = encryptionService?.sendOneWayMessage(encryptedData)
                    Log.d(TAG, "Response: $response")
                    result = response.toString()
                } catch (e: RemoteException) {
                    Log.d(TAG, "Remote Exception: $e ${e.message}")

                } catch (e: DeadObjectException) {
                    Log.d(TAG, "Dead object: $e ${e.message}")

                } catch (e: Exception) {
                    Log.e(TAG, "Error: ${e.printStackTrace()}")
                    result = "Exception: ${e.message}"
                }

            }) {
                Text(text = SEND_SECURE_MESSAGE)
            }
            Button(onClick = {
                try {
                    val response = encryptionService?.twoWayMessaging(MESSAGE.encrypt())
                    Log.d(TAG, "Response: $response")
                    result = response.toString()
                } catch (e: RemoteException) {
                    Log.d(TAG, "Remote Exception: $e ${e.message}")

                } catch (e: DeadObjectException) {
                    Log.d(TAG, "Dead object: $e ${e.message}")

                } catch (e: Exception) {
                    Log.d(TAG, "Error: $e ${e.message}")
                    result = "Exception: ${e.printStackTrace()}"
                }
            }) {
                Text(text = "Send two way Messaging")
            }
        }
    }


}

