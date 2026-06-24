# Resilience Hardening & Release — Test Plan

> To be detailed when this feature is started.

## Instrumented / Manual fault injection
- [ ] Airplane toggle; server restart; disk full → no corruption
- [ ] Reboot → stale SAF Uri re-auth
- [ ] Security: encrypted creds, no secret logging, SHA-256 fingerprint

## Release checks
- [ ] applicationId no longer `com.example.*`; R8 build runs; licenses listed
