package com.example.minipaint


import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.pow


private const val STROKE_WIDTH = 12f


class MyCanvasView(context: Context, private val supportFragmentManager: FragmentManager): View(context),DialogLenght.DialogLenghtListener{
    private val circleRadius = 30f
    private var isFigureDone = false
    private var counterPointId = 0

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
    private var listPoints :MutableList<MyPoint> = mutableListOf()
    private var isFirstTouch =true

    private val paint = Paint().apply{
        color = drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    //поля для увеличения
//    private val mCurrentViewport = RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX)
//    private val mContentRect: Rect? = null


    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y
//        when(event.action){
//            MotionEvent.ACTION_DOWN -> touchDown()
//        //здесь потом добавим обработку перетягивания точки
//        //    MotionEvent.ACTION_MOVE -> touchMove()
//            MotionEvent.ACTION_UP -> touchUp()
//        }
        //заменил обработчик событий на детектор жестов
         detector.onTouchEvent(event).let {
             when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        touchDown()
                    }
                    MotionEvent.ACTION_UP -> {
                        touchUp()
                        if(isFigureDone)scaleCanvasToEdge()
                        //сброс пути и перерисовка
                        path.reset()
                        invalidate()
                    }
                    else -> true
                }
        }
        return true
    }

    //работаем обработчиком жестов
    private val myListener = object : GestureDetector.SimpleOnGestureListener(){
        override fun onLongPress(e: MotionEvent?) {
            super.onLongPress(e)

            Toast.makeText(context,"h ${extraCanvas.height}; w${extraCanvas.width};",Toast.LENGTH_LONG).show()
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    }

    private fun scaleCanvasToEdge() {
        //  extraCanvas.scale(0.9F, 0.9F)
        paint.setColor(Color.CYAN)
        var pathRect = Path()
        var pathDest = Path()
        var rectfBounds = RectF()
        path.computeBounds(rectfBounds, true);
        var rectfDest = RectF()
        var matrix = Matrix()

        //10% on ширины девайса
        var bounds = (extraCanvas.width*0.1).toFloat()
        rectfDest.set(bounds, bounds, extraCanvas.width-bounds, extraCanvas.height-bounds)

        matrix.reset()
        matrix.setRectToRect(rectfBounds, rectfDest, Matrix.ScaleToFit.CENTER);
        path.transform(matrix, pathDest);
//        extraCanvas.drawRect(rectfDest, paint)
//         extraCanvas.drawRect(rectfBounds, paint)
        paint.setColor(Color.GREEN)

        val arrF = FloatArray(listPoints.size*2)
       var iter = 0
        listPoints.forEach{
            arrF[iter]=it.x.toFloat()
            iter++
            arrF[iter]=it.y.toFloat()
            iter++
        }


        Log.d("log",arrF.joinToString("          ;"))
        matrix.mapPoints(arrF)

        Log.d("log",arrF.joinToString("          ;"))



        extraCanvas.drawPath(pathDest, paint)

        iter = 0
        listPoints.forEach{

            it.x = arrF[iter].toInt()
            iter++
            it.y= arrF[iter].toInt()
            iter++
        }


    }

    private val detector: GestureDetector = GestureDetector(context,myListener)

    private fun touchDown() {
        if(listPoints.isEmpty()){
         //   Добавляем первую точку если начало чертежа
            listPoints.add(MyPoint(motionTouchEventX.toInt(), motionTouchEventY.toInt()))
        }

        //    Опускаем кисть на начальную точку чертежа
        path.moveTo(listPoints[0].x.toFloat(), listPoints[0].y.toFloat())
    }

    private fun editSide(motionTouchEventX: Float, motionTouchEventY:
    Float, listPointsEdited: MutableList<MyPoint>){
         //настраиваю кисть для эдита и путь
        val paintEdit = Paint().apply{
            color = Color.RED
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_WIDTH
        }
        val pathEdit = Path()
        // для нажатия выберем середину отрезка с таким радиусом
        val circleRadiusEdit = 50
        for (i in 0 until listPointsEdited.size) {
            val h = calcDistance(listPointsEdited[i].middleX, listPointsEdited[i].middleY, motionTouchEventX.toInt(), motionTouchEventY.toInt())
        //условие при котором мы сравниваем радиус точки касания с нашей серединой отрезка
            if(circleRadiusEdit >= h){
               //здесь мы каснёмся последнего отрезка и Тостуем
                if(listPointsEdited[i].idPoint == listPointsEdited.size-1){
                    Toast.makeText(context,R.string.lastDistance,Toast.LENGTH_LONG).show()
                    break
                }
                val xy = calcStartPoint(listPointsEdited[i])
                pathEdit.moveTo(xy[0], xy[1])
                pathEdit.lineTo(listPointsEdited[i].x.toFloat(), listPointsEdited[i].y.toFloat())
                extraCanvas.drawPath(pathEdit, paintEdit)
                //показываю диалог для ввода ширины
                val dialofLenght = DialogLenght(this, listPointsEdited[i])
                dialofLenght.show(supportFragmentManager, "missiles")
                break
            }
        }
    }

    private fun touchUp() {
        //если не начало и не конец чертежа добавляем точку в список
        if(!isFirstTouch && !isFigureDone) {
                counterPointId++
                listPoints.add(MyPoint(motionTouchEventX.toInt(), motionTouchEventY.toInt(), counterPointId))
        }
        //Заливаем канву цветом
        extraCanvas.drawColor(backgroundColor)
        //Рисуем весь путь с изветными точками
        listPoints.forEach{
            path.lineTo(
                    it.x.toFloat(), it.y.toFloat()
            )
        }
        //Здесь меняем цвет пути рандомно , чтобы понимать отклонения от пути и как работает перерисовка
//        val rnd =  Random()
//        val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//        paint.color = color
        //Снимаем показатели уже построенного пути и узнаём показатели последней точки для анализа дальнейшей

        val pMeasure = PathMeasure(path, false)
        val pMeasureLenght = pMeasure.length
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        pMeasure.getPosTan(pMeasureLenght, pos, tan)
        // Если не начало и не конец чертежа
        if(!isFirstTouch && !isFigureDone ) {
            //Забираем длину последнего отрезка
            val dest = calcDistance(listPoints.last().x, listPoints.last().y,
                    (listPoints.get(listPoints.size - 2)).x, (listPoints.get(listPoints.size - 2).y))
            //Рассчитываем последнюю точку с учетом длины последнего отрезка и округлённого поворота последней точки
            //для того чтобы угол был либо 90 либо 45
            listPoints = calcLastPoint(listPoints, tan, dest)
            //Меняем последнюю точку в листе , но не рисуем её покачто
            //Находим расстояние от последней рассчитаной точки до самой первой точки
            //и если оно меньше чем радиус нашим кружков, значит мы попали в кружок
            //и замыкаем чертёж автоматически и фигура заканчивается
            val h = calcDistance(listPoints[0].x, listPoints[0].y, motionTouchEventX.toInt(), motionTouchEventY.toInt())
                        if(circleRadius >= h){
                            listPoints.last().x = listPoints[0].x
                            listPoints.last().y = listPoints[0].y
                            listPoints.last().middleX = (listPoints.get(listPoints.size - 2).x + listPoints[0].x)/2
                            listPoints.last().middleY = (listPoints.get(listPoints.size - 2).y + listPoints[0].y)/2
                            //Пересчитываю последнюю длину с учетом изменений
                            listPoints.last().distance = calcDistance(listPoints.get(listPoints.size - 2).x, listPoints.get(listPoints.size - 2).y, listPoints.last().x, listPoints.last().y)
                            isFigureDone = true
                     }
            //меняем последнюю точку пути в path на нашу расчитуннню
            path.setLastPoint(listPoints.last().x.toFloat(), listPoints.last().y.toFloat())
            }
        //наносим на канву наш изменённый путь
        extraCanvas.drawPath(path, paint)
        //наносим кружки на наш путь
        listPoints.forEach{
            extraCanvas.drawCircle(it.x.toFloat(), it.y.toFloat(), circleRadius, paint)
        }

        if(isFigureDone){
            drawSquarePerimetr(listPoints)
            editSide(motionTouchEventX, motionTouchEventY, listPoints)

        }
        drawNumberLenght()


        //Это не первая точка черчежа
        isFirstTouch = false

//        var str = StringBuilder()
//            listPoints.forEach{
//                str!!.append("[${it.x} , ${it.y} , dist ${it.distance}]")
//            }
//        Log.d("Log", str.toString())

    }

    private fun drawNumberLenght() {
        var p = Paint();

        p.setStrokeWidth(4F);
        p.setStyle(Paint.Style.FILL);
        p.textSize = 40f
        p.color = Color.BLACK

        var path2 = Path()
        var widthText = 0f;
        var distStr = ""

        listPoints.forEach {
            //проверяю что это не первая точка
            if (it.middleX != 0 && it.middleY != 0) {
                distStr = (it.distance.toInt()).toString()
                val xy =calcStartPoint(it)
                path2.moveTo(xy[0], xy[1])
                path2.lineTo(it.x.toFloat(), it.y.toFloat())
                widthText = p.measureText(distStr);
                extraCanvas.drawTextOnPath(distStr, path2, (it.distance.toInt() - widthText) / 2, -10F, p)
                path2.reset()
            }
        }
    }

    private fun calcStartPoint(it: MyPoint):FloatArray{
       var startXY = FloatArray(2)
        startXY[0] =(2 * it.middleX - it.x).toFloat()
        startXY[1] =(2 * it.middleY - it.y).toFloat()
        return startXY
    }

    private fun calcLastPoint(listPoints: MutableList<MyPoint>, tan: FloatArray, dest: Float): MutableList<MyPoint> {
        listPoints.last().mCos = roundOffDecimal(tan[0], "#").toFloat()

        listPoints.last().mSin = roundOffDecimal(tan[1], "#").toFloat()

        listPoints.last().x = (listPoints[listPoints.size - 2].x +  dest*listPoints.last().mCos).toInt()
        listPoints.last().y = (listPoints[listPoints.size - 2].y +  dest*listPoints.last().mSin).toInt()
        listPoints.last().distance = dest

        listPoints.last().middleX = (listPoints[listPoints.size - 2].x +  (dest/2)*listPoints.last().mCos).toInt()
        listPoints.last().middleY = (listPoints[listPoints.size - 2].y +  (dest/2)*listPoints.last().mSin).toInt()

        return listPoints
    }

    private fun roundOffDecimal(number: Float, s: String): String {
        val df = DecimalFormat(s)
        df.roundingMode = RoundingMode.HALF_EVEN

//        val f: NumberFormat = NumberFormat.getInstance()
//        if (f is DecimalFormat) {
//            f.isDecimalSeparatorAlwaysShown = true
//            f.roundingMode = RoundingMode.HALF_EVEN
//
//        }


        return df.format(number)

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
    private fun calcDistance(x1: Int, y1: Int, x2: Int, y2: Int): Float {
        var corx: Double = (x2-x1).toDouble()
        var cory: Double = (y2-y1).toDouble()
        return (Math.sqrt(corx.pow(2) + cory.pow(2))).toFloat()

    }


    override fun onDialogPositiveClick(lenght: String, idPoint: Int) {
        listPoints = recalculatePoints(lenght, idPoint)
        //метод отрисовки площади и переметра
        drawSquarePerimetr(listPoints)
    }


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

    private fun drawSquarePerimetr(listPoints: MutableList<MyPoint>) {
        val arrX = mutableListOf<Int>()
        val arrY = mutableListOf<Int>()
        var numPoints = listPoints.size-1
        var square = 0f
        var perimeter = 0f

        listPoints.forEach{
            arrY.add(it.x)
            arrX.add(it.y)
           perimeter+=it.distance
        }
//        Log.d("log",arrX.toString())
//        Log.d("log",arrY.toString())
//        arrX = mutableListOf(4,  4,  8,  8, -4,-4)
//        arrY = mutableListOf(6, -4, -4, -8, -8, 6)
//        numPoints = 6-1
//        arrY = mutableListOf(197, 197, 508, 508, 174, 174)
//        arrX = mutableListOf(1001, 534, 534, 915, 915, 971)
//        numPoints = 6-1


        for(i in 0 .. numPoints){
            square+=(arrX[numPoints]+arrX[i])*(arrY[numPoints]-arrY[i])
            numPoints = i
        }
        square /= 2
       val sq1 = roundOffDecimal((square/10000), "#.#")
        val perRounded =roundOffDecimal((perimeter/100),"#.#")
       val sq2 = square
        val p = Paint();

        p.strokeWidth = 4F;
        p.style = Paint.Style.FILL;
        p.textSize = 40f
        p.color = Color.BLACK
        extraCanvas.drawText("S = $sq1 m\u00b2 ; P = $perRounded m", 50f, 50f, p)
        extraCanvas.drawText("S= $sq2 pix\u00b2; P= $perimeter pix", 50f, 100f, p)

    }

    private fun recalculatePoints(lenght: String, idPoint: Int): MutableList<MyPoint> {
        val newListPoint = mutableListOf<MyPoint>()
        var previousPoint:MyPoint = listPoints[0]
        for(i in 0 until listPoints.size) {

            if(listPoints[i].idPoint == idPoint){
                var startXY = calcStartPoint(listPoints[i])
//                listPoints = calcLastPoint(listPoints, tan, dest)
         //       listPoints.last().x = (listPoints[listPoints.size - 2].x +  dest*listPoints.last().mCos).toInt()
              //  Toast.makeText(context,listPoints[i].distance.toString(),Toast.LENGTH_LONG).show()
                Log.d("log", "${listPoints[i].x} ; ${listPoints[i].y}")
                previousPoint = listPoints[i]
                newListPoint.add(listPoints[i])
                newListPoint.last().x = (listPoints[i - 1].x + lenght.toInt()*(listPoints[i].mCos)).toInt()
                newListPoint.last().y = (listPoints[i - 1].y + lenght.toInt()*(listPoints[i].mSin)).toInt()
                newListPoint.last().distance = lenght.toFloat()
                newListPoint.last().middleX = (newListPoint.last().x + (listPoints[i - 1].x))/2
                newListPoint.last().middleY = (newListPoint.last().y + (listPoints[i - 1].y))/2

                Log.d("log", "${newListPoint.last().x} ; ${newListPoint.last().y}")
            }else{

                newListPoint.add(listPoints[i])
                newListPoint.last().middleX = (newListPoint.last().x + (previousPoint.x))/2
                newListPoint.last().middleY = (newListPoint.last().y + (previousPoint.y))/2
                newListPoint.last().distance = calcDistance(newListPoint.last().x, newListPoint.last().y, previousPoint.x, previousPoint.y)

                previousPoint = listPoints[i]

            }


        }

            return newListPoint
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        //надо найти центр фигуры, и вычислить увеличение про прямоугольнику вписанной фигуры
        extraCanvas.scale(0.3f,0.3f, 400f,400f)
    }


}

class MyPoint(var x: Int, var y: Int, var idPoint: Int = 0, var distance: Float = 0f,
              var mCos: Float = 0f, var mSin: Float = 0f, var middleX: Int = 0, var middleY: Int = 0)