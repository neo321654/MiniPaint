package com.example.minipaint

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment


class DialogLenght(myCanvasView: MyCanvasView, editedPoint: MyPoint) : DialogFragment()  {

    private val myCanvasView = myCanvasView
    private val editedPoint = editedPoint
    internal lateinit var listener: DialogLenghtListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       val dialog =  activity?.let {
            val builder = AlertDialog.Builder(it)
           val inflater = requireActivity().layoutInflater;
            builder
                .setView(inflater.inflate(R.layout.dialog_signin, null))
                .setMessage(R.string.interLenght)
                    .setPositiveButton(R.string.posBut) { dialog, id ->
                        listener.onDialogPositiveClick(getDialog()?.findViewById<EditText>(R.id.needLenght)?.text.toString(), editedPoint.idPoint)
                    }
                    .setNegativeButton(R.string.cancel) { dialog, id ->
                        listener.onDialogNegativeClick(this)
                    }

            builder.create()
        }?: throw IllegalStateException("Activity can't be null")


        dialog.setOnShowListener{
            Log.d("log", "setOnShowListener")

          //  Log.d("log", "${view?.findViewById<EditText>(R.id.needLenght)}")

            Log.d("log", "${dialog.getButton(Dialog.BUTTON_NEGATIVE)}")

        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//       val ed = view.findViewById<EditText>(R.id.needLenght)
//        val imm: InputMethodManager = myCanvasView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(ed, InputMethodManager.SHOW_IMPLICIT)
        Log.d("log", "onViewCreated")
    }

    override fun onResume() {
        super.onResume()
//        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//        imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        Log.d("log", "${view?.findViewById<EditText>(R.id.needLenght)}")
       // view?.findViewById<EditText>(R.id.needLenght)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        Log.d("log", "onAttach")
        try{
            listener = myCanvasView as DialogLenghtListener
        }catch (e: ClassCastException){
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    interface DialogLenghtListener {
        fun onDialogPositiveClick(dialog: String, idPoint: Int)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }
}