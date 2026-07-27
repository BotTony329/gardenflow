package com.tony.gardenflow.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tony.gardenflow.R
import com.tony.gardenflow.domain.model.Plant
import com.tony.gardenflow.domain.model.TaskType

enum class GardenIcon {
    Water,
    Fertilise,
    CheckGrowth,
    Harvest,
    Custom,
    Location,
    DailyReminder,
    WorkFinish,
    Notifications,
    HighTemperature,
    DeepSeek,
    ExportData,
    About,
    Sunny,
    PartlyCloudy,
    Rainy,
    Night,
    Cloudy,
    Lemon,
    Tomato,
    Broccoli,
    BasilMint,
    Carrot,
    Strawberry,
    Rose,
    Sunflower,
    Apple,
    Orange,
    Aloe,
    Cactus,
    Succulent,
    Cucumber,
    Daffodil,
    Lavender,
    Lettuce,
    Orchid,
    Pepper,
    Tulip,
    Potato,
    Seedling,
    Camera,
    Keyboard,
    Check,
    Settings
}

fun TaskType.gardenIcon(): GardenIcon = when (this) {
    TaskType.WATER -> GardenIcon.Water
    TaskType.FERTILISE -> GardenIcon.Fertilise
    TaskType.CHECK_GROWTH -> GardenIcon.CheckGrowth
    TaskType.HARVEST -> GardenIcon.Harvest
    TaskType.CUSTOM -> GardenIcon.Custom
}

fun Plant.gardenIcon(): GardenIcon {
    val saved = iconName.orEmpty().lowercase()
    return if (saved.isBlank() || saved == "plant_other") {
        plantGardenIcon("$name ${variety.orEmpty()}")
    } else {
        plantGardenIcon(saved)
    }
}

fun plantGardenIcon(name: String): GardenIcon {
    val n = name.lowercase()
    return when {
        n == "lemon" -> GardenIcon.Lemon
        n == "tomato" -> GardenIcon.Tomato
        n == "broccoli" -> GardenIcon.Broccoli
        n == "basil" -> GardenIcon.BasilMint
        n == "carrot" -> GardenIcon.Carrot
        n == "strawberry" -> GardenIcon.Strawberry
        n == "rose" -> GardenIcon.Rose
        n == "sunflower" -> GardenIcon.Sunflower
        n == "apple" -> GardenIcon.Apple
        n == "orange" -> GardenIcon.Orange
        n == "aloe" -> GardenIcon.Aloe
        n == "cactus" -> GardenIcon.Cactus
        n == "succulent" -> GardenIcon.Succulent
        n == "cucumber" -> GardenIcon.Cucumber
        n == "daffodil" -> GardenIcon.Daffodil
        n == "lavender" -> GardenIcon.Lavender
        n == "lettuce" -> GardenIcon.Lettuce
        n == "orchid" -> GardenIcon.Orchid
        n == "pepper" -> GardenIcon.Pepper
        n == "tulip" -> GardenIcon.Tulip
        n == "potato" -> GardenIcon.Potato
        "lemon" in n || "lime" in n || "柠檬" in n || "青柠" in n -> GardenIcon.Lemon
        "tomato" in n || "番茄" in n || "西红柿" in n -> GardenIcon.Tomato
        "broccoli" in n || "西兰花" in n -> GardenIcon.Broccoli
        "basil" in n || "mint" in n || "罗勒" in n || "薄荷" in n -> GardenIcon.BasilMint
        "bear paw" in n || "cotyledon" in n || "succulent" in n || "多肉" in n -> GardenIcon.Succulent
        "nopalxochia" in n || "disocactus" in n || "orchid cactus" in n || "epiphyllum" in n || "cactus" in n || "仙人掌" in n -> GardenIcon.Cactus
        "aloe" in n || "芦荟" in n -> GardenIcon.Aloe
        "carrot" in n || "胡萝卜" in n -> GardenIcon.Carrot
        "strawberry" in n || "草莓" in n -> GardenIcon.Strawberry
        "rose" in n || "玫瑰" in n -> GardenIcon.Rose
        "sunflower" in n || "向日葵" in n -> GardenIcon.Sunflower
        "apple" in n || "苹果" in n -> GardenIcon.Apple
        "orange" in n || "橙" in n -> GardenIcon.Orange
        "cucumber" in n || "黄瓜" in n -> GardenIcon.Cucumber
        "daffodil" in n || "水仙" in n -> GardenIcon.Daffodil
        "lavender" in n || "薰衣草" in n -> GardenIcon.Lavender
        "lettuce" in n || "生菜" in n -> GardenIcon.Lettuce
        "orchid" in n || "兰花" in n -> GardenIcon.Orchid
        "pepper" in n || "chilli" in n || "辣椒" in n -> GardenIcon.Pepper
        "tulip" in n || "郁金香" in n -> GardenIcon.Tulip
        "potato" in n || "土豆" in n || "马铃薯" in n -> GardenIcon.Potato
        "tree" in n || "树" in n -> GardenIcon.Seedling
        else -> GardenIcon.Seedling
    }
}

