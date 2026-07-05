package com.example.ui.camera

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.EnumMap

class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
        hints[DecodeHintType.TRY_HARDER] = true
        hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.CODE_128,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.ITF,
            BarcodeFormat.CODABAR
        )
        setHints(hints)
    }

    private var lastScannedBarcode: String? = null
    private var lastScanTime: Long = 0L

    override fun analyze(image: ImageProxy) {
        if (image.format == ImageFormat.YUV_420_888 || image.format == ImageFormat.YUV_422_888 || image.format == ImageFormat.YUV_444_888) {
            val buffer = image.planes[0].buffer
            val rowStride = image.planes[0].rowStride
            val width = image.width
            val height = image.height

            // Copy Y plane row-by-row to completely remove any padding/stride bytes.
            // This is critical because rowStride can be larger than width, causing skewed images in PlanarYUVLuminanceSource.
            val data = ByteArray(width * height)
            for (i in 0 until height) {
                buffer.position(i * rowStride)
                val bytesToRead = minOf(width, buffer.remaining())
                if (bytesToRead > 0) {
                    buffer.get(data, i * width, bytesToRead)
                }
            }

            val source = PlanarYUVLuminanceSource(
                data,
                width,
                height,
                0,
                0,
                width,
                height,
                false
            )

            val bitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = reader.decode(bitmap)
                val barcode = result.text
                val currentTime = System.currentTimeMillis()
                // Avoid spamming the callback with the same barcode in rapid succession
                if (barcode != lastScannedBarcode || currentTime - lastScanTime > 1500) {
                    lastScannedBarcode = barcode
                    lastScanTime = currentTime
                    onBarcodeScanned(barcode)
                }
            } catch (e: NotFoundException) {
                // No barcode found
            } finally {
                image.close()
            }
        } else {
            image.close()
        }
    }
}
