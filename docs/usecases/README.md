# Use Cases (MVP)

These are the use cases for the RunTomo MVP. They describe the core loop:

**Walk / Run → Earn TomoCoins + Bond → Care for Tomo → Summon new Tomos → Keep Moving**

Features outside the MVP (battles, trading, skins, clubs, location sharing, events, habitats, extra currencies) are left out on purpose. See `RunTomo_Idea.md` §45 for the roadmap.

## Actors

| Actor | Description |
|---|---|
| Player | A signed-in user of the mobile app |
| Guest | A user who has not signed in yet |
| System | The RunTomo backend and the app's background services |
| Friend | Another Player linked to the Player |

## Index

| ID | Use Case | Primary Actor | Priority |
|---|---|---|---|
| [UC-01](UC-01-sign-up-sign-in.md) | Sign Up / Sign In | Guest | Must |
| [UC-02](UC-02-choose-starter-tomo.md) | Choose Starter Tomo | Player | Must |
| [UC-03](UC-03-track-activity.md) | Track Activity | Player | Must |
| [UC-04](UC-04-validate-activity.md) | Validate Activity | System | Must |
| [UC-05](UC-05-calculate-activity-rewards.md) | Calculate Activity Rewards | System | Must |
| [UC-06](UC-06-view-activity-summary.md) | View Activity Summary | Player | Must |
| [UC-07](UC-07-view-activity-history.md) | View Activity History | Player | Must |
| [UC-08](UC-08-view-home-screen.md) | View Home Screen | Player | Must |
| [UC-09](UC-09-interact-with-tomo.md) | Interact With Tomo | Player | Must |
| [UC-10](UC-10-update-tomo-mood.md) | Update Tomo Mood | System | Must |
| [UC-11](UC-11-manage-tomo-collection.md) | Manage Tomo Collection | Player | Must |
| [UC-12](UC-12-summon-tomo.md) | Summon Tomo (Gacha) | Player | Must |
| [UC-13](UC-13-complete-daily-quests.md) | Complete Daily Quests | Player | Must |
| [UC-14](UC-14-maintain-streak.md) | Maintain Activity Streak | System | Must |
| [UC-15](UC-15-add-friend.md) | Add Friend | Player | Should |
| [UC-16](UC-16-view-friend-leaderboard.md) | View Friend Leaderboard | Player | Should |
| [UC-17](UC-17-unlock-achievement.md) | Unlock Achievement | System | Should |
| [UC-18](UC-18-feed-tomo.md) | Feed Tomo | Player | Should |

**Must** = needed to test the core loop. **Should** = add if time allows.

## Tunable Values

Numbers marked *(tunable)* are starting points to balance later. Keep them in server config, not hard-coded in the app.

## Template

Every use case file uses these sections:

- **ID / Name / Primary Actor / Priority**
- **Goal**: what the actor wants
- **Preconditions**: what must be true before it starts
- **Trigger**: what starts it
- **Main Flow**: the happy path, numbered
- **Alternate / Exception Flows**: numbered after the main-flow step they branch from
- **Postconditions**: what is true afterwards
- **Business Rules**: constraints and formulas
- **Related**: links to other use cases
- **Open Questions**: decisions still to make
