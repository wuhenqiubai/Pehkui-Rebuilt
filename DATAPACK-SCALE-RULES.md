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

`pack.mcmeta` minimum content:

The pack format can be found in the [Minecraft Wiki - Pack format section](https://minecraft.wiki/w/Pack_format#Data_pack_format_history)

```json
{
	"pack": { 
		"pack_format": 107, 
		"description": "My scale rules",
		"min_format": 48,
		"max_format": 107
	}
}
```

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
	  "pehkui:width": 0.5, "pehkui:height": 0.5 // required, what to set
	},
	"priority": 10,                                   // optional, default 0
	"fabric:load_conditions": [ ... ]                 // optional, mod-load gating
}
```

| Field                    | Required | Description                                                                |
|--------------------------|----------|----------------------------------------------------------------------------|
| `name`                   | no       | Display name of the rule (used for logging/tooltips).                      |
| `description`            | no       | Longer description of the rule.                                            |
| `conditions`             | **yes**  | An [EntityPredicate](#conditions) matching entities this rule applies to.  |
| `scales`                 | **yes*** | Map of `pehkui:<scale_type>` to the value to set.                          |
| `scale_type` + `value`   | *alt*    | Legacy single-scale form: `"scale_type": "pehkui:width", "value": 0.5`.    |
| `priority`               | no       | Higher priority rules win when several match the same entity. Default `0`. |
| `fabric:load_conditions` | no       | Fabric resource conditions; the rule is skipped if not met.                |

\* Either `scales` (multi) or the legacy `scale_type` + `value` pair must be present. A rule with no valid scales is rejected and skipped (see [Troubleshooting](#troubleshooting)).

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

If you set both `base` and `width` in the **same** rule, they are independent dimensions but `width`'s effective value still derives from `base`. If two **different** rules set `base` and `width`, only the higher-`priority` rule applies (they do not combine).

## Behavior

### Periodic check

Every `scaleRuleCheckInterval` ticks (default **10 ticks = 0.5 seconds**), each server-side entity is matched against the rules:

- If a rule matches, its scales are applied.
- If an entity **stops** matching a previously applied rule, **all scales that rule had set are reset to their default values** (1.0 / vanilla). This also happens when a higher-priority rule takes over.

### Multiple rules do not stack

When several rules match the same entity, **only the one with the highest `priority` applies** — scale values are **never multiplied or added** across rules. This is a common misunderstanding: two rules setting `width 0.5` and `width 2.0` on the same mob do **not** result in `1.0`; the higher-priority rule wins outright.

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
3. **Invalid scale value** — values ≤ 0, `NaN`, or `Infinity` are rejected (see log).
4. **Missing required fields** — `conditions` and (`scales` or `scale_type`+`value`) are required.
5. **Targeting players** — rules only affect players if `scaleRulesAffectPlayers` is `true`.
6. **Blocked by `fabric:load_conditions`** — a required mod is not installed; the rule is skipped.

**Logs**: on the server, open `latest.log` and search for **`pehkui`**. Skipped/rejected rules are logged with the **file name** and a reason, e.g.:

```
Unknown scale type 'pehkui:foo' in 'data/example/pehkui_scale_rules/zombies.json'. Expected a registered type (e.g. 'pehkui:width').
Invalid value 'Infinity' for 'scales.pehkui:width' in 'data/example/pehkui_scale_rules/zombies.json'. Expected a finite value greater than 0 (max 256.0).
Missing required 'conditions' field in 'data/example/pehkui_scale_rules/zombies.json'. Expected an EntityPredicate object (e.g. { "entity_type": ["minecraft:zombie"] }).
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
