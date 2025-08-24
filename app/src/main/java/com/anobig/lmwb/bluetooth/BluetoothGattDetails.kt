package com.anobig.lmwb.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.annotation.RequiresPermission

class BluetoothGattDetails(private val updateData: Runnable): BluetoothGattCallback() {
    private var disconnected: Boolean = false
    private var gatt: BluetoothGatt? = null
    private val TAG: String = "BluetoothGattDetails"
    private var device: BluetoothDevice? = null
    private var info: String = ""

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getName(): CharSequence {
        return device?.name?:device?.address?:"Connecting..."
    }

    fun getInfo(): CharSequence {
        return info
    }

    private fun addInfo(line: String) {
        info += line + "\n"
        updateData.run()
        Log.e(TAG, line)
        Log.e(TAG, "All info:" + info)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onConnectionStateChange(
        gatt: BluetoothGatt?,
        status: Int,
        newState: Int
    ) {
        super.onConnectionStateChange(gatt, status, newState)
        if (disconnected)
            return
        
        device = gatt?.device
        this.gatt = gatt
        updateData.run()
        val deviceAddress = gatt?.device?.address
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.discoverServices()
                addInfo("Successfully connected to $deviceAddress")
                // TODO: Store a reference to BluetoothGatt
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                addInfo("Successfully disconnected from $deviceAddress")
                gatt?.close()
            }
        } else {
            addInfo("Error $status encountered for $deviceAddress! Disconnecting...")
            gatt?.close()
        }
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        with(gatt)  {
            addInfo("Discovered ${services.size} services for ${device.address}")
            if (services.isEmpty()) {
                addInfo("No service and characteristic available, call discoverServices() first?")
                return
            }
            services.forEach { service ->
                val characteristicsTable = service.characteristics.joinToString(
                    separator = "|--",
                    prefix = "|--"
                ) { it.uuid.toString()+"\n" }
                addInfo("Service ${service.uuid}\nCharacteristics:\n$characteristicsTable"
                )
            }

            // Consider connection setup as complete here
        }
        disconnect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        disconnected = true;
        gatt?.disconnect()
    }
}
