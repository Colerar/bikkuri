package me.hbj.bikkuri.bili.data.message

enum class SessionType(val code: Int?) {
  UNKNOWN(null),

  NORMAL(1),

  FANS(3),

  FOLDED(5),
}
