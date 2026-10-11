# Changelog

All notable changes to **Temptation Companions** are documented in this file.

---

## [1.0-fabric-26.3] - 2026-10-11 - Port to Minecraft 26.3

### Minecraft 26.3 & Platform Support
* **Full Compatibility with Minecraft 26.3**:
  * Adapted to Mojang's migration to SDL3 input handling and modern key/mouse button mapping.
  * Migrated rendering pipelines to the Minecraft 26.3 architecture and Mojang mappings.
* **GeckoLib 5.5.7 Compatibility & Animation Fixes**:
  * **Strict GeckoLib requirement**: Requires GeckoLib 5.5.7 for Minecraft 26.3 (and GeckoLib 5.5.3 for Minecraft 26.2).
  * **Asset Sanitization**: Completely sanitized all 15 animation files, removing legacy/corrupted Blockbench easing strings, stray tokens (`.`, `NaN-2*2`), and unresolved Molang variables (`size`, `pitch`, `yaw`).
  * **Molang Parser Safety Net (`MathParserMixin`)**: Restored lenient Molang identifier fallback from 5.5.3 to prevent `CompoundException` bake failures on custom expressions.
  * **Keyframe Extrapolation Fix (`AnimationPointMixin`)**: Fixed GeckoLib 5.5.7's keyframe clamping bug before the first keyframe, eliminating negative time progress and preventing character bone stretching or massive scaling.
  * Resolved all scene T-posing and emotion/facial overlay stacking issues across all 12 companions.

---

## [1.0] - 2026-10-11 - Temptation Companions 1.0

### Complete Roster (12 Companions)
* **Jenny, Ellie, Bia, Luna** — fully ported, polished animations, audio, and camera tracking.
* **Slime Girl** — bouncy physics, unique slime sounds, special encounter logic.
* **Allie (The Genie)** — magical companion summoned exclusively from **Allie's Lamp**. Includes custom arrival/dismissal smoke animations, unique dialogue, and scene sets.
* **Bee Girl** — custom pollinator companion with hovering idle cycles and playful scenes.
* **Goblin Girl** — gold-obsessed cave dweller with custom voice lines and greedy payment animations.
* **Kobold Girl** — tribal dragonkin with complete sound sets and animations.
* **Galath** — majestic dragon princess featuring royal attire, Dragon Staff, and Galath Coin payments.
* **Manglelie** — enigmatic companion with full animations, voice acting, and distinct interactions.

### Items & World Generation
* **Unique Spawn Eggs**: Handcrafted custom pixel-art icons for all 12 companion spawn eggs (Jenny, Ellie, Bia, Luna, Slime, Allie, Bee, Goblin, Kobold, Galath, Manglelie, Tribe Egg).
* **Allie's Lamp**:
  * Nether dungeon loot: naturally generates with a rare chance in Nether Fortresses and Bastion Remnant chests.
  * Dual-mode rendering: 2D pixel-art icon in inventories/hotbar, 2D sprite when dropped or held in third-person (preventing glitchy phantom Steve arms), and a high-detail 3D GeckoLib model in first-person view.
  * Dynamic player skin applied to the holding hand in first-person view.
  * Fixed an issue where the inspection animation would continuously loop while holding the lamp.
  * Magic summon & dismiss lifecycle: Allie appears upon lamp use; exiting or climaxing plays Allie's custom vanishing animation and consumes the lamp.
* **Dragon Staff**: Features a functional 3D Ender Crystal model crowned with an authentic animated crystal texture atop the staff.
* **Galath Coin**: Replaced awkward 3D models with the classic 2D coin texture for payments.
* **Lasso**:
  * Allows capturing, taming, and leading companions.
  * Blacklisted one-time scene characters (Allie, Bee, Slime) from being tamed.
  * Automatically deactivates "Stay at Home" mode upon leashing/lassoing.
* **Horny Potions**: Added the complete Horny Potion suite (Normal, Splash, Lingering, and Tipped Arrow) with full Creative Tab integration under the mod's tab.

### AI, Navigation & Bed Approach Overhauls
* **Direct Bed Pathfinding**:
  * Eliminated redundant player approaches: companions now walk directly to the target bed when no payment is required or when scenes are launched via "All scenes for all girls".
  * Fixed bed boundary stalling: widened bed arrival threshold (1.6 blocks base + 1.8 blocks path leeway) so companions no longer get stuck at 1.2 blocks waiting for the player to nudge them.
  * Removed disruptive distance timeouts that previously caused immersion-breaking teleportation through walls.
* **Home System & Teleportation Polish**:
  * Re-architected "Go Home" / "Return Home" behavior: companion halts movement, turns toward home, plays the Ender Pearl throwing animation, holds the pose for 1–2 seconds, and executes a clean instant `SnapTo` teleport.
  * Resolved bugs causing the pearl throw animation to play twice or the companion to slide along the ground.
* **Villager-like Conversation Focus**: Companions now stop wandering and lock their gaze onto the player while interaction dialog menus are open.

### Rendering & Shading Fixes
* **Player Body Skin Shading**: Fixed a critical visual glitch where the player's torso rendered pitch-black on Galath and Manglelie by filtering out extraneous bone masks in `GirlSteveGeoLayer`.
* **Soft Shading Compatibility**: Removed legacy software shading overrides to ensure full compatibility with modern Minecraft 26.2 pipeline and shader packs (Iris/Oculus).

### Audio, Quality of Life & Polish
* Comprehensive sound sets integrated: girl voice sets, moans, chuckles, eating, jumping, footsteps, screaming, and custom misc effects.
* Cleaned up duplicate items (removed extraneous Kobold Egg item in favor of the official Tribe Egg and spawn egg).
* Full English (`en_us.json`) and Russian (`ru_ru.json`) localization across all entities, items, categories, commands, and subtitles.
