//package viewer
//
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.compose.ui.graphics.asImageBitmap
//import java.awt.image.BufferedImage
//import java.io.File
//import java.io.InputStream
//import javax.imageio.ImageIO
//import org.apache.batik.transcoder.image.PNGTranscoder
//import org.apache.batik.transcoder.TranscoderInput
//import java.awt.RenderingHints
//import java.awt.Graphics2D
//
//fun loadMapImage(resourcePath: String, targetWidth: Int, targetHeight: Int): ImageBitmap {
//    return if (resourcePath.endsWith(".svg", ignoreCase = true)) {
//        loadSvgAsBitmap(resourcePath, targetWidth, targetHeight)
//    } else {
//        val stream: InputStream = object {}.javaClass.classLoader.getResourceAsStream(resourcePath)
//            ?: error("Cannot load image: $resourcePath")
//        val buffered = ImageIO.read(stream)
//        resizeImage(buffered, targetWidth, targetHeight).asImageBitmap()
//    }
//}
//
//fun loadSvgAsBitmap(path: String, width: Int, height: Int): ImageBitmap {
//    val input = object {}.javaClass.classLoader.getResourceAsStream(path)
//        ?: error("Cannot load SVG: $path")
//
//    val transcoder = PNGTranscoder().apply {
//        addTranscodingHint(PNGTranscoder.KEY_WIDTH, width.toFloat())
//        addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height.toFloat())
//    }
//
//    val outputStream = java.io.ByteArrayOutputStream()
//    transcoder.transcode(TranscoderInput(input), org.apache.batik.transcoder.TranscoderOutput(outputStream))
//    val imageBytes = outputStream.toByteArray()
//    val buffered = ImageIO.read(imageBytes.inputStream()) ?: error("Failed to decode SVG as PNG")
//    return buffered.asImageBitmap()
//}
//
//fun resizeImage(img: BufferedImage, width: Int, height: Int): BufferedImage {
//    val scaled = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
//    val g2 = scaled.createGraphics()
//    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
//    g2.drawImage(img, 0, 0, width, height, null)
//    g2.dispose()
//    return scaled
//}
