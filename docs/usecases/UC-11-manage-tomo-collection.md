# UC-11: Manage Tomo Collection

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Must |

## Goal

The Player browses the Tomos they own, chooses which one to walk with, and can rename them.

## Preconditions

- The Player is signed in.

## Trigger

The Player opens the **Tomos** tab.

## Main Flow

1. The app shows a grid of every species in the MVP roster. Owned Tomos show their art. Species not owned yet show a silhouette.
2. The app shows collection progress, for example **4 / 10 collected**.
3. The Player taps an owned Tomo.
4. The app shows its detail card: art, nickname, species, rarity, Bond Level, Distance Together and Birthday.
5. The Player taps **Equip**.
6. The System makes it the equipped Tomo, and the Home Screen updates.

## Alternate / Exception Flows

- **3a. The Player taps a silhouette:** Show the species name and rarity, with a hint to summon it ([UC-12](UC-12-summon-tomo.md)).
- **5a. The Player taps Rename:** The Player enters a new nickname (1–16 characters), and the System saves it.
- **5b. An activity is being tracked right now:** The Player cannot equip a different Tomo until it ends.
- **1a. Sort / filter:** The Player can sort by rarity, Bond or Distance Together.

## Postconditions

- The chosen Tomo is equipped, or a nickname has been updated.

## Business Rules

- BR-1: Exactly one Tomo is equipped at any time.
- BR-2: Each Tomo keeps its own Bond and Distance Together. Switching Tomos does not move progress from one to another.
- BR-3: The data model stores species and skin separately, so skins can be added later. The MVP uses only the default skin.

## Related

- [UC-02 Choose Starter Tomo](UC-02-choose-starter-tomo.md)
- [UC-12 Summon Tomo](UC-12-summon-tomo.md)

## Open Questions

- Can a Player own more than one Tomo of the same species, or do duplicates always convert ([UC-12](UC-12-summon-tomo.md) BR-4)?
