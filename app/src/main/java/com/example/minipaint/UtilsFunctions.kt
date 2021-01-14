package com.example.minipaint

import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class UtilsFunctions {

}

fun calcStartPoint(it: MyPoint):FloatArray{
    val startXY = FloatArray(2)
    startXY[0] =(2 * it.middleX - it.x).toFloat()
    startXY[1] =(2 * it.middleY - it.y).toFloat()
    return startXY
}

//todo Need to change , для редактирования scaled
 fun calcNextPoint(listPoints: MutableList<MyPoint>, tan: FloatArray, dest: Float): MutableList<MyPoint> {
    listPoints.last().mCos = roundOffDecimal(tan[0], "#").toFloat()
    listPoints.last().mSin = roundOffDecimal(tan[1], "#").toFloat()

    listPoints.last().x = (listPoints[listPoints.size - 2].x +  dest*listPoints.last().mCos).toInt()
    listPoints.last().y = (listPoints[listPoints.size - 2].y +  dest*listPoints.last().mSin).toInt()
    listPoints.last().distance = dest

    listPoints.last().middleX = (listPoints[listPoints.size - 2].x +  (dest/2)*listPoints.last().mCos).toInt()
    listPoints.last().middleY = (listPoints[listPoints.size - 2].y +  (dest/2)*listPoints.last().mSin).toInt()
    return listPoints
}

 fun roundOffDecimal(number: Float, s: String): String {
    val df = DecimalFormat(s)
    df.roundingMode = RoundingMode.HALF_EVEN
    return df.format(number)
}

 fun calcDistance(x1: Int, y1: Int, x2: Int, y2: Int): Float {
    val corx: Double = (x2-x1).toDouble()
    val cory: Double = (y2-y1).toDouble()
    return (sqrt(corx.pow(2) + cory.pow(2))).toFloat()
}

//Метод остался от рисования пальцем на канвасе
//private fun touchMove() {
//    val dx = abs(motionTouchEventX - currentX)
//    val dy = abs(motionTouchEventY - currentY)
//    //Log.d("dff",touchTolerance.toString())
////        if(dx >= touchTolerance|| dy >= touchTolerance){
//    if(dx >= 200 || dy >= 200){
////            path.quadTo(currentX,currentY,(motionTouchEventX + currentX)/2
////            ,(motionTouchEventY +currentY)/2)
//        path.lineTo(
//                (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2
//        )
//        currentX = motionTouchEventX
//        currentY = motionTouchEventY
//        extraCanvas.drawPath(path, paint)
//    }
//    invalidate()
//}


//    function polygonArea(X, Y, numPoints)
//    {
//        area = 0;   // Accumulates area
//        j = numPoints-1;
//
//        for (i=0; i<numPoints; i++)
//        { area +=  (X[j]+X[i]) * (Y[j]-Y[i]);
//            j = i;  //j is previous vertex to i
//        }
//        return area/2;
//    }

//    var xPts = [4,  4,  8,  8, -4,-4];
//    var yPts = [6, -4, -4, -8, -8, 6];
//    var a = polygonArea(xPts, yPts, 6);
//    alert("Area  = " + a);