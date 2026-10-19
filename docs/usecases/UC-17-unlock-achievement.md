# UC-17: Unlock Achievement

| | |
|---|---|
| **Primary Actor** | System |
| **Priority** | Should |

## Goal

Mark long-term milestones so the Player can see their journey and feels it was worth it.

## Preconditions

- The Player is signed in.

## Trigger

The System updates progress after an activity ([UC-05](UC-05-calculate-activity-rewards.md) step 7), after a streak update ([UC-14](UC-14-maintain-streak.md)) or after a summon ([UC-12](UC-12-summon-tomo.md)).

## Main Flow

1. The System checks every achievement the Player has not unlocked yet against their updated totals.
2. For each one that is now met, the System unlocks it and credits its reward.
3. The app shows an "Achievement Unlocked" pop-up on the next screen, for example the activity summary.
4. The Player can see every achievement, locked and unlocked, under **Profile → Achievements**.

## Alternate / Exception Flows

- **2a. Several achievements unlock at once:** Show them one after another, or as one grouped pop-up.

## Postconditions

- The achievements are unlocked, and their rewards are credited.

## Business Rules

- BR-1: MVP achievement list *(tunable)*:

  | Achievement | Condition | Reward |
  |---|---|---:|
  | First Steps | Total verified distance of 1 km | 200 TomoCoins |
  | Getting Started | 5 activities completed | 300 TomoCoins |
  | Marathon Journey | Total verified distance of 42.195 km | 1,000 TomoCoins |
  | Century | Total verified distance of 100 km | 1,500 TomoCoins |
  | One Week Strong | Streak of 7 days | 500 TomoCoins |
  | Inseparable | One Tomo with 42.195 km Distance Together | 500 TomoCoins |
  | Collector | 5 species owned | 500 TomoCoins |
  | Early Bird | Activity started before 07:00 | 200 TomoCoins |

- BR-2: Each achievement unlocks only once.
- BR-3: Achievement rewards do not count toward the daily coin cap.

## Related

- [UC-05 Calculate Activity Rewards](UC-05-calculate-activity-rewards.md)
- [UC-14 Maintain Activity Streak](UC-14-maintain-streak.md)

## Open Questions

- Should achievements unlock titles or badges as well as coins?