fun weatherGardenIcon(raw: String): GardenIcon = when (raw) {
    "rainy" -> GardenIcon.Rainy
    "sunny" -> GardenIcon.Sunny
    "night" -> GardenIcon.Night
    "cloudy" -> GardenIcon.Cloudy
    else -> GardenIcon.PartlyCloudy
}

@Composable
fun GardenIconBadge(
    icon: GardenIcon,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier.size(56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            GardenLineIcon(icon = icon, modifier = Modifier.size(32.dp), tint = tint)
        }
    }
}

@Composable
fun GardenLineIcon(
    icon: GardenIcon,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Image(
        painter = painterResource(icon.drawableRes()),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(tint)
    )
}

@DrawableRes
fun GardenIcon.drawableRes(): Int = when (this) {
    GardenIcon.Water -> R.drawable.gf_icon_water
    GardenIcon.Fertilise -> R.drawable.gf_icon_fertilise
    GardenIcon.CheckGrowth -> R.drawable.gf_icon_check_growth
    GardenIcon.Harvest -> R.drawable.gf_icon_harvest
    GardenIcon.Custom -> R.drawable.gf_icon_custom
    GardenIcon.Location -> R.drawable.gf_icon_garden_location
    GardenIcon.DailyReminder -> R.drawable.gf_icon_daily_reminder
    GardenIcon.WorkFinish -> R.drawable.gf_icon_work_finish_time
    GardenIcon.Notifications -> R.drawable.gf_icon_notifications
    GardenIcon.HighTemperature -> R.drawable.gf_icon_high_temp_rule
    GardenIcon.DeepSeek -> R.drawable.gf_icon_deepseek_api
    GardenIcon.ExportData -> R.drawable.gf_icon_export_data
    GardenIcon.About -> R.drawable.gf_icon_about
    GardenIcon.Sunny -> R.drawable.gf_icon_sunny
    GardenIcon.PartlyCloudy -> R.drawable.gf_icon_partly_cloudy
    GardenIcon.Rainy -> R.drawable.gf_icon_rainy
    GardenIcon.Night -> R.drawable.gf_icon_night
    GardenIcon.Cloudy -> R.drawable.gf_icon_cloudy
    GardenIcon.Lemon -> R.drawable.gf_icon_lemon
    GardenIcon.Tomato -> R.drawable.gf_icon_tomato
    GardenIcon.Broccoli -> R.drawable.gf_icon_broccoli
    GardenIcon.BasilMint -> R.drawable.gf_icon_basil
    GardenIcon.Carrot -> R.drawable.gf_icon_carrot
    GardenIcon.Strawberry -> R.drawable.gf_icon_strawberry
    GardenIcon.Rose -> R.drawable.gf_icon_rose
    GardenIcon.Sunflower -> R.drawable.gf_icon_sunflower
    GardenIcon.Seedling -> R.drawable.gf_icon_plant_other
    GardenIcon.Camera -> R.drawable.gf_icon_setting_other
    GardenIcon.Keyboard -> R.drawable.gf_icon_setting_other
    GardenIcon.Check -> R.drawable.gf_icon_check_growth
    GardenIcon.Settings -> R.drawable.gf_icon_setting_other
    GardenIcon.Apple -> R.drawable.gf_icon_apple
    GardenIcon.Orange -> R.drawable.gf_icon_orange
    GardenIcon.Aloe -> R.drawable.gf_icon_aloe
    GardenIcon.Cactus -> R.drawable.gf_icon_cactus
    GardenIcon.Succulent -> R.drawable.gf_icon_succulent
    GardenIcon.Cucumber -> R.drawable.gf_icon_cucumber
    GardenIcon.Daffodil -> R.drawable.gf_icon_daffodil
    GardenIcon.Lavender -> R.drawable.gf_icon_lavender
    GardenIcon.Lettuce -> R.drawable.gf_icon_lettuce
    GardenIcon.Orchid -> R.drawable.gf_icon_orchid
    GardenIcon.Pepper -> R.drawable.gf_icon_pepper
    GardenIcon.Tulip -> R.drawable.gf_icon_tulip
    GardenIcon.Potato -> R.drawable.gf_icon_potato
}

