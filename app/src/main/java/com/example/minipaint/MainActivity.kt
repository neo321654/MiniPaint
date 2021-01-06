package com.example.minipaint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class MainActivity : AppCompatActivity() {
    private lateinit var myCanvasView: MyCanvasView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myCanvasView = MyCanvasView(this, supportFragmentManager)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
       // myCanvasView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
       // hideSystemUI()
        window.decorView.setOnSystemUiVisibilityChangeListener {
            hideSystemUI()
        }
        setContentView(myCanvasView)
    }



    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
         hideSystemUI()
    }
//
//    override fun onDialogPositiveClick(lenght: String) {
//
//        Toast.makeText(this,lenght,Toast.LENGTH_LONG).show()
//    }
//
//    override fun onDialogNegativeClick(dialog: DialogFragment) {
//        Toast.makeText(this,dialog.id.toString(),Toast.LENGTH_LONG).show()
//    }
    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}