# Feature Roadmap

This document is the staged implementation contract for Baby App.

Each stage produces a usable, tested increment. Stages are implemented one at a time and only when the project owner explicitly asks for that stage. When product scope changes, update both this roadmap and [README.md](README.md).

## Status legend

- `[ ]` Not started
- `[~]` In progress
- `[x]` Complete
- `[-]` Removed by product decision

## Definition of done for every stage

A stage is complete only when:

- Its listed features are implemented.
- Relevant unit, integration, and UI tests pass.
- The app builds successfully in Android Studio and from the command line.
- Existing completed features still work.
- Loading, empty, error, and accessibility states are handled.
- New user-facing text is available in Danish and English where localization is active.
- Privacy and safety requirements relevant to the stage are addressed.
- README and roadmap status match the implementation.
- Known limitations and deferred work are documented.
- The handoff includes a feature-specific Android Studio test checklist.
- The handoff includes copy-ready Git commands and an appropriate commit message for use after project-owner testing.
- No commit or push is performed unless the project owner explicitly requests it.

## Stage 0 — Product specification

**Status:** `[x] Complete`

### Deliverables

- Product vision and target users
- Feature inventory
- Danish healthcare alignment
- Medical-safety boundaries
- Android technical direction
- Privacy principles
- Staged development roadmap

### Completion criteria

- [x] README contains the living product specification.
- [x] Roadmap divides the complete app into implementable stages.
- [x] No application code has been created.

---

## Stage 1 — Android foundation and design system

**Status:** `[x] Complete`

**Outcome:** A buildable Android app with the agreed architecture, navigation shell, visual foundation, and automated quality checks.

### Features

- Create the Android Studio project in Kotlin.
- Configure Jetpack Compose and Material 3.
- Decide and document minimum and target Android SDK versions.
- Add Gradle Kotlin DSL and version catalog.
- Establish initial modules without premature fragmentation.
- Add dependency injection, navigation, coroutines, and serialization.
- Create light, dark, and night-capable theme foundations.
- Define typography, spacing, shapes, colors, icons, and accessible controls.
- Create empty Today, Timeline, Insights, Guide, and Family destinations.
- Add Danish and English string resources.
- Establish test, lint, formatting, and CI-ready build commands.
- Add the initial privacy-safe logging strategy.

### Acceptance criteria

- [x] Project opens and builds with the Android Studio toolchain.
- [x] Debug app launches on an Android emulator.
- [x] All five navigation destinations are reachable.
- [x] Themes support light and dark system modes plus a night-mode foundation.
- [x] Navigation UI test passes at normal and 150% font scale.
- [x] Unit and Compose UI test examples run successfully.
- [x] No analytics, advertising, account, or network dependency is included.

### Not included

- Persistent child data
- Tracking features
- Cloud synchronization
- Health content

---

## Stage 2 — Onboarding and child profiles

**Status:** `[x] Complete`

**Outcome:** Parents can complete onboarding and safely create, edit, select, and remove local child profiles.

### Features

- Welcome and product-boundary onboarding.
- Language, region, units, and theme preferences.
- Local privacy explanation.
- Create and edit child profiles.
- Profile photo or built-in avatar.
- Birth and due-date information.
- Birth measurements and gestational age.
- Premature-child data and corrected-age calculation.
- Optional GP, hospital, health visitor, allergy, and medical notes.
- Multiple children.
- Clearly visible active-child selector.
- Confirmation for destructive profile removal.
- Room database and migration-test foundation.
- DataStore preferences.

### Acceptance criteria

- [x] App works without an account or internet connection.
- [x] Multiple profiles can be created and switched.
- [x] Active child remains selected after app restart.
- [x] Corrected age is calculated and clearly labeled where relevant.
- [x] Profile images are downsampled and stored in private internal storage.
- [x] Required validation and accessible error messages are present.
- [x] Removing a child requires explicit confirmation.
- [x] Database and ViewModel tests cover profile operations.

---

## Stage 3 — Core tracking: feeding and diapers

**Status:** `[ ] Not started`

**Outcome:** The most frequent newborn activities can be recorded quickly and corrected later.

### Features

