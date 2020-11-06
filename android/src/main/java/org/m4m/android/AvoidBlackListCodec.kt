package org.m4m.android

import android.media.MediaCodec
import android.media.MediaCodecList
import java.io.IOException
import kotlin.jvm.Throws

/**
 * 一部のコーデックにおいてエンコードまたはデコード、その他の処理が
 * 正常に動作しない事象を確認したため、そのコーデックをブラックリストとして定義。
 * 現在はSamsung製のコーデックで確認したが、他にもベンダー独自のコーデックを仕込んでる可能性があるため
 * 逐次リストに定義する
 */
class AvoidBlackListCodec {

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
        @Throws(IOException::class)
        fun createDecoder(mimeType: String): MediaCodec {
            return MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.filter { codecInfo ->
                codecInfo.supportedTypes.contains(mimeType) and !codecInfo.isEncoder
            }.first { codecInfo ->
                !isMatchingBlackList(codecInfo.name)
            }.let { codecInfo ->
                MediaCodec.createByCodecName(codecInfo.name)
            }
        }

        @JvmStatic
        @Throws(IOException::class)
        fun createEncoder(mimeType: String): MediaCodec {
            return MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.filter { codecInfo ->
                codecInfo.supportedTypes.contains(mimeType) and codecInfo.isEncoder
            }.first { codecInfo ->
                !isMatchingBlackList(codecInfo.name)
            }.let { codecInfo ->
                MediaCodec.createByCodecName(codecInfo.name)
            }
        }

        @JvmStatic
        fun hasBlackListEncoder(mimeType: String): Boolean {
            return MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.filter { codecInfo ->
                codecInfo.supportedTypes.contains(mimeType) and codecInfo.isEncoder
            }.any { codecInfo ->
                isMatchingBlackList(codecInfo.name)
            }
        }

        @JvmStatic
        fun hasBlackListDecoder(mimeType: String): Boolean {
            return MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.filter { codecInfo ->
                codecInfo.supportedTypes.contains(mimeType) and !codecInfo.isEncoder
            }.any { codecInfo ->
                isMatchingBlackList(codecInfo.name)
            }
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
