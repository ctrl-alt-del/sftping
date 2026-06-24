# Atomic File Operations — Test Plan

> To be detailed when this feature is started.

## Unit
- [ ] Overwrite-name detection logic
- [ ] Partial-file cleanup on cancel (logic)

## Integration / Instrumented
- [ ] Upload round-trip (SAF → remote → listing)
- [ ] Download round-trip (remote → cache → SAF), size verified
- [ ] Delete (file + recursive folder), rename

## Edge Cases
- [ ] Cancel mid-transfer; permission denied; stale SAF Uri
