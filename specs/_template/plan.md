---
feature_id: "NNN"
name: "Feature Name"
status: "🚧 In Progress"
depends_on: []
touches:
  - "FileA.ext"
  - "FileB.ext"
created: "YYYY-MM-DD"
---

# [Feature Name] — Implementation Plan

<!--
  INSTRUCTIONS FOR AI:
  - Fill every section. Use [NEEDS CLARIFICATION: ...] for unknown details.
  - Run the Constitution Check gates FIRST. If any gate fails, document in
    Complexity Tracking with justification.
  - Keep the main document high-level. Extract lengthy code samples, detailed
    algorithms, or schema definitions to the relevant section or reference
    files.
  - Delete this comment block when done.
-->

## Technical Context

**Language/Version**: [e.g., Kotlin 2.0, or NEEDS CLARIFICATION]
**Primary Dependencies**: [e.g., Hilt, Room, WorkManager, JSch mwiede, or NEEDS CLARIFICATION]
**Storage**: [e.g., Room (SQLite), DataStore, or N/A]
**Testing**: [JUnit 5, app/src/test/ (JVM) and app/src/androidTest/ (device) or NEEDS CLARIFICATION]
**Target Platform**: [Android, minSdk 28, targetSdk 36 or NEEDS CLARIFICATION]
**Performance Goals**: [e.g., <100ms UI jank, <10s transfer startup or NEEDS CLARIFICATION]
**Constraints**: [e.g., offline-first, no external APIs, single :app module or NEEDS CLARIFICATION]
**Scale/Scope**: [e.g., single user, <1000 files/dir, <2GB files or NEEDS CLARIFICATION]

## Constitution Check

*GATE: Must pass before implementation. Re-check after design changes.*

- [ ] **Article I (Spec-First)**: spec.md approved by stakeholder?
- [ ] **Article II (Test-First)**: test plan written before implementation code?
- [ ] **Article III (Modularity)**: feature is a self-contained module with clear boundaries?
- [ ] **Article IV (Simplicity)**: ≤3 new files? No speculative features? No future-proofing?
- [ ] **Article V (Anti-Abstraction)**: using framework directly? No unnecessary wrappers?
- [ ] **Article VI (Integration Reality)**: contract tests defined? Real components over mocks?
- [ ] **Article VII (Observability)**: health checks, structured logs, and metrics planned?

## Approach

[High-level strategy. What's the main design idea? How does it fit into the existing architecture?]

## Data Model

<!-- Entities, their key attributes, and relationships. No DDL or code — describe at design level. -->

| Entity | Key Attributes | Relationships |
|--------|---------------|---------------|
| [Entity1] | [attr1, attr2, attr3] | belongs to [Entity2] |
| [Entity2] | [attr1, attr2] | has many [Entity1] |

## API / CLI Contract

<!-- For this Compose/Android app, define Compose UI contracts (screen parameters,
     ViewModel StateFlow shapes), Intent/action contracts, or Worker input/output schemas. -->

| Method / Component | Purpose | Input | Output |
|--------|---------------|---------|-------|
| [Composable/Screen] | [purpose] | [parameters] | [StateFlow shape] |
| [Worker/Action] | [purpose] | [input data] | [result] |

## Alternatives Considered

<!-- One of the MOST IMPORTANT sections. Document what you considered and WHY you chose this approach. -->

| Alternative | Pros | Cons | Why Rejected |
|-------------|------|------|--------------|
| [Alternative A] | [advantages] | [disadvantages] | [reason] |
| [Alternative B] | [advantages] | [disadvantages] | [reason] |

**Decision**: [Which approach was chosen and why the trade-offs are acceptable]

## Risks

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| [Risk description] | Low/Med/High | Low/Med/High | [How we mitigate] |

## Dependencies

| Depends On | Status | Blocking? |
|-----------|--------|-----------|
| [Feature, API, library, or team] | Ready/In Progress/Blocked | Yes/No |

## Complexity Tracking

> Fill ONLY if Constitution Check has violations that must be justified

| Article | Violation | Why Needed | Simpler Alternative Rejected Because |
|---------|-----------|------------|--------------------------------------|
| Art. IV | 5 new files | [justification] | [why simpler approach doesn't work] |

## Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Create | app/src/... | New data model |
| Modify | app/src/... | Add new endpoint |

## Quickstart Validation

<!-- Key scenarios to verify the feature works end-to-end after implementation -->
1. [Validation scenario 1 — step by step]
2. [Validation scenario 2 — step by step]
