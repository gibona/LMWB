package com.anobig.lmwb.map.db

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import androidx.room.Room
import com.anobig.lmwb.map.dialogs.AddPinDialog
import com.anobig.lmwb.map.dialogs.EditPinDialog
import com.anobig.lmwb.utility.App
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DbInterface {
    companion object {

        fun addPinDialogShow(activity: Activity, latLng: LatLng) {
            AddPinDialog(activity, latLng).show()
        }

        fun editPinDialogShow(activity: Activity, uid: String?) {
            CoroutineScope(Dispatchers.IO).launch {
                val pins = DB.pinDao().getPin(uid?.toInt()?:0)
                if (pins.isEmpty())
                    return@launch
                val pin = pins[0]

                CoroutineScope(Dispatchers.Main).launch {
                    EditPinDialog(activity, pin).show()
                }
            }
        }

        private val DB by lazy {
            Room.databaseBuilder(
                App.get(),
                MapDb::class.java, "map-db"
            ).build()
        }

        fun addPin(latLng: LatLng, name: String, description: String?) {
            CoroutineScope(Dispatchers.IO).launch {
                DB.pinDao().insertAll(
                    Pin(
                        name = name,
                        lat = latLng.latitude,
                        long = latLng.longitude,
                        description = description
                    )
                )

            }
        }

        private val MARKERS = HashMap<Int, Marker>()

        fun observePinChanges(activity: AppCompatActivity, map: GoogleMap) {
            MARKERS.clear()
            DB.pinDao().getPins().distinctUntilChanged()
                .observe(activity, object : Observer<List<Pin>> {
                    override fun onChanged(value: List<Pin>) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val markersToDelete = HashMap(MARKERS)
                            for (pin in value) {
                                var marker = MARKERS.get(pin.uid)
                                if (marker != null) {
                                    //marker is already on the map
                                    marker.title = pin.name
                                    marker.position = LatLng(pin.lat, pin.long)
                                    markersToDelete.remove(pin.uid)
                                } else {
                                    // marker needs to be added
                                    marker = map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(pin.lat, pin.long))
                                            .title(pin.name)
                                            .snippet(pin.uid.toString())
                                    )
                                    MARKERS.put(pin.uid, marker!!)
                                }
                            }

                            for (marker in markersToDelete.values) {
                                marker.remove() // remove it from map
                                MARKERS.remove(marker.snippet?.toInt()) // remove it from reference map
                            }

                        }
                    }
                })
        }

        fun updatePin(pin: Pin) {
            CoroutineScope(Dispatchers.IO).launch {
                DB.pinDao().update(pin)
            }
        }

        fun deletePin(pin: Pin) {
            CoroutineScope(Dispatchers.IO).launch {
                DB.pinDao().delete(pin.uid)
            }
        }
    }
}