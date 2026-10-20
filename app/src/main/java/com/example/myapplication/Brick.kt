package com.example.myapplication

class Brick(var row: Int, var column: Int, var width: Int, var height: Int) {
    // รอรับค่าจาก GameView
    var isVisible: Boolean = true
    fun setInvisible() {
        isVisible = false
    }
}