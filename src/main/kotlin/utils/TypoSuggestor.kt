package me.hbj.bikkuri.utils

import moe.sdl.yac.core.jaroWinklerSimilarity

@Suppress("FLOAT_IN_ACCURATE_CALCULATIONS")
fun suggestTypo(input: String, possibleValues: Collection<String>) =
  possibleValues.asSequence()
    .filter { input.first() == it.first() }
    .map { it to jaroWinklerSimilarity(input, it) }
    .filter { it.second > 0.8 }
    .sortedByDescending { it.second }
    .map { it.first }
    .firstOrNull()
