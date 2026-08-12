# Temptation Companions

Adult companion NPCs for **Minecraft 26.2** (Fabric). Spawn a companion, talk her into a scene from
her menu, pay her, and the mod takes over with a fully animated, camera-directed scene.

> **18+ only.** This mod adds explicit sexual content. Do not install it if you are a minor, and do
> not put it on a server where minors play.

Status: **alpha.** Playable end to end, but expect rough edges — scene placement, camera framing and
proportions are still being tuned, and only part of the original roster is switched on.

## Features

- **Four companions** — Jenny, Ellie, Bia and Luna, each with her own model, voice lines and set of
  scenes.
- **Animated scenes** with an approach, a payment animation, an intro, a paced active phase and a
  finish, driven server-side so they stay in sync in multiplayer.
- **Directed camera** — every scene has its own framing per phase, with fades over the cuts.
- **Interaction menu** in the style of the original: scene buttons grouped into categories, each with
  its price shown next to it, plus the universal commands on the left edge.
- **Companion commands** — follow me, stop following, go home, set new home, and an equipment panel;
  armour and held items render on the companion.
- **Free camera** you can fly around during a scene.
- **In-game tuning panel** for camera distance, scene placement and body proportions, saved to config.
- **English and Russian** localisation.

## Requirements

| | |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | any 26.2 build (0.155.2+26.2 is what it is developed against) |
| GeckoLib | 5.5.3 for 26.2 |
| Java | 25 |

## Installation

1. Install Fabric Loader for 26.2.
2. Drop **Fabric API**, **GeckoLib** and the Temptation Companions jar into `.minecraft/mods`.
3. Launch the game.

## Getting started

```
/sexmod spawn jenny
```

Companions do not spawn naturally — use the command (`jenny`, `ellie`, `bia`, `luna`). Right-click
one to open her menu, pick a category, then pick a scene; you need the price in your inventory.

Each companion has her own set of scenes; her menu shows what she offers and what it costs — prices
run from a single gold ingot up to three emeralds, paid in gold, diamonds, emeralds or ender pearls.
Some scenes happen on a bed and need one within reach; she will walk to it and take you along.

`/sexmod scene <name>` starts a scene with the nearest companion within 8 blocks, which is handy for
testing.

## Controls

During a scene:

| Key | |
|---|---|
| **Shift** | set the pace — tap or hold |
| **Space** | finish, once the meter is full; ends the scene otherwise |
| **X** | end the scene |
| **G** | toggle the free camera |
| **N** | open the tuning panel |

With the free camera on, `[` `]` change movement speed and `;` `'` change vertical speed. The keypad
keys fine-tune the fade over scene cuts. Everything is rebindable under Options → Controls →
Temptation Companions.

## Configuration

Tuning done in-game is written to `.minecraft/config/`:

- `sexmod-camera.json` — camera framing per companion, scene and phase
- `sexmod-jenny-scene-layout.properties` — where the pair is placed for each scene
- `sexmod-jenny-scene-body.properties` — per-scene body proportions

Each file only holds what you changed. Delete a file to fall back to the values shipped in the mod.

## Building from source

```bash
git clone https://github.com/nxrtwee/Temptation-Companions
cd Temptation-Companions
./gradlew build
```

The jar lands in `build/libs/`.

Two things are **not** in this repository and have to be supplied locally:

- **GeckoLib** — there is no Maven release for 26.2, so put `geckolib-fabric-26.2-5.5.3.jar` into
  `libs/`.
- **Character assets** — models, animations, textures and sounds are not redistributed here. A fresh
  clone compiles and runs, but companions stay invisible and silent until an asset tree is present
  under `src/main/resources/assets/sexmod/` (`geckolib/`, `textures/`, `sounds/`).

## Credits and license

This is an independent port of a legacy 1.12.2 Forge mod to modern Fabric. The code in this
repository is mine; the characters, models, animations and sounds belong to the authors of the
original mod and are deliberately not included here.

**All rights reserved.** See [LICENSE](LICENSE).

Russian version of this page: [README.ru.md](README.ru.md).
