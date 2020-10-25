package io.github.team_sippo.mnist.mnist_location_flutter

import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity: FlutterActivity() {
    companion object {
        private const val CHANNEL = "location_plugin"
        private const val METHOD_GET_LIST = "LocationPlugin.registerLocation"
        private const val TAG = "FlutterActivity"
    }
    private lateinit var channel: MethodChannel
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        channel.setMethodCallHandler { methodCall: MethodCall, result: MethodChannel.Result ->
            val args = methodCall.arguments<ArrayList<*>>()
            if (methodCall.method == METHOD_GET_LIST) {
                val name = args[0]
                Log.d(TAG, "Android name=$name")
                result.success("OK")
            }
            else
                result.notImplemented()
        }
    }
}
