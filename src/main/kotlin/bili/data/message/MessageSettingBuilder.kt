package me.hbj.bikkuri.bili.data.message

import me.hbj.bikkuri.bili.data.message.MessageSetting.*
import me.hbj.bikkuri.bili.util.dsl.DslSwitch2
import me.hbj.bikkuri.bili.util.dsl.DslSwitch2Status
import me.hbj.bikkuri.bili.util.dsl.DslSwitch3
import me.hbj.bikkuri.bili.util.dsl.DslSwitch3Status

@Suppress("ClassName")
private sealed interface MessageSetting {
  val key: String
  val code: Int

  enum class _Notify(override val code: Int) : MessageSetting {
    ON(1), OFF(3);

    override val key: String = "msg_notify"
  }

  enum class _Intercept(override val code: Int) : MessageSetting {
    ON(1), OFF(0);

    override val key: String = "ai_intercept"
  }

  enum class _Comment(override val code: Int) : MessageSetting {
    ON(0), ONLY_FOLLOW(1), OFF(2);

    override val key: String = "set_comment"
  }

  enum class _At(override val code: Int) : MessageSetting {
    ON(0), ONLY_FOLLOW(1), OFF(2);

    override val key: String = "set_at"
  }

  enum class _Like(override val code: Int) : MessageSetting {
    ON(1), OFF(5);

    override val key: String = "set_like"
  }

  enum class _FoldUnfollowed(override val code: Int) : MessageSetting {
    ON(1), OFF(0);

    override val key: String = "show_unfollowed_msg"
  }
}

/**
 * DSL 消息提醒设置
 *
 * 可配置项首字母大写 可用选项小写开头
 *
 * 用法:
 * ```kotlin
 * // this: MessageSettingBuilder
 * Notify set on
 * Intercept set off
 * At set followed
 * ```
 *
 * 如果多次设置一个元素，那么将只有最后调用的有效。
 *
 */
@Suppress("PropertyName")
class MessageSettingBuilder {
  private val list: MutableList<MessageSetting> = mutableListOf()

  internal fun build(): List<Pair<String, Int>> = list
    .reversed().distinctBy { it::class }
    .map { it.key to it.code }

  inline val on: DslSwitch2Status
    get() = DslSwitch2Status.FIRST
  inline val off: DslSwitch2Status
    get() = DslSwitch2Status.SECOND
  inline val followed: DslSwitch3Status
    get() = DslSwitch3Status.THIRD

  val Notify: DslSwitch2 = object : DslSwitch2() {
    override fun slot1() {
      list.add(_Notify.ON)
    }

    override fun slot2() {
      list.add(_Notify.OFF)
    }
  }

  val Intercept: DslSwitch2 = object : DslSwitch2() {
    override fun slot1() {
      list.add(_Intercept.ON)
    }

    override fun slot2() {
      list.add(_Intercept.OFF)
    }
  }

  val Comment: DslSwitch3 = object : DslSwitch3() {
    override fun slot1() {
      list.add(_Comment.ON)
    }

    override fun slot2() {
      list.add(_Comment.OFF)
    }

    override fun slot3() {
      list.add(_Comment.ONLY_FOLLOW)
    }
  }

  val At: DslSwitch3 = object : DslSwitch3() {
    override fun slot1() {
      list.add(_At.ON)
    }

    override fun slot2() {
      list.add(_At.OFF)
    }

    override fun slot3() {
      list.add(_At.ONLY_FOLLOW)
    }
  }

  val Like: DslSwitch2 = object : DslSwitch2() {
    override fun slot1() {
      list.add(_Like.ON)
    }
    override fun slot2() {
      list.add(_Like.OFF)
    }
  }

  val FoldUnfollowed: DslSwitch2 = object : DslSwitch2() {
    override fun slot1() {
      list.add(_FoldUnfollowed.ON)
    }

    override fun slot2() {
      list.add(_FoldUnfollowed.OFF)
    }
  }
}
