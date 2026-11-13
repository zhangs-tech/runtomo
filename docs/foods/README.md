# Foods (MVP)

Each file in this folder describes one food item for [UC-18 Feed Tomo](../usecases/UC-18-feed-tomo.md): its name, cost, Bond and look. The **Appearance** section of each file is a complete prompt. You can paste it into an image generator as it is.

## Menu

| Name | Code | Cost | Bond | TomoCoins per Bond | File |
|---|---|---:|---:|---:|---|
| Banana | `banana` | 50 | +3 | 16.7 | [banana.md](banana.md) |
| Onigiri | `onigiri` | 100 | +6 | 16.7 | [onigiri.md](onigiri.md) |
| Bento | `bento` | 150 | +10 | 15 | [bento.md](bento.md) |

Cost and Bond are *(tunable)*. Bigger foods give slightly more Bond per TomoCoin, so saving up is worth it. Bento has the most Bond, and a Tomo can eat at most 3 times per day, so food gives at most 30 Bond per day (UC-18 BR-3). Keep the top food at +10 Bond or less, or change BR-3 too.

## Naming

Food names follow the same rule as Tomo names ([tomos/README.md](../tomos/README.md#naming)): English and Indonesian speakers must both know the word. *Banana*, *onigiri* and *bento* are all familiar in Indonesia.

## Shared Art Style

Foods use the same soft pastel kawaii style as the Tomos ([tomos/prompts.md](../tomos/prompts.md)), so they look right next to a Tomo on the Home Screen. Every Appearance prompt ends with this shared formula:

> Cute pastel kawaii anime illustration of a single food item for a mobile fitness pet game. 2D item icon, centered, slightly from above. Chubby rounded shapes, soft glossy highlights. Delicate warm brown lineart, soft pastel watercolor washes, gentle ambient shading. Plain white background, no face on the food, no harsh black lines, no scenery, no text.

Keep these rules when you add a food:

- No faces on food. The Tomo eats it.
- One main color per food, so Players can tell items apart at icon size.
- A clear silhouette that still reads at about 48 dp, the size of a shop or inventory slot.
- Bigger foods look bigger: a single snack, then a handheld meal, then a full box.

## Fields

Foods are game data, not database rows. The fields go in the server's food resource file (`server/src/main/resources/game/`). Player data refers to a food by its code (`food_code`, see the [ERD](../erd.md)).

| Field | Game data |
|---|---|
| Name | name |
| Code | code (permanent, never renamed) |
| Cost | cost in TomoCoins |
| Bond | Bond points per feeding |
