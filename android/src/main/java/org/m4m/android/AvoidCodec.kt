package org.m4m.android

import android.media.MediaCodec
import android.media.MediaCodecList
import jp.studist.teachme_biz.controller.util.LogUtil
import java.io.IOException

/**
 * 一部のコーデックにおいてエンコードまたはデコード、その他の処理が
 * 正常に動作しない事象を確認したため、そのコーデックをブラックリストとして定義。
 * 現在はSamsung製のコーデックで確認したが、他にもベンダー独自のコーデックを仕込んでる可能性があるため
 * 逐次リストに定義する
 */
class AvoidCodec {

    companion object {

        /**
         * ブラックリストとして選択しないコーデック名の定義
         * ユニークに判断可能であれば部分的な名前で定義可能
         */
        private val blackListCodec = listOf(
                // Samsung社製のコーデック
                "Exynos"
        )

        @JvmStatic
        fun createDecoderWhileAvoidingBlackListCodec(mimeType: String): MediaCodec? {

            MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.filter { codecInfo ->
                codecInfo.supportedTypes.contains(mimeType)
            }.first { codecInfo ->
                !isMatchingBlackList(codecInfo.name)
            }.let { codecInfo ->
                try {
                    return MediaCodec.createByCodecName(codecInfo.name)
                } catch (e: IOException) {
                    LogUtil.stackTrace(e)
                    e.printStackTrace()
                }
            }

            return null
        }

        @JvmStatic
        fun createEncoderWhileAvoidingBlackList(mimeType: String): MediaCodec? {

            MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.filter { codecInfo ->
                codecInfo.supportedTypes.contains(mimeType) and codecInfo.isEncoder
            }.first { codecInfo ->
                !isMatchingBlackList(codecInfo.name)
            }.let { codecInfo ->
                try {
                    return MediaCodec.createByCodecName(codecInfo.name)
                } catch (e: IOException) {
                    LogUtil.stackTrace(e)
                    e.printStackTrace()
                }
            }

            return null
        }

        private fun isMatchingBlackList(codecName: String): Boolean {
            blackListCodec.forEach { targetCodec ->
                if (codecName.contains(targetCodec, ignoreCase = true)) {
                    return true
                }
            }

            return false
        }
    }

}
