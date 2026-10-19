# UC-18: Feed Tomo

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Should |

## Goal

The Player buys a treat and feeds it to their Tomo. This is a light caring moment and a second way to spend TomoCoins.

## Preconditions

- The Player is signed in, online and has an equipped Tomo.

## Trigger

The Player taps **Feed** on the Home Screen.

## Main Flow

1. The app shows the food items, with prices and how many the Player has.
2. The Player picks an item they own, or buys one with TomoCoins.
3. The System deducts the cost, if the Player is buying.
4. The Player drags the food to the Tomo, or taps **Give**.
5. The app plays an eating animation and a happy reaction.
6. The System adds a small amount of Bond to the Tomo.

## Alternate / Exception Flows

- **2a. Not enough TomoCoins:** Show the shortfall and a **Start Run** button.
- **6a. The Tomo has already been fed the daily limit:** It plays a "full" animation. No Bond is given and the item is not used up.

## Postconditions

- The Player's food stock or TomoCoins are reduced, and the Tomo's Bond goes up.

## Business Rules

- BR-1: MVP food items *(tunable)*:

  | Item | Cost | Bond |
  |---|---:|---:|
  | Snack | 50 TomoCoins | +3 |
  | Treat | 150 TomoCoins | +10 |

- BR-2: Each Tomo can be fed at most 3 times per day *(tunable)*, so coins cannot simply be traded for Bond.
- BR-3: The Bond from food stays small compared with the Bond from moving, so movement remains the main way to grow.

## Related

- [UC-08 View Home Screen](UC-08-view-home-screen.md)
- [UC-10 Update Tomo Mood](UC-10-update-tomo-mood.md)

## Open Questions

- Should species have favorite or disliked foods in the MVP, or later?
- Does feeding change the mood, for example to Happy for 1 hour?
