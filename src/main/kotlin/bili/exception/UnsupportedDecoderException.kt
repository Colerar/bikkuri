package me.hbj.bikkuri.bili.exception

import kotlinx.serialization.encoding.Decoder

class UnsupportedDecoderException(decoder: Decoder) : IllegalArgumentException("Unsupported decoder $decoder")
