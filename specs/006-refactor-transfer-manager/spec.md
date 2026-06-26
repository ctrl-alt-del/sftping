# Spec: Refactor TransferManager to Decoupled Architecture

## **Title: Refactor: Decoupled Transfer Orchestration (Strategy & UseCase Pattern)**

**Status:** 🚧 In Progress (Planning)  
**Version:** 1.0  
**Last Updated:** 202X-XX-XX

---

## **1. Overview**
The current implementation of `TransferManager` and `SftpTransferWorker` suffers from high coupling. The manager simultaneously handles UI state coordination, business logic (retry/offset calculations), and protocol-specific SFTP operations.

This refactor migrates the application to a **layered, decoupled architecture**.

---

## **2. Current State & Problems**
*   **The "God Object" Risk:** `TransferManager` is becoming a monolithic coordinator. It knows too much about `JSch` and the specific mechanics of SFTP.
*   **Scalability Ceiling:** Adding new protocols (FTP, SCP) currently requires invasive changes to the core manager.
*   **Testability Barriers:** It is difficult to unit-test the "business logic" (e.g., "What happens when a transfer is paused and then resumed?") without mocking the entire system and the networking layer.

---

## **3. Proposed Architecture**

We will move from a single-layer orchestration to a three-tier architecture:

### **3.1 The Protocol Layer (`TransferStrategy`)**
An interface that defines *what* a protocol can do, without knowing *how* it is done.
*   **Key Methods:** `get()`, `put()`, `list()`, `checkIntegrity()`.
*   **Implementation:** The existing SFTP logic will be moved into an `SftpTransferStrategy`.
*   **Benefit:** Adding FTP or SCP becomes a matter of adding a new class implementing this interface, with zero changes required to the `TransferManager`.

### **3.2 The Logic Layer (`TransferUseCase`)**
Encapsulates the "Brain" of the transfer.
*   **Responsibilities:** 
    *   Calculating byte offsets for resumable transfers.
    *   Managing retry policies and error classification.
    *   Orchestrating the checksum verification (Integrity) process.
*   **Classes:** `DownloadUseCase`, `UploadUseCase`, `IntegrityCheckUseCase`.

### **3.3 The Coordination Layer (`TransferManager`)**
A "Thin" singleton state holder.
*   **Responsibility:** Exposing a single `StateFlow<List<TransferItem>>` to the UI.
*   **Logic:** It only receives progress/status updates from the UseCases and broadcasts them to the UI. It has **zero** knowledge of SFTP, streams, or file bytes.

---

## **4. Implementation Plan (Commit Sequence)**

The refactor will be executed in six atomic, verifiable commits:

1.  **`refactor: introduce TransferStrategy interface`**  
    Define the contract and the `TransferProgress` data class.
2.  **`feat: implement SftpTransferStrategy`**  
    Move current `JSch` logic into the strategy implementation.
3.  **`feat: implement DownloadUseCase and UploadUseCase`**  
    Move all "business logic" (resumption/retries) into these classes.
4.  **`refactor: convert TransferManager to thin coordinator`**  
    Remove all logic from `TransferManager`, leaving only state management.
5.  **`refactor: delegate SftpTransferWorker to UseCases`**  
    Update the background worker to trigger UseCases instead of direct manager calls.
6.  **`test: add E2E integration tests`**  
    Verify the complete flow from Worker $\rightarrow$ UseCase $\rightarrow$ Strategy $\rightarrow$ Manager.

---

## **5. Success Criteria**
*   [ ] `TransferManager` contains no imports from `com.jcraft.jsch` or any file-stream libraries.
*   [ ] `DownloadUseCase` can be unit-tested in isolation using a `MockStrategy`.
*   [ ] `SftpTransferWorker` no longer contains logic for calculating byte offsets.
*   [ ] **Regression Check:** The UI continues to display transfer progress correctly without modification.

## **6. Risk Assessment**
*   **Regression in State Flow:** Changes to how `TransferManager` updates its state might cause UI flickering.
    *   *Mitigation:* Maintain exact compatibility with the existing `TransferItem` model and `StateFlow` signatures.
*   **Increased Complexity:** The number of classes in the codebase will increase.
    *   *Mitigation:* This is a calculated investment to enable the "Smart Orchestrator" and Multi-Protocol support in Phase 2.
