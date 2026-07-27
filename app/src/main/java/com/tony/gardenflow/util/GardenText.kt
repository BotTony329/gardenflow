package com.tony.gardenflow.util

import java.util.Locale

object GardenText {
    private var overrideLanguageCode: String? = null

    val isZh: Boolean
        get() = when (overrideLanguageCode) {
            "zh" -> true
            "en" -> false
            else -> Locale.getDefault().language.startsWith("zh")
        }

    fun setLanguage(code: String?) {
        overrideLanguageCode = code?.takeIf { it == "zh" || it == "en" }
    }

    fun s(en: String, zh: String): String = if (isZh) zh else en

    val back get() = s("Back", "返回")
    val settings get() = s("Settings", "设置")
    val taskReminder get() = s("Task reminder", "任务提醒")
    val taskReminderTime get() = s("Reminder check time", "提醒检查时间")
    val taskReminderHint get() = s("GardenFlow only notifies when a plant has a task due.", "只有植物有到期任务时才会通知。")
    val addPlant get() = s("Add a plant", "添加植物")
    val yourPlants get() = s("Your Plants", "我的植物")
    val water get() = s("Water", "浇水")
    val fertilise get() = s("Fertilise", "施肥")
    val today get() = s("Today", "今天")
    val yesterday get() = s("Yesterday", "昨天")
    val threeDaysAgo get() = s("3 days ago", "3 天前")
    val notPlantedYet get() = s("Not planted yet", "还没种下")
    val notRecorded get() = s("Not recorded", "未记录")
    val notEnoughData get() = s("Not enough data yet", "信息还不够")
    val chooseDate get() = s("Choose date", "选择日期")
    val select get() = s("Select", "选择")
    val cancel get() = s("Cancel", "取消")
    val plantName get() = s("Plant name", "植物名称")
    val createCarePlan get() = s("Create care plan", "生成护理计划")
    val addToGarden get() = s("Add to my garden", "加入我的花园")
    val regenerate get() = s("Regenerate", "重新生成")
    val careHistory get() = s("Care History", "护理记录")

    fun greeting(hour: Int): String = when (hour) {
        in 5..11 -> s("Good morning", "早上好")
        in 12..16 -> s("Good afternoon", "下午好")
        in 17..21 -> s("Good evening", "晚上好")
        else -> s("Good night", "夜深了")
    }

    fun waterIn(days: Long): String = when {
        days > 1 -> s("Water in $days days", "$days 天后浇水")
        days == 1L -> s("Water tomorrow", "明天浇水")
        days == 0L -> s("Water today", "今天需要浇水")
        days == -1L -> s("Overdue by 1 day", "已逾期 1 天")
        else -> s("Overdue by ${-days} days", "已逾期 ${-days} 天")
    }

    fun day(day: Long): String = s("Day $day", "第 $day 天")
    fun estimatedGrowth(): String = s("Estimated growth", "预计生长")
    fun stageSummary(stage: String, day: Long): String = s("$stage - Day $day", "$stage - 第 $day 天")
    fun everyDays(action: String, days: Int): String = s("$action every $days days", "每 $days 天$action")
    fun dueToday(label: String): String = s("$label due today", "$label 今天到期")
}
