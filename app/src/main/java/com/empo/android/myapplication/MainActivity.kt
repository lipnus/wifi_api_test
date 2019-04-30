package com.empo.android.myapplication

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.util.Log
import java.util.ArrayList
import android.net.wifi.WifiConfiguration
import android.provider.Settings
import android.net.TrafficStats
import android.support.v7.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import android.net.wifi.WifiInfo
import android.net.ConnectivityManager.setProcessDefaultNetwork
import android.os.Build
import android.net.Network
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest






class MainActivity : AppCompatActivity() {


    private val wifiManager by lazy { applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }

    private val wifiScanReceiver: BroadcastReceiver by lazy {
         object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {

                Log.d("SSS", "<WifiReceiber>")

                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)

                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
         }
    }


    private fun scanSuccess() {
        val results = wifiManager.scanResults
        for(i in 0..results.size-1){
            Log.d("SSS",

      "[SSID]: " + results[i].SSID +
            " [frequency]: " + results[i].frequency +
            " [BSSID]: " + results[i].BSSID )
        }
    }


    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager.scanResults
        Log.d("SSS", "scanFailure(): " + results)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initButton()
        initPermission()

    }


    override fun onResume() {
        Log.d("SSS", "onResume(), 리시버등록")
        registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        super.onResume()
    }


    override fun onPause() {
        Log.d("SSS", "onResume(), 리시버제거")
        unregisterReceiver(wifiScanReceiver)
        super.onPause()
    }


    fun initPermission(){

        val permissionListener: PermissionListener = object: PermissionListener {

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                Log.d("SSS", "onPermissionDenied(): " + deniedPermissions.toString())
            }

            override fun onPermissionGranted() {
                Log.d("SSS", "onPermissionGranted()")
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setRationaleMessage("위치권한좀")
            .setDeniedMessage("거절 \n하지만 [설정] > [권한] 에서 권한을 허용할 수 있다")
            .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .check()
    }


    fun initButton(){
        wifiOn_btn.setOnClickListener {
            Log.d("SSS", "와이파이 켜기")
            wifiManager.isWifiEnabled = true
        }


        wifiOff_btn.setOnClickListener {
            Log.d("SSS", "와이파이 끄기")
            wifiManager.isWifiEnabled = false
        }


        wifiState_btn.setOnClickListener {
            Log.d("SSS", "와이파이생태: " + wifiManager.isWifiEnabled() )
        }


        wifiScan_btn.setOnClickListener {

            wifiManager.startScan()
            Log.d("SSS", "scanResult: " + wifiManager.scanResults)
        }

        wifiConnet_btn.setOnClickListener {
            connectWifi()
        }

        wifiDisconnect_btn.setOnClickListener {
            Log.d("SSS", "와이파이 끊음")
            wifiManager.disconnect()
        }

        airplaneDetect_btn.setOnClickListener {
            Log.d("SSS", "비행기모드: " + isAirplaneModeOn(this))
        }

        wifiMeasure_btn.setOnClickListener {

            //부팅된 이후로의 데이터 계
            val mobileTx = TrafficStats.getMobileTxBytes().toInt()
            val mobileRx = TrafficStats.getMobileRxBytes().toInt()
            val wifiTx = (TrafficStats.getTotalTxBytes() - mobileTx).toInt()
            val wifiRx = (TrafficStats.getTotalRxBytes() - mobileRx).toInt()

            Log.d("SSS", "[mobileTx]:$mobileTx, [mobileRx]:$mobileRx, [wifiTx]:$wifiTx, [wifiRx]:$wifiRx")
        }

        connected_info_btn.setOnClickListener {
            connectedWifiInfoBtn()
        }
    }


    private fun connectedWifiInfoBtn(){

        val info = wifiManager.connectionInfo
        val ssid = info.ssid
        Log.d("SSS", "연결된 와이파이: "+ ssid)
    }


    private fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.System.getInt(context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0) !== 0
    }


    fun connectWifi(){

        val info = wifiManager.connectionInfo //get WifiInfo
        val pastId = info.networkId //get id of currently connected network


        Log.d("SSS", "기존와이파이: $pastId")

        //remember id
        wifiManager.disconnect() //기존의 것은 끊고
        wifiManager.disableNetwork(pastId) //싹을 자른다
        wifiManager.removeNetwork(pastId)

        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = String.format("\"%s\"", "EMPO-3479K68")
        wifiConfig.preSharedKey = String.format("\"%s\"", "YXDVUENL")
        val netId = wifiManager.addNetwork(wifiConfig)

        wifiManager.enableNetwork(netId, true)
        wifiManager.reconnect()

        Log.d("SSS", "접속한 와이파이:$netId")

        ddd()

    }


    fun ddd(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val builder: NetworkRequest.Builder
            builder = NetworkRequest.Builder()

            //set the transport type do WIFI
            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

            manager.requestNetwork(builder.build(), object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        manager.bindProcessToNetwork(network)
                    } else {
                        ConnectivityManager.setProcessDefaultNetwork(network)
                    }
                    try {
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    manager.unregisterNetworkCallback(this)
                }
            })
        }
    }



}
