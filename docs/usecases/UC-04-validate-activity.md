# UC-04: Validate Activity

| | |
|---|---|
| **Primary Actor** | System |
| **Priority** | Must |

## Goal

Work out how much of an activity's distance was really done on foot (the **verified distance**), so that rewards cannot be farmed by driving, cycling or GPS spoofing.

## Preconditions

- The System has received an activity's raw data ([UC-03](UC-03-track-activity.md)).

## Trigger

An activity upload finishes.

## Main Flow

1. The System splits the GPS track into segments between consecutive points.
2. For each segment, the System calculates its speed from distance ÷ time.
3. The System marks a segment as **invalid** if:
   - its speed is above the human on-foot limit, or
   - it jumps further than is physically possible (a GPS teleport).
4. The System compares the steps taken during each part of the activity with the distance covered in that part.
5. If the distance is much higher than the steps can explain, the System marks that part as **suspicious**.
6. The System calculates:
   - **Recorded distance** = all segments
   - **Verified distance** = valid segments only, with suspicious parts removed
7. The System gives the activity a status: `valid`, `partially_valid` or `flagged`.
8. The System passes the verified distance to reward calculation ([UC-05](UC-05-calculate-activity-rewards.md)).

## Alternate / Exception Flows

- **4a. The device has no step counter sensor:** Skip steps 4–5 and rely on the speed checks only.
- **7a. More than 50% of the distance is invalid:** Set the status to `flagged`. The activity earns no TomoCoins and is left out of leaderboards, but it stays in the Player's history.
- **7b. The device reports a mock location provider:** Set the status to `flagged`.

## Postconditions

- The activity has a recorded distance, a verified distance and a validation status.

## Business Rules

- BR-1: Segments faster than 25 km/h are invalid *(tunable)*.
- BR-2: Steps vs. distance check: if the distance per step is above 2.5 m over a sustained period, that part is suspicious *(tunable)*.
- BR-3: Fitness stats always use the **recorded** distance. Rewards and leaderboards use only the **verified** distance. A Player never loses their exercise history because of a low validation result.
- BR-4: Validation runs on the server, not only on the device.
- BR-5: Because raw data is kept, validation can be re-run if these rules change.

## Related

- [UC-03 Track Activity](UC-03-track-activity.md)
- [UC-05 Calculate Activity Rewards](UC-05-calculate-activity-rewards.md)

## Out of Scope (later versions)

- Full sensor fusion (accelerometer and gyroscope) and a Movement Confidence Score
- Manual review queue
- Multi-account detection

## Open Questions

- Should the Player see why part of an activity was not counted (for example "2.1 km looked like vehicle travel")?
