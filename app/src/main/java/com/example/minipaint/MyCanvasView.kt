package com.example.minipaint

import android.annotation.SuppressLint
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

private const val STROKE_WIDTH = 12f

class MyCanvasView(context: Context, private val supportFragmentManager: FragmentManager) : View(context), DialogLenght.DialogLenghtListener {


    private var scaledListPoints: MutableList<MyPoint> = mutableListOf()
    private val matrixIsFigureDone = Matrix()
    private val circleRadius = 30f
    private val textSize = 40f
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

    //Это поле было для рисования пальцем
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop
    private var listPoints: MutableList<MyPoint> = mutableListOf()
    private var isFirstTouch = true

    //Настройка кисти для отображения черчежа
    private val paint = Paint().apply {
        color = drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    //работаем обработчиком жестов
    //синглтон для жестов лисенер
    private val listenerForDetectorGesture = object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent?) {
            super.onLongPress(e)
         //   scaleCanvas(true,false)

          //  scaleCanvasTest()
//            touchDown()
//
//            touchUp()
               Toast.makeText(context, "h ${extraCanvas.height}; w${extraCanvas.width};", Toast.LENGTH_LONG).show()
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            super.onDoubleTap(e)
            scaleCanvas(false,false)
            return true
        }

        //Этот метод необходимо переопределить а то не сработают другие жесты
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    }

    private fun scaleCanvasTest() {

        //  extraCanvas.scale(0.9F, 0.9F)
       // paint.color = Color.CYAN
        //    var pathRect = Path()
        val pathDest = Path()
        scaledListPoints.forEach{
           if(it.idPoint == 0){
               pathDest.moveTo(it.x.toFloat(),it.y.toFloat())
           }else{
               pathDest.lineTo(it.x.toFloat(),it.y.toFloat())
           }

        }
        val rectfBounds = RectF()
        //   val rectfDest = RectF()
        val matrix = Matrix()
        matrix.reset()
       // if(isToEdge){
            //10% on ширины девайса
            val bounds = (extraCanvas.width*0.1).toFloat()
            //прямоугольник-рамка для вписания

            val rectfDest = RectF()
            rectfDest.set(bounds, bounds*2, extraCanvas.width-bounds, extraCanvas.height-bounds)
            //вычисление границ чертежа и присвоение этих границ прямоугольнику
        pathDest.computeBounds(rectfBounds, true);
            //матрица выполняющая вписание одного прямоугольника в другой
            matrix.setRectToRect(rectfBounds,rectfDest, Matrix.ScaleToFit.CENTER);
            // попробую найти матрицу по краям чечежа и увеличить её
     //   }else{
           // matrix.setRectToRect(rectfBounds, rectfBounds, Matrix.ScaleToFit.CENTER);
     //   }
     //   path.transform(matrix)
    //    extraCanvas.drawPath(path,paint)
//        extraCanvas.drawRect(rectfDest,paint)
//        extraCanvas.drawRect(rectfBounds,paint)

//        if (isScale) {
//            matrix.setScale(1.1F, 1.1F, scaledListPoints[1].x.toFloat(), scaledListPoints[1].y.toFloat())
//            path.transform(matrix, pathDest);
//        } else {
//            matrix.setScale(0.91F, 0.91F, scaledListPoints[1].x.toFloat(), scaledListPoints[1].y.toFloat())
//            path.transform(matrix, pathDest);
//        }
    //    path.transform(matrix, pathDest);
        //  extraCanvas.drawPath(path,paint)

     //   paint.color = Color.GREEN
        //здесь находим новые точки с помощью матрицы matrix.mapPoints()
        val arrF = FloatArray(scaledListPoints.size * 2)
        var iter = 0
        scaledListPoints.forEach {
            arrF[iter] = it.x.toFloat()
            iter++
            arrF[iter] = it.y.toFloat()
            iter++
        }
        //    Log.d("log",arrF.joinToString("          ;"))
        matrix.mapPoints(arrF)
      //  Log.d("log",arrF.joinToString("          ;"))
        //   extraCanvas.drawPath(pathDest, paint)
        iter = 0
        //меняю ху на преобразованые из матрицы
        scaledListPoints.forEach {
            it.x = arrF[iter].toInt()
            iter++
            it.y = arrF[iter].toInt()
            iter++
        }

        //     listPoints.last().middleX = (listPoints[listPoints.size - 2].x +  (dest/2)*listPoints.last().mCos).toInt()
        var lastP = MyPoint(0, 0)
        //меняем средниные точки для изменённых
        scaledListPoints.forEach {
            if (it.idPoint != 0) {
                it.middleX = (lastP.x + it.x) / 2
                it.middleY = (lastP.y + it.y) / 2
                //  it.distance = calcDistance(lastP.x,lastP.y,it.x,it.y)
            }
            lastP = it
        }

//        touchDown()
//        touchUp()
    }

    //   private val detector: GestureDetector = GestureDetector(context,myListener)
    private val detectorGesture: GestureDetector = GestureDetector(context, listenerForDetectorGesture)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        motionTouchEventX = event.x
        motionTouchEventY = event.y
        //заменил обработчик событий на детектор жестов
        detectorGesture.onTouchEvent(event).let {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchDown()
                }
                MotionEvent.ACTION_UP -> {
                    touchUp()
                    //условие для выполнения увеличения
                    //if(isFigureDone)scaleCanvasToEdge()
                    //сброс пути и перерисовка
                    path.reset()
                    invalidate()
                }
                else -> true
            }
        }
        return true
    }

    private fun touchDown() {
        if (listPoints.isEmpty()) {
            //   Добавляем первую точку если начало чертежа
            listPoints.add(MyPoint(motionTouchEventX.toInt(), motionTouchEventY.toInt()))
        }
        //    Опускаем кисть на начальную точку чертежа
        if (isFigureDone) {
            path.reset()
            path.moveTo(scaledListPoints[0].x.toFloat(), scaledListPoints[0].y.toFloat())
        } else {
            path.reset()
            path.moveTo(listPoints[0].x.toFloat(), listPoints[0].y.toFloat())
        }
    }

    private fun touchUp() {
        //  Log.d("log","$motionTouchEventX     $motionTouchEventY   ")
        //если не начало и не конец чертежа добавляем точку в список
        if (!isFirstTouch && !isFigureDone) {
            counterPointId++
            listPoints.add(MyPoint(motionTouchEventX.toInt(), motionTouchEventY.toInt(), counterPointId))
        }
        //Заливаем канву цветом
        extraCanvas.drawColor(backgroundColor)
        //Рисуем весь путь с изветными точками
        if (isFigureDone) {
            scaledListPoints.forEach {
                path.lineTo(
                        it.x.toFloat(), it.y.toFloat()
                )
            }
        } else {
            listPoints.forEach {
                path.lineTo(
                        it.x.toFloat(), it.y.toFloat()
                )
            }
        }

        //Здесь меняем цвет пути рандомно , чтобы понимать отклонения от пути и как работает перерисовка
//        val rnd =  Random()
//        val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//        paint.color = color
        //Снимаем показатели уже построенного пути и узнаём показатели последней точки для анализа дальнейшей
        if (!isFirstTouch && !isFigureDone) {
            val pMeasure = PathMeasure(path, false)
            val pMeasureLength = pMeasure.length
            val pos = FloatArray(2)
            val tan = FloatArray(2)
            pMeasure.getPosTan(pMeasureLength, pos, tan)
            // Если не начало и не конец чертежа
            //Забираем длину последнего отрезка
            val dest = calcDistance(listPoints.last().x, listPoints.last().y,
                    (listPoints[listPoints.size - 2]).x, (listPoints[listPoints.size - 2].y))
            //Рассчитываем последнюю точку с учетом длины последнего отрезка и округлённого поворота последней точки
            //для того чтобы угол был либо 90 либо 45
            listPoints = calcNextPoint(listPoints, tan, dest)
            //Меняем последнюю точку в листе , но не рисуем её покачто
            //Находим расстояние от последней рассчитаной точки до самой первой точки
            //и если оно меньше чем радиус нашим кружков, значит мы попали в кружок
            //и замыкаем чертёж автоматически и фигура заканчивается
            val h = calcDistance(listPoints[0].x, listPoints[0].y, motionTouchEventX.toInt(), motionTouchEventY.toInt())
            if (circleRadius >= h) {
                listPoints.last().x = listPoints[0].x
                listPoints.last().y = listPoints[0].y
                listPoints.last().middleX = (listPoints.get(listPoints.size - 2).x + listPoints[0].x) / 2
                listPoints.last().middleY = (listPoints.get(listPoints.size - 2).y + listPoints[0].y) / 2
                //Пересчитываю последнюю длину с учетом изменений
                listPoints.last().distance = calcDistance(listPoints.get(listPoints.size - 2).x, listPoints.get(listPoints.size - 2).y, listPoints.last().x, listPoints.last().y)
                isFigureDone = true
                // назначаю увеличеные точки для маштабов
                listPoints.forEach {
                    //копирую дата объекты что бы не было проблем с ссылками
                    scaledListPoints.add(it.copy())
                }
            }
            //меняем последнюю точку пути в path на нашу расчитанную
            path.setLastPoint(listPoints.last().x.toFloat(), listPoints.last().y.toFloat())

        }

        //здесь проверяю на совпадение с последней точкой для удаления её
        //удаление последней точки в чертеже
        if (listPoints.size > 2 && !isFigureDone) {
            val xyToDelete = calcDistance(listPoints[listPoints.size - 2].x, listPoints[listPoints.size - 2].y,
                    motionTouchEventX.toInt(), motionTouchEventY.toInt())
            if (xyToDelete <= 50) {
                //   Toast.makeText(context,"Last point",Toast.LENGTH_SHORT).show()
                listPoints.removeAt(listPoints.size - 1)
                listPoints.removeAt(listPoints.size - 1)
                path.reset()
                path.moveTo(listPoints[0].x.toFloat(), listPoints[0].y.toFloat())
                listPoints.forEach {
                    path.lineTo(
                            it.x.toFloat(), it.y.toFloat()
                    )
                }
            }
        }
        //наносим на канву наш изменённый путь
        extraCanvas.drawPath(path, paint)

        //наносим кружки на наш путь
        if (isFigureDone) {
            scaledListPoints.forEach {
                extraCanvas.drawCircle(it.x.toFloat(), it.y.toFloat(), circleRadius, paint)
            }
        } else {
            listPoints.forEach {
                extraCanvas.drawCircle(it.x.toFloat(), it.y.toFloat(), circleRadius, paint)
            }
        }

        //рисую крейнюю точку для удаления
        if (!isFigureDone) {
            val pCircle = Paint()
            pCircle.color = Color.RED
            extraCanvas.drawCircle(listPoints.last().x.toFloat(), listPoints.last().y.toFloat(), circleRadius - 5, pCircle)
            extraCanvas.drawLine(listPoints.last().x.toFloat() - 15, listPoints.last().y.toFloat(),
                    listPoints.last().x.toFloat() + 15, listPoints.last().y.toFloat(), paint)
        }

        //выводим площадь и перметр
        if (isFigureDone) {
            //todo вот для чего нужен началный лист, чтобы правильно вычислять площадь , но его надо релактировать при изменении сторо
            drawSquarePerimetr(listPoints)

            Log.d("log", "$motionTouchEventX    $motionTouchEventY  editSide DO")
            editSide(motionTouchEventX, motionTouchEventY, scaledListPoints)

        }
        //рисуем длину отрезков
        //todo надо или не надо рисовать округлённые значения , а точки сохранять во float , для большей точности при увеличении и уменьшении.
        drawNumberLength()

    //    scaleCanvasTest()
        //Это не первая точка черчежа
        isFirstTouch = false

//        val str = StringBuilder()
//            listPoints.forEach{
//                str!!.append("[${it.x} , ${it.y} , dist ${it.distance}, mX ${it.middleX}, mY ${it.middleY}]")
//            }
//        Log.d("Log", str.toString())
//
//        val str1 = StringBuilder()
//        scaledListPoints.forEach{
//            str1!!.append("[${it.x} , ${it.y} , dist ${it.distance}, dist ${it.realDistance}, mX ${it.middleX}, mY ${it.middleY}]")
//        }
//        Log.d("Log", "*$str1")

    }

    // Увеличение чертежа до краёв
    private fun scaleCanvas(isScale: Boolean, isToEdge: Boolean) {
        //  extraCanvas.scale(0.9F, 0.9F)
        paint.color = Color.CYAN
        //    var pathRect = Path()
        val pathDest = Path()
        val rectfBounds = RectF()
        //   val rectfDest = RectF()
        val matrix = Matrix()
        matrix.reset()
        if(isToEdge){
            //10% on ширины девайса
             val bounds = (extraCanvas.width*0.1).toFloat()
            //прямоугольник-рамка для вписания

            val rectfDest = RectF()
            rectfDest.set(bounds, bounds, extraCanvas.width-bounds, extraCanvas.height-bounds)
            //вычисление границ чертежа и присвоение этих границ прямоугольнику
             path.computeBounds(rectfBounds, true);
            //матрица выполняющая вписание одного прямоугольника в другой
            matrix.setRectToRect(rectfBounds,rectfDest, Matrix.ScaleToFit.CENTER);
        // попробую найти матрицу по краям чечежа и увеличить её
        }else{
            matrix.setRectToRect(rectfBounds, rectfBounds, Matrix.ScaleToFit.CENTER);
        }

//        if (isScale) {
//            matrix.setScale(1.1F, 1.1F, scaledListPoints[1].x.toFloat(), scaledListPoints[1].y.toFloat())
//            path.transform(matrix, pathDest);
//        } else {
//            matrix.setScale(0.91F, 0.91F, scaledListPoints[1].x.toFloat(), scaledListPoints[1].y.toFloat())
//            path.transform(matrix, pathDest);
//        }
        path.transform(matrix, pathDest);
      //  extraCanvas.drawPath(path,paint)

        paint.color = Color.GREEN
        //здесь находим новые точки с помощью матрицы matrix.mapPoints()
        val arrF = FloatArray(scaledListPoints.size * 2)
        var iter = 0
        scaledListPoints.forEach {
            arrF[iter] = it.x.toFloat()
            iter++
            arrF[iter] = it.y.toFloat()
            iter++
        }
        //    Log.d("log",arrF.joinToString("          ;"))
        matrix.mapPoints(arrF)
           Log.d("log",arrF.joinToString("          ;"))
        //   extraCanvas.drawPath(pathDest, paint)
        iter = 0
        //меняю ху на преобразованые из матрицы
        scaledListPoints.forEach {
            it.x = arrF[iter].toInt()
            iter++
            it.y = arrF[iter].toInt()
            iter++
        }

        //     listPoints.last().middleX = (listPoints[listPoints.size - 2].x +  (dest/2)*listPoints.last().mCos).toInt()
        var lastP = MyPoint(0, 0)
        //меняем средниные точки для изменённых
        scaledListPoints.forEach {
            if (it.idPoint != 0) {
                it.middleX = (lastP.x + it.x) / 2
                it.middleY = (lastP.y + it.y) / 2
                //  it.distance = calcDistance(lastP.x,lastP.y,it.x,it.y)
            }
            lastP = it
        }
    }

    private fun editSide(motionTouchEventX: Float, motionTouchEventY:
    Float, listPointsEdited: MutableList<MyPoint>) {
        //настраиваю кисть для эдита и путь
        val paintEdit = Paint().apply {
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
            val h = calcDistance(listPointsEdited[i].middleX, listPointsEdited[i].middleY,
                    motionTouchEventX.toInt(), motionTouchEventY.toInt())
            //условие при котором мы сравниваем радиус точки касания с нашей серединой отрезка
            if (circleRadiusEdit >= h && h != 0f) {

                //здесь мы каснёмся последнего отрезка и Тостуем
//        if(listPointsEdited[i].idPoint == listPointsEdited.size-1){//TODO его надо редактировать чтобы точка смешалась редактируя последний угол,но не задивая последнюю длину
//        Toast.makeText(context, R.string.lastDistance, Toast.LENGTH_LONG).show()
//        break
//        }
                // рисуем выделение красным
                val xy = calcStartPoint(listPointsEdited[i])
                pathEdit.moveTo(xy[0], xy[1])
                pathEdit.lineTo(listPointsEdited[i].x.toFloat(), listPointsEdited[i].y.toFloat())
                extraCanvas.drawPath(pathEdit, paintEdit)
                //показываю диалог для ввода ширины
                //todo() заменить диалог на активити с холо темой .т.к. в не могу сразу показать клаву для эдит текста

                val dialogLength = DialogLenght(this, listPointsEdited[i])
                dialogLength.show(supportFragmentManager, "missiles")

                break
            }

        }

    }



    //отображаем длинну отрезка
    private fun drawNumberLength() {
        val p = Paint();
        p.strokeWidth = 4F;
        p.style = Paint.Style.FILL;
        p.strokeJoin = Paint.Join.ROUND
        p.strokeCap = Paint.Cap.ROUND
        p.textSize = textSize
        p.color = drawColor

        val path2 = Path()
        var widthText = 0f;
        var distStr = ""

        if (isFigureDone) {
            scaledListPoints.forEach {
                //проверяю что это не первая точка
                if (it.middleX != 0 && it.middleY != 0 && it.idPoint != 0) {
                    distStr = (it.distance.toInt()).toString()
                    val xy = calcStartPoint(it)
                    path2.moveTo(xy[0], xy[1])
                    path2.lineTo(it.x.toFloat(), it.y.toFloat())


                    val realDist = calcDistance(xy[0].toInt(), xy[1].toInt(), it.x, it.y)
                    it.realDistance = realDist
                    widthText = p.measureText(distStr);
                    //увловие для последнего отрезка, беру значение не из увеличенного листа а из первого листа
                    if(it.idPoint == scaledListPoints.size-1 ){
                        distStr = roundOffDecimal(listPoints.last().distance,"#")
                    }
                  //  extraCanvas.drawTextOnPath(distStr, path2, (realDist - widthText) / 2, -10F, p)
                    p.color= drawColor


                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        extraCanvas.drawRoundRect(it.middleX.toFloat()-widthText/2,it.middleY.toFloat()+textSize/2,
                                it.middleX.toFloat()+widthText/2,it.middleY-textSize/2,8f,8f,p)
                    }else{
                        extraCanvas.drawRect(it.middleX.toFloat()-widthText/2,it.middleY.toFloat()+textSize/2,
                                it.middleX.toFloat()+widthText/2,it.middleY-textSize/2,p)
                    }

                    p.color= Color.BLACK
                    extraCanvas.drawText(distStr,it.middleX.toFloat()-(widthText/2),it.middleY+(textSize/2-STROKE_WIDTH/2),p)

                    path2.reset()
                }
            }
        } else {
            listPoints.forEach {
                //проверяю что это не первая точка
                if (it.middleX != 0 && it.middleY != 0 && it.idPoint != 0) {
                    distStr = (it.distance.toInt()).toString()
                    val xy = calcStartPoint(it)
                    path2.moveTo(xy[0], xy[1])
                    path2.lineTo(it.x.toFloat(), it.y.toFloat())
                    widthText = p.measureText(distStr);
                    p.color= drawColor

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        extraCanvas.drawRoundRect(it.middleX.toFloat()-widthText/2,it.middleY.toFloat()+textSize/2,
                                it.middleX.toFloat()+widthText/2,it.middleY-textSize/2,8f,8f,p)
                    }else{
                        extraCanvas.drawRect(it.middleX.toFloat()-widthText/2,it.middleY.toFloat()+textSize/2,
                                it.middleX.toFloat()+widthText/2,it.middleY-textSize/2,p)
                    }
                    p.color= Color.BLACK

                    extraCanvas.drawText(distStr,it.middleX.toFloat()-(widthText/2),it.middleY+(textSize/2-STROKE_WIDTH/2),p)
                  //  extraCanvas.drawTextOnPath(distStr, path2, (it.distance.toInt() - widthText) / 2, -10F, p)
                    path2.reset()
                }
            }
        }
    }

    //ищем все следующие точки , если фигура закончена
    private fun calcAllNextPoints(editedListPoints: MutableList<MyPoint>, idPoint: Int, length: String): MutableList<MyPoint> {

        var lengthInt = 0
        //todo эту обработку надо сделать в самом диалоге, чтобы не бесить юзера
        try {
            lengthInt = length.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(context, R.string.notNumber, Toast.LENGTH_SHORT).show()
            motionTouchEventX = 0f
            motionTouchEventY = 0f
            return editedListPoints
        }
        for (i in 0 until editedListPoints.size) {
            if (editedListPoints[i].idPoint == idPoint) {
                val coefici = editedListPoints[i].realDistance / editedListPoints[i].distance
                var realDistanceScaled = lengthInt * coefici
                editedListPoints[i].distance = lengthInt.toFloat()
//todo надо сделать редактирование начального листпоинта для правильного пересчёта площади
                editedListPoints[i].x = (editedListPoints[i - 1].x + realDistanceScaled * editedListPoints[i].mCos).toInt()
                editedListPoints[i].y = (editedListPoints[i - 1].y + realDistanceScaled * editedListPoints[i].mSin).toInt()
                editedListPoints[i].middleX = (editedListPoints[i].x + editedListPoints[i - 1].x)/2
                editedListPoints[i].middleY = (editedListPoints[i].y + editedListPoints[i - 1].y)/2

                editedListPoints[i].realDistance = realDistanceScaled
            //меняю точки следующие за редактируемым отрезком
                for(j in i until editedListPoints.size){
                    realDistanceScaled = editedListPoints[j].distance * coefici
                    editedListPoints[j].x = (editedListPoints[j - 1].x + realDistanceScaled * editedListPoints[j].mCos).toInt()
                    editedListPoints[j].y = (editedListPoints[j - 1].y + realDistanceScaled * editedListPoints[j].mSin).toInt()

                    editedListPoints[j].middleX = (editedListPoints[j].x + editedListPoints[j - 1].x)/2
                    editedListPoints[j].middleY = (editedListPoints[j].y + editedListPoints[j - 1].y)/2

                    editedListPoints[j].realDistance = realDistanceScaled
                    editedListPoints[j].distance = realDistanceScaled/coefici

                    //обрабатываю последний отрезок
                    if(j == editedListPoints.size-1){
                        editedListPoints[j].x = editedListPoints[0].x
                        editedListPoints[j].y = editedListPoints[0].y
                        editedListPoints[j].middleX = (editedListPoints[j].x + editedListPoints[j - 1].x)/2
                        editedListPoints[j].middleY = (editedListPoints[j].y + editedListPoints[j - 1].y)/2
                        editedListPoints[j].realDistance = calcDistance(editedListPoints[0].x,editedListPoints[0].y,
                                editedListPoints[j - 1].x,editedListPoints[j - 1].y)
                        editedListPoints[j].distance = editedListPoints[j].realDistance/coefici

                    }
                }

            }


        }
        Log.d("log", "$motionTouchEventX    $motionTouchEventY  calcAllNextPoints")

        return editedListPoints
    }

    //отображаем площадь и периметр
    private fun drawSquarePerimetr(listPoints: MutableList<MyPoint>) {
        val arrX = mutableListOf<Int>()
        val arrY = mutableListOf<Int>()
        var numPoints = listPoints.size - 1
        var square = 0f
        var perimeter = 0f

        listPoints.forEach {
            arrY.add(it.x)
            arrX.add(it.y)
            perimeter += it.distance
        }
        for (i in 0..numPoints) {
            square += (arrX[numPoints] + arrX[i]) * (arrY[numPoints] - arrY[i])
            numPoints = i
        }
        square /= 2
        val sq1 = roundOffDecimal((square / 10000), "#.#")
        val perRounded = roundOffDecimal((perimeter / 100), "#.#")
        val sq2 = square
        val p = Paint();

        p.strokeWidth = 4F;
        p.style = Paint.Style.FILL;
        p.textSize = 40f
        p.color = Color.BLACK
        extraCanvas.drawText("S = $sq1 m\u00b2 ; P = $perRounded m", 50f, 50f, p)
        extraCanvas.drawText("S= $sq2 pix\u00b2; P= $perimeter pix", 50f, 100f, p)

    }

    //ок в диалоге
    override fun onDialogPositiveClick(dist: String, idPoint: Int) {
        //  listPoints = recalculatePoints(dialog, idPoint)
        //scaledListPoints = recalculatePoints(dialog, idPoint)
        motionTouchEventX = 0f
        motionTouchEventY = 0f
        scaledListPoints = calcAllNextPoints(scaledListPoints, idPoint, dist)
        listPoints = calcAllNextPoints(listPoints, idPoint, dist)

        //чтобы увеличить чертеж до краёв
     //   scaleCanvas(true,true)

        scaleCanvasTest()
        touchDown()

        touchUp()
    }

    //нет в диалоге
    override fun onDialogNegativeClick(dialog: DialogFragment) {
        //надо найти центр фигуры, и вычислить увеличение про прямоугольнику вписанной фигуры
        //  extraCanvas.scale(0.3f,0.3f, 400f,400f)
    }

    //здесь происходит начальная инициализация канваса
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
    }

    //перерисовка
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }
}

data class MyPoint(var x: Int, var y: Int, var idPoint: Int = 0, var distance: Float = 0f,
                   var mCos: Float = 0f, var mSin: Float = 0f, var middleX: Int = 0, var middleY: Int = 0, var realDistance: Float = 0f)