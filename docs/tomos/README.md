# Tomo Species (MVP)

Each file in this folder describes one Tomo species: its name, rarity and look. The **Appearance** section of each file is a complete prompt. You can paste it into an image generator as it is. For extended asset generation prompts (movement sprites, UI avatars, mood expression sheets, and platform settings), see [prompts.md](prompts.md).

## Roster

| Name | Code | Rarity | Starter | In gacha pool | Element hint | File |
|---|---|---|---|---|---|---|
| Luppy | `luppy` | Common | Yes | No | Fire | [luppy.md](luppy.md) |
| Rocky | `rocky` | Common | Yes | No | Earth | [rocky.md](rocky.md) |
| Aeroblast | `aeroblast` | Common | Yes | No | Wind | [aeroblast.md](aeroblast.md) |

These are the 3 starters for [UC-02 Choose Starter Tomo](../usecases/UC-02-choose-starter-tomo.md). Starters are Common and do not appear in the gacha (UC-02 BR-1). The gacha pool for [UC-12](../usecases/UC-12-summon-tomo.md) is not decided yet.

## Element Hints

The MVP has no elements. Each species still looks like it belongs to one, so elements can be added later and Players can guess them from the art alone.

| Tomo | Animal | Element hint | Color | Shapes | Element cue | Matches a Player who likes to |
|---|---|---|---|---|---|---|
| Luppy | Dachshund puppy | Fire | Ginger orange, red | Lively, pointed | Flame-shaped tuft on its tail | Jog |
| Rocky | Tortoise | Earth | Moss green, stone gray | Heavy, round | Shell like a mossy boulder | Walk |
| Aeroblast | Rabbit | Wind | Sky blue, white | Light, curling | Ears that curl into wind swirls, cloud-puff tail | Sprint |

The element hint and the Player match are flavor only. In the MVP, every species earns the same rewards.

## Naming

Names must be easy to read and say for both English and Indonesian speakers:

- Build names from English words that most Indonesians already know, such as *rock*, *aero* and *blast*. Avoid words that only one group knows, from either language.
- A name can be a common word, a play on one, two words joined together, or an anagram of a common word.
- Check for unfortunate meanings in both languages. For example, *ember* means "bucket" in Indonesian.
- Use a different first letter from every other species.
- Where possible, hint at the animal or the element.

| Name | Pronunciation | From |
|---|---|---|
| Luppy | LUP-ee | A play on *puppy* |
| Rocky | ROK-ee | *rock* |
| Aeroblast | AIR-oh-blast | *aero* ("air") + *blast* |

## Shared Art Style

Every Appearance prompt follows the soft pastel kawaii anime art style detailed in [prompts.md](prompts.md), ending with this shared formula:

> Cute pastel kawaii anime illustration for a mobile fitness pet game. 2D character art, full body, three-quarter front view, centered. Squishy baby chibi proportions, big head and chubby cheeks with soft airbrushed blush, large soulful glossy dark eyes with warm reflections. Delicate warm brown lineart, soft pastel watercolor washes, gentle ambient shading. Plain white background, no harsh black lines, no scenery, no text.

Keep these rules when you add a species:

- One main color per species, so Players can tell Tomos apart at a glance. The main color also suggests the element hint.
- One element cue: a body part that shows the element, such as Luppy's flame-shaped tail tip.
- Shapes that fit the element: pointed for Fire, round and heavy for Earth, curling for Wind.
- A clear silhouette that still reads at small sprite sizes.
- At most one accessory, which hints at a kind of exercise.

## Fields

Species are game data, not database rows. The fields go in the server's species resource file (`server/src/main/resources/game/`). Player data refers to a species by its code ([ERD](../erd.md)).

| Field | Game data |
|---|---|
| Name | name |
| Code | code (permanent, never renamed) |
| Rarity | rarity |
| Starter | starter flag |
| In gacha pool | in a banner's species list, with a weight ([UC-12](../usecases/UC-12-summon-tomo.md)) |
| Element hint | None yet. Add a field if elements are added. |
