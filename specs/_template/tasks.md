# [Feature Name] — Tasks

<!--
  INSTRUCTIONS FOR AI (population rules — follow these strictly):

  1. Derive tasks from spec.md user stories (each gets a phase), plan.md
     technical approach, and data model entities.
  2. Organize by phase:
     - Phase 1: Setup (project scaffolding, dependencies)
     - Phase 2: Foundational (blocking prerequisites — MUST complete before any user story)
     - Phase 3+: One phase per user story in priority order (P1 → P2 → P3)
     - Final Phase: Polish (cross-cutting improvements)
  3. Mark INDEPENDENT tasks within a phase with [P] (different files, no deps).
  4. Label EVERY task with its user story: [US1], [US2], [US3].
  5. Tests (if requested) come FIRST within each user story phase — TDD Red phase.
  6. Within a story phase: tests → models → services → endpoints → integration.
  7. ONE task = ONE commit. If it's too big, split it.

  TASK FORMAT: `- [ ] T### [P?] [US#] Description — file paths`
  Example: `- [ ] T012 [P] [US1] Create User model — src/models/user.py`
-->

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Create project structure per implementation plan
- [ ] T002 Initialize project with dependencies from plan.md Technical Context
- [ ] T003 [P] Configure linting and formatting

---

## Phase 2: Foundational (Blocking Prerequisites)

<!--
  ⚠️ CRITICAL: No user story work can begin until this phase is complete.
  These are the shared abstractions all stories depend on.
-->

- [ ] T004 Set up database schema and migrations
- [ ] T005 [P] Configure authentication/authorization framework
- [ ] T006 [P] Set up API routing and middleware
- [ ] T007 Create base models/entities shared across stories
- [ ] T008 Configure error handling and logging infrastructure
- [ ] T009 Set up environment configuration

**Checkpoint**: Foundation ready — user story implementation can now begin.

---

## Phase 3: User Story 1 — [Title] (Priority: P1) 🎯 MVP

**Goal**: [Brief description of what this story delivers]
**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 1 ⚠️

> Write tests FIRST. Confirm they FAIL before implementing. (TDD Red phase)

- [ ] T010 [P] [US1] Contract test for [endpoint] — tests/contract/test_[name].py
- [ ] T011 [P] [US1] Integration test for [user journey] — tests/integration/test_[name].py

### Implementation for User Story 1

- [ ] T012 [P] [US1] Create [Entity1] model — src/models/[entity1].py
- [ ] T013 [P] [US1] Create [Entity2] model — src/models/[entity2].py
- [ ] T014 [US1] Implement [Service] — src/services/[service].py (depends on T012, T013)
- [ ] T015 [US1] Implement [endpoint] — src/api/[routes].py
- [ ] T016 [US1] Add validation and error handling
- [ ] T017 [US1] Add logging for user story 1 operations

**Checkpoint**: User Story 1 fully functional and independently testable.

---

## Phase 4: User Story 2 — [Title] (Priority: P2)

**Goal**: [Brief description]
**Independent Test**: [How to verify independently]

### Tests for User Story 2 ⚠️

- [ ] T018 [P] [US2] Contract test for [endpoint] — tests/contract/test_[name].py
- [ ] T019 [P] [US2] Integration test for [user journey] — tests/integration/test_[name].py

### Implementation for User Story 2

- [ ] T020 [P] [US2] Create [Entity] model — src/models/[entity].py
- [ ] T021 [US2] Implement [Service] — src/services/[service].py
- [ ] T022 [US2] Implement [endpoint] — src/api/[routes].py
- [ ] T023 [US2] Integrate with US1 components (if needed)

**Checkpoint**: User Stories 1 AND 2 both work independently.

---

## Phase 5: User Story 3 — [Title] (Priority: P3)

**Goal**: [Brief description]
**Independent Test**: [How to verify independently]

### Tests for User Story 3 ⚠️

- [ ] T024 [P] [US3] Contract test for [endpoint] — tests/contract/test_[name].py
- [ ] T025 [P] [US3] Integration test for [user journey] — tests/integration/test_[name].py

### Implementation for User Story 3

- [ ] T026 [P] [US3] Create [Entity] model — src/models/[entity].py
- [ ] T027 [US3] Implement [Service] — src/services/[service].py
- [ ] T028 [US3] Implement [endpoint] — src/api/[routes].py

**Checkpoint**: All user stories independently functional.

---

## Phase N: Polish & Cross-Cutting Concerns

- [ ] TXXX [P] Documentation updates in docs/
- [ ] TXXX Code cleanup and refactoring
- [ ] TXXX Performance optimization across all stories
- [ ] TXXX Security hardening
- [ ] TXXX Run quickstart.md validation scenarios

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — starts immediately
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories
- **User Stories (Phases 3+)**: All depend on Foundational completion
  - Stories can proceed in parallel (if team capacity) or sequentially (P1 → P2 → P3)
- **Polish (Final)**: Depends on all desired user stories complete

### Within Each User Story

- Tests MUST be written and FAIL before implementation (TDD)
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before next priority (if sequential)

### Parallel Opportunities

- [P] tasks within the same phase can run in parallel
- Once Foundational completes, all user story phases can run in parallel
- Tests within a story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel

---

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Phase 1: Setup → Phase 2: Foundational → Phase 3: User Story 1
2. STOP and VALIDATE: Test US1 independently, demo/deploy if ready

### Incremental Delivery
1. Setup + Foundational → Foundation ready
2. + User Story 1 → Test independently → MVP deploy
3. + User Story 2 → Test independently → Deploy
4. + User Story 3 → Test independently → Deploy
5. + Polish → Final release

### Parallel Team Strategy
1. Team completes Setup + Foundational together
2. Once Foundational done: Dev A → US1, Dev B → US2, Dev C → US3
3. Each story completes and integrates independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing (TDD Red phase)
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
