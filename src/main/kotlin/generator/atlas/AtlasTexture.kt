package dev.uraxys.idleclient.atlaser.generator.atlas

import com.luciad.imageio.webp.WebPWriteParam
import dev.uraxys.idleclient.atlaser.Utils
import dev.uraxys.idleclient.atlaser.generator.image.ImageNode
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

class AtlasTexture(
	val width: Int,
	val height: Int,
	val name: String,
) {

	private val image: BufferedImage = BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB)
	private val graphics: Graphics2D = this.image.createGraphics()
	private val node: ImageNode = ImageNode(0, 0, this.width, this.height)

	val sources: MutableMap<String, Rectangle> = mutableMapOf()

	fun addTexture(texture: AtlasTexturePart, padding: Int): Boolean {
		val node = this.node.insert(texture.image, padding) ?: return false
		this.graphics.drawImage(texture.image, null, node.rect.x, node.rect.y)
		this.sources[texture.id] = node.rect
		return true
	}

	fun save(directory: File, useWebP: Boolean, type: Int = WebPWriteParam.LOSSLESS_COMPRESSION, quality: Float = 1f) {
		try {
			val extension = if (useWebP) "webp" else "png"
			val file = File(directory, "${this.name}.$extension")

			if (useWebP) {
				val writer = ImageIO.getImageWritersByFormatName("webp").next()
				val ios = ImageIO.createImageOutputStream(file)
				writer.output = ios

				val writeParam = writer.defaultWriteParam
				if (writeParam.canWriteCompressed()) {
					writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
					writeParam.compressionType = writeParam.compressionTypes[type]
					writeParam.compressionQuality = quality.coerceIn(0.0f, 1.0f)
				}

				writer.write(null, IIOImage(this.image, null, null), writeParam)
				ios.close()
				writer.dispose()
			} else {
				ImageIO.write(this.image, "png", file)
			}

			Utils.info("Atlas saved to: ${file.absolutePath}, size: ${this.width}x${this.height}")
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}
}