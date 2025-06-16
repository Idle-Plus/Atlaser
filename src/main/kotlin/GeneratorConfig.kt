package dev.uraxys.idleclient.atlaser

import dev.uraxys.idleclient.atlaser.generator.image.ImageOperation
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties
import kotlin.system.exitProcess

class GeneratorConfig {

	val sourceDir: File
	val outputDir: File
	val itemsFile: File
	val useTexturesAsItems: Boolean
	val namePostfix: String
	val includeMissingTexture: Boolean

	val atlasSheetName: String
	val atlasDataName: String

	val atlasSize: Int
	val atlasScale: Int
	val atlasUseWebP: Boolean
	val atlasImageOperations: List<ImageOperation>
	val atlasGenerateSubImages: List<Int>
	val atlasPadding: Int
	val atlasGenerateId: Boolean
	val atlasGenerateName: Boolean

	init {
		// Get the config, if it doesn't exist, then copy the default one.
		val config = Utils.file(Utils.userDir, "config.properties")
		if (!config.exists()) {
			javaClass.getResourceAsStream("/config.properties")?.use { input ->
				Files.copy(input, config.toPath(), StandardCopyOption.REPLACE_EXISTING)
			} ?: throw IllegalStateException("Failed to copy default config.properties file.")
		}

		val properties = Properties()
		properties.load(config.inputStream())

		this.sourceDir = Utils.file(Utils.userDir, properties.getProperty("general.input"))
		this.outputDir = Utils.file(Utils.userDir, properties.getProperty("general.output"))
		this.itemsFile = Utils.file(Utils.userDir, properties.getProperty("general.items"))
		this.useTexturesAsItems = properties.getProperty("general.use-textures-as-items").toBoolean()
		this.namePostfix = properties.getProperty("general.name-postfix") ?: ""
		this.includeMissingTexture = properties.getProperty("general.include-missing-texture").toBoolean()

		this.atlasSheetName = properties.getProperty("atlas.sheet.name") + this.namePostfix
		this.atlasDataName = properties.getProperty("atlas.data.name") + this.namePostfix
		this.atlasSize = properties.getProperty("atlas.size").toInt()
		this.atlasScale = properties.getProperty("atlas.scale").toInt()
		this.atlasUseWebP = properties.getProperty("atlas.use-webp").toBoolean()
		this.atlasImageOperations = properties.getProperty("atlas.image-operations")
			?.split(",")?.map { it.trim() }?.map { ImageOperation.fromString(it) }
			?.filter { it != ImageOperation.NONE }?.toList() ?: emptyList()
		this.atlasGenerateSubImages = properties.getProperty("atlas.generate-sub-images")
			?.split(",")?.map { it.trim() }?.map { it.toInt() }
			?.filter { it > 0 }?.toList() ?: emptyList()
		this.atlasPadding = properties.getProperty("atlas.padding").toInt()
		this.atlasGenerateId = properties.getProperty("atlas.generate.id").toBoolean()
		this.atlasGenerateName = properties.getProperty("atlas.generate.name").toBoolean()

		if (!this.sourceDir.exists()) this.sourceDir.mkdirs()
		if (!this.outputDir.exists()) this.outputDir.mkdirs()

		if (!this.itemsFile.exists()) {
			Utils.warn("Items file not found: ${this.itemsFile.absolutePath}")
			exitProcess(1)
		}
	}
}