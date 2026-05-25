# Pehkui Continuation

Pehkui Continuation is an unofficial continuation of Pehkui, a library mod that allows mods and commands to change the size of entities and scale-related behavior.

This branch currently targets Minecraft `1.21.11` for Fabric and Quilt. The project metadata also keeps the historical Fabric/Quilt range from `1.14.4` through `1.21.11` where supported by the build.

## What It Does

Pehkui lets mods and commands modify entity scale data. That can affect entity size, movement, reach, hitboxes, camera behavior, explosions, and other properties that depend on scale.

Most players install Pehkui because another mod requires it. Mod developers can use it as an API for scale-related features.

## Current Branch Status

- Minecraft target: `1.21.11`
- Fabric Loader: `0.19.2`
- Fabric API: `0.141.4+1.21.11`
- Mod version: `3.8.3`
- Published loaders configured in this branch: Fabric and Quilt

## Installation

1. Install Fabric Loader or Quilt Loader for the supported Minecraft version.
2. Install Fabric API or Quilt Standard Libraries as required by your loader and modpack.
3. Put the Pehkui Continuation jar in your `mods` folder.
4. Install any mods that depend on Pehkui.
5. Launch the game.

## Downloads

Use the approved Modrinth or CurseForge project page when available. Development builds may also be attached to GitHub releases for this repository.

## For Developers

The original Pehkui API style is preserved where possible. Mods that already depend on Pehkui should continue to use their normal dependency declarations unless a continuation release note says otherwise.

When targeting this fork directly, depend on the released continuation jar that matches your Minecraft version and loader.

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