- Today dashboard with configurable quick actions.
- Breastfeeding timer with left, right, switch, pause, resume, and stop.
- Last-side memory.
- Bottle records with content, amount offered, and amount consumed.
- Pumping timer and amount records.
- Wet, dirty, both, and dry diaper quick records.
- Optional diaper observations.
- Manual and backdated entry for every included event.
- Edit and soft-delete records.
- Recent-event cards and daily totals.
- Correct child attached to every event.
- Persistent notification for active feeding or pumping.
- Timer recovery after process death.

### Acceptance criteria

- [ ] Common feeding and diaper events can be recorded in approximately two taps.
- [ ] A running session remains correct after screen rotation and process recreation.
- [ ] Left/right switching produces accurate segment and total durations.
- [ ] Manual records support past dates and times.
- [ ] Editing recalculates totals correctly.
- [ ] Deleted records no longer appear but remain recoverable internally.
- [ ] Events never silently move between children.
- [ ] Notification controls operate the active timer correctly.
- [ ] Timer, Room, and core interaction tests pass.

---

## Stage 4 — Sleep tracking and unified timeline

**Status:** `[ ] Not started`

**Outcome:** Parents can track sleep reliably and review all care events in one editable timeline.

### Features

- Start and stop sleep with one tap.
- Nap and nighttime sleep types.
- Optional location, settling method, awakenings, quality, and notes.
- Persistent sleep notification and lock-screen actions.
- Active timer restoration after interruption or process death.
- Overlap detection and resolution.
- Unified chronological timeline.
- Filters by event type, date, and child.
- Day grouping and clear running-event display.
- Add, edit, and soft-delete from the timeline.
- Empty states and large-history performance.

### Acceptance criteria

- [ ] Sleep can be started and stopped in approximately two taps.
- [ ] Invalid overlapping sleep sessions are prevented or resolved explicitly.
- [ ] Daylight-saving changes do not corrupt recorded duration.
- [ ] Timeline displays feeding, diaper, pumping, and sleep consistently.
- [ ] Filters and editing remain usable with a large local dataset.
- [ ] Notification and app controls stay synchronized.
- [ ] Timeline and sleep tests pass.

---

## Stage 5 — Insights, reminders, export, and backup

**Status:** `[ ] Not started`

**Outcome:** Recorded data becomes useful for understanding routines and preparing for healthcare conversations.

### Features

- Daily and weekly feeding summaries.
- Bottle amount summaries.
- Diaper counts and observation summaries.
- Sleep totals, naps, longest stretch, and time-of-day patterns.
- Neutral, non-diagnostic insight wording.
- Configurable date ranges.
- Local reminders and notification preferences.
- PDF health-visit summary.
- CSV or structured data export.
- Local encrypted backup and restore.
- Questions and notes for the next appointment.

### Acceptance criteria

- [ ] Summaries update correctly after add, edit, or delete.
- [ ] No insight claims a diagnosis or guarantees normal health.
- [ ] Reminder behavior accounts for Android permission and battery restrictions.
- [ ] Export clearly identifies child, date range, units, and time zone.
- [ ] Backup and restore reproduce the original data.
- [ ] Sensitive exports require an explicit user action and warning.
- [ ] Calculation, export, and restoration tests pass.

---

## Stage 6 — Growth, teeth, and development

**Status:** `[ ] Not started`

**Outcome:** Parents can maintain a useful long-term record of growth, teeth, milestones, and memories.

### Features

- Weight, length or height, and head-circumference records.
- Temperature and optional practical measurements.
- Source and method for measurements.
- Growth history and visual curves.
- Clear distinction between observations and medical interpretation.
- Interactive tooth chart and eruption records.
- Toothbrushing, dental visit, and injury records.
- Developmental and custom milestones.
- Non-competitive milestone language.
- Photo, video, and journal attachments.
- Updated timeline, insights, and export support.

### Acceptance criteria

- [ ] Measurement units and Danish decimal formatting are correct.
- [ ] Charts remain readable with sparse or dense data.
- [ ] A single measurement never generates a health conclusion.
- [ ] Premature-child age context is displayed where relevant.
- [ ] Tooth and milestone records are editable and exportable.
- [ ] Sensitive attachments are stored privately.
- [ ] Calculation, chart, and record tests pass.

