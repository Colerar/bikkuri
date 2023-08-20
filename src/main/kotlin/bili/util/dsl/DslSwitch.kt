package me.hbj.bikkuri.bili.util.dsl

interface DslSwitchStatus

enum class DslSwitch2Status : DslSwitchStatus { FIRST, SECOND; }

enum class DslSwitch3Status : DslSwitchStatus { FIRST, SECOND, THIRD; }

interface DslSwitch

abstract class DslSwitch2 : DslSwitch {
  infix fun set(status: DslSwitch2Status): Unit = when (status) {
    DslSwitch2Status.FIRST -> slot1()
    DslSwitch2Status.SECOND -> slot2()
  }

  internal abstract fun slot1()

  internal abstract fun slot2()
}

abstract class DslSwitch3 : DslSwitch2() {
  infix fun set(status: DslSwitch3Status): Unit = when (status) {
    DslSwitch3Status.FIRST -> slot1()
    DslSwitch3Status.SECOND -> slot2()
    DslSwitch3Status.THIRD -> slot3()
  }

  internal abstract fun slot3()
}
