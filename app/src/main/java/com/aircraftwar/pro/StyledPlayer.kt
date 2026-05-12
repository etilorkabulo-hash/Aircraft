package com.aircraftwar.pro

import android.content.Context
import android.graphics.Canvas
import kotlin.random.Random

class GameMap(private val context: Context) {
    private val player = StyledPlayer(500, 1000)
    private val enemies = mutableListOf<StyledEnemy>()
    private val bullets = mutableListOf<StyledBullet>()
    private var spawnTimer = 0
    private val screenWidth = 1080
    private val screenHeight = 1920

    fun movePlayer(x: Int, y: Int) {
        player.x = x.coerceIn(0, screenWidth)
        player.y = y.coerceIn(0, screenHeight)
    }

    fun update() {
        spawnTimer++
        if (spawnTimer > 30) {
            enemies.add(StyledEnemy(Random.nextInt(screenWidth), 0))
            spawnTimer = 0
        }

        enemies.forEach { it.update() }
        enemies.removeAll { it.y > screenHeight }

        bullets.forEach { it.update() }
        bullets.removeAll { it.y < 0 }

        detectCollisions()

        if (Random.nextFloat() < 0.1f) {
            bullets.add(StyledBullet(player.x, player.y))
        }
    }

    private fun detectCollisions() {
        val bulletIterator = bullets.iterator()
        while (bulletIterator.hasNext()) {
            val bullet = bulletIterator.next()
            val enemyIterator = enemies.iterator()
            while (enemyIterator.hasNext()) {
                val enemy = enemyIterator.next()
                if (bullet.collidesWith(enemy)) {
                    bulletIterator.remove()
                    enemyIterator.remove()
                    break
                }
            }
        }
    }

    fun draw(canvas: Canvas) {
        player.draw(canvas)
        enemies.forEach { it.draw(canvas) }
        bullets.forEach { it.draw(canvas) }
    }
}
