package com.example.minipaint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class MainActivity : AppCompatActivity() ,DialogLenght.DialogLenghtListener{
    private lateinit var myCanvasView: MyCanvasView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myCanvasView = MyCanvasView(this, supportFragmentManager)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        myCanvasView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        myCanvasView.contentDescription = getString(R.string.canvasContentDescription)
        myCanvasView.setOnLongClickListener {

            Toast.makeText(this, "Long click detected", Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
        setContentView(myCanvasView)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        Toast.makeText(this,dialog.id.toString(),Toast.LENGTH_LONG).show()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        Toast.makeText(this,dialog.id.toString(),Toast.LENGTH_LONG).show()
    }
}