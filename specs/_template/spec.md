# [Feature Name] — Specification

<!--
  INSTRUCTIONS FOR AI:
  - Focus on WHAT users need and WHY — never HOW to implement
  - Mark ALL ambiguities as [NEEDS CLARIFICATION: specific question]
  - Do NOT guess. If the user didn't specify something, mark it.
  - Each user story MUST be independently testable and deliverable as MVP
  - Delete this comment block before sharing with stakeholders
-->

## User Stories

<!--
  Stories MUST be prioritized (P1, P2, P3). Each story is an INDEPENDENTLY
  TESTABLE slice — implementing just one story should deliver usable value.
  Assign P1 to the most critical story that forms the MVP.
-->

### User Story 1 — [Brief Title] (Priority: P1 🎯 MVP)

As a [user role], I want [goal] so that [reason].

**Why this priority**: [Explain the value and why it has this priority level]

**Independent Test**: [Describe how this can be verified independently — e.g., "Can be fully tested by [action] and delivers [value]"]

**Acceptance Scenarios**:

1. **Given** [precondition], **When** [action], **Then** [outcome]
2. **Given** [precondition], **When** [action], **Then** [outcome]

---

### User Story 2 — [Brief Title] (Priority: P2)

As a [user role], I want [goal] so that [reason].

**Why this priority**: [Explain]

**Independent Test**: [Describe how to verify independently]

**Acceptance Scenarios**:

1. **Given** [precondition], **When** [action], **Then** [outcome]

---

### User Story 3 — [Brief Title] (Priority: P3)

As a [user role], I want [goal] so that [reason].

**Why this priority**: [Explain]

**Independent Test**: [Describe how to verify independently]

**Acceptance Scenarios**:

1. **Given** [precondition], **When** [action], **Then** [outcome]

---

## Edge Cases

<!-- Enumerate boundary conditions and error states -->
- What happens when [boundary condition]?
- How does the system handle [error scenario]?
- What if [race condition / concurrent access]?

## Functional Requirements

<!-- Numbered for traceability to plan.md and tasks.md -->
- **FR-001**: System MUST [specific capability]
- **FR-002**: System MUST [specific capability]
- **FR-003**: Users MUST be able to [key interaction]
- **FR-004**: System MUST [data requirement]
- **FR-005**: [NEEDS CLARIFICATION: ...]

## Non-Functional Requirements

- **Performance**: [e.g., page load < 200ms p95, 1000 concurrent users]
- **Security**: [e.g., auth method, data encryption, input validation]
- **Accessibility**: [e.g., WCAG 2.1 AA, screen reader support]
- **Observability**: [e.g., structured logging, health check endpoint]
- **Offline**: [e.g., works offline, sync when connected, or not required]

## Success Criteria

<!-- Measurable, technology-agnostic outcomes -->
- **SC-001**: [Measurable metric — e.g., "Users can complete primary task in under 2 minutes"]
- **SC-002**: [Measurable metric]
- **SC-003**: [Business/user satisfaction metric]

## Assumptions

<!-- Document what was assumed when the user didn't specify -->
- [Assumption about target users/environment]
- [Assumption about scope boundaries — what's explicitly OUT of scope]
- [Assumption about existing systems/services to reuse]
- [Dependency on external system or API]
