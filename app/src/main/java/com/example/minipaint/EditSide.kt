package com.example.minipaint

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.minipaint.databinding.ActivityEditSideBinding

class EditSide : Activity() {

    lateinit var bi: ActivityEditSideBinding
    private var idPoint: Int = 0
    private var isLast: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_edit_side)
        bi = ActivityEditSideBinding.inflate(layoutInflater)
        setContentView(bi.root)
        //показываем клаву сразу
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //получаю id точки
        idPoint = intent.getIntExtra("idPoint", 0)
        isLast = intent.getBooleanExtra("isLast", false)
        bi.needLenght.setOnKeyListener { v, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                sendIntent(v)
                return@setOnKeyListener true
            }
            false
        }
        bi.button.setOnClickListener(sendIntent)
    }

    //лямбда для завершения активонотси
    private val sendIntent: (v: View) -> Unit = {
        val lengthInt: Int
        try {
            lengthInt = bi.needLenght.text.toString().toInt()
            val intent = Intent()
            intent.putExtra("length", lengthInt)
            intent.putExtra("idPoint", idPoint)
            intent.putExtra("isLast", isLast)

            setResult(RESULT_OK, intent)
            finish()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, R.string.notNumber, Toast.LENGTH_SHORT).show()
        }
    }
}