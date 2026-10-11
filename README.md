# Temptation Companions

Adult companion NPCs for **Minecraft 26.3 & 26.2** (Fabric). Spawn a companion, talk her into a scene from
her menu, pay her, and the mod takes over with a fully animated, camera-directed scene.

> **18+ only.** This mod adds explicit adult content. Do not install it if you are a minor, and do
> not put it on a server where minors play.

Status: **Release 1.0.** Complete roster of 12 companions, fully overhauled pathfinding and bed logic,
cinematic camera director, custom items, dynamic skin layering, and multiplayer support.

## Features

- **12 Companions** — Jenny, Ellie, Bia, Luna, Slime Girl, Allie (The Genie), Bee, Goblin, Kobold,
  Galath, and Manglelie, each with her own 3D model, voice lines, animations, and unique scenes.
- **Custom Items & Mechanics**:
  - **Allie's Lamp** — generates as rare loot in Nether Fortresses and Bastions. Summons Allie in a puff of
    smoke; holds statically in hand with player skin dynamic texturing in first-person; Allie vanishes cleanly
    upon climax or cancellation.
  - **Lasso** — tame and lead companions anywhere. Automatically disengages "Stay at home" mode; one-time
    companions (Allie, Bee, Slime) are protected from accidental taming.
  - **Dragon Staff & Galath Coin** — majestic staff topped with an authentic animated Ender Crystal and custom
    currency for royal dragon payments.
  - **Horny Potions** — normal, splash, lingering, and tipped arrows available directly in the mod's creative tab.
  - **Custom Spawn Eggs** — unique handcrafted pixel-art icons for all 12 companions.
- **Overhauled AI & Navigation**:
  - **Direct Bed Approach** — companions walk straight to beds for free or all-girls scenes without redundant
    player approaches.
  - **Zero Bed Stalling** — tuned arrival detection prevents getting stuck right before beds.
  - **Graceful Home Return** — plays an Ender Pearl throwing animation, pauses for immersion, and snaps home cleanly.
  - **Dialogue Focus** — companions freeze movement and face the player like villagers while talking.
- **Animated Scenes** with an approach, payment, intro, paced active phase, and climax, driven server-side
  so they stay in sync in multiplayer.
- **Directed & Free Camera** — cinematographic framing per phase with smooth cuts, plus a flyable free camera (**G**).
- **Interaction Menu** — categorized scenes, real-time price indicators, universal commands, and equipment viewing.
- **"All Scenes for All Girls" Toggle** — unleash any scene across companions with smart fallback alignments.
- **In-Game Tuning Panel** (**N**) — fine-tune camera angles, pair offsets, and body scaling with live config saving.
- **English and Russian** localisation.

## Requirements

> [!IMPORTANT]
> **Strict GeckoLib Version Requirements:**
> Due to breaking internal parser and keyframe interpolation changes across GeckoLib releases:
> * For **Minecraft 26.3**, you **MUST use GeckoLib 5.5.7** (`geckolib-fabric-26.3-5.5.7.jar`).
> * For **Minecraft 26.2**, you **MUST use GeckoLib 5.5.3** (`geckolib-fabric-26.2-5.5.3.jar`).
> Mismatched GeckoLib versions will lead to animation bake errors, T-posing, and visual glitches.

| Component | Minecraft 26.3 | Minecraft 26.2 |
|---|---|---|
| **Minecraft** | `26.3` | `26.2` |
| **Fabric Loader** | `>= 0.19.3` | `>= 0.19.3` |
| **Fabric API** | Compatible 26.3 build | Compatible 26.2 build |
| **GeckoLib** | **Strictly 5.5.7** | **Strictly 5.5.3** |
| **Java** | `25` | `25` |

## Installation

1. Install Fabric Loader for your target Minecraft version (26.3 or 26.2).
2. Download the appropriate mod build from [Releases](https://github.com/nxrtwee/Temptation-Companions/releases):
   * For **Minecraft 26.3**: `temptation-companions-1.0-fabric-26.3.jar`
   * For **Minecraft 26.2**: `temptation-companions-1.0-fabric-26.2.jar`
3. Drop **Fabric API**, the corresponding version of **GeckoLib** (see strict requirements above), and the Temptation Companions jar into your `.minecraft/mods` folder.
4. Launch the game.

## Getting started

Companions can be spawned via their dedicated creative tab spawn eggs, found in the wild (such as finding
Allie's Lamp in Nether chests), or spawned via commands:

```
/sexmod spawn jenny
```

Right-click a companion to open her menu, choose a category, and select a scene (ensure you have the
required payment in your inventory). Some scenes occur on a bed — if one is nearby, she will guide you there.

Use `/sexmod scene <name>` to quickly preview a scene with the nearest companion.

## Controls

During a scene:

| Key | Action |
|---|---|
| **Shift** | Set pace — tap or hold |
| **Space** | Finish / Climax (when horny meter is full; ends scene otherwise) |
| **X** | End scene |
| **G** | Toggle free camera |
| **N** | Open scene tuning panel |

With free camera enabled, `[` and `]` adjust movement speed, while `;` and `'` adjust vertical speed.
All keybindings can be customized in Options → Controls → Temptation Companions.

## Configuration

In-game tuning is automatically saved to `.minecraft/config/`:
- `sexmod-camera.json` — camera angles and offsets per scene phase
- `sexmod-<girl>-scene-layout.properties` — character placement and distance
- `sexmod-<girl>-scene-body.properties` — character proportions

## Building from source

```bash
git clone https://github.com/nxrtwee/Temptation-Companions
cd Temptation-Companions
./gradlew build
```

The compiled jar will be located in `build/libs/`.

*Note: GeckoLib must be placed locally in `libs/` as `geckolib-fabric-26.2-5.5.3.jar`.*

## Credits and license

This is an independent modern Fabric port and continuation of the classic companion mod. The code in
this repository is open for community enjoyment under the repository license. Original character designs,
rigs, animations, and voice lines remain the property of their respective creators.

Russian documentation: [README.ru.md](README.ru.md).
