# Files Page: Hidden Toggle, Sort & Search — Takeaways

## What Went Well
- Splitting `rawFiles` (network) from `files` (displayed) made hidden/sort/search pure,
  in-memory recomputations — zero extra SFTP round-trips.
- Extracting `FileView` as a pure object kept all filter/sort logic in fast JVM unit tests
  and left the ViewModel thin.
- Defaulting `sortMode = NAME_ASC` + folders-first + `showHidden = false` preserved the
  existing "directories first then by name" test with no change.

## What We Learned
- A `compareByDescending { it.isDirectory }.then(sortComparator).thenBy { name }` chain
  cleanly expresses "folders first, then chosen sort, then stable tiebreak".
- Persist sort/hidden across navigation but clear the search query on folder change — it
  matches user expectations and avoids stale filters hiding a new folder's contents.

## API / Tech Surprises
- `material-icons-extended` already covered `Icons.Filled.Search`/`Sort`; no new deps.
- Keeping the sort/hidden `DropdownMenu` open on the hidden toggle (but closing on a sort
  pick) reads better — the user sees the checkmark flip.

## Patterns Worth Reusing
<!-- These get promoted to root MEMORY.md after ship -->
- Raw-vs-derived list in a browser ViewModel + pure `FileView` (see MEMORY #ui /
  "Patterns That Worked" and ADR-008).
