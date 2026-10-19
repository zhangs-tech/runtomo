# UC-01: Sign Up / Sign In

| | |
|---|---|
| **Primary Actor** | Guest |
| **Priority** | Must |

## Goal

The Guest creates an account or signs in, so their activity, Tomos and currency are saved on the server and kept across devices.

## Preconditions

- The app is installed and the device is online.

## Trigger

The Guest opens the app with no valid session.

## Main Flow

1. The app shows the welcome screen with sign-in options.
2. The Guest picks a sign-in method.
3. The System authenticates the Guest.
4. The System checks whether an account exists for this identity.
5. If the account is new, the System creates a Player profile and asks for a display name.
6. The Guest enters a display name.
7. The System saves the profile and issues a session.
8. A new Player goes to [UC-02 Choose Starter Tomo](UC-02-choose-starter-tomo.md). A returning Player goes to [UC-08 Home Screen](UC-08-view-home-screen.md).

## Alternate / Exception Flows

- **3a. Authentication fails:** The app shows an error and returns to step 1.
- **3b. Device is offline:** The app shows a "No connection" message with a retry button.
- **6a. Display name is invalid** (too short, too long or blocked word): The app shows the rule and asks again.
- **7a. Returning Player with a valid stored session:** Skip steps 1–7 and go straight to the Home Screen.

## Postconditions

- The Player has an account and an active session on the device.

## Business Rules

- BR-1: A display name is 3–20 characters. It does not need to be unique, because a separate friend code identifies the Player (see [UC-15](UC-15-add-friend.md)).
- BR-2: The System creates a unique friend code when it creates the account.
- BR-3: The app asks for location and activity-recognition permissions only when they are first needed, in [UC-03](UC-03-track-activity.md), not at sign-up.

## Related

- [UC-02 Choose Starter Tomo](UC-02-choose-starter-tomo.md)

## Open Questions

- Which sign-in methods for the MVP: Google only, or Google + email/password?
- Should guests be able to play before signing in, with an account linked later?
