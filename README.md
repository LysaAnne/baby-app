# Baby App

An Android-first companion for pregnancy, newborn care, and childhood, designed around Danish families and the Danish healthcare system.

The app will help parents quickly record everyday care such as feeding, diaper changes, and sleep while also providing a longer-term overview of growth, development, teeth, health, appointments, and important memories.

The product should feel simple, modern, calm, and cute. It must be useful during a tired 03:00 feeding, but structured well enough to follow a child for years.

> [!IMPORTANT]
> This repository is currently in the planning stage. Implementation is divided into explicit stages in [ROADMAP.md](ROADMAP.md). A stage is only implemented when the project owner asks for that specific stage.

## Product vision

The app should make it possible to:

- Record the most important baby activities in seconds.
- Understand feeding, diaper, sleep, and growth patterns without overwhelming parents.
- Keep information for multiple children in one place.
- Share useful summaries with a health visitor, GP, or other caregiver.
- Give new parents trustworthy, age-appropriate information based on Danish guidance.
- Support both everyday observations and important health records.
- Grow from a newborn tracker into a useful family health and development journal.

The app is a tracker, organizer, and information tool. It is not a diagnostic service and must never imply that it replaces a doctor, health visitor, midwife, hospital, emergency service, or other healthcare professional.

## Target users

- Expecting parents
- Parents of newborns and infants
- Parents of toddlers
- Families with multiple children
- Partners and other trusted caregivers
- Parents of premature children
- Single parents and shared households

The first releases will focus on newborns and toddlers. The data model and design should leave room for pregnancy, postpartum, and older-child features later.

## Product principles

### Fast when it matters

Common events should take approximately two taps to start or record. Running timers must be easy to stop from the app or an Android notification.

### Calm, not judgmental

The app should show patterns and observations without grading parents or children. Normal variation must be respected, especially for sleep, feeding, and development.

### Local-first and private

The core app should work offline and without an account. Sensitive child and health information must not be used for advertising or profiling.

### Danish by design

Terminology, healthcare milestones, vaccination information, safety guidance, and contact paths should fit the Danish health and hospital system.

### Useful for different families

The app must support breastfeeding, bottle feeding, mixed feeding, pumping, premature children, twins or multiple children, and different family structures without presenting one approach as the only correct one.

### Accessible at night

Large controls, one-handed operation, dark mode, low-brightness night mode, and screen-reader support are core requirements rather than optional polish.

## Main navigation

The main app uses five destinations:

1. **Today** — current status, running timers, quick actions, recent events, totals, and reminders.
2. **Timeline** — a chronological record with filtering, editing, deletion, and manual entry.
3. **Insights** — feeding, sleep, diaper, health, and growth summaries.
4. **Guide** — age-specific information, Danish healthcare milestones, safety guidance, and official sources.
5. **Family** — child profiles, caregivers, customization, privacy, export, backup, and app settings.

A persistent add action provides access to all record types. The selected child must always be clearly visible.

## Planned features

### Child profiles

- Multiple child profiles
- Name, nickname, date and time of birth, and due date
- Profile photo or illustrated avatar
- Biological sex where required for clinical growth references
- Birth weight, length, and head circumference
- Gestational age and premature-birth information
- Chronological and corrected age where relevant
- Birth hospital, GP, and health visitor details
- Allergies and important medical notes
- Optional blood type
- Child-specific color and theme
- Safe child switching that prevents recording for the wrong profile

### Feeding

#### Breastfeeding

- Start on left or right breast with one tap
- Switch sides during a session
- Pause, resume, and stop
- Duration per side and total duration
- Remember the last breast used
- Optional latch, discomfort, position, and behavior notes
- Manual and backdated entries
- Persistent Android notification for active sessions

#### Bottle feeding

- Breast milk, formula, mixed, water, or custom contents
- Amount offered and consumed
- Timestamp or timed session
- Optional reusable formula presets
- Optional notes for refusal, spit-up, or reactions

#### Pumping and stored milk

- Left, right, or both sides
- Timer and amount per side
- Storage location and use-before information
- Refrigerated and frozen milk inventory
- Mark stored milk as used or discarded

#### Solid food

- Food and meal type
- Approximate amount and texture
- Enjoyed or refused
- Allergen introduction log
- Possible reaction log
- Guidance based on development and readiness rather than a rigid date

