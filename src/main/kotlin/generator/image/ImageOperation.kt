package dev.uraxys.idleclient.atlaser.generator.image

import com.mortennobel.imagescaling.AdvancedResizeOp
import com.mortennobel.imagescaling.ResampleFilters
import com.mortennobel.imagescaling.ResampleOp
import java.awt.Point
import java.awt.RenderingHints
import java.awt.image.BufferedImage

private fun resize(source: BufferedImage, targetSize: Int): BufferedImage {
	val resampleOp = ResampleOp(targetSize, targetSize)
	resampleOp.filter = ResampleFilters.getLanczos3Filter()
	resampleOp.unsharpenMask = AdvancedResizeOp.UnsharpenMask.Soft

	val destination = BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB)
	return resampleOp.filter(source, destination)
}

private fun padToSquare(source: BufferedImage): BufferedImage {
	val width = source.width
	val height = source.height
	if (width == height) return source

	val size = maxOf(width, height)
	val result = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
	val g = result.createGraphics()

	g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
	g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

	val x = (size - width) / 2
	val y = (size - height) / 2
	g.drawImage(source, x, y, null)
	g.dispose()

	return result
}

private fun trimAndPadToSquare(image: BufferedImage, square: Boolean): BufferedImage {
	val width = image.width
	val height = image.height

	var minX = width
	var minY = height
	var maxX = 0
	var maxY = 0

	for (y in 0 until height) {
		for (x in 0 until width) {
			val alpha = (image.getRGB(x, y) shr 24) and 0xff
			if (alpha != 0) {
				if (x < minX) minX = x
				if (y < minY) minY = y
				if (x > maxX) maxX = x
				if (y > maxY) maxY = y
			}
		}
	}

	// Fully transparent image
	if (maxX < minX || maxY < minY) {
		return BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
	}

	if (!square) {
		val croppedWidth = maxX - minX + 1
		val croppedHeight = maxY - minY + 1

		val cropped = BufferedImage(croppedWidth, croppedHeight, BufferedImage.TYPE_INT_ARGB)
		val g = cropped.createGraphics()
		g.drawImage(image, 0, 0, croppedWidth, croppedHeight, minX, minY, maxX + 1, maxY + 1, null)
		g.dispose()

		return cropped
	}

	val croppedWidth = maxX - minX + 1
	val croppedHeight = maxY - minY + 1
	val squareSize = maxOf(croppedWidth, croppedHeight)

	val square = BufferedImage(squareSize, squareSize, BufferedImage.TYPE_INT_ARGB)
	val g = square.createGraphics()

	// Center the trimmed image in the square canvas
	val offsetX = (squareSize - croppedWidth) / 2
	val offsetY = (squareSize - croppedHeight) / 2

	g.drawImage(
		image,
		offsetX, offsetY, offsetX + croppedWidth, offsetY + croppedHeight,
		minX, minY, maxX + 1, maxY + 1,
		null
	)

	g.dispose()
	return square
}

enum class ImageOperation(val function: (BufferedImage) -> BufferedImage) {
	NONE({ it }),

	RESIZE_16({ resize(it, 16) }),
	RESIZE_32({ resize(it, 32) }),
	RESIZE_48({ resize(it, 48) }),
	RESIZE_64({ resize(it, 64) }),
	RESIZE_128({ resize(it, 128) }),

	PAD_TO_SQUARE({ padToSquare(it) }),
	SQUARE_TRIM({ trimAndPadToSquare(it, true) }),
	TRIM({ trimAndPadToSquare(it, false) });

	companion object {
		fun fromString(name: String?): ImageOperation {
			return ImageOperation.entries.find { it.name == name } ?: NONE
		}

		fun RESIZE_TO(image: BufferedImage, size: Int): BufferedImage {
			return resize(image, size)
		}
	}
}