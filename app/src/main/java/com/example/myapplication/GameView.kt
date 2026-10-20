package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Handler
import android.view.View
import java.util.Random
import android.graphics.Canvas
import android.content.Intent
import android.view.MotionEvent

class GameView(context: Context) : View(context) {

    // ตัวแปรเก็บพิกัดตำแหน่งลูกบอล
    var ballX: Float = 0f
    var ballY: Float = 0f

    // เรียกใช้คลาส Velocity ที่เราสร้างไว้ก่อนหน้านี้ (ความเร็วแกน X=25, Y=32)
    var velocity = Velocity(25, 32)

    // ตัวแปรสำหรับระบบลูปเวลาในเกม
    lateinit var gameHandler: Handler
    val updateMillis: Long = 30
    lateinit var runnable: Runnable

    // เครื่องมือพู่กันสำหรับวาดรูป (Paint) และขนาดตัวอักษร
    var textPaint = Paint()
    var healthPaint = Paint()
    var brickPaint = Paint()
    val textSize: Float = 120f

    // ตัวแปรพิกัดไม้กระดาน (Paddle) และการลากนิ้ว
    var paddleX: Float = 0f
    var paddleY: Float = 0f
    var oldX: Float = 0f
    var oldPaddleX: Float = 0f

    // คะแนนของผู้เล่น เริ่มต้นที่ 0
    var points: Int = 0
    var life: Int = 3
    lateinit var ball: Bitmap
    lateinit var paddle: Bitmap
    var dWidth: Int = 0
    var dHeight: Int = 0
    var ballWidth: Int = 0
    var ballHeight: Int = 0
    lateinit var mpHit: MediaPlayer
    lateinit var mpMiss: MediaPlayer
    lateinit var mpBreak: MediaPlayer
    var random = Random()
    var bricks = arrayOfNulls<Brick>(30)
    var numBricks: Int = 0
    var brokenBricks: Int = 0
    var gameOver: Boolean = false

    init {
        ball = BitmapFactory.decodeResource(resources, R.drawable.ball)
        paddle = BitmapFactory.decodeResource(resources, R.drawable.paddle)
        gameHandler = Handler()
        runnable = Runnable {
            invalidate()
        }
        mpHit = MediaPlayer.create(context, R.raw.hit)
        mpMiss = MediaPlayer.create(context, R.raw.miss)
        mpBreak = MediaPlayer.create(context, R.raw.breaking)

        textPaint.color = Color.RED
        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.LEFT

        healthPaint.color = Color.GREEN
        brickPaint.setARGB(255, 249, 129, 0)

        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        dWidth = size.x
        dHeight = size.y
        random = Random()
        ballX = random.nextInt(dWidth - 50).toFloat()
        ballY = (dHeight / 3).toFloat()
        paddleY = ((dHeight * 4) / 5).toFloat()
        paddleX = (dWidth / 2 - paddle.width / 2).toFloat()
        ballWidth = ball.width
        ballHeight = ball.height
        createBricks()
    }

    private fun createBricks() {
        val brickWidth = dWidth / 8
        val brickHeight = dHeight / 16
        for (column in 0 until 8) {
            for (row in 0 until 3) {
                bricks[numBricks] = Brick(row, column, brickWidth, brickHeight)
                numBricks++
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)

        // ขยับตำแหน่งลูกบอลตามค่าความเร็ว
        ballX += velocity.x
        ballY += velocity.y

        // ถ้าลูกบอลชนขอบซ้ายหรือขวา ให้เด้งกลับทิศทาง
        if (ballX >= dWidth - ball.width || ballX <= 0) {
            velocity.x *= -1
        }

        // ถ้าลูกบอลชนขอบบน ให้เด้งกลับลงมา
        if (ballY <= 0) {
            velocity.y *= -1
        }

        // ถ้าลูกบอลตกเลยไม้กระดานลงไปด้านล่าง (ผู้เล่นรับไม่ทัน)
        if (ballY > paddleY + paddle.height) {
            ballX = (1 + random.nextInt(dWidth - ball.width - 1)).toFloat()
            ballY = (dHeight / 3).toFloat()

            if (mpMiss != null) {
                mpMiss.start()
            }

            velocity.x = xVelocity()
            velocity.y = 32
            life--
            if (life == 0) {
                gameOver = true
                launchGameOver()
                return
            }
        }
        if (((ballX + ball.width) >= paddleX)
            && (ballX <= paddleX + paddle.width)
            && (ballY + ball.height >= paddleY)
            && (ballY + paddle.height <= paddleY + paddle.height)) {

            if (mpHit != null) {
                mpHit.start()
            }

            velocity.x += 1
            velocity.y = (velocity.y + 1) * -1
        }

        canvas.drawBitmap(ball, ballX, ballY, null)
        canvas.drawBitmap(paddle, paddleX, paddleY, null)
        for (i in 0 until numBricks) {
            val b = bricks[i]
            if (b != null && b.isVisible) {
                canvas.drawRect(
                    (b.column * b.width + 1).toFloat(),
                    (b.row * b.height + 1).toFloat(),
                    (b.column * b.width + b.width - 1).toFloat(),
                    (b.row * b.height + b.height - 1).toFloat(),
                    brickPaint
                )
            }
        }
        canvas.drawText("" + points, 20f, textSize, textPaint)
        if (life == 2) {
            healthPaint.color = Color.YELLOW
        } else if (life == 1) {
            healthPaint.color = Color.RED
        }
        canvas.drawRect(
            (dWidth - 200).toFloat(),
            30f,
            (dWidth - 200 + 60 * life).toFloat(),
            80f,
            healthPaint
        )
        for (i in 0 until numBricks) {
            val b = bricks[i]
            if (b != null && b.isVisible) {
                if (ballX + ballWidth >= b.column * b.width
                    && ballX <= b.column * b.width + b.width
                    && ballY <= b.row * b.height + b.height
                    && ballY >= b.row * b.height) {

                    if (mpBreak != null) {
                        mpBreak.start()
                    }

                    velocity.y = (velocity.y + 1) * -1
                    b.setInvisible()
                    points += 10
                    brokenBricks++

                    if (brokenBricks == 24) {
                        launchGameOver()
                    }
                }
            }
        }
        if (brokenBricks == numBricks) {
            gameOver = true
        }
        if (!gameOver) {
            gameHandler.postDelayed(runnable, updateMillis)
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        if (touchY >= paddleY) {
            val action = event.action

            if (action == MotionEvent.ACTION_DOWN) {
                oldX = event.x
                oldPaddleX = paddleX
            }

            if (action == MotionEvent.ACTION_MOVE) {
                val shift = oldX - touchX
                val newPaddleX = oldPaddleX - shift

                if (newPaddleX <= 0f) {
                    paddleX = 0f
                } else if (newPaddleX >= (dWidth - paddle.width)) {
                    paddleX = (dWidth - paddle.width).toFloat()
                } else {
                    paddleX = newPaddleX
                }
            }
        }
        return true
    }




    private fun launchGameOver() {
        gameHandler.removeCallbacksAndMessages(null)
        val intent = Intent(context, GameOver::class.java)
        intent.putExtra("points", points)
        context.startActivity(intent)
        (context as Activity).finish()
    }





    private fun xVelocity(): Int {
        val values = intArrayOf(-35, -30, -25, 25, 30, 35)
        val index = random.nextInt(6)
        return values[index]
    }
}

