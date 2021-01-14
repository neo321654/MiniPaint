package com.example.minipaint

import android.util.Log
import android.widget.Toast
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

//private fun recalculatePoints(length: String, idPoint: Int): MutableList<MyPoint> {
//    var lengthInt = 0
//    //todo эту обработку надо сделать в самом диалоге, чтобы не бесить юзера
//    try {
//        lengthInt = length.toInt()
//    } catch (e: NumberFormatException) {
//        Toast.makeText(context,R.string.notNumber, Toast.LENGTH_SHORT).show()
//        motionTouchEventX = 0f
//        motionTouchEventY = 0f
//        return listPoints
//    }
//
//    val coefici = scaledListPoints.last().realDistance/listPoints.last().distance
//    var realDistanceScaled = lengthInt*coefici
//
//    Log.d("log", "$coefici   realDistanceScaled = $realDistanceScaled")
//    //todo после ввода значений эдит текста при увеличении не правильно прорисовывается , либо лишняя отрисовка либо отсутствие заливки
//
//    val newListPoint = mutableListOf<MyPoint>()
//    var previousPoint:MyPoint = scaledListPoints[0]
//    for(i in 0 until scaledListPoints.size) {
//        //здесь вылетела ошибка вышел за пределы масива
//        if(scaledListPoints[i].idPoint == idPoint){
//            var startXY = calcStartPoint(scaledListPoints[i])
//// сделать редактирование последнего только смещение будет идти в обратную сторону
//            if(scaledListPoints.last().idPoint == idPoint){
//                //todo сделать обработку последнего отрезка с помощью кругов, радиусов, или забить на это и
//
//
//                previousPoint = scaledListPoints[i]
//                newListPoint.add(scaledListPoints[i])
////                    newListPoint.last().x = (listPoints[i - 1].x + lengthInt*(listPoints[i].mCos)).toInt()
////                    newListPoint.last().y = (listPoints[i - 1].y + lengthInt*(listPoints[i].mSin)).toInt()
//                newListPoint.last().x = scaledListPoints[i].x
//                newListPoint.last().y = scaledListPoints[i].y
//
//                newListPoint.last().distance = lengthInt.toFloat()
//                newListPoint.last().middleX = (newListPoint.last().x + (scaledListPoints[i - 1].x))/2
//                newListPoint.last().middleY = (newListPoint.last().y + (scaledListPoints[i - 1].y))/2
//            }
//            else{
//                //здесь преобразуем длину отрезка , но не последнего
//                previousPoint = scaledListPoints[i]
//                newListPoint.add(scaledListPoints[i])
//                //надо сравнить дистанцию увеличенного и начального отрезка , выявить коэфицент,
//                //после найти длину для вставки
//
//
//                newListPoint.last().x = (scaledListPoints[i - 1].x + realDistanceScaled*(scaledListPoints[i].mCos)).toInt()
//                newListPoint.last().y = (scaledListPoints[i - 1].y + realDistanceScaled*(scaledListPoints[i].mSin)).toInt()
//                //дистанцию у увеличеного листа pacчитываю реально, хотя это не требуется она отображается как надо
//
//                newListPoint.last().distance = lengthInt.toFloat()
//                newListPoint.last().realDistance = realDistanceScaled
//                //преобразую длину и следующую точку в начальном не редактируемом листе
//                listPoints[i].x = (listPoints[i - 1].x + lengthInt*(listPoints[i].mCos)).toInt()
//                listPoints[i].y = (listPoints[i - 1].y + lengthInt*(listPoints[i].mSin)).toInt()
//                listPoints[i].middleX = (listPoints[i].x + (listPoints[i - 1].x))/2
//                listPoints[i].middleY = (listPoints[i].y + (listPoints[i - 1].y))/2
//                listPoints[i].distance = lengthInt.toFloat()
//
//
//                newListPoint.last().middleX = (newListPoint.last().x + (scaledListPoints[i - 1].x))/2
//                newListPoint.last().middleY = (newListPoint.last().y + (scaledListPoints[i - 1].y))/2
//            }
//
//        }else{
//            //здесь допалняем лист если не совпали айдишники
//            newListPoint.add(scaledListPoints[i])
//            newListPoint.last().middleX = (newListPoint.last().x + (previousPoint.x))/2
//            newListPoint.last().middleY = (newListPoint.last().y + (previousPoint.y))/2
//            // newListPoint.last().distance = calcDistance(newListPoint.last().x, newListPoint.last().y, previousPoint.x, previousPoint.y)
//
//            previousPoint = scaledListPoints[i]
//        }
//    }
//    return newListPoint
//}

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