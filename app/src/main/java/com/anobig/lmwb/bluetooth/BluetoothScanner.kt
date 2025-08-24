package com.anobig.lmwb.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.anobig.lmwb.utility.App


class BluetoothScanner(val listener: Listener) {
    interface Listener {
        fun onBatchScanResults(results: MutableList<ScanResult>?)
        fun onScanFailed(errorCode: Int)
        fun onScanResult(callbackType: Int, result: ScanResult?)
        fun onBluetoothPermissionNotGranted()
        fun onStopScanning()
        fun onStartScanning()
        fun bluetoothNotEnabled()
        fun onLocationPermissionNotGranted()
    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val TAG = "BluetoothScanner"
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val scanCallback = object: ScanCallback(){
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            listener.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            listener.onScanFailed(errorCode)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            listener.onScanResult(callbackType, result)
        }
    }

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    init {
        try {
            val bluetoothManager = App.get().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
            if (bluetoothManager != null)
                bluetoothAdapter = bluetoothManager.adapter
            else {
                Log.e(TAG, "failed to get BluetoothManager");
            }
            bluetoothLeScanner = bluetoothAdapter?.getBluetoothLeScanner()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanningWithTimeout() {
        if (!hasBLEPermission())
            return

        if (!hasLocationPermission())
            return

        if(bluetoothAdapter?.isEnabled == false) {
            listener.bluetoothNotEnabled()
            return
        }
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner?.stopScan(scanCallback)
                listener.onStopScanning()
            }, SCAN_PERIOD)
            scanning = true
            listener.onStartScanning()

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(1L)
                .build()

            val filter = ScanFilter.Builder().setDeviceName(null).build()

            bluetoothLeScanner?.startScan(arrayListOf(filter), settings, scanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (!hasBLEPermission())
            return
        scanning = false
        bluetoothLeScanner?.stopScan(scanCallback)
        listener.onStopScanning()
    }

    private fun hasLocationPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                App.get(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            listener.onLocationPermissionNotGranted()
            return false
        }
        return true
    }

    private fun hasBLEPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                App.get(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            listener.onBluetoothPermissionNotGranted()
            return false
        }
        return true
    }


}