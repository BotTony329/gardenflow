package com.tony.gardenflow.ui.components

import com.tony.gardenflow.domain.model.Plant

fun Plant.emoji(): String = plantEmoji(name)

fun plantEmoji(name: String): String {
    val n = name.lowercase()
    return when {
        "lemon" in n || "柠檬" in n -> "🍋"
        "tomato" in n || "番茄" in n || "西红柿" in n -> "🍅"
        "broccoli" in n || "西兰花" in n -> "🥦"
        "potato" in n || "土豆" in n || "马铃薯" in n -> "🥔"
        "basil" in n || "罗勒" in n -> "🌿"
        "mint" in n || "薄荷" in n -> "🌿"
        "lettuce" in n || "生菜" in n -> "🥬"
        "cabbage" in n || "卷心菜" in n || "包菜" in n -> "🥬"
        "carrot" in n || "胡萝卜" in n -> "🥕"
        "cucumber" in n || "黄瓜" in n -> "🥒"
        "pepper" in n || "chilli" in n || "辣椒" in n -> "🌶️"
        "strawberry" in n || "草莓" in n -> "🍓"
        "apple" in n || "苹果" in n -> "🍎"
        "orange" in n || "橙" in n -> "🍊"
        "lime" in n || "青柠" in n -> "🍋"
        "rose" in n || "玫瑰" in n -> "🌹"
        "sunflower" in n || "向日葵" in n -> "🌻"
        "flower" in n || "花" in n -> "🌼"
        "tree" in n || "树" in n -> "🌳"
        else -> "🌱"
    }
}
