package me.hbj.bikkuri.bili.data.message.contents

import kotlinx.serialization.json.Json

sealed interface RecvContent

abstract class ContentFactory<T : RecvContent> {
  abstract val code: Int
  abstract fun decode(json: Json, data: String): T

  companion object {
    private val factories by lazy {
      listOf<ContentFactory<*>>(
        Text,
        Pop,
      )
    }

    val map: Map<Int, ContentFactory<*>> by lazy {
      factories.associateBy { it.code }
    }
  }
}