### Diapers

- One-tap wet, dirty, both, or dry records
- Optional urine amount
- Stool color and consistency
- Blood, mucus, smell, or unusual observations
- Diaper rash tracking
- Notes and optional sensitive photo attachment
- Educational diaper guide for the first days and weeks

The app must not diagnose a condition from diaper information.

### Sleep

- One-tap start and stop
- Nap or nighttime sleep
- Optional sleep location and settling method
- Awakenings and parent-observed sleep quality
- Manual additions and corrections
- Persistent notification for an active timer
- Overlap detection
- Daily and weekly totals, naps, longest stretch, and patterns

Sleep information should describe age-based ranges and individual variation rather than assign a simplistic sleep score.

### Growth and measurements

- Weight
- Length or height
- Head circumference
- Temperature
- Optional clothing and shoe sizes
- Measurement source: home, health visitor, GP, or hospital
- Growth history and curves
- Notes and measurement method

Growth displays should emphasize trends. The app must not infer that one measurement confirms a health problem or that a child is healthy.

### Teeth

- Interactive mouth diagram
- Eruption dates
- Symptoms and notes
- Toothbrushing routine
- Dental visits
- Dental injuries
- Tooth-loss tracking for older children

### Development and memories

- Common milestones such as smiling, rolling, sitting, crawling, walking, and first words
- Custom milestones
- Approximate and non-competitive milestone guidance
- Notes, photos, and video memories
- Family journal

### Health

#### Symptoms and illness

- Temperature and symptoms
- Start and end time
- Eating, drinking, wet diapers, energy, and responsiveness
- Parent-observed severity
- Healthcare contacts and advice received
- Attachments and notes
- Clinician-friendly event summary

The app records observations and must not suggest a diagnosis.

#### Medication and supplements

- Medication name, strength, prescribed dose, and schedule
- Given, skipped, and delayed records
- Prescriber and treatment period
- Reaction and notes
- Reminders for clinician-recommended medication and supplements

Medication dosage must not be calculated from weight unless a future, properly validated medical feature explicitly supports it.

#### Appointments and Danish healthcare timeline

- Health visitor visits
- GP appointments
- Preventive child examinations
- Vaccinations
- Hospital follow-ups
- Hearing, vision, and dental appointments
- User-created appointments
- Appointment notes and questions to ask

The app should include the Danish preventive child examinations at approximately 5 weeks, 5 months, 12 months, and 2, 3, 4, and 5 years.

#### Vaccinations

- Current Danish childhood vaccination schedule
- Scheduled, completed, postponed, or declined status
- Optional batch number, injection site, and reaction notes
- Reminders and official source links
- Visible distinction between app records and official health records

The app must not imply synchronization with MinSundhed, sundhed.dk, a GP, or a hospital unless an official integration is implemented.

### Information guide

The guide will be organized by age and situation instead of being one large encyclopedia.

Topics may include:

- The first days after birth
- Signs of adequate feeding
- Breastfeeding and bottle preparation
- Diapers and stool
- Safe sleep
- Crying and soothing
- Skin, bathing, umbilical cord, and nails
- Tummy time and head shape
- Temperature and clothing
- Vitamin supplements
- Solid food and allergens
- Teeth and dental care
- Common childhood infections
- Accident prevention
- Car seats and transport
- Development and play
- Screen use
- Daycare and returning to work
- Parental mental health
- Relationships and shared parenting
- Support for single parents
- When and whom to call

Every health article should include:

- Applicable age
- What is common
- What to be aware of
- When to seek professional help
- Source and link
- Last clinical review date
- Content version

Clinical content should be reviewed by an appropriate Danish healthcare professional before public release.

### Urgent help

A permanently accessible help screen should provide:

- 112 for life-threatening emergencies
- The user's own GP
- Region-specific out-of-hours medical service
- Relevant maternity ward or birth department
- Poison information
- Saved health visitor and hospital contacts
- Short, clinician-reviewed warning signs

The app should ask for the user's Danish region so it can display the relevant route to urgent care.

The interface must clearly state that the app cannot determine whether a child is seriously ill and that worried parents should contact a healthcare professional.

### Insights and export

