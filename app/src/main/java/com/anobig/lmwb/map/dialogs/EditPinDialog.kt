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
import com.anobig.lmwb.map.db.Pin
import com.anobig.lmwb.utility.App

class EditPinDialog(context: Context, private val pin: Pin) : AlertDialog(context), DialogInterface.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {

        val rootView: View? = LayoutInflater.from(context).inflate(R.layout.pin_dialog, null)
        setView(rootView)

        setTitle(App.get().getString(R.string.edit_pin))

        val nameView = rootView?.findViewById<AppCompatEditText>(R.id.name)
        val descriptionView = rootView?.findViewById<AppCompatEditText>(R.id.description)

        nameView?.setText(pin.name)
        descriptionView?.setText(pin.description)

        setButton(BUTTON_POSITIVE, App.get().getString(R.string.edit), this)
        setButton(BUTTON_NEGATIVE, App.get().getString(R.string.cancel), this)
        setButton(BUTTON_NEUTRAL, App.get().getString(R.string.delete), this)

        super.onCreate(savedInstanceState)
    }

    override fun onClick(dialog: DialogInterface?, button: Int) {
        val nameView = findViewById<AppCompatEditText>(R.id.name)
        val descriptionView = findViewById<AppCompatEditText>(R.id.description)

        if (button == BUTTON_POSITIVE) {
            pin.name = nameView?.text.toString()
            pin.description = descriptionView?.text.toString()
            DbInterface.updatePin(pin)
            return
        }

        if (button == BUTTON_NEUTRAL) {
            DbInterface.deletePin(pin)
            return
        }

    }
}