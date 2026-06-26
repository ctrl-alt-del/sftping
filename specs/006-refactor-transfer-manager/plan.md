---
feature_id: "006"
name: "TransferManager Refactor"
status: "✅ Done"
depends_on: ["004"]
touches:
  - "app/src/main/java/com/example/sftping/transfer/TransferManager.kt"
  - "app/src/main/java/com/example/sftping/transfer/TransferItem.kt"
  - "app/src/main/java/com/example/sftping/transfer/strategy/"
  - "app/src/main/java/com/example/sftping/transfer/usecase/"
  - "app/src/main/java/com/example/sftping/work/SftpTransferWorker.kt"
  - "app/src/main/java/com/example/sftping/di/SftpModule.kt"
created: "2026-06-26"
---

# Plan: Refactor TransferManager

### **Overview**
Decouple `TransferManager` from protocol-specific logic and business logic using the Strategy and UseCase patterns. This prepares the codebase for multi-protocol support (SCP, FTPS) and an intelligent orchestrator.

### **Tasks**

| ID | Task | Description | Status |
| :--- | :--- | :--- | :--- |
| 1 | `refactor: introduce TransferStrategy interface` | Define `TransferStrategy` interface and `TransferProgress` data class. | ⏳ Pending |
| 2 | `feat: implement SftpTransferStrategy` | Move `JSch` logic from manager/worker into `SftpTransferStrategy`. | ⏳ Pending |
| 3 | `feat: implement DownloadUseCase and UploadUseCase` | Extract offset calculation, retries, and stream handling into UseCases. | ⏳ Pending |
| 4 | `refactor: convert TransferManager to thin coordinator` | Strip all logic from `TransferManager`. It should only manage `StateFlow`. | ⏳ Pending |
| 5 | `refactor: delegate SftpTransferWorker to UseCases` | Update `SftpTransferWorker` to invoke `DownloadUseCase`/`UploadUseCase`. | ⏳ Pending |
| 6 | `test: add E2E integration tests` | Verify Worker $\rightarrow$ UseCase $\rightarrow$ Strategy $\rightarrow$ Manager flow. | ⏳ Pending |

### **Verification Strategy**

*   **Unit Tests:**
    *   `TransferStrategy` implementations (Mocking the stream/connection).
    *   `DownloadUseCase`/`UploadUseCase` (Mocking the strategy).
    *   `TransferManager` (Testing state emission on progress updates).
*   **Integration Tests:**
    *   `SftpTransferWorker` + `UseCases` (Simulate connection/transfer on device).
*   **E2E Tests:**
    *   Full flow from `Worker` triggering `UseCase` to `UI` receiving `StateFlow` updates.
