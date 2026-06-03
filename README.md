# ChainChat ⛓️💬

ChainChat is a decentralized, database-less, and end-to-end encrypted (E2EE) messaging application for Android. It operates on a custom peer-to-peer blockchain network where every device acts as a node, maintaining its own local ledger and synchronizing with others over a local network.

## 🌟 Key Features

-   **Decentralized Architecture**: No central servers. Messaging happens directly between peers (P2P) using TCP sockets.
-   **End-to-End Encryption (E2EE)**:
    -   **Key Agreement**: ECDH (Elliptic Curve Diffie-Hellman) using `secp256r1`.
    -   **Signatures**: ECDSA with SHA-256 for integrity and authentication.
    -   **Encryption**: AES-GCM (256-bit) for message payload privacy.
-   **Blockchain Ledger**: Messages are stored as transactions within blocks. Each block is linked to the previous one via SHA-256 hashes.
-   **Proof-of-Work (PoW)**: Implements a mining mechanism to ensure ledger integrity and prevent spam.
-   **Modern UI**: Built with Jetpack Compose featuring a dark-themed, glassmorphic design.
-   **Local Multi-Profile Switcher**: Facilitates testing by allowing users to toggle between identities (e.g., Alice and Bob) on a single device.

## 🛠️ Tech Stack

-   **Language**: [Kotlin](https://kotlinlang.org/)
-   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
-   **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
-   **Networking**: Java TCP Sockets (`ServerSocket` & `Socket`)
-   **Cryptography**: Java Cryptography Architecture (JCA)

## 🏗️ Architecture

### Blockchain Structure
-   **Transaction**: Contains sender/recipient addresses (public key hashes), ephemeral public keys for ECDH, ciphertext, IV, and an ECDSA signature.
-   **Block**: Contains an index, timestamp, list of transactions, previous block hash, nonce, and current block hash.
-   **Consensus**: Follows the "longest chain" rule. Nodes validate incoming blocks based on hash integrity and PoW requirements.

### P2P Networking
Nodes listen on a default port (`9922`). Communication involves:
-   **Handshake**: Exchanging public keys and identity info.
-   **Sync**: Fetching missing blocks from peers.
-   **Broadcast**: Sending newly mined blocks to all connected peers.

## 🚀 Getting Started

### Prerequisites
-   Android Studio (Ladybug or newer recommended)
-   Android SDK 34+
-   Two or more devices (or emulators) on the same Wi-Fi network.

### Installation
1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Deploy to your Android devices.

### How to Use
1. **Initialize Profile**: Enter a username and generate your cryptographic keys.
2. **Find Your Address**: Your public address is displayed at the top of the screen.
3. **Connect to Peer**: 
    - Enter the recipient's **Public Address**.
    - (Optional) Enter the recipient's **IP Address** to establish a direct connection.
4. **Send Message**: Type your message and hit send. The app will:
    - Derive a shared secret via ECDH.
    - Encrypt the message with AES-GCM.
    - Sign the transaction with ECDSA.
    - Mine a new block (PoW).
    - Broadcast the block to the network.

## 📂 Project Structure

```text
app/src/main/java/com/example/myapplication/
├── blockchain/         # Block and Transaction data models, Mining logic
├── crypto/             # ECDH, ECDSA, and AES implementation
├── network/            # P2P Server and Client socket management
├── ui/                 # ViewModels and UI state management
└── MainActivity.kt     # Jetpack Compose UI entries
```

## 🔒 Security Protocol

1.  **Identity**: Every user has a persistent EC KeyPair. The Public Key acts as their address.
2.  **Encryption**: For every message, an ephemeral EC KeyPair is generated. A shared secret is derived using the sender's ephemeral private key and the recipient's static public key.
3.  **Authentication**: The recipient verifies the sender's signature using the sender's static public key, ensuring the message hasn't been tampered with and truly came from the claimed sender.

## 📄 License
This project is licensed under the MIT License.
