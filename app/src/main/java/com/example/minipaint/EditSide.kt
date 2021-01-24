package com.example.minipaint


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.example.minipaint.databinding.ActivityEditSideBinding


class EditSide : Activity() {
    lateinit var bi: ActivityEditSideBinding
     private var idPoint: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_side)
        bi = ActivityEditSideBinding.inflate(layoutInflater)
        setContentView(bi.root)
        //показываем клаву сразу
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //получаю id точки
        idPoint = intent.getIntExtra("idPoint",0)
        bi.button.setOnClickListener {
            var lengthInt = 0

            try {
                lengthInt = bi.needLenght.text.toString().toInt()
                apiSelected(lengthInt)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, R.string.notNumber, Toast.LENGTH_SHORT).show()
//                motionTouchEventX = 0f
//                motionTouchEventY = 0f
            }
        }
    }


    private fun apiSelected(length: Int)
    {
        val intent = Intent()
        intent.putExtra("length", length)
        intent.putExtra("idPoint", idPoint)
        setResult(RESULT_OK, intent)
        finish()
    }
}