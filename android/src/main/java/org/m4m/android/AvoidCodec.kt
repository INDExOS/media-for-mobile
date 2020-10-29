package org.m4m.android

import android.media.MediaCodec
import android.media.MediaCodecList
import jp.studist.teachme_biz.controller.util.LogUtil
import java.io.IOException

/**
 * 一部のコーデックにおいてエンコードまたはデコード、その他の処理が
 * 正常に動作しない事象を確認したため、そのコーデックをブラックリストとして定義。
 * 現在はSamsung製のコーデックで確認したが、他にもベンダー独自のコーデックを仕込んでる可能性があるため
 * enumで列挙する
 */
enum class AvoidCodec(var codecName: String) {
    // Samsung社製のコーデック
    EXYNOS("Exynos");

    companion object {
        fun shouldUseDecoder(mimeType: String): MediaCodec? {

            MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.forEach { codecInfo ->
                codecInfo.supportedTypes.filter {
                    containsShouldAvoidCodec(it)
                }.first {
                    !it.equals(mimeType, ignoreCase = true)
                }.let {
                    try {
                        return MediaCodec.createDecoderByType(it)
                    } catch (e: IOException) {
                        LogUtil.stackTrace(e)
                        e.printStackTrace()
                    }
                }
            }

            return null
        }

        fun shouldUseEncoder(mimeType: String): MediaCodec? {

            MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.forEach { codecInfo ->
                codecInfo.supportedTypes.filter {
                    containsShouldAvoidCodec(it)
                }.first {
                    !it.equals(mimeType, ignoreCase = true)
                }.let {
                    try {
                        return MediaCodec.createEncoderByType(it)
                    } catch (e: IOException) {
                        LogUtil.stackTrace(e)
                        e.printStackTrace()
                    }
                }
            }

            return null
        }

        private fun containsShouldAvoidCodec(codecType: String): Boolean {
            values().forEach { avoidCodec ->
                if (codecType.contains(avoidCodec.codecName, ignoreCase = true)) {
                    return true
                }
            }

            return false
        }
    }

}