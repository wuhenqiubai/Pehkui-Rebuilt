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

`pack.mcmeta` minimum content — the data pack format for 26.3 is **121**:

```json
{
	"pack": {
		"description": "My scale rules",
		"min_format": 121,
		"max_format": 121
	}
}
```

> **Format numbers changed.** Since 1.21.9 a pack declares a `min_format` / `max_format` range
> rather than a bare `pack_format`, and a `pack_format` above **81** is *rejected* for data packs.
> Copying the old single-number style from an older example gives you a pack that silently fails to
> load. Current numbers: [Minecraft Wiki - Pack format](https://minecraft.wiki/w/Pack_format#Data_pack_format_history).

### 2. Understand namespaces

The **namespace** is the name of the sub-folder under `data/`. It must be a valid identifier (lowercase letters, digits, `_`, `-`, `.`). It does **not** have to match the datapack folder name. Multiple datapacks can use the same or different namespaces — all `pehkui_scale_rules` folders across all datapacks are merged.

### 3. Reload

After creating or editing a rule file, run **`/reload`** in-game (requires operator permission). Rules are only applied on the server and take effect after the reload.

## Rule Format

```json
{
	"name": "Small Zombies",                          // optional, display name
	"description": "Zombies are 50% width and height",// optional, description
	"conditions": {
	  "entity_type": ["minecraft:zombie"]  // required, what to match
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
| `name`                   | no       | Display name of the rule (used for logging/tooltips).                                     |
| `description`            | no       | Longer description of the rule.                                                           |
| `conditions`             | **yes**  | An [EntityPredicate](#conditions) matching entities this rule applies to.                 |
| `scales`                 | **yes*** | Map of `pehkui:<scale_type>` to a number (a `set`) or an [operation object](#operations). |
| `modifiers`              | no       | Map of `pehkui:<scale_type>` to an array of [registered modifier ids](#modifiers).        |
| `scale_type` + `value`   | *alt*    | Legacy single-scale form: `"scale_type": "pehkui:width", "value": 0.5`.                   |
| `priority`               | no       | Ordering when several rules match; see [Stacking](#multiple-rules-stack). Default `0`.    |
| `fabric:load_conditions` | no       | Fabric resource conditions; the rule is skipped if not met.                               |

\* Either `scales`, `modifiers`, or the legacy `scale_type` + `value` pair must be present. A rule with none of them is rejected and skipped (see [Troubleshooting](#troubleshooting)).

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
| `delay`   | integer | Ticks the size takes to interpolate to the new value (0 = instant).     |
| `easing`  | id      | Easing curve, e.g. `pehkui:quadratic_out`, `pehkui:bounce_in_out`.      |
| `persist` | boolean | Whether this scale type is saved to the entity's NBT.                   |

They apply while a rule covers that scale type, and are restored to whatever they were before when
the rule stops matching.

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

| Key                                                        | Value                                                    | Matches                                                                                     |
|------------------------------------------------------------|----------------------------------------------------------|---------------------------------------------------------------------------------------------|
| `entity_type`                                              | id, `#tag`, or array                                     | A specific type (`"minecraft:zombie"`), a tag (`"#minecraft:skeletons"`), or one of a list. |
| `entity_tags`                                              | `{ "any_of": [...], "all_of": [...], "none_of": [...] }` | Entities in the given tags.                                                                 |
| `team`                                                     | string                                                   | Entities on a scoreboard team.                                                              |
| `gamemode`                                                 | object                                                   | Player gamemode.                                                                            |
| `level`                                                    | object                                                   | Player experience level.                                                                    |
| `scores`                                                   | object                                                   | Scoreboard scores.                                                                          |
| `predicate`                                                | id                                                       | Another datapack predicate id.                                                              |
| `flags` / `effects` / `equipment` / `nbt` / `distance` ... | ...                                                      | Other vanilla predicate fields.                                                             |

### Examples

```json
{ "conditions": { "entity_type": ["minecraft:zombie"] } }              // one type
{ "conditions": { "entity_type": "#minecraft:skeletons" } }            // a tag
{ "conditions": { "entity_type": ["minecraft:zombie", "minecraft:husk"], "team": "boss" } }  // type + team
{ "conditions": { "entity_tags": { "all_of": ["#minecraft:skeletons"] } } }
```

> **Performance note**: prefer lightweight conditions (`entity_type`, `team`, `gamemode`). Heavy ones (`nbt`, `distance`, `predicate`) run for every candidate entity and can hurt server performance if many rules are defined.

### `fabric:load_conditions`

Fabric resource conditions gate whether a rule is loaded. Common ones:

| Condition           | Meaning                                           |
|---------------------|---------------------------------------------------|
| `all_mods_loaded`   | True if **all** listed mods are installed.        |
| `any_mods_loaded`   | True if **at least one** listed mod is installed. |
| `tags_populated`    | True if the given tags are non-empty.             |
| `features_enabled`  | True if the given features are enabled.           |
| `registry_contains` | True if the given registry entries exist.         |

```json
{
  "conditions": { "entity_type": ["minecraft:zombie"] },
  "scales": { "pehkui:width": 0.5 },
  "fabric:load_conditions": [
    { 
		"condition": "fabric:any_mods_loaded", 
		"values": ["some_mob_mod", "another_mod"]
	}
  ]
}
```

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

### `pehkui:base` explained

`base` is the **root scale** that most other types derive from: setting `base` to `2` roughly doubles width, height, movement, reach, and other derived dimensions at once. It is the closest thing to a "scale the whole entity uniformly" switch.

The derived relationship is `width = base × width`, `height = base × height`, etc. In practice:

- **Scale everything proportionally** → set `pehkui:base`.
- **Change only the body shape without touching movement / reach / attack distance** → set `pehkui:width` / `pehkui:height` (or `pehkui:model_width` / `pehkui:model_height`) individually.

If you set both `base` and `width` in the **same** rule, they are independent dimensions but `width`'s effective value still derives from `base`. Two **different** rules setting `base` and `width` apply to their own scale type independently — see [Stacking](#multiple-rules-stack) for how overlapping rules combine.

## Behavior

### Periodic check

Every `scaleRuleCheckInterval` ticks (default **10 ticks = 0.5 seconds**), each server-side entity is matched against the rules:

- Every matching rule applies; the whole set is folded in ascending `priority` order.
- If an entity **stops** matching, the affected scale types are restored to the size they held **before a rule first took them over** — not to the default `1.0`, so a size the entity had for other reasons survives.

### Multiple rules stack

When several rules match the same entity, **all of them apply**, in **ascending `priority` order** (lowest first). Each operation folds onto the running result.

For rules that set the same scale type to a plain number, the result is unchanged from before: the highest priority value wins, because it is folded last. `width 0.5` at priority 10 plus `width 2.0` at priority 20 still ends up at `2.0`.

> **Migration note for existing datapacks.** Coverage of rules that touch *different* scale types has
> changed. Previously only the single highest-priority matching rule was applied and every other
> matching rule was discarded entirely; now each matching rule contributes its own scale types. A
> low-priority rule that sets `pehkui:width` used to be silently ignored whenever a higher-priority
> rule matched the same mob for any reason — including one that only sets `pehkui:health`. It now
> takes effect. Audit packs that rely on one rule shadowing another.

### Entity creation timing

Rules are checked periodically, so a freshly spawned entity does **not** change instantly — it is scaled at the next check cycle (within `scaleRuleCheckInterval` ticks).

### Players

By default rules do **not** affect players (`scaleRulesAffectPlayers` is `false`); enable it to let rules apply to players too.

## Configuration

All knobs live in `config/pehkui/config.json` (editable in-game via **ModMenu → Pehkui → Configuration** on Fabric). Full minimal template:

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
| `scaleRuleCheckInterval`  | `10`    | Ticks between rule checks. Lower = more responsive but more server overhead. |
| `scaleRuleMaxScale`       | `256`   | Maximum value applied by rules.                                              |
| `scaleRulesAffectPlayers` | `false` | Whether rules may apply to players.                                          |

> **Caution**: raising `scaleRuleMaxScale` to extreme values can produce giant collision boxes that lag or crash the server. Keep it sane unless you know what you are doing.

## Troubleshooting

Rules not applying? Work through this checklist:

1. **File path / namespace wrong** — the file must be exactly at `data/<namespace>/pehkui_scale_rules/<name>.json`.
2. **JSON syntax error** — missing comma/quote; check the file parses as valid JSON.
3. **Invalid operand** — `NaN` or `Infinity` is rejected. Note that a negative operand is legal (`add -0.5`); it is the folded *result* that must end up finite and above `0`.
4. **Unknown operation / easing / modifier id** — the id must exist in the matching registry (`pehkui:scale_operations`, `pehkui:scale_easings`, `pehkui:scale_modifiers`).
5. **Missing required fields** — `conditions` and (`scales`, `modifiers`, or `scale_type`+`value`) are required.
6. **Targeting players** — rules only affect players if `scaleRulesAffectPlayers` is `true`.
7. **Blocked by `fabric:load_conditions`** — a required mod is not installed; the rule is skipped.

**Logs**: on the server, open `latest.log` and search for **`pehkui`**. Skipped/rejected rules are logged with the **file name** and a reason, e.g.:

```
Unknown scale type 'pehkui:foo' for 'scales' in 'example:pehkui_scale_rules/zombies.json'. Expected a registered type (e.g. 'pehkui:width').
Invalid value 'Infinity' for 'scales.pehkui:width' in 'example:pehkui_scale_rules/zombies.json'. Expected a finite number.
Unknown operation 'multply' for 'scales.pehkui:width' in 'example:pehkui_scale_rules/zombies.json'. Expected a registered operation (e.g. 'set', 'multiply', 'pehkui:add').
Unknown easing 'pehkui:quadraticout' for 'scales.pehkui:width' in 'example:pehkui_scale_rules/zombies.json'. Expected a registered easing (e.g. 'pehkui:quadratic_out').
Missing required 'conditions' field in 'example:pehkui_scale_rules/zombies.json'. Expected an EntityPredicate object (e.g. { "entity_type": ["minecraft:zombie"] }).
```

The id in these messages is the rule's resource id (`<namespace>:pehkui_scale_rules/<file>.json`), not the path on disk.

A rule whose operations *parse* but fold to a value that is not a usable scale is reported
separately, and only that scale type is left alone — the rest of the rule still applies:

```
Scale rule produced an invalid value -1.0 for scale type 'pehkui:width' (baseline 1.0). Leaving that scale type untouched.
```

## Advanced Examples

### 1. Two conditions: team + entity tag

Only skeletons in the `boss` team are enlarged:

```json
// data/example/pehkui_scale_rules/boss_skeletons.json
{
  "conditions": {
    "entity_type": "#minecraft:skeletons",
    "team": "boss"
  },
  "scales": { "pehkui:height": 2.0, "pehkui:width": 2.0 },
  "priority": 20
}
```

### 2. Combat scaling set

Resize a mob and boost its combat stats at the same time:

```json
// data/example/pehkui_scale_rules/elite_zombie.json
{
  "conditions": { "entity_type": ["minecraft:zombie"], "team": "elite" },
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

Only load a rule when a specific mob mod is installed:

```json
// data/example/pehkui_scale_rules/extra_mobs.json
{
  "conditions": { "entity_type": "#example:extra_mobs" },
  "scales": { "pehkui:width": 0.7 },
  "fabric:load_conditions": [
    { "condition": "fabric:all_mods_loaded", "values": ["example_mob_mod"] }
  ]
}
```

### 4. Relative resizes that compose

Shrink every skeleton by 25%, then make the `boss` team ones twice as large again — neither rule
has to know the other exists:

```json
// data/example/pehkui_scale_rules/skeletons.json
{
  "conditions": { "entity_type": "#minecraft:skeletons" },
  "scales": { "pehkui:base": { "operation": "multiply", "value": 0.75 } },
  "priority": 0
}
```

```json
// data/example/pehkui_scale_rules/boss_skeletons.json
{
  "conditions": { "entity_type": "#minecraft:skeletons", "team": "boss" },
  "scales": { "pehkui:base": { "operation": "multiply", "value": 2.0 } },
  "priority": 10
}
```

A boss skeleton lands on `1.0 × 0.75 × 2.0 = 1.5`. Both rules target `pehkui:base`, and the
starting point is the size the entity had before either took over — if something else had already
set it to `2.0`, the result would be `3.0`.

### 5. Smooth transition

```json
// data/example/pehkui_scale_rules/elite_growth.json
{
  "conditions": { "entity_type": ["minecraft:zombie"], "team": "elite" },
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

Eases into the new size over two seconds instead of snapping to it.
