# Requirements Document

## Introduction

This document specifies the requirements for the Recurrence Reminders feature of EchoList. The feature adds two capabilities to the task list editor: (1) visual urgency coloring on the due date pill (DueDateTag) that shifts color based on proximity to the due date, and (2) cross-platform local notifications that fire when a recurring task becomes due. Together, these ensure users are alerted to upcoming and overdue recurring tasks both visually within the app and externally via platform notifications.

## Glossary

- **DueDateTag**: The composable pill element that displays a task's due date and optional recurrence icon in the task list editor.
- **DueDateUrgency**: An enumeration with three values (Normal, Warning, Overdue) representing the urgency level of a task based on date proximity.
- **DueDateUrgencyCalculator**: A pure function object that computes a `DueDateUrgency` value given a due date and today's date.
- **NotificationScheduler**: A platform-agnostic interface for scheduling and canceling local notifications tied to recurring tasks.
- **Recurring_Task**: A MainTask that has both a non-empty due date and a non-empty recurrence rule (RRULE string).
- **EditTaskListViewModel**: The ViewModel that manages task list editing state and coordinates notification scheduling.
- **EchoListTheme**: The application's theme system providing color, typography, shape, and dimension tokens.

## Requirements

### Requirement 1: Urgency Level Computation

**User Story:** As a user, I want the app to automatically determine the urgency of my recurring tasks based on how close the due date is, so that I can quickly see which tasks need immediate attention.

#### Acceptance Criteria

1. WHEN the calendar day difference (dueDate - today) is less than or equal to 0, THE DueDateUrgencyCalculator SHALL return Overdue.
2. WHEN the calendar day difference (dueDate - today) is 1, 2, or 3, THE DueDateUrgencyCalculator SHALL return Warning.
3. WHEN the calendar day difference (dueDate - today) is greater than 3, THE DueDateUrgencyCalculator SHALL return Normal.
4. THE DueDateUrgencyCalculator SHALL be a pure function that produces the same output for the same inputs without side effects.
5. THE DueDateUrgencyCalculator SHALL return exactly one value from the DueDateUrgency enumeration for any valid pair of (dueDate: LocalDate, today: LocalDate).

### Requirement 2: Urgency Visual Coloring

**User Story:** As a user, I want the due date pill to change color based on urgency, so that I get an at-a-glance sense of how pressing a task is without reading dates.

#### Acceptance Criteria

1. WHILE the urgency is Normal, THE DueDateTag SHALL render with EchoListTheme.materialColors.surfaceVariant background and EchoListTheme.materialColors.onSurfaceVariant text color.
2. WHILE the urgency is Warning, THE DueDateTag SHALL render with EchoListTheme.echoListColorScheme.warning background and EchoListTheme.echoListColorScheme.onWarning text color.
3. WHILE the urgency is Overdue, THE DueDateTag SHALL render with EchoListTheme.materialColors.error background and EchoListTheme.materialColors.onError text color.
4. WHILE the task is recurring, THE DueDateTag SHALL apply the same resolved urgency text color to the recurrence icon tint as used for the date text.
5. THE DueDateTag SHALL remain stateless, receiving urgency as an external parameter and emitting no side effects.
6. WHEN the urgency value changes due to date progression, THE DueDateTag SHALL immediately reflect the new coloring on the next recomposition.

### Requirement 3: Unparseable Due Date Handling

**User Story:** As a user, I want the app to handle invalid or empty due dates gracefully, so that the UI does not break or display confusing visuals.

#### Acceptance Criteria

1. IF the due date string is empty (zero-length or whitespace-only), THEN THE calling composable SHALL pass DueDateUrgency.Normal to DueDateTag.
2. IF the due date string is non-empty but cannot be parsed as a valid ISO-8601 date (yyyy-MM-dd), THEN THE calling composable SHALL pass DueDateUrgency.Normal to DueDateTag.
3. WHEN DueDateUrgency.Normal is passed due to an unparseable date, THE DueDateTag SHALL render the raw date string as-is with surfaceVariant background and onSurfaceVariant text color.

### Requirement 4: Notification Scheduling

**User Story:** As a user, I want to receive a local notification when a recurring task becomes due, so that I am reminded even when the app is not open.

#### Acceptance Criteria

1. WHEN the EditTaskListViewModel persists a task with a non-empty due date and a non-empty recurrence rule, THE NotificationScheduler SHALL schedule a local notification for that task's due date, using the task ID as the unique notification identifier.
2. WHEN a notification is scheduled for a task whose task ID already has a pending notification, THE NotificationScheduler SHALL cancel the existing notification and schedule the new one, ensuring only one notification per task ID exists at any time.
3. WHEN a task's recurrence rule is set to an empty string, THE NotificationScheduler SHALL cancel any pending notification matching that task's ID.
4. WHEN a task is deleted from the task list by the user, THE EditTaskListViewModel SHALL invoke cancellation of any pending notification for that task's ID.
5. THE NotificationScheduler SHALL accept a task ID (String), title (maximum 100 characters), body (maximum 300 characters), and ISO-8601 due date string as scheduling parameters.
6. IF cancellation of a pending notification fails due to a platform error, THEN THE NotificationScheduler SHALL complete without throwing exceptions and SHALL log a warning indicating the task ID and failure reason.

