package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class GameOver : AppCompatActivity() {

    lateinit var tvPoints: TextView
    lateinit var ivNewHighest: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_over)

        ivNewHighest = findViewById(R.id.ivNewHeighest)
        tvPoints = findViewById(R.id.tvPoints)

        // รับค่าคะแนนที่ถูกส่งข้ามมาทาง Intent
        val points = intent.extras?.getInt("points") ?: 0

        // ถ้าทำคะแนนได้ 240 แต้ม (เคลียร์เกมหรือทำสถิติสูงสุด) ให้แสดงรูปป้ายพิเศษขึ้นมา
        if (points == 240) {
            ivNewHighest.visibility = View.VISIBLE
        }
        tvPoints.text = "$points"
    }
    fun restart(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun exit(view: View) {
        finish()
    }
}