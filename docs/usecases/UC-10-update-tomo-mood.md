# UC-10: Update Tomo Mood

| | |
|---|---|
| **Primary Actor** | System |
| **Priority** | Must |

## Goal

Set each Tomo's mood from the Player's recent activity and the time of day, so the Tomo encourages healthy habits without punishing the Player.

## Preconditions

- The Player owns at least one Tomo.

## Trigger

- The Home Screen loads, or
- an activity finishes, or
- a Tomo is fed ([UC-18](UC-18-feed-tomo.md), if built).

## Main Flow

1. The System reads the time of the Player's last valid activity and the Player's local time.
2. The System picks a mood using the rules in BR-1.
3. The app shows the animation and label for that mood.

## Alternate / Exception Flows

- None.

## Postconditions

- The equipped Tomo shows its current mood.

## Business Rules

- BR-1: Mood rules, checked in this order *(tunable)*:

  | Condition | Mood |
  |---|---|
  | Local time is 23:00–06:00 | Sleepy |
  | An activity finished in the last 2 hours | Excited |
  | An activity today | Happy |
  | Last activity was yesterday | Relaxed |
  | No activity for 2 days or more | Bored ("missed you") |

- BR-2: Mood is worked out when it is needed, from data. It is not a value that drains over time.
- BR-3: Mood never lowers Bond and never blocks any feature.

## Related

- [UC-08 View Home Screen](UC-08-view-home-screen.md)
- [UC-09 Interact With Tomo](UC-09-interact-with-tomo.md)

## Open Questions

- Should moods also depend on each species' personality later?
