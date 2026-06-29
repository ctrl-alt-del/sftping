# Files Page: Hidden Toggle, Sort & Search — Test Plan

## Unit Tests

### FileView (JVM)
- [ ] **hides dot-files by default**: Arrange a mix incl. `.bashrc`; Act
  `apply(showHidden=false, query="", NAME_ASC)`; Assert dot-files absent.
- [ ] **shows dot-files when enabled**: Act `apply(showHidden=true, ...)`; Assert
  dot-files present.
- [ ] **search filters by name (case-insensitive, dirs + files)**: query `"LOG"`;
  Assert only entries containing `log` remain.
- [ ] **NAME_ASC / NAME_DESC**: Assert files ordered A–Z / Z–A within the files group.
- [ ] **SIZE**: Assert files ordered largest-first; folders still first.
- [ ] **LAST_MODIFIED**: Assert newest-first.
- [ ] **folders always first**: Assert all directories precede files regardless of
  sort mode.
- [ ] **empty query / empty input**: Assert no crash, returns filtered+sorted list.

### FilesViewModel (JVM)
- [ ] **existing**: "sorts directories first then by name" still passes (default
  NAME_ASC, folders-first, showHidden=false).
- [ ] **toggleShowHidden recomputes without re-listing**: Arrange listing incl.
  dot-file; Act `toggleShowHidden()`; Assert dot-file now shown and `listFiles`
  invoked only once.
- [ ] **setSearchQuery filters in-memory**: Act `setSearchQuery("a")`; Assert
  `files` filtered and `listFiles` not called again.
- [ ] **setSortMode reorders in-memory**: Act `setSortMode(NAME_DESC)`; Assert
  order reversed (files group) and no extra `listFiles`.
- [ ] **navigate clears search, keeps sort/hidden**: set query + sort + hidden,
  `navigateTo`; Assert query cleared, sort/hidden retained.

## Integration / UI (manual)
- [ ] Search reveals/clears; sort menu radio reflects active; hidden checkbox works.
- [ ] Empty search → "No matching files"; empty folder → "This folder is empty".

## Edge Cases
- [ ] Tie-break by name for equal size / directories under Size sort.
- [ ] Query with no matches → informative empty state, no crash.