### Requirement 5: Notification Content

**User Story:** As a user, I want notifications to identify which task list and task is due, so that I can act on the reminder without opening the app first.

#### Acceptance Criteria

1. THE NotificationScheduler SHALL use the format "Task due: {taskListName}" as the notification title, where {taskListName} is the name of the task list containing the task.
2. THE NotificationScheduler SHALL use the task's description text as the notification body.
3. IF the task description is empty, THEN THE NotificationScheduler SHALL use the task list name as the notification body.
4. THE NotificationScheduler SHALL schedule the notification to fire at 00:00 (midnight) of the due date in the user's local time zone.
5. IF the notification body text exceeds 200 characters, THEN THE NotificationScheduler SHALL truncate it to 200 characters.

### Requirement 6: Platform Notification Implementations

**User Story:** As a user on any supported platform, I want notifications to work using native platform APIs, so that reminders integrate naturally with my device.

#### Acceptance Criteria

1. THE Android NotificationScheduler implementation SHALL use AlarmManager to schedule notifications and NotificationManager to display them.
2. WHEN cancel is called on Android, THE Android NotificationScheduler implementation SHALL remove the corresponding AlarmManager pending intent and dismiss any displayed notification for that task ID.
3. THE iOS NotificationScheduler implementation SHALL use UNUserNotificationCenter with a UNCalendarNotificationTrigger to schedule notifications, and removePendingNotificationRequests to cancel them.
4. IF SystemTray is supported by the JVM runtime, THEN THE JVM Desktop NotificationScheduler implementation SHALL display a system tray notification. IF SystemTray is not supported, THEN THE JVM Desktop NotificationScheduler implementation SHALL use a coroutine-delayed in-process alert.
5. THE Web NotificationScheduler implementation SHALL use the browser Notification API for both JS and WasmJS targets.
6. THE NotificationScheduler interface SHALL be provided via Koin dependency injection with platform-specific implementations bound in each platform's Koin module.
7. WHEN cancel is called, THE NotificationScheduler implementation on each platform SHALL remove any pending scheduled notification matching the given task ID within 1 second.

### Requirement 7: Notification Permission Handling

**User Story:** As a user, I want the app to handle notification permissions gracefully, so that missing permissions do not cause crashes or degrade the urgency coloring feature.

#### Acceptance Criteria

1. IF notification permission is denied or has never been granted by the platform, THEN THE NotificationScheduler.schedule() and NotificationScheduler.cancel() SHALL complete without throwing exceptions and without scheduling or canceling any notification.
2. IF notification permission is denied, THEN THE DueDateTag urgency coloring SHALL continue to render according to Requirement 2 acceptance criteria independently of the notification subsystem state.
3. WHILE notification permission is denied, THE NotificationScheduler SHALL log a warning at each schedule() invocation indicating the task ID and reason (permission denied).

### Requirement 8: Past Due Date Notification Handling

**User Story:** As a user, I want the system to handle edge cases where a due date is already past when scheduling occurs, so that no stale or confusing notifications are delivered.

#### Acceptance Criteria

1. IF the due date is strictly before today in the user's local time zone when schedule is called, THEN THE NotificationScheduler SHALL skip scheduling and complete without creating a notification or throwing an exception.
2. WHEN schedule is called with a due date equal to today in the user's local time zone, THE NotificationScheduler SHALL proceed with scheduling the notification normally.
3. IF the due date is strictly before today in the user's local time zone, THEN THE DueDateTag SHALL display Overdue urgency coloring independently of whether a notification was scheduled or skipped.

### Requirement 9: Notification Security

**User Story:** As a user, I want my task data to be handled securely in notifications, so that sensitive information is not exposed on my lock screen.

#### Acceptance Criteria

1. THE Android NotificationScheduler implementation SHALL use VISIBILITY_PRIVATE for notification content to prevent lock screen exposure, displaying a redacted placeholder such as "You have a task reminder" on the lock screen.
2. THE NotificationScheduler SHALL not persist task content (title, body) beyond what is required for the pending notification payload; once the notification fires or is canceled, any stored content SHALL be removed.
3. THE iOS NotificationScheduler implementation SHALL set hiddenPreviewsBodyPlaceholder on the notification category so that lock screen previews do not reveal the task description.
