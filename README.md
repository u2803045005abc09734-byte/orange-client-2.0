# Orange Client

A new, independently structured Fabric client project for **Minecraft 1.21.11 / Java 21**. The ClickGUI follows the dark, rounded Aurora reference with purple accents and five main categories.

## Build

On Windows:

```bat
gradlew.bat build
```

On Linux/macOS:

```sh
./gradlew build
```

The included bootstrap scripts download Gradle 9.2.1 on first use. Gradle then resolves Minecraft, Yarn, Fabric Loader and Fabric API from their official Maven repositories. Open the folder in IntelliJ IDEA as a Gradle project to let the IDE import the toolchain.

The expected output is:

```text
build/libs/orange-client-1.0.0.jar
```

Install that JAR together with Fabric API in a Minecraft 1.21.11 Fabric client running Java 21.

## Controls

- **Right Shift** opens or closes the ClickGUI only while a player is attached to a loaded client world.
- **Escape** closes the GUI (or exits search first).
- The GUI is non-pausing and closes automatically if the world/player disappears.

## Implemented features

- Independent module registry and persistent JSON configuration (`config/orange-client.json`).
- Five categories: PvP, Visuals, HUD, Player and Misc.
- Aurora-inspired responsive ClickGUI, custom toggles, search, theme cycling and compact/description preferences.
- Auto Sprint.
- Player nametags (client-local name visibility).
- Entity hitbox visibility through Minecraft 1.21.11's debug-HUD profile entry.
- Fullbright entry currently sets **vanilla gamma to its maximum supported value**; it is not a shader-based gamma bypass.
- Local vanilla glowing outlines for other players and End Crystals.
- HUD toggles for coordinates, FPS, armor durability and local clock.
- No View Bobbing.
- Config state is loaded on startup and saved after GUI changes / screen close.

## Deliberate limitations

This is a fresh source implementation and foundation, not a claim that every feature in the long specification is finished. The source has not been compiled or run in this environment because the build tools and required dependency downloads were unavailable here. The Hand and Items visual sub-pages currently report that those rendering effects are not implemented. Custom outline colours, gradient/rainbow modes, glow strength controls, a shader-based world blur, draggable HUD editing, resource-pack browsing and cosmetic systems are also not implemented in this source drop. They are not represented as active controls.

The player and crystal outline modules use Minecraft's built-in glowing flag and therefore inherit vanilla/team outline colouring. Fullbright uses the maximum vanilla gamma setting rather than bypassing the game's lighting pipeline. The current source also does not yet include the reference screenshot's full animation polish, the Ambience module, adjustable render sliders/dropdowns, or the full HUD editor. The current module set is intentionally smaller than the requested end-state.

## Toolchain pins

- Minecraft: `1.21.11`
- Java: `21`
- Fabric Loader: `0.18.4`
- Fabric API: `0.141.6+1.21.11`
- Yarn: `1.21.11+build.6`
- Fabric Loom Remap: `1.14.10`
- Gradle: `9.2.1`
