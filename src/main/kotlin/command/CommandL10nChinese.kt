package me.hbj.bikkuri.command

import moe.sdl.yac.output.Localization

object CommandL10nChinese : Localization {
  override fun usageError(message: String) = "错误: $message"

  override fun badParameter() = "参数无效"

  override fun badParameterWithMessage(message: String) = "参数无效: $message"

  override fun badParameterWithParam(paramName: String) = "\"$paramName\" 的参数无效"

  override fun badParameterWithMessageAndParam(paramName: String, message: String) =
    "\"$paramName\" 的参数无效: $message"

  override fun missingOption(paramName: String) = "缺少选项 \"$paramName\""

  override fun missingArgument(paramName: String) = "缺少参数 \"$paramName\""

  override fun noSuchSubcommand(name: String, possibilities: List<String>): String {
    return "无子命令: \"$name\"" + when (possibilities.size) {
      0 -> ""
      1 -> ". 是否想输入 \"${possibilities[0]}\"?"
      else -> possibilities.joinToString(prefix = ". (可能的子命令: ", postfix = ")")
    }
  }

  override fun noSuchOption(name: String, possibilities: List<String>): String {
    return "无选项: \"$name\"" + when (possibilities.size) {
      0 -> ""
      1 -> ". 是否想输入 \"${possibilities[0]}\"?"
      else -> possibilities.joinToString(prefix = ". (可能的选项: ", postfix = ")")
    }
  }

  override fun incorrectOptionValueCount(name: String, count: Int): String {
    return when (count) {
      0 -> "选项 $name 不需要值"
      1 -> "选项 $name 需要一个参数"
      else -> "选项 $name 需要 $count 个参数"
    }
  }

  override fun incorrectArgumentValueCount(name: String, count: Int): String {
    return when (count) {
      0 -> "参数 $name 不需要值"
      1 -> "参数 $name 需要一个值"
      else -> "参数 $name 需要 $count 个值"
    }
  }

  override fun mutexGroupException(name: String, others: List<String>): String {
    return "选项 $name 不可用于 ${others.joinToString(" 或 ")}"
  }

  override fun fileNotFound(filename: String) = "找不到文件 $filename"

  override fun invalidFileFormat(filename: String, message: String) = "文件的格式错误 $filename: $message"

  override fun invalidFileFormat(filename: String, lineNumber: Int, message: String) =
    "文件格式错误 $filename, 第 $lineNumber 行: $message"

  override fun unclosedQuote() = "引号错误"

  override fun fileEndsWithSlash() = "文件以 \\ 结尾"

  override fun extraArgumentOne(name: String) = "输入了多余的参数 $name, 想输入带空格的关键词请使用 \" 号括起来"

  override fun extraArgumentMany(name: String, count: Int) =
    "输入了多余的参数 $name, 想输入带空格的关键词请使用 \" 号括起来"

  override fun invalidFlagValueInFile(name: String) = "Invalid flag value in file for option $name"

  override fun switchOptionEnvvar() = "environment variables not supported for switch options"

  override fun requiredMutexOption(options: String) = "必须提供这些选项中的一个 $options"

  override fun invalidGroupChoice(value: String, choices: List<String>): String =
    "无效的选择: $value. (选择列表 ${choices.joinToString()})"

  override fun floatConversionError(value: String) = "$value 不是一个有效的浮点数"

  override fun intConversionError(value: String) = "$value 不是一个有效的整数"

  override fun boolConversionError(value: String) = "$value 不是一个有效的布尔值"

  override fun rangeExceededMax(value: String, limit: String) = "$value 超过了最大限制 $limit."

  override fun rangeExceededMin(value: String, limit: String) = "$value 少于最少限制 $limit."

  override fun rangeExceededBoth(value: String, min: String, max: String) = "$value 不在允许的范围 $min 到 $max 内."

  override fun invalidChoice(choice: String, choices: List<String>): String {
    return "无效选择: $choice. (可用列表： ${choices.joinToString()})"
  }

  override fun pathTypeFile() = "文件"

  override fun pathTypeDirectory() = "文件夹"

  override fun pathTypeOther() = "路径"

  override fun pathDoesNotExist(pathType: String, path: String) = "$pathType \"$path\" 不存在."

  override fun pathIsFile(pathType: String, path: String) = "$pathType \"$path\" 是文件."

  override fun pathIsDirectory(pathType: String, path: String) = "$pathType \"$path\" 是文件夹."

  override fun pathIsNotWritable(pathType: String, path: String) = "$pathType \"$path\" 不可写."

  override fun pathIsNotReadable(pathType: String, path: String) = "$pathType \"$path\" 不可读."

  override fun pathIsSymlink(pathType: String, path: String) = "$pathType \"$path\" 是软链接."

  override fun defaultMetavar() = "值"

  override fun stringMetavar() = "文本"

  override fun floatMetavar() = "浮点数"

  override fun intMetavar() = "整数"

  override fun pathMetavar() = "路径"

  override fun fileMetavar() = "文件"

  override fun usageTitle(): String = "用法:"

  override fun optionsTitle(): String = "选项:"

  override fun argumentsTitle(): String = "参数:"

  override fun commandsTitle(): String = "子命令:"

  override fun optionsMetavar(): String = "[选项]"

  override fun commandMetavar(): String = "命令 [参数]..."

  override fun helpTagDefault(): String = "默认"

  override fun helpTagRequired(): String = "必须"

  override fun helpOptionMessage(): String = "显示帮助信息"
}
