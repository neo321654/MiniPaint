package com.example.minipaint

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import java.lang.ClassCastException
import java.lang.IllegalStateException

class DialogLenght(myCanvasView: MyCanvasView) : DialogFragment()  {
    private val myCanvasView = myCanvasView
    internal lateinit var listener: DialogLenghtListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       val dialog =  activity?.let {
            val builder = AlertDialog.Builder(it)
           val inflater = requireActivity().layoutInflater;
            builder
                .setView(inflater.inflate(R.layout.dialog_signin, null))
                .setMessage(R.string.interLenght)
                    .setPositiveButton(R.string.posBut) { dialog, id ->
                        listener.onDialogPositiveClick(getDialog()?.findViewById<EditText>(R.id.needLenght)?.text.toString())
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
            listener = myCanvasView as DialogLenghtListener
        }catch (e: ClassCastException){
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    interface DialogLenghtListener {
        fun onDialogPositiveClick(dialog: String)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }
}