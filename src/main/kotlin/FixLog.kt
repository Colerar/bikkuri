package me.hbj.bikkuri

import java.io.OutputStream
import java.io.PrintStream
import java.util.*

fun fixLog() {
  val origStd = System.out
  System.setOut(object : PrintStream(OutputStream.nullOutputStream()) {
    @Suppress("NOTHING_TO_INLINE")
    private inline fun println0(x: Any? = null) {
      val stackTrace = Exception().stackTrace
      if (stackTrace[1]?.className?.startsWith("com.github.unidbg") == true) {
        return
      }
      if (stackTrace[1]?.className?.startsWith("moe.fuqiuluo.unidbg.env.QSecJni") == true) {
        return
      }
      origStd.println(x)
    }

    override fun println(x: Any?) = println0(x)

    override fun println(x: Boolean) = println0(x)

    override fun println(x: Char) = println0(x)

    override fun println(x: CharArray) = println0(x.contentToString())

    override fun println(x: Double) = println0(x)

    override fun println(x: Float) = println0(x)

    override fun println(x: Int) = println0(x)

    override fun println(x: Long) = println0(x)

    override fun println(x: String?) = println0(x)

    override fun printf(format: String, vararg args: Any?): PrintStream {
      println0(String.format(format, args = args))
      return this
    }

    override fun format(format: String, vararg args: Any?): PrintStream {
      String.format(format, args = args)
      return this
    }

    override fun format(l: Locale?, format: String, vararg args: Any?): PrintStream {
      println0(String.format(l, format, args = args))
      return this
    }

    override fun printf(l: Locale?, format: String, vararg args: Any?): PrintStream {
      return super.printf(l, format, *args)
    }

    override fun println() = println0()

    override fun append(csq: CharSequence?): PrintStream {
      println0(csq)
      return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): PrintStream {
      println0(csq)
      return this
    }

    override fun writeBytes(buf: ByteArray) = origStd.write(buf)

    override fun write(buf: ByteArray) = origStd.write(buf)

    override fun write(buf: ByteArray, off: Int, len: Int) = origStd.write(buf, off, len)

    override fun print(x: Any?) = origStd.print(x)

    override fun print(x: Boolean) = origStd.print(x)

    override fun print(x: Char) = origStd.print(x)

    override fun print(x: CharArray) = origStd.print(x.joinToString())

    override fun print(x: Double) = origStd.print(x)

    override fun print(x: Float) = origStd.print(x)

    override fun print(x: Int) = origStd.print(x)

    override fun print(x: Long) = origStd.print(x)

    override fun print(x: String?) = origStd.print(x)

    override fun append(c: Char): PrintStream {
      origStd.print(c)
      return this
    }
  })

  System.setProperty(
    "org.apache.commons.logging.Log",
    "org.apache.commons.logging.impl.NoOpLog",
  )
  listOf(
    "com.github.unidbg.linux.ARM64SyscallHandler",
    "com.github.unidbg.linux.AndroidSyscallHandler",
  ).forEach {
    java.util.logging.Logger.getLogger(it).setLevel(java.util.logging.Level.OFF)
  }
}
