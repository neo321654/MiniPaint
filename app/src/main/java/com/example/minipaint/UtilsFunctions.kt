package com.example.minipaint

import android.util.Log
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

//ищу начальную точку для отрисовки отрезка крвсным и тд
fun calcStartPoint(it: MyPoint): FloatArray {
    val startXY = FloatArray(2)
    startXY[0] = (2 * it.middleX - it.x).toFloat()
    startXY[1] = (2 * it.middleY - it.y).toFloat()
    return startXY
}

//ищет следующую точку при начальной отрисовке
fun calcNextPoint(listPoints: MutableList<MyPoint>, tan: FloatArray, dest: Float): MutableList<MyPoint> {
//    listPoints.last().mCos = round(tan[0], 0)
//    listPoints.last().mSin = round(tan[1], 0)
//    listPoints.last().mCos = tan[0].toFloat()
//    listPoints.last().mSin = tan[1].toFloat()
    var sinEdited = tan[1]
    var cosEdited = tan[0]

    if(0.515< sinEdited && sinEdited<0.8572){
        sinEdited = 0.707106781f
    }else if(-0.515> sinEdited && sinEdited>-0.8572){
        sinEdited=-0.707106781f
    }
    else{
        sinEdited = roundOffDecimal(sinEdited,"#").toFloat()
    }

    if(0.515< cosEdited && cosEdited<0.8572){
        cosEdited = 0.707106781f
    }else if(-0.515> cosEdited && cosEdited>-0.8572){
        cosEdited=-0.707106781f
    }
    else{
        cosEdited = roundOffDecimal(cosEdited,"#").toFloat()
    }

    Log.d("log", "sin = ${sinEdited}")
    Log.d("log", "cos = ${cosEdited}")

    listPoints.last().x = (listPoints[listPoints.size - 2].x + dest * cosEdited).toInt()

    listPoints.last().y = (listPoints[listPoints.size - 2].y + dest * sinEdited).toInt()
    listPoints.last().distance = dest
    listPoints.last().realDistance = dest

    listPoints.last().middleX = (listPoints[listPoints.size - 2].x + listPoints.last().x)/2
    listPoints.last().middleY = (listPoints[listPoints.size - 2].y + listPoints.last().y)/2
    return listPoints
}

//округляю необоходимое число
fun roundOffDecimal(number: Float, s: String): String {
    val df = DecimalFormat(s)
    df.roundingMode = RoundingMode.HALF_EVEN
    return df.format(number)
}
fun round(number: Float, scale: Int): Float {
    var pow = 10f
    for (i in 1 until scale) pow *= 10
    val tmp = number * pow
    return ((if (tmp - tmp.toInt() >= 0.5f) tmp + 1 else tmp).toInt()).toFloat() / pow
}



//расчёт растояния от одной точки до другой
fun calcDistance(x1: Int, y1: Int, x2: Int, y2: Int): Float {
    val corx: Double = (x2 - x1).toDouble()
    val cory: Double = (y2 - y1).toDouble()
    return (sqrt(corx.pow(2) + cory.pow(2))).toFloat()
}
