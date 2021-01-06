package com.example.minipaint

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import java.lang.ClassCastException
import java.lang.IllegalStateException

class DialogLenght : DialogFragment()  {
    internal lateinit var listener: DialogLenghtListener
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


       val dialog =  activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.interLenght)
                    .setPositiveButton(R.string.posBut) { dialog, id ->
                        listener.onDialogPositiveClick(this)
                    }
                    .setNegativeButton(R.string.cancel) {dialog, id ->
                        listener.onDialogNegativeClick(this)
                    }

            builder.create()
        }?: throw IllegalStateException("Activity can't be null")

        return dialog
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try{
            listener = context as DialogLenghtListener
        }catch (e: ClassCastException){
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    interface DialogLenghtListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }
}