# UC-12: Summon Tomo (Gacha)

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Must |

## Goal

The Player spends TomoCoins earned by moving to hatch an egg and get a new Tomo.

## Preconditions

- The Player is signed in.
- The device is online. Summons run on the server only.

## Trigger

The Player opens **Tomos → Summon**.

## Main Flow

1. The app shows the standard banner: the species in the pool, the rarity rates, the cost per summon, the Player's TomoCoin balance and the pity counter (for example "Epic or higher guaranteed in 7 summons").
2. The Player taps **Summon**.
3. The app asks the Player to confirm the cost.
4. The System checks the balance and deducts the cost.
5. The System rolls the rarity, applying pity (BR-3), then picks a species of that rarity.
6. The System saves the result and updates the pity counter. This happens in a single transaction with step 4.
7. The app plays the egg animation: the equipped Tomo watches the egg, the egg shakes and cracks, and the background glows in the rarity's color.
8. The new Tomo appears with its name and rarity.
9. The Player can give it a nickname, or **Equip** it now.

## Alternate / Exception Flows

- **4a. Not enough TomoCoins:** Show how many km are still needed (for example "Walk about 3.2 km more to summon") and a **Start Run** button.
- **5a. The result is a species the Player already owns:** Show the duplicate and convert it under BR-4.
- **6a. Network error after the coins were deducted:** The transaction rolls back on the server. The app gets the final state again before showing any result.

## Postconditions

- The cost is deducted, and the Player owns the new Tomo or received the duplicate conversion.
- A summon log is saved, for auditing and pity.

## Business Rules

- BR-1: The cost is 1,000 TomoCoins per summon, about 10 verified km *(tunable)*.
- BR-2: Rarity rates for the MVP *(tunable)*:

  | Rarity | Rate |
  |---|---:|
  | Common | 50% |
  | Uncommon | 28% |
  | Rare | 15% |
  | Epic | 6% |
  | Legendary | 1% |

- BR-3: Pity: if the last 19 summons gave nothing Epic or higher, the 20th is Epic or higher. The counter resets whenever an Epic or higher comes out *(tunable)*.
- BR-4: A duplicate is converted into 200 TomoCoins *(tunable)*. Species Shards come later.
- BR-5: The rates shown in the app must exactly match the rates on the server.
- BR-6: Summons cannot be bought with real money in the MVP.

## Related

- [UC-05 Calculate Activity Rewards](UC-05-calculate-activity-rewards.md)
- [UC-11 Manage Tomo Collection](UC-11-manage-tomo-collection.md)

## Open Questions

- What is the MVP roster: 8–10 species, and how many at each rarity?
- Should there be a 10-summon option?
- Should the first summon be guaranteed Rare or higher, as an onboarding hook?
