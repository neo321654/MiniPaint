package com.example.minipaint

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import java.lang.Math.acos
import java.lang.Math.asin
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


private const val STROKE_WIDTH = 12f


class MyCanvasView(context: Context): View(context) {
    private val circleRadius = 30f
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private var path = Path()
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    private var currentX = 0f
    private var currentY = 0f
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private val paint = Paint().apply{
        color = drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when(event.action){
            MotionEvent.ACTION_DOWN -> touchDown()
        //    MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private var listPoints :MutableList<MyPoint> = mutableListOf()
    private var isFirstTouch =true

    private fun touchDown() {
        if(listPoints.isEmpty()){
            Log.d("log","in isEmpty")
            listPoints.add(MyPoint(motionTouchEventX.toInt(),motionTouchEventY.toInt()))
        }
        path.moveTo(listPoints.get(0).x.toFloat(), listPoints.get(0).y.toFloat())

    }
    private fun calcDistance(x1: Int,y1: Int,x2:Int,y2:Int): Float {
        var corx: Double = (x2-x1).toDouble()
        var cory: Double = (y2-y1).toDouble()
        return (Math.sqrt(corx.pow(2) + cory.pow(2))).toFloat()

    }
    private fun touchUp() {

        if(!isFirstTouch) {


                listPoints.add(MyPoint(motionTouchEventX.toInt(),motionTouchEventY.toInt()))




        }

        extraCanvas.drawColor(backgroundColor)

        listPoints.forEach{
            path.lineTo(
                    it.x.toFloat(), it.y.toFloat()
            )
        }



        val rnd =  Random()
        val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        paint.color = color

        val pMeasure = PathMeasure(path,false)


        var pMeasureLenght = pMeasure.length
        var pos = FloatArray(2)
        var tan = FloatArray(2)
        pMeasure.getPosTan(pMeasureLenght, pos, tan)

        var lastDistOrZero = 0f

          listPoints.last().distance = lastDistOrZero

        if(!isFirstTouch) {
            val dest = calcDistance(listPoints.last().x,listPoints.last().y ,
                    (listPoints.get(listPoints.size-2)).x,(listPoints.get(listPoints.size-2).y))

            Log.d("log"," [${pos.get(0)} ; ${pos.get(1)}] cos ${tan.get(0)} sin ${tan.get(1)}")
            Log.d("log", "____________$dest")
            Log.d("log", "lastx(-2) = ${listPoints.get(listPoints.size -2).x} lasty(-2) =  ${listPoints.get(listPoints.size -2).y} ")

            Log.d("log", "lastx = ${listPoints.last().x} lasty = ${listPoints.last().y} ")

            var XXX = listPoints.get(listPoints.size-2).x +  dest*roundOffDecimal(tan.get(0))
            var YYY = listPoints.get(listPoints.size-2).y +  dest*roundOffDecimal(tan.get(1))

            Log.d("log", "XXX = ${XXX} YYY = ${YYY} ")
            listPoints.last().x = XXX.toInt()
            listPoints.last().y = YYY.toInt()

            var h = calcDistance(listPoints.get(0).x,listPoints.get(0).y, motionTouchEventX.toInt(), motionTouchEventY.toInt())
            Log.d("log" ,"dist = $h")
            if(circleRadius >= h){
                Log.d("log","in11111111111111111111111")
                listPoints.last().x =listPoints.get(0).x
                listPoints.last().y = listPoints.get(0).y
                XXX= listPoints.get(0).x.toFloat()
                YYY = listPoints.get(0).y.toFloat()
            }


            path.setLastPoint(XXX,YYY)
            }

        listPoints.forEach{
            extraCanvas.drawCircle( it.x.toFloat(), it.y.toFloat(),circleRadius,paint)
        }

        extraCanvas.drawPath(path, paint)


        path.reset()
        invalidate()

        isFirstTouch = false

        var str = StringBuilder()
            listPoints.forEach{
                str!!.append("[${it.x} , ${it.y} , dist ${it.distance}]")
            }
    //    Log.d("Log", str.toString())



    }
    fun roundOffDecimal(number: Float): Float {
        val df = DecimalFormat("#")
        df.roundingMode = RoundingMode.HALF_EVEN
        return df.format(number).toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)

    }

    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)

        //Log.d("dff",touchTolerance.toString())



//        if(dx >= touchTolerance|| dy >= touchTolerance){
        if(dx >= 200 || dy >= 200){
//            path.quadTo(currentX,currentY,(motionTouchEventX + currentX)/2
//            ,(motionTouchEventY +currentY)/2)

            path.lineTo(
                (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2
            )

            currentX = motionTouchEventX
            currentY = motionTouchEventY

            extraCanvas.drawPath(path, paint)
        }

        invalidate()
    }
}

class MyPoint(var x: Int, var y: Int, var distance:Float = 0f)