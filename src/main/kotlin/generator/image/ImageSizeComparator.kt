package dev.uraxys.idleclient.atlaser.generator.image

import dev.uraxys.idleclient.atlaser.generator.atlas.AtlasTexturePart

class ImageSizeComparator : Comparator<AtlasTexturePart> {
	override fun compare(image1: AtlasTexturePart, image2: AtlasTexturePart): Int {
		val area1 = image1.image.width * image1.image.height
		val area2 = image2.image.width * image2.image.height
		if (area1 == area2) return image1.id.compareTo(image2.id)
		return area2 - area1
	}
}