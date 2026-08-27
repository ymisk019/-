package com.example.neongame

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.random.Random

class GameView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var playerX = 0f
    private var score = 0
    private var lives = 3

    private var speed = 8f
    private var spawnTimer = 0L
    private var lastTime = System.currentTimeMillis()

    private var gameOver = false
    private var running = true

    private var screenWidth = 0
    private var screenHeight = 0

    private data class FallingObject(
        var x: Float,
        var y: Float,
        var radius: Float,
        var good: Boolean
    )

    private val objects = mutableListOf<FallingObject>()

    override fun onSizeChanged(
        width: Int,
        height: Int,
        oldWidth: Int,
        oldHeight: Int
    ) {
        screenWidth = width
        screenHeight = height

        if (playerX == 0f) {
            playerX = width / 2f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.rgb(8, 11, 24))

        drawBackground(canvas)

        if (running && !gameOver) {
            updateGame()
        }

        drawObjects(canvas)
        drawPlayer(canvas)
        drawUI(canvas)

        if (gameOver) {
            drawGameOver(canvas)
        }

        if (running) {
            postInvalidateDelayed(16)
        }
    }

    private fun updateGame() {

        val now = System.currentTimeMillis()
        val delta = now - lastTime
        lastTime = now

        spawnTimer += delta

        if (spawnTimer >= 650) {

            spawnTimer = 0

            val good = Random.nextFloat() > 0.25f

            objects.add(
                FallingObject(
                    x = Random.nextFloat() *
                            (screenWidth - 100) + 50,

                    y = -40f,

                    radius = Random.nextInt(
                        18,
                        30
                    ).toFloat(),

                    good = good
                )
            )
        }

        val iterator = objects.iterator()

        while (iterator.hasNext()) {

            val obj = iterator.next()

            obj.y += speed

            val playerY = screenHeight - 120f

            if (
                abs(obj.x - playerX) < 60 &&
                obj.y > playerY - 60 &&
                obj.y < playerY + 60
            ) {

                if (obj.good) {
                    score++
                    speed += 0.15f
                } else {
                    lives--
                }

                iterator.remove()
                continue
            }

            if (obj.y > screenHeight + 50) {

                if (obj.good) {
                    lives--
                }

                iterator.remove()
            }
        }

        if (lives <= 0) {
            gameOver = true
        }
    }

    private fun drawBackground(canvas: Canvas) {

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f

        paint.color = Color.argb(
            35,
            50,
            220,
            255
        )

        for (x in 0 until screenWidth step 100) {

            canvas.drawLine(
                x.toFloat(),
                0f,
                x.toFloat(),
                screenHeight.toFloat(),
                paint
            )
        }

        paint.style = Paint.Style.FILL
    }

    private fun drawPlayer(canvas: Canvas) {

        val y = screenHeight - 120f

        paint.color = Color.rgb(
            138,
            43,
            226
        )

        canvas.drawCircle(
            playerX,
            y,
            52f,
            paint
        )

        paint.color = Color.rgb(
            30,
            230,
            255
        )

        canvas.drawCircle(
            playerX,
            y,
            40f,
            paint
        )

        paint.color = Color.rgb(
            8,
            11,
            24
        )

        canvas.drawCircle(
            playerX,
            y,
            28f,
            paint
        )
    }

    private fun drawObjects(canvas: Canvas) {

        for (obj in objects) {

            paint.color =
                if (obj.good) {
                    Color.rgb(
                        53,
                        255,
                        154
                    )
                } else {
                    Color.rgb(
                        255,
                        49,
                        92
                    )
                }

            canvas.drawCircle(
                obj.x,
                obj.y,
                obj.radius,
                paint
            )

            paint.color = Color.rgb(
                8,
                11,
                24
            )

            canvas.drawCircle(
                obj.x,
                obj.y,
                obj.radius * 0.45f,
                paint
            )
        }
    }

    private fun drawUI(canvas: Canvas) {

        paint.typeface = Typeface.DEFAULT_BOLD

        paint.textAlign = Paint.Align.LEFT

        paint.textSize = 28f
        paint.color = Color.rgb(
            85,
            234,
            255
        )

        canvas.drawText(
            "NEON CATCH",
            25f,
            45f,
            paint
        )

        paint.textSize = 22f
        paint.color = Color.WHITE

        canvas.drawText(
            "SCORE: $score",
            25f,
            85f,
            paint
        )

        paint.color = Color.rgb(
            255,
            79,
            154
        )

        canvas.drawText(
            "LIVES: $lives",
            screenWidth - 145f,
            85f,
            paint
        )
    }

    private fun drawGameOver(canvas: Canvas) {

        paint.color = Color.argb(
            235,
            2,
            3,
            10
        )

        canvas.drawRect(
            50f,
            screenHeight / 2f - 170f,
            screenWidth - 50f,
            screenHeight / 2f + 170f,
            paint
        )

        paint.textAlign = Paint.Align.CENTER

        paint.textSize = 45f
        paint.color = Color.rgb(
            255,
            79,
            154
        )

        canvas.drawText(
            "GAME OVER",
            screenWidth / 2f,
            screenHeight / 2f - 60f,
            paint
        )

        paint.textSize = 28f
        paint.color = Color.WHITE

        canvas.drawText(
            "Score: $score",
            screenWidth / 2f,
            screenHeight / 2f,
            paint
        )

        paint.textSize = 20f
        paint.color = Color.rgb(
            53,
            255,
            154
        )

        canvas.drawText(
            "TAP TO PLAY AGAIN",
            screenWidth / 2f,
            screenHeight / 2f + 70f,
            paint
        )

        paint.textAlign = Paint.Align.LEFT
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {

        when (event.action) {

            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {

                if (gameOver) {
                    restart()
                    return true
                }

                playerX = event.x.coerceIn(
                    60f,
                    screenWidth - 60f
                )

                invalidate()
            }
        }

        return true
    }

    private fun restart() {

        score = 0
        lives = 3
        speed = 8f
        spawnTimer = 0
        lastTime = System.currentTimeMillis()

        objects.clear()

        gameOver = false
        running = true

        invalidate()
    }

    fun startGame() {

        running = true
        lastTime = System.currentTimeMillis()

        invalidate()
    }

    fun stopGame() {
        running = false
    }
}