---

## Stage 7 — Health records and Danish healthcare schedule

**Status:** `[ ] Not started`

**Outcome:** The app can organize symptoms, medicine, supplements, appointments, and the Danish preventive-care schedule without acting as a diagnostic tool.

### Features

- Symptom and illness event log.
- Temperature, eating, drinking, wet diapers, energy, and responsiveness observations.
- Healthcare contact and advice records.
- Medication definitions and administration log.
- Supplement reminders.
- No automatic medication-dose calculation.
- Appointments and questions to ask.
- Danish preventive child-examination schedule.
- Danish vaccination schedule and user records.
- Scheduled, completed, postponed, and declined vaccination states.
- Clear label that app records are not official health records.
- Summary for GP, health visitor, or hospital conversations.

### Acceptance criteria

- [ ] Health records use neutral observation language.
- [ ] Medication entries require the parent to enter the prescribed dose.
- [ ] Preventive examination dates can be personalized without rewriting the official schedule.
- [ ] Vaccination guidance includes source and review date.
- [ ] The UI never implies connection to sundhed.dk, MinSundhed, a GP, or a hospital.
- [ ] Relevant health data appears in timeline and export.
- [ ] Health-domain tests pass.

---

## Stage 8 — Danish information guide and urgent help

**Status:** `[ ] Not started`

**Outcome:** Parents receive reviewed, age-appropriate guidance and can quickly find the correct route to professional help.

### Features

- Versioned content model with source and review date.
- Age- and situation-based guide navigation.
- Initial articles for first days, feeding, diapers, sleep, crying, temperature, supplements, safety, food, teeth, and development.
- “Common”, “be aware”, and “seek help” content structure.
- Contextual tips with strict notification limits.
- Permanently accessible urgent-help screen.
- 112 guidance.
- Own GP and stored care contacts.
- Region-specific route to out-of-hours medical care.
- Hospital, health visitor, maternity, and poison contacts.
- Visible medical disclaimer and clinician review metadata.

### Acceptance criteria

- [ ] Every health article has a source, review date, version, and applicable age.
- [ ] Danish clinical content has been reviewed by an appropriate professional before production release.
- [ ] Urgent help is reachable quickly from primary app screens.
- [ ] Region-specific information is accurate at the recorded review date.
- [ ] The app never uses tracked values to declare that a child is safe.
- [ ] Outdated content can be replaced without changing historical user records.
- [ ] Content navigation and urgent-help tests pass.

---

## Stage 9 — Solid food, allergens, and milk inventory

**Status:** `[ ] Not started`

**Outcome:** The feeding system supports later infancy with food readiness, allergen observations, and stored breast-milk management.

### Features

- Solid-food records with food, meal, amount, and texture.
- Enjoyed, refused, and reaction observations.
- Allergen introduction history.
- Explicit safety wording for suspected reactions.
- Stored breast-milk inventory.
- Storage location and use-before information.
- Used and discarded status.
- Expiry reminders with non-medical wording.
- Updated feeding insights and export.

### Acceptance criteria

- [ ] Food guidance does not assume readiness at one exact age.
- [ ] Reactions are observations and never automated diagnoses.
- [ ] Stored milk quantities remain consistent when used or discarded.
- [ ] Inventory history is auditable.
- [ ] Feeding summaries distinguish milk and solid-food events.
- [ ] Inventory and allergen-log tests pass.

---

## Stage 10 — Privacy, security, accessibility, and release hardening

**Status:** `[ ] Not started`

**Outcome:** The offline application meets the quality, privacy, accessibility, and safety bar for a serious public beta.

### Features

- Biometric or PIN app lock.
- Android Keystore-backed secrets.
- Private notification preview setting.
- Attachment and export security review.
- Data retention, full export, and deletion controls.
- Privacy notice and consent records where required.
- Threat model and documented security decisions.
- Accessibility audit and remediation.
- Performance and battery profiling.
- Database migration and recovery testing.
- Crash reporting assessment with privacy-preserving configuration, if adopted.
- Final Danish and English copy review.
- Release build configuration and signing documentation.

### Acceptance criteria

