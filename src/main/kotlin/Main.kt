package dev.uraxys.idleclient.atlaser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import dev.uraxys.idleclient.atlaser.generator.AtlasGenerator
import java.io.File
import javax.imageio.ImageIO

private val ITEMS = mutableMapOf<Int, String>()
private val TEXTURES = mutableMapOf<String, String>() // file name -> item name
private val MAPPER = ObjectMapper()

fun main() {
	ImageIO.scanForPlugins()
	val config = GeneratorConfig()

	if (config.useTexturesAsItems) {
		// Find every .png file inside the "textures" folder.
		var count = 0
		config.sourceDir.walk().forEach {
			if (!it.isFile || it.extension != "png") return@forEach
			if (!config.includePath) {
				val name = it.nameWithoutExtension
				ITEMS[count] = name
				count++
				return@forEach
			}

			val relativePath = it.relativeTo(config.sourceDir).path
			val name = relativePath.substringBeforeLast(".").replace("\\", "/")
			ITEMS[count] = name
			count++
		}
	} else {
		// Extract the item names and id from the file.
		val json = MAPPER.readTree(config.itemsFile) as ObjectNode
		json.apply { (this["data"] as ArrayNode).forEach { item -> ITEMS[item["id"].asInt()] = item["name"].asText() } }
		Utils.info("Loaded ${ITEMS.size} items.")
	}

	// Find all the textures.
	config.sourceDir.walk().forEach {
		if (!it.isFile || it.extension != "png") return@forEach
		if (!config.includePath) {
			TEXTURES[it.nameWithoutExtension] = it.relativeTo(config.sourceDir).path
			return@forEach
		}

		val relativePath = it.relativeTo(config.sourceDir).path
		TEXTURES[relativePath.substringBeforeLast(".").replace("\\", "/")] = relativePath
	}

	// Create the generator.
	val generator = AtlasGenerator(
		size = config.atlasSize,
		scale = config.atlasScale,
		useWebP = config.atlasUseWebP,
		operation = config.atlasImageOperations,
		subImages = config.atlasGenerateSubImages,
		padding = config.atlasPadding,

		source = config.sourceDir,
		output = config.outputDir,
		sheetName = config.atlasSheetName,
	)

	// Add the textures to the generator.
	var current = 0
	Utils.info("Adding ${ITEMS.size} items to the atlas...")
	ITEMS.forEach { (_, name) ->
		current++
		if (current % 10 == 0) Utils.info("Processing images... ($current / ${ITEMS.size})")
		TEXTURES[name]?.let { generator.add(it, name); return@forEach }
		Utils.warn("Texture not found for item '$name', make sure it's located in the source directory.")
		generator.add(Utils.unknownImage, name)
	}

	// Add the missing image to the generator if it's enabled.
	if (config.includeMissingTexture) {
		ITEMS[ITEMS.size] = ":missing:"
		generator.add(Utils.missingImage, ":missing:")
	}

	// Build the atlases and create the output JSON.
	Utils.info("Building atlases...")
	val atlases = generator.build()
	val output = MAPPER.createObjectNode()

	// Sort the items by id, lowest to highest, then add them to the output.
	Utils.info("Creating atlas JSON for ${atlases.size} atlases...")
	val texturesJson = MAPPER.createArrayNode()
	ITEMS.toList().sortedBy { it.first }.forEach { (id, name) ->
		// Find the texture.
		val texture = atlases.find { it.sources.containsKey(name) }
		if (texture == null) throw IllegalArgumentException("Atlas texture not found for item '$name'.")
		val index = atlases.indexOf(texture)
		// Add the texture to the output.
		val source = texture.sources[name]!!
		texturesJson.add(MAPPER.createObjectNode().apply {
			if (config.atlasGenerateId) put("id", id)
			if (config.atlasGenerateName) put("name", name + config.namePostfix)
			put("sheet", index)
			put("x", source.x)
			put("y", source.y)
			put("w", source.width)
			put("h", source.height)
		})

		// Add sub images if we have any.
		if (config.atlasGenerateSubImages.isNotEmpty()) {
			config.atlasGenerateSubImages.forEach { subImage ->
				val name = "${name}_$subImage"

				val texture = atlases.find { it.sources.containsKey(name) }
				if (texture == null) throw IllegalArgumentException("Atlas texture not found for item '$name'.")
				val index = atlases.indexOf(texture)

				val source = texture.sources[name]!!
				texturesJson.add(MAPPER.createObjectNode().apply {
					if (config.atlasGenerateId) put("id", id)
					if (config.atlasGenerateName) put("name", name + config.namePostfix)
					put("sheet", index)
					put("x", source.x)
					put("y", source.y)
					put("w", source.width)
					put("h", source.height)
				})!!
			}
		}
	}

	// Metadata.
	val metadata = MAPPER.createObjectNode().apply {
		put("scale", config.atlasScale)
		put("padding", config.atlasPadding)
		set<JsonNode>("sheets", MAPPER.createArrayNode().apply {
			atlases.forEach { sheet ->
				add(MAPPER.createObjectNode().apply {
					put("path", "${sheet.name}.${ if (config.atlasUseWebP) "webp" else "png" }")
					set<JsonNode>("size", MAPPER.createObjectNode().apply {
						put("w", sheet.width)
						put("h", sheet.height)
					})
				})
			}
		})
	}

	output.set<JsonNode>("textures", texturesJson)
	output.set<JsonNode>("metadata", metadata)

	// Save the output JSON.
	val file = File(config.outputDir, "${config.atlasDataName}.json")
	MAPPER.writeValue(file, output)
	Utils.info("Atlas JSON saved to: ${file.absolutePath}")
}