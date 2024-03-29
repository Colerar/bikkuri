package me.hbj.bikkuri.persist

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import java.io.File

/**
 * Local file data persist storage
 * @property data must be annotated with @[Serializable]
 * @property file where you store the data
 * @property scope coroutine will launch from this scope,
 * keep it same as caller side to make sure the structured concurrency has been followed
 */
interface FilePersist<T : Any> {

  val data: T?

  val file: File

  val scope: CoroutineScope

  val format: StringFormat

  /**
   * Initialize file persist
   * Must be called at program start
   */
  suspend fun init()

  /**
   * Load data from disk storage,
   * assign value to [data], then return it
   */
  suspend fun load(): T

  /**
   * Save data to disk
   *
   * Should be invoked after change, or data will be lost
   *
   * Some subclasses can automatically save data, for example [AutoSaveFilePersist]
   */
  suspend fun save(saveData: T = data!!)
}
