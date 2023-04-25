package me.hbj.bikkuri.utils

import kotlin.reflect.KClass

val KClass<*>.qualifiedOrSimple
  get() = this.qualifiedName ?: this.simpleName
