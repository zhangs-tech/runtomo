# UC-05: Calculate Activity Rewards

| | |
|---|---|
| **Primary Actor** | System |
| **Priority** | Must |

## Goal

Turn an activity's verified distance into TomoCoins and Bond for the equipped Tomo, and add it to that Tomo's Distance Together.

## Preconditions

- The activity has been validated ([UC-04](UC-04-validate-activity.md)).

## Trigger

Validation finishes.

## Main Flow

1. The System looks up how much verified distance the Player has already earned rewards for today.
2. The System calculates TomoCoins for this activity using the daily tiers (BR-1).
3. The System calculates Bond points from the verified distance (BR-3).
4. The System adds the verified distance to the equipped Tomo's **Distance Together**.
5. The System adds the Bond points to the equipped Tomo. If this crosses a threshold, the Tomo reaches the next Bond Level.
6. The System credits the TomoCoins to the Player's balance.
7. The System updates progress on daily quests ([UC-13](UC-13-complete-daily-quests.md)), the streak ([UC-14](UC-14-maintain-streak.md)) and achievements ([UC-17](UC-17-unlock-achievement.md)).
8. The System saves a reward record linked to the activity, and the summary screen shows it ([UC-06](UC-06-view-activity-summary.md)).

## Alternate / Exception Flows

- **1a. The activity is `flagged`:** It earns no TomoCoins and no Bond and does not count for quests. Skip to step 8.
- **2a. The Player is past the daily coin cap:** The activity earns 0 TomoCoins, but Bond and Distance Together still go up.

## Postconditions

- The Player's TomoCoin balance, the Tomo's Bond and Distance Together, and quest and streak progress are all updated.
- A reward record exists, so the System never pays out for the same activity twice.

## Business Rules

- BR-1: TomoCoins per verified km for each day *(tunable)*:

  | Verified km today | TomoCoins per km |
  |---|---:|
  | 0–5 km | 100 |
  | 5–10 km | 50 |
  | over 10 km | 0 |

- BR-2: "Today" is based on the Player's local time zone.
- BR-3: Bond: 10 Bond points per verified km, with no daily cap *(tunable)*.
- BR-4: Bond Level thresholds go from Level 1 to Level 10 and get further apart at higher levels *(tunable)*.
- BR-5: Bond never goes down in the MVP.
- BR-6: Only the Tomo that was equipped when the activity **started** gets Distance Together and Bond.
- BR-7: Rewards are idempotent. Uploading the same activity again does not pay out again.

## Related

- [UC-04 Validate Activity](UC-04-validate-activity.md)
- [UC-06 View Activity Summary](UC-06-view-activity-summary.md)
- [UC-13 Complete Daily Quests](UC-13-complete-daily-quests.md)
- [UC-14 Maintain Activity Streak](UC-14-maintain-streak.md)

## Open Questions

- Should running earn more per km than walking, or should all km be treated the same?
- Is there a separate Player XP / Player Level in the MVP, or only Tomo Bond?
