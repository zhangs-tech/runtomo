# UC-13: Complete Daily Quests

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Must |

## Goal

The Player completes small daily goals for extra TomoCoins, which gives them a reason to move every day.

## Preconditions

- The Player is signed in.

## Trigger

- The Player opens the quests panel, or
- the System updates quest progress after an activity ([UC-05](UC-05-calculate-activity-rewards.md) step 7).

## Main Flow

1. The System gives the Player today's 3 quests at local midnight.
2. The app shows each quest with a progress bar and its reward.
3. After each valid activity, the System updates the progress of each quest.
4. When a quest is complete, the app shows it as **Claim**.
5. The Player taps **Claim**.
6. The System credits the reward and marks the quest as claimed.

## Alternate / Exception Flows

- **4a. The day ends with completed quests not yet claimed:** The System claims them automatically, so the Player never loses a reward they earned.
- **3a. A flagged activity:** It does not count toward quests.

## Postconditions

- Quest progress is updated, and claimed rewards are added to the balance.

## Business Rules

- BR-1: MVP quest list *(tunable)*:

  | Quest | Reward |
  |---|---:|
  | Walk or run 1 km | 100 TomoCoins |
  | Walk or run 3 km | 200 TomoCoins |
  | Take 5,000 steps during activities | 150 TomoCoins |

- BR-2: Quest rewards do **not** count toward the daily coin cap in [UC-05](UC-05-calculate-activity-rewards.md).
- BR-3: Quests reset at the Player's local midnight.

## Related

- [UC-05 Calculate Activity Rewards](UC-05-calculate-activity-rewards.md)
- [UC-08 View Home Screen](UC-08-view-home-screen.md)

## Open Questions

- Should the quests be the same every day, or drawn from a pool?
- Should there be a bonus for completing all 3?
