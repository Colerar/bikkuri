
import me.hbj.bikkuri.config.TEST_DIR
import me.hbj.bikkuri.setupTerminal
import java.io.File

suspend fun main() {
  setupTerminal(File(TEST_DIR))
}