private fun DrawScope.drawGardenIcon(icon: GardenIcon, tint: Color) {
    val s = size.minDimension
    val left = (size.width - s) / 2f
    val top = (size.height - s) / 2f
    fun p(x: Float, y: Float) = Offset(left + s * x, top + s * y)
    fun sz(w: Float, h: Float) = Size(s * w, s * h)
    val stroke = Stroke(width = (s * 0.07f).coerceAtLeast(2f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun line(a: Offset, b: Offset) = drawLine(tint, a, b, stroke.width, StrokeCap.Round)
    fun circle(center: Offset, r: Float) = drawCircle(tint, radius = s * r, center = center, style = stroke)
    fun oval(x: Float, y: Float, w: Float, h: Float) = drawOval(tint, topLeft = p(x, y), size = sz(w, h), style = stroke)
    fun path(block: Path.() -> Unit) {
        val path = Path().apply(block)
        drawPath(path, tint, style = stroke)
    }

    when (icon) {
        GardenIcon.Water -> path {
            moveTo(p(0.5f, 0.12f).x, p(0.5f, 0.12f).y)
            cubicTo(p(0.22f, 0.44f).x, p(0.22f, 0.44f).y, p(0.22f, 0.72f).x, p(0.22f, 0.72f).y, p(0.5f, 0.88f).x, p(0.5f, 0.88f).y)
            cubicTo(p(0.78f, 0.72f).x, p(0.78f, 0.72f).y, p(0.78f, 0.44f).x, p(0.78f, 0.44f).y, p(0.5f, 0.12f).x, p(0.5f, 0.12f).y)
        }
        GardenIcon.Fertilise -> {
            line(p(0.34f, 0.2f), p(0.66f, 0.2f))
            line(p(0.42f, 0.2f), p(0.42f, 0.45f))
            line(p(0.58f, 0.2f), p(0.58f, 0.45f))
            path {
                moveTo(p(0.42f, 0.45f).x, p(0.42f, 0.45f).y)
                lineTo(p(0.22f, 0.84f).x, p(0.22f, 0.84f).y)
                lineTo(p(0.78f, 0.84f).x, p(0.78f, 0.84f).y)
                lineTo(p(0.58f, 0.45f).x, p(0.58f, 0.45f).y)
            }
        }
        GardenIcon.CheckGrowth, GardenIcon.Seedling, GardenIcon.Potato -> {
            line(p(0.5f, 0.86f), p(0.5f, 0.36f))
            oval(0.16f, 0.28f, 0.36f, 0.22f)
            oval(0.48f, 0.2f, 0.36f, 0.24f)
        }
        GardenIcon.Harvest -> {
            path {
                moveTo(p(0.22f, 0.4f).x, p(0.22f, 0.4f).y)
                lineTo(p(0.78f, 0.4f).x, p(0.78f, 0.4f).y)
                lineTo(p(0.68f, 0.84f).x, p(0.68f, 0.84f).y)
                lineTo(p(0.32f, 0.84f).x, p(0.32f, 0.84f).y)
                close()
            }
            path { moveTo(p(0.34f, 0.4f).x, p(0.34f, 0.4f).y); cubicTo(p(0.36f, 0.14f).x, p(0.36f, 0.14f).y, p(0.64f, 0.14f).x, p(0.64f, 0.14f).y, p(0.66f, 0.4f).x, p(0.66f, 0.4f).y) }
        }
        GardenIcon.Custom -> {
            line(p(0.26f, 0.76f), p(0.72f, 0.3f))
            line(p(0.62f, 0.2f), p(0.82f, 0.4f))
            line(p(0.2f, 0.82f), p(0.32f, 0.78f))
        }
        GardenIcon.Location -> {
            circle(p(0.5f, 0.42f), 0.1f)
            path { moveTo(p(0.5f, 0.88f).x, p(0.5f, 0.88f).y); cubicTo(p(0.2f, 0.56f).x, p(0.2f, 0.56f).y, p(0.23f, 0.16f).x, p(0.5f, 0.16f).y, p(0.5f, 0.16f).x, p(0.5f, 0.16f).y); cubicTo(p(0.77f, 0.16f).x, p(0.77f, 0.16f).y, p(0.8f, 0.56f).x, p(0.8f, 0.56f).y, p(0.5f, 0.88f).x, p(0.5f, 0.88f).y) }
        }
        GardenIcon.DailyReminder -> {
            circle(p(0.5f, 0.55f), 0.28f)
            line(p(0.5f, 0.55f), p(0.5f, 0.38f))
            line(p(0.5f, 0.55f), p(0.64f, 0.63f))
            line(p(0.28f, 0.2f), p(0.2f, 0.28f))
            line(p(0.72f, 0.2f), p(0.8f, 0.28f))
        }
        GardenIcon.WorkFinish -> {
            path { moveTo(p(0.2f, 0.38f).x, p(0.2f, 0.38f).y); lineTo(p(0.8f, 0.38f).x, p(0.8f, 0.38f).y); lineTo(p(0.8f, 0.78f).x, p(0.8f, 0.78f).y); lineTo(p(0.2f, 0.78f).x, p(0.2f, 0.78f).y); close() }
            line(p(0.38f, 0.38f), p(0.38f, 0.26f)); line(p(0.62f, 0.38f), p(0.62f, 0.26f)); line(p(0.38f, 0.26f), p(0.62f, 0.26f))
        }
        GardenIcon.Notifications -> {
            path { moveTo(p(0.28f, 0.68f).x, p(0.28f, 0.68f).y); cubicTo(p(0.32f, 0.55f).x, p(0.32f, 0.55f).y, p(0.28f, 0.26f).x, p(0.5f, 0.26f).y, p(0.5f, 0.26f).x, p(0.5f, 0.26f).y); cubicTo(p(0.72f, 0.26f).x, p(0.72f, 0.26f).y, p(0.68f, 0.55f).x, p(0.68f, 0.55f).y, p(0.72f, 0.68f).x, p(0.72f, 0.68f).y); lineTo(p(0.28f, 0.68f).x, p(0.28f, 0.68f).y) }
            line(p(0.42f, 0.78f), p(0.58f, 0.78f))
        }
        GardenIcon.HighTemperature -> {
            line(p(0.5f, 0.18f), p(0.5f, 0.62f)); circle(p(0.5f, 0.72f), 0.12f); line(p(0.38f, 0.24f), p(0.5f, 0.24f)); line(p(0.38f, 0.4f), p(0.5f, 0.4f))
        }
        GardenIcon.DeepSeek -> {
            path { moveTo(p(0.26f, 0.32f).x, p(0.26f, 0.32f).y); lineTo(p(0.74f, 0.32f).x, p(0.74f, 0.32f).y); lineTo(p(0.74f, 0.74f).x, p(0.74f, 0.74f).y); lineTo(p(0.26f, 0.74f).x, p(0.26f, 0.74f).y); close() }
            line(p(0.5f, 0.16f), p(0.5f, 0.32f)); line(p(0.38f, 0.2f), p(0.62f, 0.2f)); line(p(0.18f, 0.46f), p(0.26f, 0.46f)); line(p(0.74f, 0.46f), p(0.82f, 0.46f)); circle(p(0.4f, 0.52f), 0.03f); circle(p(0.6f, 0.52f), 0.03f)
        }
        GardenIcon.ExportData -> {
            path { moveTo(p(0.28f, 0.24f).x, p(0.28f, 0.24f).y); lineTo(p(0.72f, 0.24f).x, p(0.72f, 0.24f).y); lineTo(p(0.72f, 0.78f).x, p(0.72f, 0.78f).y); lineTo(p(0.28f, 0.78f).x, p(0.28f, 0.78f).y); close() }
            line(p(0.5f, 0.34f), p(0.5f, 0.62f)); line(p(0.38f, 0.5f), p(0.5f, 0.62f)); line(p(0.62f, 0.5f), p(0.5f, 0.62f))
        }
        GardenIcon.About -> {
            circle(p(0.5f, 0.5f), 0.34f); line(p(0.5f, 0.45f), p(0.5f, 0.68f)); circle(p(0.5f, 0.32f), 0.02f)
        }
        GardenIcon.Sunny -> {
            circle(p(0.5f, 0.5f), 0.18f)
            line(p(0.1f, 0.5f), p(0.22f, 0.5f)); line(p(0.78f, 0.5f), p(0.9f, 0.5f)); line(p(0.5f, 0.1f), p(0.5f, 0.22f)); line(p(0.5f, 0.78f), p(0.5f, 0.9f)); line(p(0.22f, 0.22f), p(0.3f, 0.3f)); line(p(0.78f, 0.22f), p(0.7f, 0.3f)); line(p(0.22f, 0.78f), p(0.3f, 0.7f)); line(p(0.78f, 0.78f), p(0.7f, 0.7f))
        }
        GardenIcon.Cloudy -> cloud(tint, stroke, ::p)
        GardenIcon.PartlyCloudy -> { circle(p(0.34f, 0.36f), 0.14f); cloud(tint, stroke, ::p, yOffset = 0.1f) }
        GardenIcon.Rainy -> { cloud(tint, stroke, ::p); line(p(0.34f, 0.72f), p(0.28f, 0.84f)); line(p(0.5f, 0.72f), p(0.44f, 0.84f)); line(p(0.66f, 0.72f), p(0.6f, 0.84f)) }
        GardenIcon.Night -> path {
            moveTo(p(0.68f, 0.18f).x, p(0.68f, 0.18f).y)
            cubicTo(p(0.34f, 0.22f).x, p(0.34f, 0.22f).y, p(0.24f, 0.56f).x, p(0.46f, 0.76f).y, p(0.46f, 0.76f).x, p(0.46f, 0.76f).y)
            cubicTo(p(0.58f, 0.88f).x, p(0.58f, 0.88f).y, p(0.78f, 0.78f).x, p(0.78f, 0.78f).y, p(0.84f, 0.6f).x, p(0.84f, 0.6f).y)
            cubicTo(p(0.56f, 0.68f).x, p(0.56f, 0.68f).y, p(0.48f, 0.36f).x, p(0.48f, 0.36f).y, p(0.68f, 0.18f).x, p(0.68f, 0.18f).y)
        }
        GardenIcon.Lemon -> {
            oval(0.24f, 0.24f, 0.52f, 0.52f)
            oval(0.56f, 0.12f, 0.22f, 0.16f)
            line(p(0.56f, 0.22f), p(0.46f, 0.32f))
        }
        GardenIcon.Tomato -> { circle(p(0.5f, 0.56f), 0.28f); line(p(0.5f, 0.28f), p(0.5f, 0.16f)); line(p(0.5f, 0.28f), p(0.36f, 0.22f)); line(p(0.5f, 0.28f), p(0.64f, 0.22f)) }
        GardenIcon.Broccoli -> { circle(p(0.38f, 0.36f), 0.12f); circle(p(0.5f, 0.28f), 0.12f); circle(p(0.62f, 0.36f), 0.12f); line(p(0.5f, 0.48f), p(0.5f, 0.84f)); line(p(0.38f, 0.5f), p(0.5f, 0.62f)); line(p(0.62f, 0.5f), p(0.5f, 0.62f)) }
        GardenIcon.BasilMint -> { line(p(0.5f, 0.84f), p(0.5f, 0.18f)); oval(0.2f, 0.34f, 0.3f, 0.18f); oval(0.5f, 0.24f, 0.3f, 0.18f); oval(0.22f, 0.56f, 0.28f, 0.18f) }
        GardenIcon.Carrot -> { path { moveTo(p(0.42f, 0.28f).x, p(0.42f, 0.28f).y); lineTo(p(0.58f, 0.28f).x, p(0.58f, 0.28f).y); lineTo(p(0.5f, 0.86f).x, p(0.5f, 0.86f).y); close() }; line(p(0.5f, 0.28f), p(0.4f, 0.14f)); line(p(0.5f, 0.28f), p(0.6f, 0.14f)) }
        GardenIcon.Strawberry -> { path { moveTo(p(0.5f, 0.84f).x, p(0.5f, 0.84f).y); cubicTo(p(0.2f, 0.58f).x, p(0.2f, 0.58f).y, p(0.3f, 0.26f).x, p(0.3f, 0.26f).y, p(0.5f, 0.34f).x, p(0.5f, 0.34f).y); cubicTo(p(0.7f, 0.26f).x, p(0.7f, 0.26f).y, p(0.8f, 0.58f).x, p(0.8f, 0.58f).y, p(0.5f, 0.84f).x, p(0.5f, 0.84f).y) }; line(p(0.5f, 0.34f), p(0.5f, 0.16f)) }
        GardenIcon.Rose -> { circle(p(0.5f, 0.28f), 0.12f); line(p(0.5f, 0.4f), p(0.5f, 0.86f)); oval(0.22f, 0.5f, 0.28f, 0.16f); oval(0.5f, 0.62f, 0.28f, 0.16f) }
        GardenIcon.Sunflower -> { circle(p(0.5f, 0.36f), 0.12f); repeat(8) { i -> val a = i * 0.7853982f; line(p(0.5f + kotlin.math.cos(a) * 0.2f, 0.36f + kotlin.math.sin(a) * 0.2f), p(0.5f + kotlin.math.cos(a) * 0.3f, 0.36f + kotlin.math.sin(a) * 0.3f)) }; line(p(0.5f, 0.5f), p(0.5f, 0.86f)) }
        GardenIcon.Camera -> { path { moveTo(p(0.18f, 0.36f).x, p(0.18f, 0.36f).y); lineTo(p(0.82f, 0.36f).x, p(0.82f, 0.36f).y); lineTo(p(0.82f, 0.78f).x, p(0.82f, 0.78f).y); lineTo(p(0.18f, 0.78f).x, p(0.18f, 0.78f).y); close() }; circle(p(0.5f, 0.57f), 0.12f); line(p(0.36f, 0.36f), p(0.42f, 0.24f)); line(p(0.42f, 0.24f), p(0.58f, 0.24f)); line(p(0.58f, 0.24f), p(0.64f, 0.36f)) }
        GardenIcon.Keyboard -> { path { moveTo(p(0.16f, 0.3f).x, p(0.16f, 0.3f).y); lineTo(p(0.84f, 0.3f).x, p(0.84f, 0.3f).y); lineTo(p(0.84f, 0.72f).x, p(0.84f, 0.72f).y); lineTo(p(0.16f, 0.72f).x, p(0.16f, 0.72f).y); close() }; line(p(0.28f, 0.44f), p(0.3f, 0.44f)); line(p(0.42f, 0.44f), p(0.44f, 0.44f)); line(p(0.56f, 0.44f), p(0.58f, 0.44f)); line(p(0.3f, 0.6f), p(0.7f, 0.6f)) }
        GardenIcon.Check -> { line(p(0.24f, 0.52f), p(0.42f, 0.7f)); line(p(0.42f, 0.7f), p(0.78f, 0.3f)) }
        GardenIcon.Settings -> { circle(p(0.5f, 0.5f), 0.16f); circle(p(0.5f, 0.5f), 0.34f); line(p(0.5f, 0.1f), p(0.5f, 0.2f)); line(p(0.5f, 0.8f), p(0.5f, 0.9f)); line(p(0.1f, 0.5f), p(0.2f, 0.5f)); line(p(0.8f, 0.5f), p(0.9f, 0.5f)) }
        else -> circle(p(0.5f, 0.5f), 0.24f)
    }
}

private fun DrawScope.cloud(
    color: Color,
    stroke: Stroke,
    p: (Float, Float) -> Offset,
    yOffset: Float = 0f
) {
    val path = Path().apply {
        moveTo(p(0.22f, 0.62f + yOffset).x, p(0.22f, 0.62f + yOffset).y)
        cubicTo(p(0.24f, 0.46f + yOffset).x, p(0.24f, 0.46f + yOffset).y, p(0.38f, 0.46f + yOffset).x, p(0.38f, 0.46f + yOffset).y, p(0.42f, 0.48f + yOffset).x, p(0.42f, 0.48f + yOffset).y)
        cubicTo(p(0.48f, 0.32f + yOffset).x, p(0.48f, 0.32f + yOffset).y, p(0.7f, 0.38f + yOffset).x, p(0.7f, 0.38f + yOffset).y, p(0.72f, 0.54f + yOffset).x, p(0.72f, 0.54f + yOffset).y)
        cubicTo(p(0.88f, 0.56f + yOffset).x, p(0.88f, 0.56f + yOffset).y, p(0.86f, 0.76f + yOffset).x, p(0.86f, 0.76f + yOffset).y, p(0.68f, 0.76f + yOffset).x, p(0.68f, 0.76f + yOffset).y)
        lineTo(p(0.32f, 0.76f + yOffset).x, p(0.32f, 0.76f + yOffset).y)
        cubicTo(p(0.2f, 0.76f + yOffset).x, p(0.2f, 0.76f + yOffset).y, p(0.16f, 0.66f + yOffset).x, p(0.16f, 0.66f + yOffset).y, p(0.22f, 0.62f + yOffset).x, p(0.22f, 0.62f + yOffset).y)
    }
    drawPath(path, color, style = stroke)
}