- [ ] Data protection impact assessment is completed before production release.
- [ ] Security review finds no unresolved critical or high-risk issues.
- [ ] No advertising or health-data profiling exists.
- [ ] CPR number is not collected.
- [ ] Export and permanent deletion are verified end to end.
- [ ] TalkBack, large text, contrast, and touch-target checks pass.
- [ ] Core flows perform acceptably on the minimum supported device.
- [ ] Release build passes the full automated test suite.

### Release milestone

Completion of Stage 10 produces the planned **offline-first public beta**.

---

## Stage 11 — Accounts, cloud backup, and caregiver sharing

**Status:** `[ ] Not started`

**Outcome:** Families can optionally share records and securely synchronize across devices while local-only use remains supported.

### Features

- Backend selection based on privacy, EU hosting, reliability, and operational assessment.
- Optional user accounts.
- Encrypted cloud backup and synchronization.
- Partner and caregiver invitations.
- Roles and permissions.
- Near-real-time event updates.
- Shared active timers.
- Author and change history.
- Defined conflict resolution.
- Caregiver handover summary.
- Access revocation.
- Account export and deletion.
- Recovery flow.

### Acceptance criteria

- [ ] The app remains fully usable without an account.
- [ ] Transport and stored cloud data are encrypted appropriately.
- [ ] Simultaneous edits have deterministic, tested behavior.
- [ ] Revoked caregivers lose access promptly.
- [ ] Audit history identifies the author of shared records.
- [ ] Account deletion removes server data according to the documented policy.
- [ ] Sync works after long offline periods without losing records.
- [ ] Security and privacy reviews cover the selected backend.

---

## Stage 12 — Advanced Android experience

**Status:** `[ ] Not started`

**Outcome:** The app uses Android platform capabilities to make frequent recording even faster.

### Features

- Configurable home-screen widgets.
- Android app shortcuts.
- Improved lock-screen controls.
- Optional voice shortcuts.
- Tablet and foldable layouts.
- Multi-window behavior.
- Careful background-work and battery optimization.
- Wear OS feasibility evaluation and optional quick recording.

### Acceptance criteria

- [ ] Widgets always show the selected child clearly.
- [ ] Quick actions cannot silently record for the wrong child.
- [ ] Platform features degrade safely when permissions are denied.
- [ ] Phone, tablet, foldable, and multi-window layouts remain usable.
- [ ] Battery profiling shows no unjustified persistent background work.
- [ ] Platform-specific UI and integration tests pass.

---

## Stage 13 — Pregnancy and postpartum

**Status:** `[ ] Not started`

**Outcome:** The app supports families before birth and through postpartum recovery while integrating naturally with the child profile created after birth.

### Features

- Pregnancy profile and due date.
- Pregnancy week and appointments.
- Scans, symptoms, notes, and attachments.
- Birth preferences.
- Contraction timer.
- Hospital bag checklist.
- Birth record and conversion to a child profile.
- Postpartum recovery observations.
- Parent medication and reminders.
- Pelvic-floor and recovery reminders.
- Parent well-being check-ins and support contacts.

### Acceptance criteria

- [ ] Pregnancy data converts safely to a child profile without duplicate records.
- [ ] Contraction timing survives process death.
- [ ] Parent and child health records remain clearly separated.
- [ ] Well-being features do not diagnose mental-health conditions.
- [ ] Guidance and urgent contacts are source-reviewed.
- [ ] Pregnancy, birth-transition, and postpartum tests pass.

---

## Stage 14 — Older-child support

**Status:** `[ ] Not started`

**Outcome:** The product remains useful beyond toddlerhood while maintaining appropriate child privacy and health boundaries.

### Features

- School health checks.
- Hearing and vision records.
- Injuries and chronic-condition journal.
- Sports and activity records where useful.
- Medicine-at-school information.
- Emotional well-being journal with safety boundaries.
- Documents and emergency information.
- Age-appropriate child access and consent design.
- Updated navigation that does not burden newborn users.

### Acceptance criteria

- [ ] Older-child features remain hidden or unobtrusive for newborn profiles.
- [ ] Child access follows an explicit age and privacy model.
- [ ] Sensitive parent-only notes cannot leak into child access.
- [ ] Health features remain observational and non-diagnostic.
- [ ] Older-child records are included correctly in export and deletion.
- [ ] Permission, privacy, and domain tests pass.

