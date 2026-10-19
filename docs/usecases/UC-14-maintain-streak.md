# UC-14: Maintain Activity Streak

| | |
|---|---|
| **Primary Actor** | System |
| **Priority** | Must |

## Goal

Count how many days in a row the Player has been active. Rest Days keep a single missed day from wiping out their progress.

## Preconditions

- The Player is signed in.

## Trigger

- A valid activity finishes ([UC-05](UC-05-calculate-activity-rewards.md) step 7), or
- the Player's local day ends.

## Main Flow

1. When a valid activity finishes, the System checks whether today already counts as active.
2. If today now reaches the active-day minimum (BR-1) and was not active before, the streak goes up by 1.
3. Every 7 days of streak, the Player earns 1 Rest Day, up to the limit in BR-3.
4. The Home Screen shows the streak and the Rest Days left.

## Alternate / Exception Flows

- **Day ends with no active day, and the Player has a Rest Day:** The System uses one Rest Day automatically. The streak stays the same. On their next visit the app tells the Player, for example: "Mochi let you rest yesterday 💤 1 Rest Day left".
- **Day ends with no active day, and no Rest Days left:** The streak resets to 0. The **longest streak** is kept in the Player's profile.

## Postconditions

- The current streak, the longest streak and the Rest Day count are up to date.

## Business Rules

- BR-1: A day is active if it has at least 1 km of verified distance *(tunable)*.
- BR-2: Days follow the Player's local time zone.
- BR-3: The Player can hold at most 2 Rest Days *(tunable)*.
- BR-4: Rest Days are used automatically. The Player does not need to do anything.

## Related

- [UC-05 Calculate Activity Rewards](UC-05-calculate-activity-rewards.md)
- [UC-08 View Home Screen](UC-08-view-home-screen.md)
- [UC-17 Unlock Achievement](UC-17-unlock-achievement.md)

## Open Questions

- Should the streak give any reward (for example bonus coins at 7 or 30 days), or only show progress?
- How should a time-zone change while travelling be handled?
