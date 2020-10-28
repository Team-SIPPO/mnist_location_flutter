package io.github.team_sippo.mnist.mnist_location_flutter

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain


class LocationService : Service(), MethodChannel.MethodCallHandler {
    private var locationManager: LocationManager? = null
    private var context: Context? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var callbackHandle: Long? = null
    private var channel: MethodChannel? = null

    companion object {
        private const val CHANNEL = "location_plugin_background"
        private const val METHOD_GET_LIST = "LocationPlugin.registerLocation"
        private const val TAG = "LocationService"
        @JvmStatic
        private var flutterEngine: FlutterEngine? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        /*
        flutterEngine = FlutterEngine(this)
        channel = MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, LocationService.CHANNEL)
        channel.setMethodCallHandler(this)
         */
    }

    fun setFlutterChannel(callbackHandle: Long){
        val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
        if (callbackInfo == null) {
            Log.e(TAG, "Fatal: failed to find callback")
            return
        }
        Log.i(TAG, "Starting GeofencingService...")
        flutterEngine = FlutterEngine(this)

        val args = DartExecutor.DartCallback(
                this.getAssets(),
                FlutterMain.findAppBundlePath(),
                callbackInfo
        )
        flutterEngine!!.dartExecutor.executeDartCallback(args)
        channel!!.setMethodCallHandler(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("LocationService", "start command")
        callbackHandle = intent.getLongExtra("callbackHandle", -1)

        setFlutterChannel(callbackHandle!!)
        foreGroundStart(intent)
        startGPS()
        return START_NOT_STICKY
    }

    private fun foreGroundStart(intent: Intent){
        val requestCode = 0
        val channelId = "default"
//        val title = context!!.getString(R.string.app_name)
        val title = "aaa"
        val pendingIntent = PendingIntent.getActivity(
                context, requestCode,
                intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ForegroundにするためNotificationが必要、Contextを設定
        val notificationManager =
                context!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Notification　Channel 設定
        val fgChannel = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel(
                    channelId, title, NotificationManager.IMPORTANCE_DEFAULT
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        fgChannel.description = "Silent Notification"
        // 通知音を消さないと毎回通知音が出てしまう
        // この辺りの設定はcleanにしてから変更
        fgChannel.setSound(null, null)
        // 通知ランプを消す
        fgChannel.enableLights(false)
        fgChannel.lightColor = Color.BLUE
        // 通知バイブレーション無し
        fgChannel.enableVibration(false)
        notificationManager.createNotificationChannel(fgChannel)
        val notification = Notification.Builder(context, channelId)
                .setContentTitle(title) // 本来なら衛星のアイコンですがandroid標準アイコンを設定
                .setSmallIcon(android.R.drawable.btn_star)
                .setContentText("GPS")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .build()

        // startForeground
        startForeground(1, notification)

    }

    @SuppressLint("MissingPermission")
    protected fun startGPS() {
        val strBuf = StringBuilder()
        Log.d("LocationService", "start gps.")
        strBuf.append("startGPS\n")

        val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled) {
            // GPSを設定するように促す
            Log.d("LocationService", "promote gps.")
            enableLocationSettings()
        }
        try {
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("LocationService", "not granted.")
                return
            }
            /*
            locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MinTime.toLong(), MinDistance, this
            )
             */
            val locationRequest = LocationRequest().apply {
                // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
                // 今回は公式のサンプル通りにする。
                interval = 10000                                   // 最遅の更新間隔(但し正確ではない。)
                fastestInterval = 5000                             // 最短の更新間隔
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 精度重視
            }
            // コールバック

            if(locationCallback != null){
                fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
            }
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    // 更新直後の位置が格納されているはず
                    val location = locationResult?.lastLocation ?: return
                    Log.d("LocationService", "緯度:${location.latitude}, 経度:${location.longitude}")
                    val result = HashMap<String, Double>()
                    result["latitude"] = location.latitude
                    result["longitude"] = location.longitude
                    Log.d(TAG, "callbackHandle = " + callbackHandle.toString())
                    Log.d(TAG, "result = " + result.toString())
                    channel!!.invokeMethod("", listOf(callbackHandle, result))
                    Log.d(TAG, "invoke method")
                }
            }
            fusedLocationProviderClient!!.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()
            )
            Log.d("LocationService", "requests update finish.")


        } catch (e: Exception) {
            Log.d("LocationService", "catch errpr.")

            e.printStackTrace()
        }

    }


    private fun enableLocationSettings() {
        val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(settingsIntent)
    }

    @SuppressLint("MissingPermission")
    private fun stopGPS() {
        Log.d("LocationService", "stopGPS")
        if (fusedLocationProviderClient != null) {
            // update を止める
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGPS()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        // TODO("Not yet implemented")
        val args = call.arguments<ArrayList<*>>()
        val callbackH = args[0] as Long
        val name = args[1] as String
        Log.d(TAG, "methodcall on Service")
        result.success("OK")
    }

}