---

## Stage 15 — Final release readiness

**Status:** `[ ] Not started`

**Outcome:** All planned features work together as a coherent, documented, secure, and production-ready Android application.

### Deliverables

- Complete cross-feature regression pass.
- UX consistency review across every child age and feature.
- Clinical-content revalidation.
- Privacy, legal, medical-device, and security re-evaluation.
- Store listing, screenshots, privacy disclosures, and support material.
- Backup, restore, account, and deletion verification.
- Production monitoring and incident-response plan.
- Versioning, release, rollback, and support procedures.
- Final user acceptance test in Android Studio and on representative devices.

### Acceptance criteria

- [ ] All required earlier stages are complete or explicitly removed from scope.
- [ ] No unresolved critical defects remain.
- [ ] Full test suite and release build pass.
- [ ] Clinical sources and contact details are current at release review.
- [ ] Store privacy declarations match actual behavior.
- [ ] Production operations and support ownership are documented.
- [ ] README accurately describes the shipped product.

### Release milestone

When Stage 15 and every non-removed prerequisite stage are complete, the planned application is considered **done and ready for production release**.

## Scope change log

Use this table whenever a requested feature changes the agreed plan.

| Date | Change | Affected stage(s) | Decision |
| --- | --- | --- | --- |
| 2026-08-17 | Initial product and implementation roadmap created. | 0–15 | Accepted |
| 2026-08-17 | Android foundation implemented with API 26 minimum, API 37 target, one initial app module, Hilt, Compose, and five-destination navigation. | 1 | Complete |
| 2026-08-17 | Added a required testing and Git handoff after every implemented stage or standalone feature. | All stages | Accepted |
| 2026-08-17 | Implemented offline onboarding, Room-backed child profiles, DataStore preferences, private photos, corrected age, and active-child persistence. | 2 | Complete |
| 2026-08-17 | Improved Stage 2 with visible validation dialogs, expected-child profiles, immediate onboarding language changes, reliable sex selection, metric/imperial units, settings, and date/time pickers. | 2 | Complete |
| 2026-08-17 | Changed child-profile validation to popup-only messages in onboarding and profile editing. | 2 | Complete |
| 2026-08-17 | Added health-professional contact details and made a read-only child profile the default Family view, with edit and delete actions nested inside it. | 2 | Complete |
| 2026-08-17 | Moved language selection before welcome, changed birth status and sex to dropdowns, and expanded collapsible provider and health-information fields. | 2 | Complete |
| 2026-08-17 | Added a debug-only, confirmation-protected developer control that clears all local app data for first-run testing. | 2 | Complete |
| 2026-08-17 | Fixed first-run locale application, changed region to a dropdown, removed duplicate night mode and gestational inputs, expanded avatars, and added reusable parent profiles linked to children. | 2 | Complete |
| 2026-08-17 | Removed child creation from onboarding, added a Family add-profile menu and reusable parent/other-member profiles with two-way child linking and inferred siblings. | 2 | Complete |
| 2026-08-17 | Refined collapsible child sections, separated parent and extended-family role choices, and redesigned child viewing into colored Parents, Siblings, and Other family sections. | 2 | Complete |
| 2026-08-17 | Simplified onboarding to Danish welcome only, globalized Settings, restored two-way family linking, added family photos and child color themes, and made care providers add-driven. | 2 | Complete |
| 2026-08-17 | Replaced fixed care-provider slots with repeatable records, moved custom provider titles inside colored cards, applied child colors to headings, and removed family linking from child editing. | 2 | Complete |
| 2026-08-17 | Required every family profile to link to a child, added co-parent roles, and made child gender an explicit required choice with opt-out and Other options. | 2 | Complete |

## Implementation request examples

- “Implement Stage 1.”
- “Implement Stage 3, but leave pumping for Stage 9.”
- “Add a contraction timer to the plan, but do not implement it yet.”
- “Remove cloud sharing from the final product.”

For any scope change, update the feature descriptions, affected stages, acceptance criteria, and scope change log before or alongside implementation.
