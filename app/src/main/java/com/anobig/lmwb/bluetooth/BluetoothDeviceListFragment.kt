package com.anobig.lmwb.bluetooth

import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.anobig.lmwb.R
import com.anobig.lmwb.bluetooth.placeholder.PlaceholderContent
import com.anobig.lmwb.databinding.BluetoothdeviceListContentBinding
import com.anobig.lmwb.databinding.FragmentBluetoothdeviceListBinding
import com.anobig.lmwb.utility.App
import com.google.android.material.snackbar.Snackbar

/**
 * A Fragment representing a list of Pings. This fragment
 * has different presentations for handset and larger screen devices. On
 * handsets, the fragment presents a list of items, which when touched,
 * lead to a {@link BluetoothDeviceDetailFragment} representing
 * item details. On larger screens, the Navigation controller presents the list of items and
 * item details side-by-side using two vertical panes.
 */

class BluetoothDeviceListFragment : Fragment(), MenuProvider, BluetoothScanner.Listener {

    private lateinit var adapter: BluetoothDeviceListFragment.SimpleItemRecyclerViewAdapter
    private var scanning: Boolean = false
    private val PERMISIONS_CODE: Int = 1001;
    private lateinit var scanner: BluetoothScanner
    /**
     * Method to intercept global key events in the
     * item list fragment to trigger keyboard shortcuts
     * Currently provides a toast when Ctrl + Z and Ctrl + F
     * are triggered
     */
    private val unhandledKeyEventListenerCompat =
        ViewCompat.OnUnhandledKeyEventListenerCompat { v, event ->
            if (event.keyCode == KeyEvent.KEYCODE_Z && event.isCtrlPressed) {
                Toast.makeText(
                    v.context,
                    "Undo (Ctrl + Z) shortcut triggered",
                    Toast.LENGTH_LONG
                ).show()
                true
            } else if (event.keyCode == KeyEvent.KEYCODE_F && event.isCtrlPressed) {
                Toast.makeText(
                    v.context,
                    "Find (Ctrl + F) shortcut triggered",
                    Toast.LENGTH_LONG
                ).show()
                true
            }
            false
        }

    private var _binding: FragmentBluetoothdeviceListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        scanner = BluetoothScanner(this)
        registerForActivityResult(ActivityResultContracts.RequestPermission(), object: ActivityResultCallback<Boolean> {
            override fun onActivityResult(result: Boolean) {
                Log.e("Permission", "BLE status: $result")
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val menuHost: MenuHost = requireActivity()

        // Add the MenuProvider to the MenuHost
        menuHost.addMenuProvider(
            this, // your Fragment implements MenuProvider, so we use this here
            viewLifecycleOwner, // Only show the Menu when your Fragment's View exists
            Lifecycle.State.RESUMED // And when the Fragment is RESUMED
        )

        _binding = FragmentBluetoothdeviceListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.addOnUnhandledKeyEventListener(view, unhandledKeyEventListenerCompat)

        val recyclerView: RecyclerView = binding.bluetoothdeviceList

        // Leaving this not using view binding as it relies on if the view is visible the current
        // layout configuration (layout, layout-sw600dp)
        val itemDetailFragmentContainer: View? =
            view.findViewById(R.id.bluetoothdevice_detail_nav_container)

        setupRecyclerView(recyclerView, itemDetailFragmentContainer)
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        itemDetailFragmentContainer: View?
    ) {

        adapter = SimpleItemRecyclerViewAdapter(
            ArrayList(), itemDetailFragmentContainer
        )
        recyclerView.adapter = adapter

    }

    class SimpleItemRecyclerViewAdapter(
        private val values: ArrayList<PlaceholderContent.PlaceholderItem>,
        private val itemDetailFragmentContainer: View?
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val binding = BluetoothdeviceListContentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.id.toString()
            holder.contentView.text = item.name

            with(holder.itemView) {
                tag = item
                setOnClickListener { itemView ->
                    val item = itemView.tag as PlaceholderContent.PlaceholderItem
                    val bundle = Bundle()
                    bundle.putString(
                        BluetoothDeviceDetailFragment.ARG_ITEM_ID,
                        item.address
                    )
                    if (itemDetailFragmentContainer != null) {
                        itemDetailFragmentContainer.findNavController()
                            .navigate(R.id.fragment_bluetoothdevice_detail, bundle)
                    } else {
                        itemView.findNavController()
                            .navigate(R.id.show_bluetoothdevice_detail, bundle)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    /**
                     * Context click listener to handle Right click events
                     * from mice and trackpad input to provide a more native
                     * experience on larger screen devices
                     */
                    setOnContextClickListener { v ->
                        val item = v.tag as PlaceholderContent.PlaceholderItem
                        Toast.makeText(
                            v.context,
                            "Context click of item " + item.id,
                            Toast.LENGTH_LONG
                        ).show()
                        true
                    }
                }
            }
        }

        override fun getItemCount() = values.size

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        fun bind(results: MutableList<ScanResult>?) {
            values.clear()

            if(results == null) {
                notifyDataSetChanged()
                return
            }

            for(result in results.withIndex()) {
                values.add(
                    PlaceholderContent.PlaceholderItem(
                        result.index,
                        result.value.device.name?:result.value.device.address,
                        result.value.device.address
                ))
            }
            notifyDataSetChanged()
        }
        inner class ViewHolder(binding: BluetoothdeviceListContentBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val idView: TextView = binding.idText
            val contentView: TextView = binding.content
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.bluetooth_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.action_start -> {

                //scan start
                scanner.startScanningWithTimeout()
                return true
            }
            R.id.action_stop -> {
                //scan start
                scanner.stopScanning()
                return true
            }
            else -> return false
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        adapter.bind(results)
    }

    override fun onScanFailed(errorCode: Int) {

    }

    override fun onScanResult(callbackType: Int, result: ScanResult?) {

    }

    override fun onLocationPermissionNotGranted() {
        var activity = requireActivity()
        if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(binding.root, R.string.location_permission_needed, Snackbar.LENGTH_LONG)
                .setAction(R.string.app_permissions, {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", App.get().packageName, null))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    App.get().startActivity(intent)
                }).show()

        }
        else
            activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISIONS_CODE )

    }
    override fun onBluetoothPermissionNotGranted() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S)
            return
        var activity = requireActivity()
        if (activity.shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)) {
            Snackbar.make(binding.root, R.string.bluetooth_permission_needed, Snackbar.LENGTH_LONG)
                .setAction(R.string.app_permissions, {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", App.get().packageName, null))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    App.get().startActivity(intent)
            }).show()

        }
        else
            activity.requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), PERMISIONS_CODE )

    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        menu.findItem(R.id.action_start).setEnabled(!scanning)
        menu.findItem(R.id.action_stop).setEnabled(scanning)
    }
    override fun onStopScanning() {
        scanning = false
        requireActivity().invalidateOptionsMenu()
    }

    override fun onStartScanning() {
        scanning = true
        requireActivity().invalidateOptionsMenu()
    }

    override fun bluetoothNotEnabled() {
        Snackbar.make(binding.root, R.string.bluetooth_disabled, Snackbar.LENGTH_LONG)
            .setAction(R.string.enable, {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                App.get().startActivity(intent)
            }).show()
    }
}