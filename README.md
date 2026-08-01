# Pehkui Continuation

Pehkui Continuation is an unofficial continuation of Pehkui, a library mod that allows mods and commands to change the size of entities and scale-related behavior.

## What It Does

Pehkui lets mods and commands modify entity scale data. That can affect entity size, movement, reach, hitboxes, camera behavior, explosions, and other properties that depend on scale.

Most players install Pehkui because another mod requires it. Mod developers can use it as an API for scale-related features.

## Current Branch Status

- Minecraft target: `1.21.11` (Yarn `1.21.11+build.5`)
- Fabric Loader: `0.19.2`
- Fabric API: `0.141.4+1.21.11`
- Mod version: `3.8.3`
- Published loader format: Fabric (loadable by Quilt via its Fabric compatibility)

## Branching & Versioning

Each supported Minecraft version lives on its own branch (e.g. `fabric/1.21.11`). Branches are **single-version skeletons**: the mixin layer contains only the injection points that apply to that version — no cross-version `compat*` subpackages, no legacy version-gating infrastructure. All mixins are flattened into `mixin/` (server) and `mixin/client/`.

To port to a new Minecraft version, copy this skeleton to a new branch and adjust the mixin injection-point method descriptors for the new mappings. Version differences are isolated by branches rather than by in-tree compat code.

### 1.21.11 rendering pipeline notes

Minecraft 1.21.11 moved entity/item rendering onto the `EntityRenderState` / `EntityRenderManager` pipeline (the old `EntityRenderDispatcher` and `render(Entity, …)` entry points no longer exist). The client mixin layer on this branch targets the new pipeline directly:

- Entity model scaling is applied in `EntityRenderManagerMixin` around the `render` pass, driven by per-state model scale stored through `PehkuiEntityRenderStateExtensions`.
- First-person held item scaling hooks `HeldItemRenderer.renderFirstPersonItem`.
- The former `compat12110plus` rendering classes (which 1.21.1 did not have) are flattened into `mixin/client/`.

## Installation

1. Install Fabric Loader for the supported Minecraft version.
2. Install Fabric API as required by your loader and modpack.
3. Put the Pehkui Continuation jar in your `mods` folder.
4. Install any mods that depend on Pehkui.
5. Launch the game.

## Downloads

Use the approved Modrinth or CurseForge project page when available. Development builds may also be attached to GitHub releases for this repository.

## For Developers

The original Pehkui API style is preserved where possible. Mods that already depend on Pehkui should continue to use their normal dependency declarations unless a continuation release note says otherwise.

When targeting this fork directly, depend on the released continuation jar that matches your Minecraft version.

## Reporting Issues

Open issues on this continuation repository for bugs in this fork.

Include:

- Minecraft version
- loader and loader version
- Pehkui Continuation version
- the mod, command, or datapack that changes scale
- steps to reproduce the scale issue
- `latest.log` or the crash report

## Building

```bash
./gradlew build
```

Built jars are written to `build/libs`.

## Credits

Pehkui was originally created by Virtuoel. This continuation keeps the project available for newer versions while preserving original credits and license terms.
