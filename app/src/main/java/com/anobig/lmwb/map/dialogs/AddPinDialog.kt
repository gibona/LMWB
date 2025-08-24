package com.anobig.lmwb.map.dialogs

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.anobig.lmwb.R
import com.anobig.lmwb.map.db.DbInterface
import com.anobig.lmwb.utility.App
import com.google.android.gms.maps.model.LatLng

class AddPinDialog(context: Context, private val latLng: LatLng) : AlertDialog(context), DialogInterface.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {

        val rootView: View? = LayoutInflater.from(context).inflate(R.layout.pin_dialog, null)
        setView(rootView)

        setTitle(App.get().getString(R.string.add_pin))
        setButton(BUTTON_POSITIVE, App.get().getString(R.string.add), this)
        setButton(BUTTON_NEGATIVE, App.get().getString(R.string.cancel), this)
        super.onCreate(savedInstanceState)
    }

    override fun onClick(dialog: DialogInterface?, button: Int) {
        if (button != BUTTON_POSITIVE)
            return
        val nameView = findViewById<AppCompatEditText>(R.id.name)
        val descriptionView = findViewById<AppCompatEditText>(R.id.description)

        DbInterface.addPin(latLng, nameView?.text.toString(), descriptionView?.text.toString())

    }
}