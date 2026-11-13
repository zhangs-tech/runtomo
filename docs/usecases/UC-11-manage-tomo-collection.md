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

1. The System shows a grid of every species in the MVP roster. Owned Tomos show their art. Species not owned yet show a silhouette.
2. The System shows collection progress, for example **4 / 10 collected**.
3. The Player taps an owned Tomo.
4. The System shows its detail card: art, nickname, species, rarity, Bond Level, Distance Together and Birthday.
5. The Player taps **Equip**.
6. The System makes it the equipped Tomo, and the Home Screen updates.

## Alternate / Exception Flows

- **1a. Sort / filter:** The Player can sort by rarity, Bond or Distance Together.
- **3a. The Player taps a silhouette:** The System shows the species name and rarity, with a hint to summon it ([UC-12](UC-12-summon-tomo.md)).
- **5a. The Player taps Rename:** The Player enters a new nickname (1–16 characters), and the System saves it.

## Postconditions

- The chosen Tomo is equipped, or the Tomo has its new nickname.

## Business Rules

- BR-1: Exactly one Tomo is equipped at any time.
- BR-2: Each Tomo keeps its own Bond and Distance Together. Switching Tomos does not move progress from one to another.
- BR-3: A Player owns at most one Tomo of each species. A summoned duplicate converts to TomoCoins ([UC-12](UC-12-summon-tomo.md) BR-4).

## Related

- [UC-02 Choose Starter Tomo](UC-02-choose-starter-tomo.md)
- [UC-12 Summon Tomo](UC-12-summon-tomo.md)

## Diagrams

```mermaid
sequenceDiagram
    actor P as Player
    participant A as App
    participant S as Server

    P->>A: Open Tomos tab
    A->>S: GET /me/tomos
    S-->>A: Every species, with the Player's Tomos marked
    A-->>P: Grid and collection progress
    P->>A: Tap an owned Tomo
    A-->>P: Detail card
    alt Equip
        P->>A: Tap Equip
        A->>S: PUT /me/equipped-tomo (tomoId)
        S->>S: Check the Player owns it, set it as equipped
        S-->>A: 200
        A-->>P: Equipped, Home Screen updates
    else Rename
        P->>A: Tap Rename, enter a nickname
        A->>S: PATCH /me/tomos/{id} (nickname)
        S-->>A: 200, or 400 if not 1 to 16 characters
        A-->>P: New nickname
    end
```

### Technical notes

- The roster is every species in the server's game data. The response marks which species the Player owns.
- `user_tomos` is unique on `(user_id, species_code)`, so the database refuses a second Tomo of the same species (BR-3).
- `users.equipped_tomo_id` has a foreign key to `user_tomos (id, user_id)`, so the database refuses a Tomo the Player does not own.
- The newly equipped Tomo gets Bond and Distance Together from the next sync onward ([UC-05](UC-05-calculate-daily-rewards.md) BR-6).
- Skins are not in the MVP. Each Tomo looks like its species. A later version can add a `skins` table and a `user_tomos.skin_id` column.
