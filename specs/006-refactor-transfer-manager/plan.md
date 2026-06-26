# Plan: Refactor TransferManager

## **Feature ID:** 006
## **Status:** 🚧 In Progress

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
