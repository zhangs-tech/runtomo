# UC-15: Add Friend

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Should |

## Goal

The Player connects with people they know so they can compare activity and motivate each other.

## Preconditions

- The Player is signed in and online.

## Trigger

The Player opens **Social → Add Friend**.

## Main Flow

1. The app shows the Player's own friend code, with **Copy** and **Share** buttons.
2. The Player enters a friend's code.
3. The System looks up the code and shows that Player's display name and displayed Tomo.
4. The Player taps **Send Request**.
5. The System creates a pending request and notifies the other Player.
6. The other Player opens their requests and taps **Accept**.
7. The System links both Players as friends.

## Alternate / Exception Flows

- **3a. The code is not found:** Show "No player with this code".
- **3b. It is the Player's own code, or they are already friends:** Show a message and stop.
- **6a. The other Player declines:** The System deletes the request. The sender is not told it was declined.
- **Remove friend:** From a friend's profile the Player taps **Remove**. The link is removed for both Players, and neither is notified.

## Postconditions

- Both Players see each other in their friends lists.

## Business Rules

- BR-1: A Player can have at most 100 friends *(tunable)*.
- BR-2: Friends can see only: display name, displayed Tomo, today's, this week's and this month's verified distance, and the current streak. No location or routes are shared in the MVP.
- BR-3: Players cannot search for each other by name, which protects privacy. Friend codes or invite links are the only way to connect.

## Related

- [UC-16 View Friend Leaderboard](UC-16-view-friend-leaderboard.md)

## Open Questions

- Should invite links (deep links) be part of the MVP, or only codes?
- Are push notifications part of the MVP?
