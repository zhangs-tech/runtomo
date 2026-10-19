# UC-08: View Home Screen

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Must |

## Goal

The Player sees their Tomo and today's progress at a glance, and can start an activity with one tap.

## Preconditions

- The Player is signed in and has an equipped Tomo.

## Trigger

The Player opens the app, or taps the **Home** tab.

## Main Flow

1. The app shows the equipped Tomo in its home scene, with an animation that matches its current mood ([UC-10](UC-10-update-tomo-mood.md)).
2. The app shows:
   - the Tomo's nickname, mood and Bond Level
   - today's verified distance against the daily goal (for example **2.8 / 5 km**)
   - the TomoCoin balance
   - the current streak and rest days left ([UC-14](UC-14-maintain-streak.md))
   - a short daily-quests panel ([UC-13](UC-13-complete-daily-quests.md))
3. The app shows a large **START RUN** button.
4. The Player can:
   - tap the Tomo → [UC-09 Interact With Tomo](UC-09-interact-with-tomo.md)
   - tap **START RUN** → [UC-03 Track Activity](UC-03-track-activity.md)
   - tap the quests panel → [UC-13](UC-13-complete-daily-quests.md)

## Alternate / Exception Flows

- **1a. The Player has been away for 2 days or more:** The Tomo greets them with a "missed you" message, for example "Mochi missed you!", before showing the normal screen. There is no penalty.
- **1b. Offline:** Show the last cached values with an "Offline" label. Starting an activity still works.

## Postconditions

- None. This use case only displays information.

## Business Rules

- BR-1: The daily goal shown is the full-reward coin tier (5 km, [UC-05](UC-05-calculate-activity-rewards.md) BR-1) *(tunable)*.
- BR-2: Messages and animations never guilt or punish the Player.

## Related

- [UC-03 Track Activity](UC-03-track-activity.md)
- [UC-09 Interact With Tomo](UC-09-interact-with-tomo.md)
- [UC-10 Update Tomo Mood](UC-10-update-tomo-mood.md)

## Open Questions

- Should the Player be able to set their own daily goal?
