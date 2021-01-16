package com.example.minipaint

import java.math.RoundingMode
import java.text.DecimalFormat
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
    listPoints.last().mCos = roundOffDecimal(tan[0], "#").toFloat()
    listPoints.last().mSin = roundOffDecimal(tan[1], "#").toFloat()

    listPoints.last().x = (listPoints[listPoints.size - 2].x + dest * listPoints.last().mCos).toInt()
    listPoints.last().y = (listPoints[listPoints.size - 2].y + dest * listPoints.last().mSin).toInt()
    listPoints.last().distance = dest
    listPoints.last().realDistance = dest

    listPoints.last().middleX = (listPoints[listPoints.size - 2].x + (dest / 2) * listPoints.last().mCos).toInt()
    listPoints.last().middleY = (listPoints[listPoints.size - 2].y + (dest / 2) * listPoints.last().mSin).toInt()
    return listPoints
}

//округляю необоходимое число
fun roundOffDecimal(number: Float, s: String): String {
    val df = DecimalFormat(s)
    df.roundingMode = RoundingMode.HALF_EVEN
    return df.format(number)
}

//расчёт растояния от одной точки до другой
fun calcDistance(x1: Int, y1: Int, x2: Int, y2: Int): Float {
    val corx: Double = (x2 - x1).toDouble()
    val cory: Double = (y2 - y1).toDouble()
    return (sqrt(corx.pow(2) + cory.pow(2))).toFloat()
}
