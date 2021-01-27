package com.example.minipaint

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import java.lang.Math.pow
import kotlin.math.*


private const val STROKE_WIDTH = 6f

class MyCanvasView(context: Context) : View(context) {

    private var scaledListPoints: MutableList<MyPoint> = mutableListOf()
    private val circleRadius = 30f

    //привожу контекст к активити чтобы вызвать registerForActivityResult
    private val contextActivity: AppCompatActivity = context as AppCompatActivity

    private val startForResult = contextActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data!!
            val idPoint = intent.getIntExtra("idPoint", 10)
            val dist = intent.getIntExtra("length", 10)
            val isLast = intent.getBooleanExtra("isLast", false)
           // Toast.makeText(context,"$isLast",Toast.LENGTH_SHORT).show()

            //чтобы не было повторного нажатия диалога
            motionTouchEventX = 0f
            motionTouchEventY = 0f



            //это условие для того чтобы применить первое маштабирование , желательно зарефакторить
            if (isFirstEditForScale) {
                listPoints = calcAllNextPoints(listPoints, idPoint, dist)
                isFirstEditForScale = true
                scaledListPoints = calcAllNextPoints(scaledListPoints, idPoint, dist)
            } else {
                listPoints = calcAllNextPointsForFirstList(listPoints, idPoint, dist)
                scaledListPoints = calcAllNextPoints(scaledListPoints, idPoint, dist)
            }
            //моштабирую чертеж
            scaleCanvasToEdge()
            touchDown()
            touchUp()

            //todo написать тестовою отрисовку на канвасе , надо перенести ниже
            if(isLast){
                val pathTest = Path()
                pathTest.moveTo(scaledListPoints.last().x.toFloat(),
                        scaledListPoints.last().y.toFloat())

                val xy = calcThirdPick(scaledListPoints.last().x.toFloat(), scaledListPoints.last().y.toFloat(),
                        scaledListPoints[scaledListPoints.size-3].x.toFloat(), scaledListPoints[scaledListPoints.size-3].y.toFloat(),
                        dist.toFloat() ,scaledListPoints[scaledListPoints.size-2].realDistance)

            //  Log.d("log","$xy")
                  pathTest.lineTo(xy[0],xy[1])
                //pathTest.lineTo(100f,100f)
                paint.color=Color.BLACK
                extraCanvas.drawPath(pathTest,paint)
            }

        }
    }

    private fun calcThirdPick(x1: Float, y1:Float, x2: Float, y2:Float, sideB:Float, sideA:Float, prevX:Float = 0f, prevY:Float= 0f): List<Float> {

//        Log.d("log","x1 $x1")
//        Log.d("log","y1 $y1")
//        Log.d("log","x2 $x2")
//        Log.d("log","y2 $y2")
//        Log.d("log","sideA $sideA")
//        Log.d("log","sideB $sideB")

       val sideC = sqrt((x1 -x2).pow(2)+(y1-y2).pow(2))
        Log.d("log","$sideC")
        //todo acos может не правильно работать т.к. Оси по другому располагаются
        var xToCos =(sideC.pow(2)+sideB.pow(2)-sideA.pow(2))/(2*sideC*sideB)

        Log.d("log","xToCosFirst  $xToCos")
        //xToCos = (xToCos%Math.PI*2 ).toFloat()
        Log.d("log","xToCos  $xToCos")
        val corA = acos(xToCos)

        Log.d("log","$corA")
        //todo переделать все формулы подбирал имперически
        val x3 = x1 +sideB* sin(atan2(x2-x1,y2-y1) -corA)
        val y3 = y1 +sideB* cos(atan2(x2-x1,y2-y1) -corA)
        return listOf<Float>(x3,y3)
    }


    private val textSize = 40f
    private var isFigureDone = false
    private var counterPointId = 0
    private val circleRadiusForEdit = 50
    private var isFirstEditForScale = true

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
            scaleCanvas(false, false)
            return true
        }

        //Этот метод необходимо переопределить а то не сработают другие жесты
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    }

    private fun scaleCanvasToEdge() {
        //прорисовываю путь с известными точками т.к. он теряется в определённые моменты
        val pathDest = Path()
        scaledListPoints.forEach {
            if (it.idPoint == 0) {
                pathDest.moveTo(it.x.toFloat(), it.y.toFloat())
            } else {
                pathDest.lineTo(it.x.toFloat(), it.y.toFloat())
            }

        }

        val rectfBounds = RectF()
        val matrix = Matrix()
        matrix.reset()

        val bounds = (extraCanvas.width * 0.1).toFloat()
        //прямоугольник-рамка для вписания
        val rectfDest = RectF()
        rectfDest.set(bounds, bounds * 2, extraCanvas.width - bounds, extraCanvas.height - bounds)
        //вычисление границ чертежа и присвоение этих границ прямоугольнику
        pathDest.computeBounds(rectfBounds, true);
        //матрица выполняющая вписание одного прямоугольника в другой
        matrix.setRectToRect(rectfBounds, rectfDest, Matrix.ScaleToFit.CENTER);
        //здесь находим новые точки с помощью матрицы matrix.mapPoints()
        val arrF = FloatArray(scaledListPoints.size * 2)
        var iter = 0
        scaledListPoints.forEach {
            arrF[iter] = it.x.toFloat()
            iter++
            arrF[iter] = it.y.toFloat()
            iter++
        }
        //вычисление преобразованных точек за счёт матрицы
        matrix.mapPoints(arrF)
        iter = 0
        //меняю ху на преобразованые из матрицы
        scaledListPoints.forEach {
            it.x = arrF[iter].toInt()
            iter++
            it.y = arrF[iter].toInt()
            iter++
        }

        var lastP = MyPoint(0, 0)
        //меняем средниные точки для изменённых
        scaledListPoints.forEach {
            if (it.idPoint != 0) {
                it.middleX = (lastP.x + it.x) / 2
                it.middleY = (lastP.y + it.y) / 2
            }
            lastP = it
        }
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
//                // назначаю увеличеные точки для маштабов
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
                counterPointId--
                counterPointId--
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
            drawSquarePerimeter(listPoints)
            editSide(motionTouchEventX, motionTouchEventY, scaledListPoints)
        }
        //рисуем длину отрезков
        drawNumberLength()
        //Это не первая точка черчежа
        isFirstTouch = false

