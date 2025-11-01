package com.shreefinance.utils

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog

import kotlin.let
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty
import kotlin.text.trim
import kotlin.toString

object CustomAlertDialog {



    fun showSelectionOptionDialog(context: Context, onCameraClick: () -> Unit, onGalleryClick: () -> Unit,onDocumentClick:()-> Unit) {
        val options = arrayOf("Camera", "Gallery", "Cancel")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> onCameraClick()
                1 -> onGalleryClick()
                3 -> dialog.dismiss()
            }
        }

        builder.show()
    }

}