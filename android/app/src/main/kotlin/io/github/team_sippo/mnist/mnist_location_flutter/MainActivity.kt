package io.github.team_sippo.mnist.mnist_location_flutter

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity: FlutterActivity() {
    companion object {
        private const val CHANNEL = "location_plugin"
        private const val METHOD_GET_LIST = "LocationPlugin.registerLocation"
        private const val METHOD_INITIALIZE = "LocationPlugin.initialize"
        private const val TAG = "FlutterActivity"
        private const val REQUEST_MULTI_PERMISSIONS = 101

    }
    private var callbackHandle: Long? = null
    private lateinit var channel: MethodChannel
    @SuppressLint("NewApi")
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        channel.setMethodCallHandler { methodCall: MethodCall, result: MethodChannel.Result ->
            val args = methodCall.arguments<ArrayList<*>>()
            if (methodCall.method == METHOD_GET_LIST) {
                callbackHandle = args[0] as Long
                Log.d(TAG, "act callbackHandle = " + callbackHandle.toString())
                val name = args[1] as String
                Log.d(TAG, "Android name=$name")
                Log.d(TAG,"before")
                val map: HashMap<String, Double> = HashMap()
                map["longitude"] = 12.0
                map["latitude"] = 32.4
                // channel.invokeMethod("", listOf(callbackHandle, map))
                Log.d(TAG,"after")
                result.success("OK")
                // API 26 以降
                Log.d(TAG, "start location")
                checkMultiPermissions()
                Log.d(TAG, "end location")
            } else if (methodCall.method == METHOD_INITIALIZE) {
                callbackHandle = args[0] as Long
                Log.d(TAG, "act callbackHandle = " + callbackHandle.toString())
                val name = args[1] as String
                Log.d(TAG, "Android name=$name")
                Log.d(TAG,"before")
                // channel.invokeMethod("", listOf(callbackHandle, map))
                Log.d(TAG,"after")
                result.success("OK")
                // API 26 以降
                Log.d(TAG, "start location")
                checkMultiPermissions()
                Log.d(TAG, "end location")
            } else {
                result.notImplemented()
            }
        }
    }

    @SuppressLint("NewApi")
    fun startLocationService(){
        val intent = Intent(application, LocationService::class.java)
        if (callbackHandle != null) {
            val cbkHandle = callbackHandle
            intent.putExtra("callbackHandle", cbkHandle)
        }
         startForegroundService(intent)
    }

    // 位置情報許可の確認、外部ストレージのPermissionにも対応できるようにしておく
    private fun checkMultiPermissions() {
        // 位置情報の Permission
        val permissionLocationFore = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionLocationBack = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            true
        }


        val reqPermissions = ArrayList<String>()

        // 位置情報の Permission が許可されているか確認
        if (permissionLocationFore == PackageManager.PERMISSION_GRANTED &&
                (permissionLocationBack == PackageManager.PERMISSION_GRANTED || permissionLocationBack == true)) {
            // 許可済
        } else {
            // 未許可
            reqPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                reqPermissions.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        // 未許可
        if (!reqPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    reqPermissions.toTypedArray(),
                    REQUEST_MULTI_PERMISSIONS
            )
            // 未許可あり
        } else {
            // 許可済
            startLocationService()
        }
    }

    // 結果の受け取り
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_MULTI_PERMISSIONS) {
            if (grantResults.size > 0) {
                for (i in permissions.indices) {
                    // 位置情報
                    if (permissions[i] == android.Manifest.permission.ACCESS_FINE_LOCATION) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // 許可された
                        } else {
                            // それでも拒否された時の対応
                            toastMake("位置情報の許可がないので計測できません")
                        }
                    }
                }
                startLocationService()
            }
        }
    }


    // トーストの生成
    private fun toastMake(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        // 位置調整
        toast.setGravity(Gravity.CENTER, 0, 200)
        toast.show()
    }

}
