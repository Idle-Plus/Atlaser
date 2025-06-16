package dev.uraxys.idleclient.atlaser.generator

import dev.uraxys.idleclient.atlaser.generator.atlas.AtlasTexture
import dev.uraxys.idleclient.atlaser.generator.atlas.AtlasTexturePart
import dev.uraxys.idleclient.atlaser.generator.image.ImageOperation
import dev.uraxys.idleclient.atlaser.generator.image.ImageSizeComparator
import java.awt.image.BufferedImage
import java.io.File
import java.util.TreeSet
import javax.imageio.ImageIO

class AtlasGenerator(
	/**
	 * The size of the atlas.
	 */
	private val size: Int = 2048,
	/**
	 * The scale.
	 */
	private val scale: Int = 1,
	/**
	 * If true, then the atlas will be saved as WebP.
	 */
	private val useWebP: Boolean = false,
	/**
	 * The image operation mode. See [ImageOperation] for more details.
	 */
	private val operation: List<ImageOperation>,
	/**
	 * The size of the sub images to generate.
	 */
	private val subImages: List<Int>,
	/**
	 * The padding between the images.
	 */
	private val padding: Int = 0,

	/**
	 * The source directory.
	 */
	private val source: File,
	/**
	 * The output directory.
	 */
	private val output: File,

	/**
	 * The name of the sheets.
	 */
	private val sheetName: String,
) {

	private val atlases: MutableList<AtlasTexture> = mutableListOf()
	private val images: TreeSet<AtlasTexturePart> = TreeSet(ImageSizeComparator())

	fun add(path: String, id: String): AtlasGenerator {
		val file = File(this.source, path)
		if (!file.exists()) throw IllegalArgumentException("File does not exist: $file")
		this.add(ImageIO.read(file), id)
		return this
	}

	fun add(image: BufferedImage, id: String): AtlasGenerator {
		// Scale the image if needed.
		var result = image
		if (this.scale > 1) {
			val newImage =
				BufferedImage(image.width * this.scale, image.height * this.scale, BufferedImage.TYPE_INT_ARGB)
			val graphics = newImage.createGraphics()
			graphics.drawImage(image, 0, 0, newImage.width, newImage.height, null)
			graphics.dispose()
			result = newImage
		}

		// Do the image operations and add it to the images.
		this.operation.forEach { result = it.function(result) }
		this.images.add(AtlasTexturePart(result, id))

		// Create sub images if we have any.
		this.subImages.forEach { subSize ->
			if (subSize <= 0) return@forEach
			if (subSize >= result.width || subSize >= result.height)
				throw IllegalArgumentException("Sub image size can't be larger than original size: $subSize")

			val generated = ImageOperation.RESIZE_TO(result, subSize)
			this.images.add(AtlasTexturePart(generated, "${id}_$subSize"))
		}

		return this
	}

	fun build(): List<AtlasTexture> {
		// Validate all images.
		this.images.forEach {
			if (it.image.width > this.size || it.image.height > this.size)
				throw IllegalArgumentException("Image is too large: ${it.id}")
		}

		// Create the atlas.
		this.images.forEach {
			var added = false
			for (atlas in this.atlases) {
				if (atlas.addTexture(it, this.padding)) {
					added = true
					break
				}
			}

			if (!added) {
				val atlas = AtlasTexture(this.size, this.size, "${this.sheetName}_${this.atlases.size}")
				if (!atlas.addTexture(it, this.padding))
					throw IllegalArgumentException("Failed to add image to atlas: ${it.id}")
				this.atlases.add(atlas)
			}
		}

		// Save the atlases.
		this.atlases.forEach { atlas -> atlas.save(this.output, this.useWebP) }
		return this.atlases
	}
}