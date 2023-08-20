package me.hbj.bikkuri.bili.enums.live

sealed class LiveArea(val id: Long, val name: String) {
  object All : LiveArea(id = 0, name = "全部")

  object Entertainment : LiveArea(id = 1, name = "娱乐")

  object OnlineGame : LiveArea(id = 2, name = "网游")

  object MobileGame : LiveArea(id = 3, name = "手游")

  object Radio : LiveArea(id = 5, name = "电台")

  object IndieGame : LiveArea(id = 6, name = "单机游戏")

  object VTuber : LiveArea(id = 9, name = "虚拟主播")

  object Life : LiveArea(id = 10, name = "生活")

  object Study : LiveArea(id = 11, name = "学习")

  object Event : LiveArea(id = 12, name = "大事件")

  object Match : LiveArea(id = 13, name = "赛事")
}
