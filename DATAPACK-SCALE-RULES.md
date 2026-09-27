# Pehkui Datapack Scale Rules

Pehkui Rebuilt lets **datapacks** define scale rules: entities matching a set of conditions get their scale types set to configured values automatically. This is useful for servers and modpacks that want to resize certain mobs without writing code.

> **Version**: this feature requires Pehkui Rebuilt **3.8.5 or newer** — older versions do not load `pehkui_scale_rules` files.

## Getting Started

### 1. Create a datapack

A datapack is a folder inside the world's `datapacks/` directory (singleplayer) or the server's `datapacks/` directory (dedicated server). The folder name can be anything; it is only the display name of the pack.

```
<world or server>/datapacks/
└── my-scale-pack/                  ← folder name is arbitrary
    ├── pack.mcmeta                 ← required by vanilla
    └── data/
        └── example/                ← "example" is the namespace
            └── pehkui_scale_rules/
                └── zombies.json    ← your rules
```

`pack.mcmeta` minimum content — the data pack format for 1.21.1 is **48**:

```json
{
	"pack": {
		"pack_format": 48,
		"description": "My scale rules",
		"supported_formats": [48, 48]
	}
}
```

> **These field names are version-specific.** 1.21.1 uses a bare `pack_format` plus an optional
> `supported_formats` range. The `min_format` / `max_format` pair used from **1.21.9** onward is
> *not* understood here, and vice versa. Current numbers:
> [Minecraft Wiki - Pack format](https://minecraft.wiki/w/Pack_format#Data_pack_format_history).

### 2. Understand namespaces

The **namespace** is the name of the sub-folder under `data/`. It must be a valid identifier (lowercase letters, digits, `_`, `-`, `.`). It does **not** have to match the datapack folder name. Multiple datapacks can use the same or different namespaces — all `pehkui_scale_rules` folders across all datapacks are merged.

### 3. Reload

After creating or editing a rule file, run **`/reload`** in-game (requires operator permission). Rules are only applied on the server and take effect after the reload.

## Rule Format

This block is a **schema illustration** — the `//` annotations are not valid JSON, so don't copy it
verbatim. The ready-to-use examples are further down.

```json
{
	"name": "Small Zombies",                          // optional, display name
	"description": "Zombies are 50% width and height",// optional, description
	"conditions": {
	  "type": ["minecraft:zombie"]  // required, what to match
	},
	"scales": {
	  "pehkui:width": 0.5,                            // shorthand for a "set"
	  "pehkui:height": { "operation": "multiply", "value": 0.5, "delay": 20 }
	},
	"modifiers": {                                    // optional
	  "pehkui:motion": ["pehkui:motion_multiplier"]
	},
	"priority": 10,                                   // optional, default 0
	"fabric:load_conditions": [ ... ]                 // optional, mod-load gating
}
```

| Field                    | Required | Description                                                                               |
|--------------------------|----------|-------------------------------------------------------------------------------------------|
| `name`                   | no       | Display name. Log messages label the rule with it, followed by the resource id in parentheses. |
| `description`            | no       | Longer description. Echoed in the load-time "folds to an unusable value" warning.          |
| `conditions`             | **yes**  | An [EntityPredicate](#conditions) matching entities this rule applies to.                 |
| `scales`                 | **yes*** | Map of `pehkui:<scale_type>` to a number (a `set`) or an [operation object](#operations). |
| `modifiers`              | no       | Map of `pehkui:<scale_type>` to an array of [registered modifier ids](#modifiers).        |
| `scale_type` + `value`   | *alt*    | Legacy single-scale form: `"scale_type": "pehkui:width", "value": 0.5`.                   |
| `priority`               | no       | Ordering when several rules match; see [Stacking](#multiple-rules-stack). Default `0`.    |
| `fabric:load_conditions` | no       | Fabric resource conditions; the rule is skipped if not met.                               |

\* At least one scale or modifier has to **survive parsing** — this is checked on the parsed result, not on
whether the fields are present, so an empty `"scales": {}` counts as none. A rule that yields neither is
rejected and skipped (see [Troubleshooting](#troubleshooting)).

## Operations

Instead of a bare number, a `scales` entry may be an object applying one of the operations the
`/scale operation` command uses:

```json
"scales": {
  "pehkui:width":  { "operation": "multiply", "value": 0.5 },
  "pehkui:height": { "operation": "add",      "value": -0.2 },
  "pehkui:health": { "value": 20.0 },
  "pehkui:reach":  0.75
}
```

| Operation  | Meaning                                           |
|------------|---------------------------------------------------|
| `set`      | `value` — the default when `operation` is omitted |
| `add`      | `current + value`                                 |
| `subtract` | `current - value`                                 |
| `multiply` | `current * value`                                 |
| `divide`   | `current / value`                                 |
| `power`    | `current ^ value`                                 |

Names may be written bare (`multiply`) or fully qualified (`pehkui:multiply`). Any operation
another mod registers into `pehkui:scale_operations` works too.

### What "current" means

Relative operations need a fixed starting point. **The baseline is the scale the entity had at the
moment the first rule took that scale type over** — the rules re-evaluate periodically, and each
round recomputes from that frozen baseline, so `multiply` never feeds its own previous output back
in.

That means `{"operation": "multiply", "value": 1.5}` reads as "×1.5 relative to whatever size the
entity already had", not "×1.5 every half second". If you set `pehkui:width` to `4.0` before a rule
takes over, the result is `6.0`.

> **The value must stay a legal scale.** The operand itself may be negative (`add -0.5`), but the
> folded *result* must be finite and greater than `0`; otherwise the scale type is left untouched
> and an error is logged. This also catches `divide` by zero.

## Per-scale adjustments

The same object accepts three optional adjustments, mirroring `/scale delay`, `/scale easing` and
`/scale persist`:

| Key       | Type    | Effect                                                                 |
|-----------|---------|------------------------------------------------------------------------|
| `delay`   | integer | Ticks the size takes to interpolate to the new value (0 = instant). Omitted = leave the current setting alone; **negative is rejected** and discards the whole operation. |
| `easing`  | id      | Easing curve, e.g. `pehkui:quadratic_out`, `pehkui:bounce_in_out`.      |
| `persist` | boolean | Whether this scale type is saved to the entity's NBT.                   |

They apply while a rule covers that scale type, and are restored to whatever they were before when
**no** rule covers it any more.

> **When several rules stack on the same scale type, only the adjustments of the last-folded (highest
> `priority`) one are used.** The others are silently dropped — including their `delay` / `easing` /
> `persist`. If you need a specific transition, put it on the highest-priority rule.

## Modifiers

`modifiers` attaches entries from the `pehkui:scale_modifiers` registry to an entity's scale data,
the same thing `/scale modifier add` does:

```json
"modifiers": {
  "pehkui:motion": ["pehkui:motion_multiplier"]
}
```

Only **registered** modifier ids are accepted. This is a hard requirement rather than a
convenience: modifiers are synced to clients by id alone, so one that no client can resolve would
quietly change nothing there and leave the client showing a different size from the server.

## Conditions

`conditions` is a Minecraft **EntityPredicate** (the same object used by advancements). It accepts a number of sub-predicates keyed by name:

| Key                                  | Value                                                    | Matches                                                                                     |
|--------------------------------------|----------------------------------------------------------|---------------------------------------------------------------------------------------------|
| `type`                               | id, `#tag`, or array                                     | A specific type (`"minecraft:zombie"`), a type tag (`"#minecraft:skeletons"`), or one of a list. |
| `team`                               | string                                                   | Entities on a scoreboard team.                                                              |
| `location`                           | object                                                   | Where the entity is (biome, dimension, light, structure, …).                                |
| `stepping_on` / `movement_affected_by` | object                                                 | The block being stepped on / the block influencing movement.                                |
| `distance`                           | object                                                   | Distance to the player.                                                                     |
| `movement`                           | object                                                   | Relative movement speed.                                                                    |
| `effects`                            | object                                                   | Status effects.                                                                             |
| `nbt`                                | object                                                   | Entity NBT.                                                                                 |
| `flags`                              | object                                                   | `on_fire` / `sneaking` / `sprinting` / `swimming` / `baby`.                                 |
| `equipment` / `slots`                | object                                                   | Equipment as a set / by individual slot.                                                    |
| `periodic_tick`                      | integer                                                  | Entities whose tick count is a multiple of this.                                            |
| `vehicle` / `passenger` / `targeted_entity` | object                                            | The entity being ridden / a passenger / the mob's current attack target.                     |
| `type_specific`                      | object                                                   | A sub-predicate keyed by `type`. Registered kinds: `player`, `lightning`, `fishing_hook`, `slime`, `raider`. |

> **This version has no `entity_tags` key, and no top-level player conditions.** Gamemode, experience
> level and the like are not keys of `conditions` itself — they live inside `type_specific`, keyed by
> `"type": "minecraft:player"`:

```json
{ "conditions": {
    "type_specific": {
      "type": "minecraft:player",
      "gamemode": "creative",
      "level": { "min": 5 }
    }
} }
```

The player sub-predicate accepts `gamemode` (a mode name or array), `level` (an integer or
`{ "min": …, "max": … }`), plus `stats`, `recipes`, `advancements` and `looking_at`.

### Examples

One entity type:

```json
{ "conditions": { "type": ["minecraft:zombie"] } }
```

A type tag:

```json
{ "conditions": { "type": "#minecraft:skeletons" } }
```

Several types plus a team:

```json
{ "conditions": { "type": ["minecraft:zombie", "minecraft:husk"], "team": "boss" } }
```

Players in creative mode:

```json
{ "conditions": { "type_specific": { "type": "minecraft:player", "gamemode": "creative" } } }
```

> **Performance note**: prefer lightweight conditions (`type`, `team`). Heavy ones (`nbt`,
> `distance`, `location`) run for every candidate entity and can hurt server performance if many
> rules are defined.

### `fabric:load_conditions`

Fabric resource conditions gate whether a rule is loaded. Common ones:

| Condition                   | Meaning                                           |
|-----------------------------|---------------------------------------------------|
| `fabric:all_mods_loaded`    | True if **all** listed mods are installed.        |
| `fabric:any_mods_loaded`    | True if **at least one** listed mod is installed. |
| `fabric:tags_populated`     | True if the given tags are non-empty.             |
| `fabric:features_enabled`   | True if the given features are enabled.           |
| `fabric:registry_contains`  | True if the given registry entries exist.         |

```json
{
  "conditions": { "type": ["minecraft:zombie"] },
  "scales": { "pehkui:width": 0.5 },
  "fabric:load_conditions": [
    { 
		"condition": "fabric:any_mods_loaded", 
		"values": ["some_mob_mod", "another_mod"]
	}
  ]
}
```

### `neoforge:conditions`

The pack ships for both loaders, and each reads a **different key**:

| Loader   | Key read                       |
|----------|--------------------------------|
| Fabric   | `fabric:load_conditions`       |
| NeoForge | `neoforge:conditions` **and** `fabric:load_conditions` |

A rule carrying only the key the running loader does not read has it **silently ignored** — a rule you
intended to be conditional will load unconditionally.

> **On NeoForge, `fabric:load_conditions` only honours `all_mods_loaded` and `any_mods_loaded`.** The
> other three (`tags_populated`, `features_enabled`, `registry_contains`) are skipped there — they do
> not block the rule. Use `neoforge:conditions` if you need equivalent gating on that side.

## Scale Types

`scales` keys are Pehkui scale type ids. Built-in types (all under the `pehkui:` namespace):

| Scale type                                                                           | What it scales                                   |
|--------------------------------------------------------------------------------------|--------------------------------------------------|
| `pehkui:base`                                                                        | **Root scale** — most other types derive from it |
| `pehkui:width` / `pehkui:height`                                                     | Entity width / height                            |
| `pehkui:eye_height`                                                                  | Camera / eye height                              |
| `pehkui:hitbox_width` / `pehkui:hitbox_height`                                       | Actual collision box                             |
| `pehkui:model_width` / `pehkui:model_height`                                         | Rendered model size                              |
| `pehkui:interaction_box_width` / `pehkui:interaction_box_height`                     | Interaction box                                  |
| `pehkui:motion`                                                                      | Movement speed                                   |
| `pehkui:reach` / `pehkui:block_reach` / `pehkui:entity_reach`                        | Interaction distance                             |
| `pehkui:attack` / `pehkui:defense` / `pehkui:health`                                 | Combat stats                                     |
| `pehkui:jump_height` / `pehkui:step_height` / `pehkui:view_bobbing`                  | Movement feel                                    |
| `pehkui:projectiles` / `pehkui:explosions`                                           | Projectile / explosion effects                   |
| `pehkui:drops` / `pehkui:held_item`                                                  | Drops / held item rendering                      |
| `pehkui:knockback` / `pehkui:attack_speed` / `pehkui:mining_speed` / `pehkui:flight` | Misc                                             |
| `pehkui:visibility`                                                                  | Despawn/visibility radius                        |
| `pehkui:third_person`                                                                | Third-person model size                          |
| `pehkui:falling`                                                                     | Falling / gravity-related behaviour              |

### `pehkui:base` explained

`base` is the **root scale**. By default it is `1.0`, and raising it scales the entity up as a whole:
setting `base` to `2` roughly doubles width, height, movement, reach and the other derived dimensions
at once. It is the closest thing to a "scale the whole entity uniformly" switch.

The relationship is not "base rewrites width". Each derived type carries a **modifier** that reads
`base` when its *effective* value is computed:

```
effective_width = stored width × stored base
```

So `base` is folded in at read time as a [scale modifier](#modifiers) contribution, and the same goes for
`height`, `motion` and the rest. What each rule writes is the **stored** value of the scale type it names.

- **Scale everything proportionally** → set `pehkui:base`.
- **Change only the body shape without touching movement / reach / attack distance** → set
  `pehkui:width` / `pehkui:height` (or `pehkui:model_width` / `pehkui:model_height`) individually.

Because the fold-in happens at read time, a `width` value always has `base` applied to it — no matter
which rule wrote either one. Two rules setting `base` and `width` respectively each own the *stored*
value of their own scale type, but `width`'s effective value still gets multiplied by whatever `base`
holds. See [Stacking](#multiple-rules-stack) for how overlapping rules combine.

> **`base` also folds in the vanilla `minecraft:scale` attribute.** Pehkui adds a `pehkui:vanilla_scale`
> modifier to `base`, so an entity already resized through the attribute shows up here too. That fold-in
> is what the `applyVanillaScale` / `vanillaScaleSyncBack` config keys control.

## Behavior

### Periodic check

Every `scaleRuleCheckInterval` ticks (default **10 ticks = 0.5 seconds**), each server-side entity is matched against the rules. This only runs when `enableScaleRules` is on **and** at least one rule file has loaded — otherwise the whole pass is skipped.

- Every matching rule applies; the whole set is folded in ascending `priority` order.
- A scale type is **restored** (to the size it held **before any rule first took it over** — not to `1.0`, so a size the entity had for other reasons survives) only once **no matching rule covers that scale type any more**. If the entity merely stops matching *one* of several rules that set the same type, nothing is restored: the remaining rules are simply re-folded from the baseline.

### Multiple rules stack

When several rules match the same entity, **all of them apply**, in **ascending `priority` order** (lowest first). Each operation folds onto the running result.

For rules that set the same scale type to a plain number, the result is unchanged from before: the highest priority value wins, because it is folded last. `width 0.5` at priority 10 plus `width 2.0` at priority 20 still ends up at `2.0`.

> **A rule only shadows another for the scale types they *share*.** Since every matching rule contributes
> its own scale types, a low-priority rule setting `pehkui:width` still takes effect even when a
> higher-priority rule also matches the same mob — as long as that higher-priority rule does not set
> `pehkui:width` itself. Audit packs that assumed one rule suppressed another entirely.

### Entity creation timing

This differs by loader:

- **Fabric** — rules are only checked periodically, so a freshly spawned entity does **not** change instantly. It is scaled at the next check cycle, within `scaleRuleCheckInterval` ticks.
- **NeoForge** — an entity is additionally matched **immediately when it joins the world**, so it is scaled right away and then kept in sync by the periodic pass.

### Players

By default rules do **not** affect players (`scaleRulesAffectPlayers` is `false`); enable it to let rules apply to players too.

## Configuration

The rule-related knobs live in `config/pehkui/config.json` (the same file holds every other Pehkui option too; editable in-game via **ModMenu → Pehkui → Configuration** on Fabric). The rule keys:

```json
{
  "enableScaleRules": true,
  "scaleRuleCheckInterval": 10,
  "scaleRuleMaxScale": 256,
  "scaleRulesAffectPlayers": false
}
```

| Key                       | Default | Description                                                                  |
|---------------------------|---------|------------------------------------------------------------------------------|
| `enableScaleRules`        | `true`  | Master switch for datapack scale rules.                                      |
| `scaleRuleCheckInterval`  | `10`    | Ticks between rule checks. Lower = more responsive but more server overhead. **`0` disables the check entirely** (newly spawned mobs never get scaled). |
| `scaleRuleMaxScale`       | `256`   | Maximum value applied by rules.                                              |
| `scaleRulesAffectPlayers` | `false` | Whether rules may apply to players.                                          |

> **Caution**: raising `scaleRuleMaxScale` to extreme values can produce giant collision boxes that lag or crash the server. Keep it sane unless you know what you are doing.

## Troubleshooting

Rules not applying? Work through this checklist:

1. **File path / namespace wrong** — the file must be exactly at `data/<namespace>/pehkui_scale_rules/<name>.json`.
2. **JSON syntax error** — missing comma/quote; check the file parses as valid JSON.
3. **Invalid operand** — `NaN` or `Infinity` is rejected. Note that a negative operand is legal (`add -0.5`); it is the folded *result* that must end up finite and above `0`.
4. **Unknown operation / easing / modifier id** — the id must exist in the matching registry (`pehkui:scale_operations`, `pehkui:scale_easings`, `pehkui:scale_modifiers`).
5. **Missing required fields** — a rule is rejected when any of these is missing:
   - `conditions` at the top level;
   - `value` inside an operation object (an `operation` with no `value` is an error);
   - the file root is not a JSON object.
   And when these are malformed:
   - `delay` is negative;
   - `modifiers` maps a scale type to something that is not an array;
   - a `modifiers` entry names an unregistered modifier.
6. **Nothing survived parsing** — if `scales` and `modifiers` both end up empty (including an explicitly empty `"scales": {}`), the rule is rejected.
6. **Targeting players** — rules only affect players if `scaleRulesAffectPlayers` is `true`.
7. **Blocked by `fabric:load_conditions` / `neoforge:conditions`** — a required mod is not installed, or the key is the one the running loader does not read (see [the two keys](#neoforgeconditions)); the rule is skipped.

**Logs**: on the server, open `latest.log` and search for **`pehkui`**. Skipped/rejected rules are logged with the **file name** and a reason, e.g.:

```
Unknown scale type 'pehkui:foo' for 'scales' in 'example:pehkui_scale_rules/zombies.json'. Expected a registered type (e.g. 'pehkui:width').
Invalid value 'Infinity' for 'scales.pehkui:width' in 'example:pehkui_scale_rules/zombies.json'. Expected a finite number.
Unknown operation 'multply' for 'scales.pehkui:width' in 'example:pehkui_scale_rules/zombies.json'. Expected a registered operation (e.g. 'set', 'multiply', 'pehkui:add').
Unknown easing 'pehkui:quadraticout' for 'scales.pehkui:width' in 'example:pehkui_scale_rules/zombies.json'. Expected a registered easing (e.g. 'pehkui:quadratic_out').
Missing required 'conditions' field in 'example:pehkui_scale_rules/zombies.json'. Expected an EntityPredicate object (e.g. { "type": ["minecraft:zombie"] }).
```

The id in these messages is the rule's resource id (`<namespace>:pehkui_scale_rules/<file>.json`), not
the path on disk. When the rule declares a `name`, the messages show it first and the id in
parentheses — `Small Zombies (example:pehkui_scale_rules/zombies.json)` — so you can match a message
back to the rule you wrote.

A rule whose operations *parse* but **fold to a value that is not a usable scale** is caught twice.

**At load time**, once per scale type in the rule, folding from that scale type's default scale. (The
server parses rules more than once while starting up, so you may see the same line repeated.) This is
the one that names the file, so it is what to look for when a rule seems to do nothing:

```
Rule in 'example:pehkui_scale_rules/zombies.json' folds to -4.0 for scale type 'pehkui:width' starting from that type's default scale, which is not a usable scale. Affected entities keep their existing value for that type.
```

**At runtime**, folded from the entity's real scale. This is the authoritative check — a rule can be
fine at the default and still fail on a differently-sized mob. Only that scale type is left alone;
the rest of the rule still applies. It is reported **once per scale type per JVM**, not once per
entity per check cycle, so one bad rule cannot flood the console no matter how many mobs are loaded.
Note this suppression is process-wide and is **never reset** — not even by `/reload` or by loading a
different world in the same client session:

```
Scale rule folds to the unusable value -4.0 for scale type 'pehkui:width' (baseline 1.0). Leaving that scale type untouched. Further reports for this scale type are suppressed for the rest of this process run.
```

## Advanced Examples

> The blocks below are **valid JSON** — copy them as-is. The file path each one belongs at is given in
> the prose above it, not inside the block (JSON has no comments).

### 1. Two conditions: team + entity type tag

Only skeletons in the `boss` team are enlarged. Save as
`data/example/pehkui_scale_rules/boss_skeletons.json`:

```json
{
  "conditions": {
    "type": "#minecraft:skeletons",
    "team": "boss"
  },
  "scales": { "pehkui:height": 2.0, "pehkui:width": 2.0 },
  "priority": 20
}
```

### 2. Combat scaling set

Resize a mob and boost its combat stats at the same time. Save as
`data/example/pehkui_scale_rules/elite_zombie.json`:

```json
{
  "conditions": { "type": ["minecraft:zombie"], "team": "elite" },
  "scales": {
    "pehkui:height": 1.5,
    "pehkui:width": 1.5,
    "pehkui:motion": 1.2,
    "pehkui:health": 2.0,
    "pehkui:attack": 1.5,
    "pehkui:entity_reach": 1.5
  }
}
```

### 3. Mod-dependent rules

Only load a rule when a specific mob mod is installed. Save as
`data/example/pehkui_scale_rules/extra_mobs.json`:

```json
{
  "conditions": { "type": "#example:extra_mobs" },
  "scales": { "pehkui:width": 0.7 },
  "fabric:load_conditions": [
    { "condition": "fabric:all_mods_loaded", "values": ["example_mob_mod"] }
  ]
}
```

### 4. Relative resizes that compose

Shrink every skeleton by 25%, then make the `boss` team ones twice as large again — neither rule has
to know the other exists.

Save the first as `data/example/pehkui_scale_rules/skeletons.json`:

```json
{
  "conditions": { "type": "#minecraft:skeletons" },
  "scales": { "pehkui:base": { "operation": "multiply", "value": 0.75 } },
  "priority": 0
}
```

Save the second as `data/example/pehkui_scale_rules/boss_skeletons_bonus.json` (a *different* file name
from example 1 — same-named files would overwrite each other):

```json
{
  "conditions": { "type": "#minecraft:skeletons", "team": "boss" },
  "scales": { "pehkui:base": { "operation": "multiply", "value": 2.0 } },
  "priority": 10
}
```

A boss skeleton lands on `1.0 × 0.75 × 2.0 = 1.5`. Both rules target `pehkui:base`, and the starting
point is the size the entity had before either took over — if something else had already set it to
`2.0`, the result would be `3.0`.

### 5. Smooth transition

Eases into the new size over two seconds instead of snapping to it. Save as
`data/example/pehkui_scale_rules/elite_growth.json`:

```json
{
  "conditions": { "type": ["minecraft:zombie"], "team": "elite" },
  "scales": {
    "pehkui:base": {
      "operation": "set",
      "value": 1.5,
      "delay": 40,
      "easing": "pehkui:quadratic_out"
    }
  }
}
```
