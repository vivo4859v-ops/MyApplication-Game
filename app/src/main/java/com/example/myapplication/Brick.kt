package com.example.myapplication

class Brick(var row: Int, var column: Int, var width: Int, var height: Int) {
    // กำหนดค่าเริ่มต้นให้มองเห็นได้
    var isVisible: Boolean = true

    // ฟังก์ชันสั่งให้ซ่อน (อิฐถูกทำลาย)
    fun setInvisible() {
        isVisible = false
    }
}