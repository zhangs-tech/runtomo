# UC-10: Update Tomo Mood

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Must |
| **Included in** | [UC-03 Sync Daily Activity](UC-03-sync-daily-activity.md), [UC-08 View Home Screen](UC-08-view-home-screen.md) |

## Goal

Set the Tomo's mood from the Player's distance today, so the Tomo encourages healthy habits without punishing the Player.

## Preconditions

- The Player owns at least one Tomo.

## Trigger

One of these happens:

- The Home Screen loads.
- A sync brings new distance ([UC-03](UC-03-sync-daily-activity.md)).

## Main Flow

1. The System reads the Player's distance for today.
2. The System picks a mood using the rules in BR-1.
3. The System shows the animation and label for that mood.

## Alternate / Exception Flows

- None.

## Postconditions

- The equipped Tomo shows its current mood.

## Business Rules

- BR-1: The System checks the mood rules in this order *(tunable)*:

  | Condition | Mood |
  |---|---|
  | Today's distance reached the daily goal ([UC-08](UC-08-view-home-screen.md) BR-1) | Excited |
  | Today's distance is at least 1 km | Happy |
  | None of the above | Relaxed |

- BR-2: The System picks the mood from data each time it needs it. Mood is not a value that drains over time.
- BR-3: Mood never lowers Bond and never blocks any feature.
- BR-4: Feeding ([UC-18](UC-18-feed-tomo.md)) does not change the mood.

## Related

- [UC-08 View Home Screen](UC-08-view-home-screen.md)
- [UC-09 Interact With Tomo](UC-09-interact-with-tomo.md)

## Open Questions

- Should moods also depend on each species' personality later?
- Should the time of day (for example a Sleepy mood at night) or earlier days count later?

## Diagrams

```mermaid
flowchart TD
    subgraph Server
        IN["Today's distance"] --> E{"Today reached the daily goal?"}
        E -- Yes --> EX["Excited"]
        E -- No --> H{"Today at least 1 km?"}
        H -- Yes --> HA["Happy"]
        H -- No --> RE["Relaxed"]
    end
    subgraph App
        SHOW["Show the mood animation and label"]
    end
    EX & HA & RE --> SHOW
```

### Technical notes

- The server picks the mood whenever it builds a response that shows the Tomo: `GET /home` and `PUT /me/days/{date}`. As the ERD notes, the server does not store the mood (BR-2).
- Today's distance comes from `daily_stats.distance_m`. A day with no row counts as 0 km.
