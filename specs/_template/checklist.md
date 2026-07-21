# [Feature Name] — Requirements Quality Checklist

<!--
  INSTRUCTIONS: This checklist validates the QUALITY of requirements, not the
  code. Think of it as "unit tests for English." Run this BEFORE writing tasks.md.
  If any item fails, return to spec.md or plan.md to fix before proceeding.
-->

## Requirement Completeness

- [ ] ALL `[NEEDS CLARIFICATION]` markers in spec.md and plan.md are resolved
- [ ] Every user story has at least 2 acceptance scenarios
- [ ] Every functional requirement (FR-XXX) has a corresponding acceptance scenario
- [ ] Edge cases are enumerated for each user story (boundary, error, race condition)
- [ ] Non-functional requirements are specified: performance, security, accessibility, observability
- [ ] Data model entities are defined with key attributes and relationships

## Clarity & Unambiguity

- [ ] No vague terms without definition ("fast," "user-friendly," "scalable")
- [ ] Acceptance scenarios use concrete values, not placeholders
- [ ] Every "When" action has a specific, observable "Then" outcome
- [ ] Error handling behavior is defined (not just "handle errors gracefully")
- [ ] Scope boundaries are explicit — what is OUT of scope

## Consistency

- [ ] User story priorities (P1/P2/P3) reflect actual business value
- [ ] Independent test descriptions for each story describe a complete, useful slice
- [ ] No contradictory acceptance scenarios (e.g., Story 1 says "user is logged out" but Story 2 assumes "user is logged in" without login step)
- [ ] Plan.md technical choices don't contradict spec.md requirements
- [ ] Constitution gates are either passed or justified in Complexity Tracking

## Edge Case Coverage

- [ ] What happens when the user's session expires mid-operation?
- [ ] What happens when the network fails during a multi-step operation?
- [ ] What happens on empty state (no data, first use)?
- [ ] What happens on extreme input (very long strings, special characters, 0, negative)?
- [ ] What happens on concurrent access (two users editing the same resource)?

## Non-Functional Coverage

- [ ] Performance target is specified and measurable
- [ ] Security concerns are identified (auth, data protection, input validation)
- [ ] Accessibility requirements are explicit (WCAG level, screen reader support)
- [ ] Observability requirements are explicit (logging, metrics, health checks)
- [ ] Platform/browser/device support is specified

## Process

- [ ] No speculative or "might need in the future" features
- [ ] No implementation details have leaked into spec.md
- [ ] plan.md Alternatives Considered section is filled (not just "no alternatives")
- [ ] plan.md Complexity Tracking justifies any constitution violations
