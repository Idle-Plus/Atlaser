package dev.uraxys.idleclient.atlaser.generator.image

import java.awt.Rectangle
import java.awt.image.BufferedImage

class ImageNode(
	val rect: Rectangle,
	private val child: Array<ImageNode?> = arrayOf(null, null),
) {

	private var image: BufferedImage? = null

	constructor(
		x: Int,
		y: Int,
		width: Int,
		height: Int,
	) : this(Rectangle(x, y, width, height))

	fun isLeaf(): Boolean {
		return child[0] == null && child[1] == null
	}

	fun insert(image: BufferedImage, padding: Int): ImageNode? {
		if (!this.isLeaf()) {
			val newNode = this.child[0]!!.insert(image, padding)
			if (newNode != null) return newNode
			return this.child[1]!!.insert(image, padding)
		} else {
			// Check if the node is occupied.
			if (this.image != null) return null
			// Check if the image fits.
			if (image.width > this.rect.width || image.height > this.rect.height) return null

			// Check if the image fits perfectly.
			if (image.width == this.rect.width && image.height == this.rect.height) {
				this.image = image
				return this
			}

			val dw = this.rect.width - image.width
			val dh = this.rect.height - image.height

			if (dw > dh) {
				this.child[0] = ImageNode(this.rect.x, this.rect.y, image.width, this.rect.height)
				this.child[1] = ImageNode(padding + this.rect.x + image.width, this.rect.y,
				this.rect.width - image.width - padding, this.rect.height)
			} else {
				this.child[0] = ImageNode(this.rect.x, this.rect.y, this.rect.width, image.height)
				this.child[1] = ImageNode(this.rect.x, padding + this.rect.y + image.height,
				this.rect.width, this.rect.height - image.height - padding)
			}
			return this.child[0]!!.insert(image, padding)
		}
	}
}