//        val str = StringBuilder()
//            listPoints.forEach{
//                str!!.append("[idPoint = ${it.idPoint} , ${it.x} , ${it.y} , dist ${it.distance},realdist ${it.realDistance}, mX ${it.middleX}, mY ${it.middleY}]")
//            }
//        Log.d("Log", str.toString())
//
//        val str1 = StringBuilder()
//        scaledListPoints.forEach{
//            str1!!.append("*[idPoint =${it.idPoint} , ${it.x} , ${it.y} , dist ${it.distance}, realdist ${it.realDistance}, mX ${it.middleX}, mY ${it.middleY}]")
//        }
//        Log.d("Log", "$str1")

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
        if (isToEdge) {
            //10% on ширины девайса
            val bounds = (extraCanvas.width * 0.1).toFloat()
            //прямоугольник-рамка для вписания

            val rectfDest = RectF()
            rectfDest.set(bounds, bounds, extraCanvas.width - bounds, extraCanvas.height - bounds)
            //вычисление границ чертежа и присвоение этих границ прямоугольнику
            path.computeBounds(rectfBounds, true);
            //матрица выполняющая вписание одного прямоугольника в другой
            matrix.setRectToRect(rectfBounds, rectfDest, Matrix.ScaleToFit.CENTER);
            // попробую найти матрицу по краям чечежа и увеличить её
        } else {
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
        Log.d("log", arrF.joinToString("          ;"))
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

        for (i in 0 until listPointsEdited.size) {
            val h = calcDistance(listPointsEdited[i].middleX, listPointsEdited[i].middleY,
                    motionTouchEventX.toInt(), motionTouchEventY.toInt())
            //условие при котором мы сравниваем радиус точки касания с нашей серединой отрезка
            if (circleRadiusForEdit >= h && h != 0f) {
                //здесь мы каснёмся последнего отрезка и Тостуем
        if(listPointsEdited[i].idPoint == listPointsEdited.size-1){//TODO его надо редактировать чтобы точка смешалась редактируя последний угол,но не задивая последнюю длину
        Toast.makeText(context, R.string.lastDistance, Toast.LENGTH_LONG).show()
            // рисуем выделение красным
            val xy = calcStartPoint(listPointsEdited[i])
            pathEdit.moveTo(xy[0], xy[1])
            pathEdit.lineTo(listPointsEdited[i].x.toFloat(), listPointsEdited[i].y.toFloat())
            extraCanvas.drawPath(pathEdit, paintEdit)
            //Запускаю активити
            startForResult.launch(Intent(context, EditSide::class.java).apply {
                putExtra("idPoint", listPointsEdited[i].idPoint)
                putExtra("isLast", true)
            })
        break
        }
                // рисуем выделение красным
                val xy = calcStartPoint(listPointsEdited[i])
                pathEdit.moveTo(xy[0], xy[1])
                pathEdit.lineTo(listPointsEdited[i].x.toFloat(), listPointsEdited[i].y.toFloat())
                extraCanvas.drawPath(pathEdit, paintEdit)
                //Запускаю активити
                startForResult.launch(Intent(context, EditSide::class.java).apply {
                    putExtra("idPoint", listPointsEdited[i].idPoint)
                    putExtra("isLast", false)
                })
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

                    //подменяю длину из начального листа
                    // distStr = (roundOffDecimal(listPoints[it.idPoint].distance,"#"))
                    distStr = (roundOffDecimal(listPoints[it.idPoint].distance, "#"))
                    //   distStr = (roundOffDecimal(it.distance,"#"))

                    val xy = calcStartPoint(it)
                    path2.moveTo(xy[0], xy[1])
                    path2.lineTo(it.x.toFloat(), it.y.toFloat())


                    val realDist = calcDistance(xy[0].toInt(), xy[1].toInt(), it.x, it.y)
                    it.realDistance = realDist
                    widthText = p.measureText(distStr);
                    //увловие для последнего отрезка, беру значение не из увеличенного листа а из первого листа
                    if (it.idPoint == scaledListPoints.size - 1) {
                        distStr = roundOffDecimal(listPoints.last().distance, "#")
                    }
                    //  extraCanvas.drawTextOnPath(distStr, path2, (realDist - widthText) / 2, -10F, p)
                    p.color = drawColor


                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        extraCanvas.drawRoundRect(it.middleX.toFloat() - widthText / 2, it.middleY.toFloat() + textSize / 2,
                                it.middleX.toFloat() + widthText / 2, it.middleY - textSize / 2, 8f, 8f, p)
                    } else {
                        extraCanvas.drawRect(it.middleX.toFloat() - widthText / 2, it.middleY.toFloat() + textSize / 2,
                                it.middleX.toFloat() + widthText / 2, it.middleY - textSize / 2, p)
                    }

                    p.color = Color.BLACK
                    extraCanvas.drawText(distStr, it.middleX.toFloat() - (widthText / 2), it.middleY + (textSize / 2 - STROKE_WIDTH / 2), p)

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
                    p.color = drawColor

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        extraCanvas.drawRoundRect(it.middleX.toFloat() - widthText / 2, it.middleY.toFloat() + textSize / 2,
                                it.middleX.toFloat() + widthText / 2, it.middleY - textSize / 2, 8f, 8f, p)
                    } else {
                        extraCanvas.drawRect(it.middleX.toFloat() - widthText / 2, it.middleY.toFloat() + textSize / 2,
                                it.middleX.toFloat() + widthText / 2, it.middleY - textSize / 2, p)
                    }
                    p.color = Color.BLACK

                    extraCanvas.drawText(distStr, it.middleX.toFloat() - (widthText / 2), it.middleY + (textSize / 2 - STROKE_WIDTH / 2), p)
                    //  extraCanvas.drawTextOnPath(distStr, path2, (it.distance.toInt() - widthText) / 2, -10F, p)
                    path2.reset()
                }
            }
        }
    }

    //ищем все следующие точки , если фигура закончена
    private fun calcAllNextPoints(editedListPoints: MutableList<MyPoint>, idPoint: Int, length: Int): MutableList<MyPoint> {

        //условие если первая коректировка длины
        if (isFirstEditForScale) {

            for (i in 0 until editedListPoints.size) {
                if (editedListPoints[i].idPoint == idPoint) {

                    val coefici = editedListPoints[i].realDistance / editedListPoints[i].distance

                    var realDistanceScaled = length * coefici

                    val coefForMashtaba = length / editedListPoints[i].realDistance
                    editedListPoints[i].distance = length.toFloat()

                    //меняю точки следующие за редактируемым отрезком
                    for (j in 1 until editedListPoints.size) {
                        //выбрасвваю если то же id что и редактируемое
                        if (editedListPoints[j].idPoint == idPoint) {
                            realDistanceScaled = editedListPoints[j].distance
                        } else {
                            realDistanceScaled = editedListPoints[j].distance * coefForMashtaba
                        }


                        editedListPoints[j].x = (editedListPoints[j - 1].x + realDistanceScaled * editedListPoints[j].mCos).toInt()
                        editedListPoints[j].y = (editedListPoints[j - 1].y + realDistanceScaled * editedListPoints[j].mSin).toInt()

                        editedListPoints[j].middleX = (editedListPoints[j].x + editedListPoints[j - 1].x) / 2
                        editedListPoints[j].middleY = (editedListPoints[j].y + editedListPoints[j - 1].y) / 2


                        //уловие для отбора листа увеличенного или нет

                        editedListPoints[j].realDistance = calcDistance(editedListPoints[j].x, editedListPoints[j].y,
                                editedListPoints[j - 1].x, editedListPoints[j - 1].y)



                        if (editedListPoints[j].idPoint == idPoint) {
                            editedListPoints[j].distance = editedListPoints[j].distance
                        } else {
                            editedListPoints[j].distance = editedListPoints[j].distance * coefForMashtaba
                        }


                        //последний отрезок
                        if (j == editedListPoints.size - 1) {
                            editedListPoints[j].x = editedListPoints[0].x
                            editedListPoints[j].y = editedListPoints[0].y
                            editedListPoints[j].middleX = (editedListPoints[j].x + editedListPoints[j - 1].x) / 2
                            editedListPoints[j].middleY = (editedListPoints[j].y + editedListPoints[j - 1].y) / 2
                            editedListPoints[j].realDistance = calcDistance(editedListPoints[0].x, editedListPoints[0].y,
                                    editedListPoints[j - 1].x, editedListPoints[j - 1].y)
                            editedListPoints[j].distance = editedListPoints[j].realDistance / coefici

                        }
                    }
                }
            }
            isFirstEditForScale = false
        } else {

            for (i in 0 until editedListPoints.size) {
//todo ввести уловие для последнего отрезка и не переделывать весь лист, то же сделать для не моштабируемого листа
    
                if (editedListPoints[i].idPoint == idPoint) {

                    val coefici = editedListPoints[i].realDistance / editedListPoints[i].distance
                    var realDistanceScaled = length * coefici
                    editedListPoints[i].distance = length.toFloat()
                    editedListPoints[i].x = (editedListPoints[i - 1].x + realDistanceScaled * editedListPoints[i].mCos).toInt()
                    editedListPoints[i].y = (editedListPoints[i - 1].y + realDistanceScaled * editedListPoints[i].mSin).toInt()
                    editedListPoints[i].middleX = (editedListPoints[i].x + editedListPoints[i - 1].x) / 2
                    editedListPoints[i].middleY = (editedListPoints[i].y + editedListPoints[i - 1].y) / 2
                    editedListPoints[i].realDistance = realDistanceScaled
                    //меняю точки следующие за редактируемым отрезком
                    for (j in 1 until editedListPoints.size) {

                        realDistanceScaled = editedListPoints[j].distance * coefici
                        editedListPoints[j].x = (editedListPoints[j - 1].x + realDistanceScaled * editedListPoints[j].mCos).toInt()
                        editedListPoints[j].y = (editedListPoints[j - 1].y + realDistanceScaled * editedListPoints[j].mSin).toInt()

                        editedListPoints[j].middleX = (editedListPoints[j].x + editedListPoints[j - 1].x) / 2
                        editedListPoints[j].middleY = (editedListPoints[j].y + editedListPoints[j - 1].y) / 2
                        editedListPoints[j].realDistance = calcDistance(editedListPoints[j].x, editedListPoints[j].y,
                                editedListPoints[j - 1].x, editedListPoints[j - 1].y)
                        editedListPoints[j].distance = editedListPoints[j].realDistance / coefici

                        //вычисляется последний отрезок
                        if (j == editedListPoints.size - 1) {
                            //todo сдесь всё поменять для редакторования последнего если id last
                            editedListPoints[j].x = editedListPoints[0].x
                            editedListPoints[j].y = editedListPoints[0].y
                            editedListPoints[j].middleX = (editedListPoints[j].x + editedListPoints[j - 1].x) / 2
                            editedListPoints[j].middleY = (editedListPoints[j].y + editedListPoints[j - 1].y) / 2
                            editedListPoints[j].realDistance = calcDistance(editedListPoints[0].x, editedListPoints[0].y,
                                    editedListPoints[j - 1].x, editedListPoints[j - 1].y)
                            editedListPoints[j].distance = editedListPoints[j].realDistance / coefici

                        }
                    }
                }
            }
        }
        return editedListPoints
    }

    private fun calcAllNextPointsForFirstList(editedListPoints: MutableList<MyPoint>, idPoint: Int,
                                              length: Int): MutableList<MyPoint> {


        for (i in 0 until editedListPoints.size) {
            if (editedListPoints[i].idPoint == idPoint) {

                var realDistanceScaled = length

                editedListPoints[i].distance = length.toFloat()
                editedListPoints[i].x = (editedListPoints[i - 1].x + realDistanceScaled * editedListPoints[i].mCos).toInt()
                editedListPoints[i].y = (editedListPoints[i - 1].y + realDistanceScaled * editedListPoints[i].mSin).toInt()
                editedListPoints[i].middleX = (editedListPoints[i].x + editedListPoints[i - 1].x) / 2
                editedListPoints[i].middleY = (editedListPoints[i].y + editedListPoints[i - 1].y) / 2

                editedListPoints[i].realDistance = realDistanceScaled.toFloat()
                //меняю точки следующие за редактируемым отрезком
                for (j in 1 until editedListPoints.size) {

                    realDistanceScaled = editedListPoints[j].distance.toInt()
                    editedListPoints[j].x = (editedListPoints[j - 1].x + realDistanceScaled * editedListPoints[j].mCos).toInt()
                    editedListPoints[j].y = (editedListPoints[j - 1].y + realDistanceScaled * editedListPoints[j].mSin).toInt()

                    editedListPoints[j].middleX = (editedListPoints[j].x + editedListPoints[j - 1].x) / 2
                    editedListPoints[j].middleY = (editedListPoints[j].y + editedListPoints[j - 1].y) / 2
                    editedListPoints[j].realDistance = calcDistance(editedListPoints[j].x, editedListPoints[j].y,
                            editedListPoints[j - 1].x, editedListPoints[j - 1].y)
                    editedListPoints[j].distance = editedListPoints[j].realDistance

                    if (j == editedListPoints.size - 1) {
                        editedListPoints[j].x = editedListPoints[0].x
                        editedListPoints[j].y = editedListPoints[0].y
                        editedListPoints[j].middleX = (editedListPoints[j].x + editedListPoints[j - 1].x) / 2
                        editedListPoints[j].middleY = (editedListPoints[j].y + editedListPoints[j - 1].y) / 2
                        editedListPoints[j].realDistance = calcDistance(editedListPoints[0].x, editedListPoints[0].y,
                                editedListPoints[j - 1].x, editedListPoints[j - 1].y)
                        editedListPoints[j].distance = editedListPoints[j].realDistance

                    }
                }
            }
        }


        return editedListPoints
    }

    //отображаем площадь и периметр
    private fun drawSquarePerimeter(listPoints: MutableList<MyPoint>) {
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