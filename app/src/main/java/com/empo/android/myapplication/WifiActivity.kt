package com.empo.android.myapplication

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
/**
 * Root activity for Wi-Fi list and details.
 */
open class WifiActivity() : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 120
    }

    private val wifiManager: WifiManager
        get() = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager


    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            Log.d("SSS", "입질왔다")
            val results = wifiManager.scanResults
            if (listFragmentVisible && results != null) {
                Log.d("SSS", results.toString())
//                listFragment?.updateItems(results)
            }
        }
    }

    private var listFragmentVisible: Boolean = false
    private var wifiReceiverRegistered: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi)

//        transitionToList()

    }



    override fun onStart() {
        super.onStart()
        startScanning()
    }



    override fun onStop() {
        stopScanning()
        super.onStop()
    }

    private fun startScanning() {
        if (checkPermissions()) {
            registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            wifiReceiverRegistered = true
        }
    }

    private fun stopScanning() {
        if (wifiReceiverRegistered) {
            unregisterReceiver(wifiReceiver)
            wifiReceiverRegistered = false
        }
    }




    /**
     * Permissions
     */

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION)
            return false
        } else {
            return true
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION -> {
//                startScanning()
//            }
//        }
//    }
}
