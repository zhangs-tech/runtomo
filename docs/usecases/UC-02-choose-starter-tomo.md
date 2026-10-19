# UC-02: Choose Starter Tomo

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Must |

## Goal

A new Player gets their first Tomo, names it, and starts their journey together.

## Preconditions

- The Player has just created an account ([UC-01](UC-01-sign-up-sign-in.md)).
- The Player owns no Tomos.

## Trigger

The first sign-in finishes.

## Main Flow

1. The app shows a short intro: "Every step is an adventure. Choose your companion."
2. The app shows the starter Tomos (for example 3 Common species).
3. The Player taps a starter to preview it (animation and short description).
4. The Player confirms their choice.
5. The app asks the Player to give the Tomo a nickname. The species name is filled in as the default.
6. The Player enters a nickname, or keeps the default.
7. The System creates the Tomo with Bond Level 1, Distance Together 0 km and Birthday set to today.
8. The System makes it the Player's equipped Tomo.
9. The app plays a short "hello" animation and goes to the Home Screen ([UC-08](UC-08-view-home-screen.md)).

## Alternate / Exception Flows

- **4a. The Player goes back:** Return to step 2.
- **6a. Nickname is invalid:** Show the rule and ask again.
- **7a. Network error:** Keep the choice on the device and retry. Do not let the Player into the Home Screen without a Tomo.

## Postconditions

- The Player owns exactly one Tomo, and it is equipped.

## Business Rules

- BR-1: Starter Tomos are a fixed set of Common species. Players cannot get them from the gacha in the MVP.
- BR-2: A nickname is 1–16 characters. Players can change it later ([UC-11](UC-11-manage-tomo-collection.md)).
- BR-3: The Tomo's Birthday is the date it was obtained.

## Related

- [UC-08 View Home Screen](UC-08-view-home-screen.md)
- [UC-11 Manage Tomo Collection](UC-11-manage-tomo-collection.md)

## Open Questions

- How many starters, and which species?
- Should starters also appear in the gacha pool?
