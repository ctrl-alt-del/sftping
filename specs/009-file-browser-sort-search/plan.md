---
feature_id: "009"
name: "Files Page: Hidden Toggle, Sort & Search"
status: "✅ Done"
depends_on: ["001"]
touches:
  - "app/src/main/java/com/example/sftping/ui/files/FileView.kt"
  - "app/src/main/java/com/example/sftping/ui/files/FilesViewModel.kt"
  - "app/src/main/java/com/example/sftping/ui/files/FilesScreen.kt"
  - "app/src/test/java/com/example/sftping/ui/files/FileViewTest.kt"
  - "app/src/test/java/com/example/sftping/ui/files/FilesViewModelTest.kt"
created: "2026-06-29"
---

# Files Page: Hidden Toggle, Sort & Search — Plan

## Approach

Separate the **raw fetched listing** from the **displayed listing** so the hidden
toggle, sort selection, and search query recompute in-memory without extra SFTP
round-trips.

A new pure object `FileView` (with a `SortMode` enum) holds the filter+sort logic:
filter hidden dot-files (unless `showHidden`), filter by case-insensitive name
substring, then sort **folders-first** followed by the chosen comparator and a
name tiebreaker. Being pure, it is fully JVM unit-testable.

`FilesViewModel` keeps `rawFiles` plus the view inputs (`showHidden`, `sortMode`,
`searchQuery`) in `FilesUiState`. `loadFiles` stores `rawFiles` and derives
`files` via `FileView.apply`. New setters (`setSortMode`, `toggleShowHidden`,
`setSearchQuery`) update state and recompute — no SFTP. `sortMode` and
`showHidden` persist across navigation; `searchQuery` clears on `navigateTo` /
`navigateBack`.

`FilesScreen` adds a Search toggle (reveals a text row) and a sort/filter
`DropdownMenu` (four sort radio items + a "Show hidden files" checkable item),
keeps Refresh, and shows an adaptive empty state.

No catalog/build change (`material-icons-extended` already present).

## Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Create | `ui/files/FileView.kt` | `SortMode` enum + pure `apply()` (filter + sort) |
| Edit | `ui/files/FilesViewModel.kt` | raw/derived list, view inputs, setters, recompute |
| Edit | `ui/files/FilesScreen.kt` | search row + sort/hidden menu + empty-state |
| Create | `test/.../ui/files/FileViewTest.kt` | unit tests for filter/sort logic |
| Edit | `test/.../ui/files/FilesViewModelTest.kt` | preserve sort test; add view-input tests |

## Risks
- Existing `FilesViewModelTest` "sorts directories first then by name" must stay
  green — preserved by defaulting `sortMode = NAME_ASC`, folders-first, and
  `showHidden = false`.

## Dependencies
- 001 (Connect & Browse) — owns the Files browser.

## ADR-008
Compute the displayed listing from a retained `rawFiles` via a pure `FileView`
(filter hidden + name search, then folders-first + selected sort). Keeps SFTP
round-trips to one per directory; all view changes are in-memory and testable.
Search filters the current directory only (no recursive remote walk).
