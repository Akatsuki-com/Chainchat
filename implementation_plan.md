# Decentralized E2EE Blockchain Chat Android App

This proposal outlines the design and implementation of a native Android application for decentralized, database-less, and end-to-end encrypted (E2EE) messaging. The app uses a custom peer-to-peer blockchain network where each device runs a node, storing block data locally in memory/files, and synchronizing with other devices over Wi-Fi/local network via TCP sockets.

---

## Technical Stack & Architecture

```mermaid
flowchart TD
    subgraph Device 1 (Alice)
        UI1[Jetpack Compose UI] <--> App1[App State Manager]
        App1 <--> Crypto1[Java Security JCA]
        App1 <--> Chain1[Blockchain Controller]
        App1 <--> P2P_Server1[TCP Socket Server :9922]
        App1 <--> P2P_Client1[TCP Socket Client]
    end

    subgraph Local Wi-Fi Network
        P2P_Client1 -- Sync Blocks / Send Tx --> P2P_Server2
        P2P_Client2 -- Sync Blocks / Send Tx --> P2P_Server1
    end

    subgraph Device 2 (Bob)
        UI2[Jetpack Compose UI] <--> App2[App State Manager]
        App2 <--> Crypto2[Java Security JCA]
        App2 <--> Chain2[Blockchain Controller]
        App2 <--> P2P_Server2[TCP Socket Server :9922]
        App2 <--> P2P_Client2[TCP Socket Client]
    end
```

### 1. Core Technologies
* **Language**: Kotlin
* **UI**: Jetpack Compose (Material 3) with custom glassmorphic styling, neon color accents, and smooth transitions.
* **Architecture**: MVVM (Model-View-ViewModel) for clean state separation.
* **Cryptography**: Native Java Cryptography Architecture (`java.security`, `javax.crypto`).
  - **Key Agreement**: ECDH (Elliptic Curve Diffie-Hellman) using Secp256r1.
  - **Signatures**: ECDSA with SHA-256 for message integrity and sender authentication.
  - **Encryption**: AES-GCM (256-bit) for message payload encryption.
* **Networking**: Kotlin Coroutines + Java TCP Sockets (`ServerSocket` and `Socket`) to implement the decentralized, peer-to-peer node architecture without external central servers.

### 2. Peer-to-Peer Blockchain Protocol
To ensure no central authority can inspect, censor, or delete messages, the ledger is fully decentralized:
* **The Block**:
  ```kotlin
  data class Block(
      val index: Int,
      val timestamp: Long,
      val transactions: List<Transaction>,
      val previousHash: String,
      val nonce: Long,
      val hash: String
  )
  ```
* **The Transaction**:
  ```kotlin
  data class Transaction(
      val senderAddress: String, // ECDSA Public Key Hash
      val recipientAddress: String,
      val ephemeralPublicKey: String, // Hex-encoded ephemeral ECDH public key
      val ciphertext: String, // Hex-encoded encrypted message
      val iv: String, // Hex-encoded Initialization Vector
      val signature: String, // Hex-encoded signature by sender
      val timestamp: Long
  )
  ```
* **Proof-of-Work**: To simulate blockchain integrity and prevent spam, a block is mined by finding a nonce that yields a SHA-256 hash starting with a configurable prefix (e.g. `"00"`).
* **Consensus**: Nodes exchange blockchain length. If a peer presents a valid blockchain with a longer length (higher height), the node updates its local ledger to match (longest-chain consensus).
* **P2P Sync**:
  - **Server Socket**: Listens on a dedicated port (e.g. `9922`).
  - **Discovery/Connection**: Users type the IP address of another user to link nodes. Once connected, they exchange peer profiles and sync their block ledgers.
  - **Message Broadcast**: When Alice sends a message, her node packages the transaction, mines a block, appends it to her local chain, and sends the block to all connected peers.

---

## User Review Required

> [!IMPORTANT]
> 1. **Testing Setup**: To facilitate testing on a single device/emulator, the app will include a **Local Multi-Profile Switcher**. This allows you to create two distinct identities (e.g., Alice and Bob) on the same device and simulate messages being sent, mined, and decrypted between them locally.
> 2. **Network Permissions**: P2P communication over a local network requires the `android.permission.INTERNET` and `android.permission.ACCESS_WIFI_STATE` permissions. These will be added to the manifest.

---

## Proposed Changes

We will create the Android project in the workspace (`e:\project\BTa`).

### 1. Android Project Scaffolding
We will initialize the Android project using the Android CLI tool:
`android create empty-activity --name="ChainChat" --output=./`

### 2. Key Code Files

#### [NEW] [CryptoManager.kt](file:///e:/project/BTa/app/src/main/java/com/example/chainchat/crypto/CryptoManager.kt)
Handles all cryptographic routines:
* Generating ECDH/ECDSA keypairs.
* Deriving shared AES keys from public/private EC keys.
* Encrypting and decrypting string payloads using AES-GCM.
* Signing bytes with ECDSA and verifying signatures.

#### [NEW] [BlockchainEngine.kt](file:///e:/project/BTa/app/src/main/java/com/example/chainchat/blockchain/BlockchainEngine.kt)
Implements the blockchain logic:
* Block and Transaction data structures.
* Cryptographic block hashing (SHA-256).
* Proof-of-Work mining coroutines.
* Block validation (hash validation, signature validation, previous hash linking).

#### [NEW] [P2PManager.kt](file:///e:/project/BTa/app/src/main/java/com/example/chainchat/network/P2PManager.kt)
Manages TCP sockets:
* Starts a background `ServerSocket` listener.
* Manages active connections to other peer nodes.
* Handles JSON-based socket message formats:
  - `HANDSHAKE`: Exchanging usernames and public keys.
  - `SYNC_REQUEST` / `SYNC_RESPONSE`: Fetching missing blocks.
  - `NEW_BLOCK`: Transmitting newly mined blocks.

#### [NEW] [ChatViewModel.kt](file:///e:/project/BTa/app/src/main/java/com/example/chainchat/ui/ChatViewModel.kt)
Exposes state to the Jetpack Compose UI:
* Current user profile (keys, address, username).
* List of known peers.
* Currently selected chat history (scans blockchain ledger and decrypts messages directed to/from the selected peer).
* Blockchain status (current block height, active peers, mining progress).
* P2P server state (status, local IP address, connected clients).

#### [NEW] [MainActivity.kt](file:///e:/project/BTa/app/src/main/java/com/example/chainchat/MainActivity.kt)
The entry point. Renders the Jetpack Compose UI:
* **Glassmorphic Theme**: Dark Mode by default, utilizing purple, teal, and slate colors.
* **P2P Node Config Panel**: Shows local IP, listening port, and connection fields.
* **Chat Screen**: Clean chat bubble interface.
* **Blockchain Dashboard**: Live visualization of mined blocks, hashes, and nonces.

---

## Verification Plan

### Automated Verification
* We will verify the compilation and build of the APK using the Android CLI:
  `android run --debug` (which compiles and installs it on a running emulator/device).
* We will create a local Kotlin JUnit test class `CryptoAndChainTest.kt` to run cryptographic and blockchain validation checks.

### Manual Verification
1. Open the application.
2. Configure a profile name and generate cryptographic keypairs.
3. Switch between Alice and Bob locally to verify E2E decryption.
4. Launch a second emulator/device, connect them via IP address, send messages, and verify that the blockchain syncs, blocks are mined, and messages are decrypted in real-time.
