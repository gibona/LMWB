package com.anobig.lmwb.bluetooth

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import com.anobig.lmwb.bluetooth.placeholder.PlaceholderContent
import com.anobig.lmwb.databinding.FragmentBluetoothdeviceDetailBinding
import com.anobig.lmwb.utility.App

/**
 * A fragment representing a single Bluetooth Device detail screen.
 * This fragment is either contained in a [BluetoothDeviceListFragment]
 * in two-pane mode (on larger screen devices) or self-contained
 * on handsets.
 */
class BluetoothDeviceDetailFragment : Fragment() {

    private val emptyScanner = object :ScanCallback() {

    }
    private var itemDetailTextView: TextView? = null
    private var toolbarLayout: CollapsingToolbarLayout? = null

    private var _binding: FragmentBluetoothdeviceDetailBinding? = null
    private var bluetoothGattDetails = BluetoothGattDetails { updateContent() }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let  {
            if (it.containsKey(ARG_ITEM_ID)) {
                val bluetoothManager = App.get().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
                val bluetoothAdapter = bluetoothManager?.adapter
                val bluetoothLeScanner = bluetoothAdapter?.getBluetoothLeScanner()

                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .setReportDelay(1L)
                    .build()

                val filter = ScanFilter.Builder().setDeviceAddress(it.getString(ARG_ITEM_ID)).build()

                bluetoothLeScanner?.startScan(arrayListOf(filter), settings, object: ScanCallback() {
                    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
                    override fun onScanResult(callbackType: Int, result: ScanResult?) {
                        super.onScanResult(callbackType, result)
                        result?.device?.connectGatt(App.get(), true, bluetoothGattDetails)
                        bluetoothLeScanner.stopScan(emptyScanner)
                    }

                    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
                    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                        super.onBatchScanResults(results)
                        bluetoothLeScanner.stopScan(emptyScanner)
                        if (results?.size == 0)
                            return
                        results?.elementAt(0)?.device?.connectGatt(App.get(), true, bluetoothGattDetails)
                    }

                    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
                    override fun onScanFailed(errorCode: Int) {
                        super.onScanFailed(errorCode)
                        bluetoothLeScanner.stopScan(emptyScanner)
                    }
                })

            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentBluetoothdeviceDetailBinding.inflate(inflater, container, false)
        val rootView = binding.root

        toolbarLayout = binding.toolbarLayout
        itemDetailTextView = binding.bluetoothdeviceDetail

        updateContent()

        return rootView
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun updateContent() {
        toolbarLayout?.title = bluetoothGattDetails.getName()

        // Show the placeholder content as text in a TextView.
        itemDetailTextView?.text = bluetoothGattDetails.getInfo()
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bluetoothGattDetails.disconnect()
    }
}