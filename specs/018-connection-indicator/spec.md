# Connection Status Indicator — Specification

## User Story
As a user, I want to see at a glance whether the SFTP connection is alive,
across all tabs (Files, Transfers, Editor), so I don't have to navigate to
the Connect tab to check.

## UX/UI
- [x] Mockup: `ux-ui/mockup.png` — two-panel split showing the Files tab with
      green (connected) and red (disconnected) indicator bars.
- A thin 4dp bar sits at the very top of each screen, above the TopAppBar.
- A small 8dp colored dot marks the left side of the bar.
- **Green** (Material Green 500, `#4CAF50`) when connected.
- **Red** (Material error color) when disconnected.
- The bar uses a subtle tinted background (`color.copy(alpha = 0.15f)`).
- `AnimatedVisibility` provides a smooth fade-in/expand transition on state change.
- Not shown on the Connect tab (the Connect/Disconnect button already conveys
  connection state).

## Acceptance Criteria
- [ ] Files tab shows a green bar when connected, red when disconnected.
- [ ] Transfers tab shows a green bar when connected, red when disconnected,
      including when the transfer list is empty.
- [ ] Editor tab (both location list and editor pane) shows a green bar when
      connected, red when disconnected.
- [ ] The bar transitions smoothly (no flicker) on connect/disconnect.
- [ ] The bar is collected reactively from `SessionState.connected` via each
      ViewModel's `init {}` + `collect`. No polling.

## Edge Cases
- [ ] Transfers tab empty state still renders the indicator.
- [ ] Quick connect/disconnect/connect doesn't leave the bar in a stale state.

## Non-Functional Requirements
- Performance: no additional SFTP operations, no polling. Single StateFlow collect.
- Accessibility: the bar is decorative; colors are universally understood.
- Zero new dependencies.
