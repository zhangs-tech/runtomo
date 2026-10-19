# UC-06: View Activity Summary

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Must |

## Goal

Right after finishing, the Player sees what they achieved and how their Tomo reacted. This is the main reward moment of the loop.

## Preconditions

- The Player has just finished an activity ([UC-03](UC-03-track-activity.md)), or has opened a past one from history ([UC-07](UC-07-view-activity-history.md)).

## Trigger

The activity stops, or the Player taps an item in their history.

## Main Flow

1. The app shows the equipped Tomo's reaction, for example:
   > **Mochi walked 4.8 km with you!**
   > +48 Bond · +430 TomoCoins
2. If the Tomo reached a new Bond Level, the app plays a level-up animation.
3. The app shows the stats:
   - Distance
   - Duration
   - Average pace
   - Steps
   - Route on a map
4. The app shows the rewards: TomoCoins, Bond, and the new Distance Together total.
5. The app shows any quests or achievements completed by this activity.
6. The Player taps **Done** and returns to the Home Screen.

## Alternate / Exception Flows

- **1a. Rewards are still pending (offline):** Show the stats now and mark rewards as "Syncing…". Update the screen when they arrive.
- **1b. The activity was `partially_valid`:** Show both the recorded and the verified distance, with a short note.
- **1c. The activity was `flagged`:** Show the stats with a neutral note ("This activity didn't earn rewards"). Do not accuse the Player.
- **2a. The Tomo reached a Distance Together milestone** (for example 10, 42.195, 100 km): Show a special milestone message.

## Postconditions

- None. This use case only displays information.

## Business Rules

- BR-1: The Tomo's reaction message mentions the Tomo by nickname.
- BR-2: When opened from history, step 2 (the level-up animation) is skipped.

## Related

- [UC-03 Track Activity](UC-03-track-activity.md)
- [UC-05 Calculate Activity Rewards](UC-05-calculate-activity-rewards.md)
- [UC-07 View Activity History](UC-07-view-activity-history.md)

## Open Questions

- Should the Player be able to share the summary as an image in the MVP?
