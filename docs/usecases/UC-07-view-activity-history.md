# UC-07: View Activity History

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Must |

## Goal

The Player looks back at past walks and runs and their totals, so RunTomo works as a real fitness tracker.

## Preconditions

- The Player is signed in.

## Trigger

The Player opens the **Run** tab and picks **History**.

## Main Flow

1. The app shows totals for this week and this month: distance, number of activities and time.
2. The app shows a list of activities, newest first. Each item shows the date, type, distance, duration and the Tomo that came along.
3. The Player scrolls. Older items load as needed.
4. The Player taps an activity.
5. The app shows that activity's summary ([UC-06](UC-06-view-activity-summary.md)).

## Alternate / Exception Flows

- **2a. No activities yet:** Show an empty state with the Tomo and a **Start your first walk** button.
- **2b. Some activities are still queued on the device:** Show them at the top, marked "Waiting to sync".

## Postconditions

- None. This use case only displays information.

## Business Rules

- BR-1: History and totals use the **recorded** distance, including flagged activities ([UC-04](UC-04-validate-activity.md) BR-3).

## Related

- [UC-06 View Activity Summary](UC-06-view-activity-summary.md)

## Open Questions

- Can the Player delete an activity? If so, are the rewards it earned taken back?
