# Pehkui Datapack Scale Rules

Pehkui Rebuilt lets **datapacks** define scale rules: entities matching a set of conditions get their scale types set to configured values automatically. This is useful for servers and modpacks that want to resize certain mobs without writing code.

## File Location

Rules are loaded from every datapack, in:

```
data/<namespace>/pehkui_scale_rules/<id>.json
```

All JSON files in that folder across all loaded datapacks are merged. On servers the rules are the server's source of truth; clients do not load them (scales are synced to clients by the mod).

## Rule Format

```json
{
  "name": "Small Zombies",
  "description": "Zombies are 50% width and height",
  "conditions": { "entity_type": ["minecraft:zombie"] },
  "scales": { "pehkui:width": 0.5, "pehkui:height": 0.5 },
  "priority": 10,
  "fabric:load_conditions": [
    { "condition": "fabric:all_mods_loaded", "values": ["some_mod"] }
  ]
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | string | no | Display name of the rule (used for logging/tooltips). |
| `description` | string | no | Longer description of the rule. |
| `conditions` | object | **yes** | An [EntityPredicate](#conditions) matching entities this rule applies to. |
| `scales` | object | **yes*** | Map of `pehkui:<scale_type>` to the value to set. |
| `scale_type` + `value` | string + number | *alt* | Legacy single-scale form: equivalent to `"scales": { "<scale_type>": <value> }`. |
| `priority` | int | no | Higher priority rules win when several match the same entity. Default `0`. |
| `fabric:load_conditions` | array | no | Fabric resource conditions; the rule is skipped if they are not met (e.g. a mod is not installed). |

\* Either `scales` (multi) or the legacy `scale_type` + `value` pair must be present. A rule with no valid scales is rejected and skipped.

## Conditions

`conditions` is a Minecraft **EntityPredicate** (the same object used by advancements). It accepts a number of sub-predicates keyed by name. The most useful ones:

| Key | Value | Matches |
|---|---|---|
| `entity_type` | id string, `#tag` string, or array | Entities of a specific type (e.g. `"minecraft:zombie"`), a tag (`"#minecraft:skeletons"`), or one of a list. |
| `entity_tags` | `{ "any_of": [...], "all_of": [...], "none_of": [...] }` | Entities in the given tags. |
| `team` | string | Entities on a scoreboard team. |
| `gamemode` | ... | Player gamemode. |
| `level` | ... | Player experience level. |
| `scores` | ... | Scoreboard scores. |
| `predicate` | ... | Another datapack predicate id. |
| `flags` / `effects` / `equipment` / `nbt` / `distance` ... | ... | Other vanilla predicate fields. |

### Examples

```json
{ "conditions": { "entity_type": ["minecraft:zombie"] } }
{ "conditions": { "entity_type": "#minecraft:skeletons" } }
{ "conditions": { "entity_type": ["minecraft:zombie", "minecraft:husk"], "team": "boss" } }
{ "conditions": { "entity_tags": { "all_of": ["#minecraft:skeletons"] } } }
```

> **Performance note**: prefer lightweight conditions (`entity_type`, `team`, `gamemode`). Heavy ones (`nbt`, `distance`, `predicate`) run for every candidate entity and can hurt server performance if many rules are defined.

## Scale Types

`scales` keys are Pehkui scale type ids. Built-in types (all under the `pehkui:` namespace):

| Scale type | What it scales |
|---|---|
| `pehkui:base` | Root scale — most other types derive from it |
| `pehkui:width` / `pehkui:height` | Entity width / height |
| `pehkui:eye_height` | Camera / eye height |
| `pehkui:hitbox_width` / `pehkui:hitbox_height` | Actual collision box |
| `pehkui:model_width` / `pehkui:model_height` | Rendered model size |
| `pehkui:interaction_box_width` / `pehkui:interaction_box_height` | Interaction box |
| `pehkui:motion` | Movement speed |
| `pehkui:reach` / `pehkui:block_reach` / `pehkui:entity_reach` | Interaction distance |
| `pehkui:attack` / `pehkui:defense` / `pehkui:health` | Combat stats |
| `pehkui:jump_height` / `pehkui:step_height` / `pehkui:view_bobbing` | Movement feel |
| `pehkui:projectiles` / `pehkui:explosions` | Projectile / explosion effects |
| `pehkui:drops` / `pehkui:held_item` | Drops / held item rendering |
| `pehkui:knockback` / `pehkui:attack_speed` / `pehkui:mining_speed` / `pehkui:flight` | Misc |
| `pehkui:visibility` | Despawn/visibility radius |

## Behavior

- **Periodic check**: every `scaleRuleCheckInterval` ticks (default `10`), each server-side entity is matched against the rules. Matching scales are applied; if an entity stops matching a previously applied rule, its scale is reset to default.
- **Priority**: when several rules match one entity, the one with the highest `priority` wins.
- **Players**: by default rules do **not** affect players (`scaleRulesAffectPlayers` is `false`); enable it to let rules apply to players too.

## Safety Limits

To keep servers safe from malicious or broken datapacks:

- Scale values are clamped to `scaleRuleMaxScale` (default `256`).
- Non-finite (`Infinity`, `NaN`) and non-positive (`<= 0`) values are rejected and logged; the rule is skipped.
- Rule files are limited to 1 MB; deeply nested or malformed JSON is skipped without breaking the reload.

## Configuration

All knobs live in `config/pehkui/config.json` (editable in-game via ModMenu → Pehkui):

| Key | Default | Description |
|---|---|---|
| `enableScaleRules` | `true` | Master switch for datapack scale rules. |
| `scaleRuleCheckInterval` | `10` | Ticks between rule checks. |
| `scaleRuleMaxScale` | `256` | Maximum value applied by rules. |
| `scaleRulesAffectPlayers` | `false` | Whether rules may apply to players. |

## Full Example

Resize all skeletons to 2x height, zombies to 50% width & height, and creepers to 30% width:

```json
// data/example/pehkui_scale_rules/skeletons.json
{
  "conditions": { "entity_type": "#minecraft:skeletons" },
  "scales": { "pehkui:height": 2.0 }
}
```

```json
// data/example/pehkui_scale_rules/zombies.json
{
  "conditions": { "entity_type": ["minecraft:zombie", "minecraft:husk", "minecraft:drowned"] },
  "scales": { "pehkui:width": 0.5, "pehkui:height": 0.5 },
  "priority": 10
}
```

```json
// data/example/pehkui_scale_rules/creepers.json
{
  "conditions": { "entity_type": ["minecraft:creeper"] },
  "scales": { "pehkui:width": 0.3 }
}
```