- Daily and weekly summaries
- Feeding duration and amount
- Diaper counts and observations
- Sleep totals and patterns
- Growth history
- Medication and symptom history
- Configurable date ranges
- PDF summary for health visits
- CSV or structured data export
- Questions and notes for the next appointment

Insights must use neutral language and avoid unsupported medical conclusions.

### Family and caregiver collaboration

- Optional partner and caregiver invitations
- Role-based access
- Near-real-time synchronization
- Running timer visibility across caregivers
- Record author and audit history
- Caregiver handover summary
- Access revocation
- Export and deletion

Cloud collaboration is planned after the offline app is stable.

### Android-specific experience

- Persistent notifications for active timers
- Notification actions for switch, pause, and stop
- Lock-screen controls
- Home-screen widgets
- Android shortcuts
- Optional voice shortcuts
- Offline functionality
- Device reboot and process-death recovery
- Tablet and foldable layouts in a later stage
- Optional Wear OS quick recording if user demand supports it

### Customization

- Curated color themes
- Light, dark, and automatic mode
- Extra-dim night-feeding mode
- Child-specific accent colors
- Profile photos and illustrated avatars
- Reorderable quick actions
- Configurable dashboard cards
- Compact and comfortable display density
- Danish and English

The design should use a soft Scandinavian visual style, rounded surfaces, calm colors, accessible contrast, large tap targets, and limited friendly illustration. Important health information must remain clear and serious.

## Future expansion

### Pregnancy and postpartum

- Due date and pregnancy week
- Appointments and scans
- Pregnancy symptoms and notes
- Birth preferences
- Contraction timer
- Hospital bag checklist
- Birth record
- Postpartum recovery
- Pelvic-floor reminders
- Parent medication and well-being
- Support contacts

### Older children

- School health checks
- Hearing and vision
- Injuries and chronic conditions
- Medicine at school
- Emotional well-being
- Sports and activity
- Documents and emergency contacts
- Age-appropriate child access and privacy

These features are not part of the initial release unless the roadmap is explicitly changed.

## Technical direction

### Android stack

- Kotlin
- Jetpack Compose and Material 3
- Gradle Kotlin DSL and version catalog
- MVVM with unidirectional data flow
- Clean separation between UI, domain, and data layers
- Hilt for dependency injection
- Room for structured local data
- DataStore for preferences
- WorkManager for reliable scheduled work
- Foreground services and notifications for running timers
- Navigation Compose
- Kotlin Coroutines and Flow
- Kotlin Serialization
- Coil for images
- JUnit and Compose UI testing

The initial minimum Android version should be decided during Stage 1 after checking device-support needs. API 26 is the current planning assumption.

### Architecture

The app will be local-first:

```text
Jetpack Compose UI
        |
ViewModels and use cases
        |
Repository interfaces
        |
Room, DataStore, and encrypted files
        |
Optional encrypted synchronization backend
```

Suggested modules as the app grows:

```text
app
core:model
core:database
core:designsystem
core:ui
core:notifications
core:security
feature:onboarding
feature:today
feature:feeding
feature:diapers
feature:sleep
feature:timeline
feature:insights
feature:growth
feature:health
feature:guide
feature:family
feature:settings
```

The project should begin with only the modules needed at that stage. New modules should be extracted when feature boundaries justify them rather than creating an unnecessarily fragmented initial project.

### Core domain model

Planned entities include:

- `Child`
- `Caregiver`
- `CaregiverChildAccess`
- `TrackedEvent`
- `BreastfeedingSession`
- `BottleFeed`
- `PumpingSession`
- `StoredMilkItem`
- `SolidFoodEvent`
- `DiaperEvent`
- `SleepSession`
- `Measurement`
- `TemperatureRecord`
- `SymptomRecord`
- `Medication`
- `MedicationAdministration`
- `ToothEvent`
- `Milestone`
- `Appointment`
- `VaccinationRecord`
- `Note`
- `Attachment`
- `Reminder`
- `ContentArticle`
- `ContentVersion`
- `AuditEntry`

Records should use stable UUIDs and include the relevant child, timestamps, originating time zone, author, source, creation and modification time, notes, soft-deletion state, and synchronization version where applicable.

### Timer behavior

Timers must derive elapsed time from persisted start timestamps rather than an in-memory counter. This allows active sessions to survive:

- App termination
- Android process death
- Device rotation
- Temporary connectivity loss
- Device reboot where Android permits restoration

