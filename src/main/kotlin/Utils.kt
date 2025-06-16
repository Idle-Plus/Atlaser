package dev.uraxys.idleclient.atlaser

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File

object Utils {

	val development = System.getenv("DEVELOPMENT")?.toBoolean() == true
	val userDir = path(System.getProperty("user.dir"), if (development) "atlaser/run" else "")

	val unknownImage = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB).apply {
		createGraphics().apply {
			color = Color.MAGENTA
			fillRect(0, 0, 16, 16)
			color = Color.BLACK
			fillRect(0, 0, 8, 8)
			fillRect(8, 8, 8, 8)
			dispose()
		}
	}

	val missingImage = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB).apply {
		createGraphics().apply {
			color = Color.MAGENTA
			fillRect(0, 0, 16, 16)
			color = Color.CYAN
			fillRect(0, 0, 8, 8)
			fillRect(8, 8, 8, 8)
			dispose()
		}
	}

	fun info(message: String) {
		println("INFO: $message")
	}

	fun warn(message: String) {
		println("WARNING: $message")
	}

	fun path(parent: String, path: String): String {
		if (path.isEmpty()) return parent
		return "$parent${File.separatorChar}$path".replace("/", File.separator)
	}

	fun file(parent: String, path: String): File {
		return File(parent, path.replace("/", File.separator))
	}

	fun file(parent: File, path: String): File {
		return File(parent, path.replace("/", File.separator))
	}
}