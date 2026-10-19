# UC-16: View Friend Leaderboard

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Should |

## Goal

The Player compares their weekly distance with their friends. This is friendly competition at a scale that motivates.

## Preconditions

- The Player is signed in and has at least one friend ([UC-15](UC-15-add-friend.md)).

## Trigger

The Player opens **Social → Leaderboard**.

## Main Flow

1. The app shows this week's ranking of the Player and their friends by verified distance.
2. Each row shows the rank, display name, displayed Tomo, km this week and current streak.
3. The Player's own row is highlighted, with the gap to the next person, for example "1.8 km behind Alex".
4. The Player can switch between **Today**, **This Week** and **This Month**.
5. The Player can tap a friend to see their profile card.

## Alternate / Exception Flows

- **1a. The Player has no friends:** Show an empty state with an **Add Friend** button ([UC-15](UC-15-add-friend.md)).

## Postconditions

- None. This use case only displays information.

## Business Rules

- BR-1: Rankings use only **verified** distance. Flagged activities are left out.
- BR-2: The week runs Monday 00:00 to Sunday 23:59 in the Player's local time.
- BR-3: There is no global leaderboard in the MVP.

## Related

- [UC-15 Add Friend](UC-15-add-friend.md)
- [UC-04 Validate Activity](UC-04-validate-activity.md)

## Open Questions

- Should the top friend each week get a small reward or badge?