The app should prevent or explicitly resolve invalid overlapping sessions.

### Synchronization

Synchronization is optional and comes after the local experience:

- Account-free local use remains available.
- Data is encrypted in transit and at rest.
- Synchronization happens at event level.
- Simultaneous edits have defined conflict behavior.
- Changes retain author and audit information.
- Caregiver access can be revoked.
- Users can export and delete their data.
- EU hosting and operational requirements must be evaluated before choosing a backend.

## Privacy, security, and medical safety

The app processes sensitive information about children and health. The implementation must plan for:

- Data minimization
- Clear purpose and consent
- Privacy by design and default
- No advertising SDKs
- No sale or profiling of health data
- Encryption in transit and at rest
- Android Keystore for local secrets
- Optional biometric or PIN protection
- Private notification previews
- Caregiver permissions and access history
- Data export and deletion
- Configurable retention
- EU data hosting for cloud services
- Secure backup and recovery
- Threat modeling and security review
- A data protection impact assessment before public release
- A security incident process

CPR numbers should not be collected for the planned feature set.

Any future feature that predicts illness, interprets growth, recommends treatment, or calculates medication doses requires separate clinical, legal, and medical-device assessment.

## Testing principles

Automated and manual testing must cover:

- Timer recovery after process death and reboot
- Overlapping sessions
- Daylight-saving and time-zone changes
- Correct child selection
- Database migrations
- Notification and reminder reliability
- Unit conversion and Danish decimal formatting
- Export correctness
- Caregiver permissions and sync conflicts
- Offline use
- One-handed and nighttime use
- Twins and multiple children
- Premature-child corrected age
- Breast, bottle, mixed, and pumping workflows
- TalkBack, large text, contrast, and color-vision accessibility

## Official sources

Health content should use current primary Danish sources and store a review date rather than assuming recommendations never change.

- [Sundhedsstyrelsen: Børn 0–2 år](https://www.sst.dk/vidensbase/graviditet-og-smaaboern/barnets-sundhed/boern-0-2-aar)
- [Sundhedsstyrelsen: Børnevaccinationsprogrammet](https://www.sst.dk/boernevaccination)
- [Sundhedsstyrelsen: Amning](https://www.sst.dk/da/Borger/Graviditet-og-smaaboern/Barnets-sundhed/Anbefalinger-om-kost-til-spaed--og-smaaboern/Amning)
- [Sundhedsstyrelsen: D-vitamin og jerntilskud](https://www.sst.dk/vidensbase/graviditet-og-smaaboern/kost-til-smaaboern/d-vitamin-og-jerntilskud-til-boern)
- [Sundhedsstyrelsen: Forebyggelse af vuggedød](https://www.sst.dk/vidensbase/graviditet-og-smaaboern/barnets-sundhed/boern-0-2-aar/forebyggelse-af-vuggedoed)
- [Sundhedsstyrelsen: Anbefalinger for søvnlængde](https://www.sst.dk/vidensbase/forebyggelse/anbefalinger-for-soevnlaengde)
- [sundhed.dk: Børneundersøgelser](https://www.sundhed.dk/borger/patienthaandbogen/boern/undersoegelser/boerneundersoegelser/)
- [Datatilsynet: Helbredsoplysninger](https://www.datatilsynet.dk/regler-og-vejledning/grundlaeggende-begreber/helbred)

## Development workflow

The complete implementation plan is maintained in [ROADMAP.md](ROADMAP.md).

The following rules apply:

1. Do not implement a roadmap stage until the project owner explicitly requests it.
2. Implement only the requested stage unless a small prerequisite is necessary to make that stage work.
3. A stage is complete only when its acceptance criteria and tests pass.
4. Mark completed stages and record important decisions in the roadmap.
5. If the project owner adds, removes, postpones, or changes a feature, update this README and the roadmap in the same change.
6. Do not silently omit an agreed feature. Move it to another stage or explicitly mark it as removed.
7. When all required stages are complete, the planned app is considered complete and ready for release preparation.

## Current status

- Product plan: complete
- Technical direction: planned
- Android project: created and verified
- Implementation: Stage 1 complete
- Android baseline: minimum API 26, compile and target API 37
- Current roadmap stage: awaiting instruction for Stage 2
