# Use Cases (MVP)

These are the use cases for the RunTomo MVP. They describe the core loop:

**Walk / Run → Earn TomoCoins + Bond → Care for Tomo → Summon new Tomos → Keep Moving**

These use cases exclude features outside the MVP on purpose. Those features are GPS run tracking, checks that steps are real, battles, trading, skins, achievements, guest mode, clubs, location sharing, events, habitats and extra currencies. See `RunTomo_Idea.md` §45 for the roadmap.

## Actors

| Actor | Description |
|---|---|
| Player | A signed-in user of the mobile app |
| Guest | A user who has not signed in yet |
| Friend | Another Player linked to the Player |

Actors are people or services outside RunTomo, such as Google for sign-in and Health Connect for step data. **The System** means RunTomo as a whole: the app and the server together. The use case text treats it as a black box. The **Diagrams** section of each use case shows whether the app or the server does each step.

## Index

| ID | Use Case | Primary Actor | Priority |
|---|---|---|---|
| [UC-01](UC-01-sign-up-sign-in.md) | Sign Up / Sign In | Guest | Must |
| [UC-02](UC-02-choose-starter-tomo.md) | Choose Starter Tomo | Player | Must |
| [UC-03](UC-03-sync-daily-activity.md) | Sync Daily Activity | Player | Must |
| [UC-05](UC-05-calculate-daily-rewards.md) | Calculate Daily Rewards | Player | Must |
| [UC-06](UC-06-view-new-rewards.md) | View New Rewards | Player | Must |
| [UC-07](UC-07-view-daily-history.md) | View Daily History | Player | Must |
| [UC-08](UC-08-view-home-screen.md) | View Home Screen | Player | Must |
| [UC-09](UC-09-interact-with-tomo.md) | Interact With Tomo | Player | Must |
| [UC-10](UC-10-update-tomo-mood.md) | Update Tomo Mood | Player | Must |
| [UC-11](UC-11-manage-tomo-collection.md) | Manage Tomo Collection | Player | Must |
| [UC-12](UC-12-summon-tomo.md) | Summon Tomo (Gacha) | Player | Must |
| [UC-13](UC-13-complete-daily-quests.md) | Complete Daily Quests | Player | Must |
| [UC-15](UC-15-add-friend.md) | Add Friend | Player | Should |
| [UC-16](UC-16-view-friend-leaderboard.md) | View Friend Leaderboard | Player | Should |
| [UC-18](UC-18-feed-tomo.md) | Feed Tomo | Player | Should |

**Must** = needed to test the core loop. **Should** = add if time allows.

## Days

Every day in RunTomo runs from 00:00 to 24:00 UTC+7 (WIB), for every Player. Daily coin caps, quests, feeding limits, mood and leaderboards all use this day.

## Tunable Values

Numbers marked *(tunable)* are starting points to balance later. Keep them in server config, not hard-coded in the app.

## Template

Every use case file uses these sections:

- **ID / Name / Primary Actor / Priority**
- **Supporting Actor** (optional): outside services the System uses, such as Google
- **Included in** (optional): the use cases that run this one as one of their steps
- **Goal**: what the actor wants
- **Preconditions**: what must be true before it starts
- **Trigger**: what starts it
- **Main Flow**: the happy path, numbered
- **Alternate / Exception Flows**: numbered after the main-flow step they branch from
- **Postconditions**: what is true afterwards
- **Business Rules**: constraints and formulas
- **Related**: links to other use cases
- **Open Questions**: decisions still to make
- **Diagrams**: Mermaid sequence diagrams or flowcharts with separate App and Server parts, showing which one does each step, plus technical notes (endpoints, tokens, data). Endpoint paths are proposals until the API is built.
