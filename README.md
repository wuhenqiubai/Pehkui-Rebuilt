# Pehkui Rebuilt

Pehkui Rebuilt is an independent rebuild of Pehkui, a library mod that allows mods and commands to change the size of entities and scale-related behavior.

## What It Does

Pehkui lets mods and commands modify entity scale data. That can affect entity size, movement, reach, hitboxes, camera behavior, explosions, and other properties that depend on scale.

Most players install Pehkui because another mod requires it. Mod developers can use it as an API for scale-related features.

## Current Branch Status

- Minecraft target: `1.21.10`
- Fabric Loader: `0.19.3`
- Fabric API: `0.138.4+1.21.10`
- Mod version: `3.8.4`
- Published loader format: Fabric (loadable by Quilt via its Fabric compatibility)

## Branching & Versioning

Each supported Minecraft version lives on its own branch (e.g. `fabric/1.21.10`). Branches are **single-version skeletons**: the mixin layer contains only the injection points that apply to that version — no cross-version `compat*` subpackages, no legacy version-gating infrastructure. All mixins are flattened into `mixin/` (server) and `mixin/client/`.

To port to a new Minecraft version, copy this skeleton to a new branch and adjust the mixin injection-point method descriptors for the new mappings. Version differences are isolated by branches rather than by in-tree compat code.

### API stability

Pehkui is a **library mod** — downstream mods depend on the public API (`virtuoel.pehkui.api` package, `ScaleUtils`, `ScaleRegistries`, etc.). The public API signatures and semantics are kept **stable across versions and branches**: no breaking changes to `ScaleType` / `ScaleData` / `ScaleModifier` / `ScaleRegistries` / `ScaleTypes`. Version-specific work is confined to the mixin layer.

## Installation

1. Install Fabric Loader for the supported Minecraft version.
2. Install Fabric API as required by your loader and modpack.
3. Put the Pehkui Rebuilt jar in your `mods` folder.
4. Install any mods that depend on Pehkui.
5. Launch the game.

## Downloads

Use the approved Modrinth or CurseForge project page when available. For development builds, run the **「开发版 | Dev Version」** workflow from the **Actions** page of this repository: choose a version branch, click **Run workflow**, and download the built jar from the workflow's **Artifacts**. Each build is numbered by the run, so jars never collide.

## For Developers

The original Pehkui API style is preserved where possible. Mods that already depend on Pehkui should continue to use their normal dependency declarations.

When targeting this repository directly, depend on the released jar that matches your Minecraft version.

## Reporting Issues

Open issues on this repository for bugs.

Include:

- Minecraft version
- loader and loader version
- Pehkui Rebuilt version
- the mod, command, or datapack that changes scale
- steps to reproduce the scale issue
- `latest.log` or the crash report

## Building

```bash
./gradlew build
```

Built jars are written to `build/libs`.

## Credits

Pehkui was originally created by Virtuoel. This project rebuilds and keeps Pehkui available for newer Minecraft versions while preserving original credits and license terms.
