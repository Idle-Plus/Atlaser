# Atlaser

Atlaser is a tool for generating texture atlases, primarily used by [Idle Client](https://github.com/Idle-Plus/IdleClient), 
an unofficial third-party web client for the game Idle Clans. It automatically processes textures by resizing and 
scaling images, combining them into multiple texture atlases, and generating a JSON file that the client uses to look 
up individual textures.

## Features

- **Texture Packing**: Efficiently packs multiple textures into atlas sheets.
- **Multiple Atlas Support**: Automatically creates additional atlas sheets when needed.
- **Image Processing**: Supports various image operations:
  - Resizing to specific dimensions (16x16, 32x32, 48x48, 64x64, 128x128).
  - Padding images to make them square.
  - Trimming transparent pixels.
- **Sub-image Generation**: Can generate smaller versions of textures.
- **WebP Support**: Can output atlases in WebP format for better compression.
- **JSON Output**: Generates a JSON file with metadata for looking up textures.
- **Configurable**: Highly configurable through a properties file.

## Installation

1. Clone the repository
2. Build the project using Gradle:
   ```
   ./gradlew build
   ```
3. The built JAR file will be in the `build/libs` directory

## Usage

1. Create a `config.properties` file in the same directory as the JAR file (a default one will be created on first run)
2. Place your textures in the input directory (default: `textures/`)
3. Run the tool:
   ```
   java -jar atlaser.jar
   ```
4. The generated atlas sheets and JSON file will be in the output directory (default: `output/`)

## Configuration

Atlaser is configured using a `config.properties` file with the following settings:

### General Options

```properties
# The folder where the input textures are located
general.input=textures

# The folder where the output atlas will be saved
general.output=output

# The file which contains the items to be added to the atlas
general.items=items.json

# If the textures should be used as the items, ignoring items.json
general.use-textures-as-items=true

# The postfix to append to the resulting names
general.name-postfix=

# If a generic "missing texture" should be included
general.include-missing-texture=true
```

### Atlas Options

```properties
# The name which will be used when saving the atlas, "(NAME)_(INDEX).png"
atlas.sheet.name=item_sheet

# The name which will be used when saving the atlas json, "(NAME).json"
atlas.data.name=item_atlas

# The size of the atlas (max 2048 recommended for compatibility)
atlas.size=2048

# How much the texture should be scaled up by
atlas.scale=1

# If the generated sheets should be WebP or PNG file format
atlas.use-webp=false

# The operation to do on the images before creating the atlas
atlas.image-operations=PAD_TO_SQUARE, RESIZE_128

# If specified, generates multiple sub images in different sizes
atlas.generate-sub-images=48, 32

# The padding between each texture in the atlas
atlas.padding=0

# If the generated atlas.json should contain the item id, only works
# if general.use-textures-as-items is false.
atlas.generate.id=false

# If the generated atlas.json should contain the item name
atlas.generate.name=true
```

## License

This project is licensed under the MIT License, see the [LICENSE](https://github.com/Idle-Plus/Atlaser/blob/master/LICENSE) file for details.
