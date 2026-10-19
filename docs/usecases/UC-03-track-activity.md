# UC-03: Track Activity

| | |
|---|---|
| **Primary Actor** | Player |
| **Priority** | Must |

## Goal

The Player records a walk or run with accurate distance, duration, pace, steps and route, even while the phone is locked or the app is in the background.

## Preconditions

- The Player is signed in and has an equipped Tomo.
- The Player is not already tracking an activity.

## Trigger

The Player taps **START RUN** on the Home Screen or the Run tab.

## Main Flow

1. The app checks for location and activity-recognition permissions.
2. The Player picks the activity type: **Walk** or **Run**.
3. The app waits for a usable GPS fix and shows its signal strength.
4. The app shows a 3-second countdown, with the equipped Tomo looking excited.
5. The app starts a foreground service that records:
   - GPS points (latitude, longitude, timestamp, accuracy, speed)
   - the step counter value
6. The app shows live stats: distance, duration, current pace and steps, with a small Tomo animation that runs alongside.
7. The app saves recorded data to local storage at regular intervals.
8. The Player taps **Stop** and confirms.
9. The app stops the service and sends the activity's raw data to the System.
10. The System validates the activity ([UC-04](UC-04-validate-activity.md)) and calculates rewards ([UC-05](UC-05-calculate-activity-rewards.md)).
11. The app shows the activity summary ([UC-06](UC-06-view-activity-summary.md)).

## Alternate / Exception Flows

- **1a. Permission is missing:** The app explains why it needs the permission and asks for it. If the Player refuses, show a screen explaining that tracking cannot work without it, with a link to the system settings.
- **3a. GPS stays weak for more than 30 seconds:** The Player can start anyway. The app warns that distance may be inaccurate.
- **6a. The Player taps Pause:** Recording pauses, the duration clock stops, and distance stops adding up. **Resume** continues recording.
- **6b. The app detects no movement for 5 minutes:** It asks "Still going?" and pauses automatically if there is no answer.
- **6c. The app or phone is killed during the activity:** On the next launch, the app finds the unfinished activity in local storage and offers **Resume** or **Finish now**.
- **8a. The activity is shorter than the minimum** (less than 100 m or 1 minute): The app offers to discard it. If the Player keeps it, it is saved with no rewards.
- **9a. The device is offline:** The activity is queued on the device and uploaded later. The summary shows the stats right away, and rewards appear as "pending" until they sync.

## Postconditions

- An activity record with all its raw data is saved on the server, or queued on the device for upload.

## Business Rules

- BR-1: The app keeps and uploads raw GPS points, not only totals, so the System can recalculate rewards and re-run validation later.
- BR-2: GPS points with poor accuracy (worse than 30 m) are not counted toward distance *(tunable)*.
- BR-3: Paused time is not counted toward duration or pace.
- BR-4: Only one activity can be tracked at a time.

## Related

- [UC-04 Validate Activity](UC-04-validate-activity.md)
- [UC-05 Calculate Activity Rewards](UC-05-calculate-activity-rewards.md)
- [UC-06 View Activity Summary](UC-06-view-activity-summary.md)

## Open Questions

- Should the MVP also count background steps (with no tracked session) toward rewards, or only tracked activities?
- Does choosing Walk or Run change anything besides the label (for example the speed limits in [UC-04](UC-04-validate-activity.md))?
