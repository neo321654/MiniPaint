package com.example.minipaint

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.transform
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow


private const val STROKE_WIDTH = 12f


class MyCanvasView(context: Context): View(context) {
    private val circleRadius = 30f
    private var isFigureDone = false
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



    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when(event.action){
            MotionEvent.ACTION_DOWN -> touchDown()
        //здесь потом добавим обработку перетягивания точки
        //    MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private fun touchDown() {
        if(listPoints.isEmpty()){
         //   Добавляем первую точку если начало чертежа
            listPoints.add(MyPoint(motionTouchEventX.toInt(), motionTouchEventY.toInt()))
        }
        //    Опускаемся на начальную точку чертежа
        path.moveTo(listPoints[0].x.toFloat(), listPoints[0].y.toFloat())
    }
    private fun touchUp() {
        //если не начало и не конец чертежа добавляем точку в список
        if(!isFirstTouch && !isFigureDone) {
                listPoints.add(MyPoint(motionTouchEventX.toInt(), motionTouchEventY.toInt()))
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
        val rnd =  Random()
        val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        paint.color = color
        //Снимаем показатели уже построенного пути и узнаём показатели последней точки для анализа дальнейшей
        val pMeasure = PathMeasure(path, false)
        var pMeasureLenght = pMeasure.length
        var pos = FloatArray(2)
        var tan = FloatArray(2)
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
        //Отрисвовываю средние круги , надо потом заменить на текст размеров


      //  p.setColor(ResourcesCompat.getColor(resources, R.color.design_default_color_on_primary, null))
        var p =  Paint();

        p.setStrokeWidth(4F);
        p.setStyle(Paint.Style.FILL);
        p.textSize = 40f
        p.color = Color.BLACK

        var path2 = Path()
        var widthText = 0f;

       // var rectf = RectF(150F, 100F, 400F, 150F)
        var distAll = 0f
        listPoints.forEach{
            //проверяю что это не первая точка
            if(it.middleX != 0 && it.middleY!=0){
                distAll+=it.distance
                  path2.moveTo((2*it.middleX-it.x).toFloat(), (2*it.middleY-it.y).toFloat())
                path2.lineTo(it.x.toFloat(), it.y.toFloat())
                 widthText = p.measureText((it.distance.toInt()).toString());
                extraCanvas.drawTextOnPath((it.distance.toInt()).toString(),path2, (it.distance.toInt() -widthText)/2, -10F,p)
                path2.reset()
            }

        }

        path.reset()
        invalidate()

        isFirstTouch = false

        var str = StringBuilder()
            listPoints.forEach{
                str!!.append("[${it.x} , ${it.y} , dist ${it.distance}]")
            }
        Log.d("Log", str.toString())



    }
    fun calcLastPoint(listPoints: MutableList<MyPoint>, tan: FloatArray, dest: Float): MutableList<MyPoint> {
        listPoints.last().mCos = roundOffDecimal(tan[0])
        listPoints.last().mSin = roundOffDecimal(tan[1])

        listPoints.last().x = (listPoints[listPoints.size - 2].x +  dest*listPoints.last().mCos).toInt()
        listPoints.last().y = (listPoints[listPoints.size - 2].y +  dest*listPoints.last().mSin).toInt()
        listPoints.last().distance = dest

        listPoints.last().middleX = (listPoints[listPoints.size - 2].x +  (dest/2)*listPoints.last().mCos).toInt()
        listPoints.last().middleY = (listPoints[listPoints.size - 2].y +  (dest/2)*listPoints.last().mSin).toInt()

        return listPoints
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

    private fun calcDistance(x1: Int, y1: Int, x2: Int, y2: Int): Float {
        var corx: Double = (x2-x1).toDouble()
        var cory: Double = (y2-y1).toDouble()
        return (Math.sqrt(corx.pow(2) + cory.pow(2))).toFloat()

    }
}

class MyPoint(var x: Int, var y: Int, var distance: Float = 0f, var mCos: Float = 0f, var mSin: Float = 0f, var middleX: Int = 0, var middleY: Int = 0)