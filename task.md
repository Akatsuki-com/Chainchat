# ChainChat Project Tasks 📋

## ✅ Completed
- [x] Initial project scaffolding with Jetpack Compose.
- [x] Basic Cryptography Manager (ECDH, ECDSA, AES-GCM utilities).
- [x] Blockchain Engine (Block/Transaction models, SHA-256 hashing, Proof-of-Work mining).
- [x] Basic P2P Networking (TCP Server/Client, Block broadcasting).
- [x] Prototype UI (Glassmorphic chat interface, Node dashboard).
- [x] Initial `README.md` documentation.
- [x] Application Logo & Icons (Neon Hex-Chain design).

## 🚧 In Progress
- [ ] **Handshake Protocol**: Implement node-to-node handshakes to exchange public keys and identity info in `P2PManager`.
- [ ] **Blockchain Sync**: Implement `SYNC_REQUEST` to fetch missing blocks when connecting to a new peer.
- [ ] **True E2EE Integration**: Implement shared secret derivation in `ChatViewModel` using the recipient's public key.

## 📅 Backlog (Next Steps)
- [ ] **Message Decryption**: Update `updateMessages()` to attempt decryption of all transactions in the ledger using the user's private key.
- [ ] **Multi-Profile Switcher**: Add UI and logic to toggle between "Alice" and "Bob" profiles on a single device for easier testing.
- [ ] **Persistent Ledger**: Save the blockchain to a local file or database (e.g., Room or simple JSON file) so history survives app restarts.
- [ ] **Consensus Logic**: Implement the "Longest Chain" rule to handle forks or discrepancies between peer ledgers.
- [ ] **UI Polish**: Improve the glassmorphic styling, add neon accents, and implement smoother transitions between chat messages.
- [ ] **Network Discovery**: Implement mDNS or similar for automatic local peer discovery without manual IP entry.

## 🧪 Testing & Verification
- [ ] Unit tests for `CryptoManager` (signing/verification, encryption/decryption).
- [ ] Integration tests for `BlockchainEngine` (mining, chain validation).
- [ ] Manual multi-device testing for P2P block broadcasting.
