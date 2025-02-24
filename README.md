```mermaid
---
config:
  layout: fixed
---
flowchart TD
    %% Encryption Process Subgraph
    subgraph Encryption_Process [Encryption Process]
        A["Plaintext Data"]
        B["Get Secret Key from Keystore"]
        C["Initialize Cipher in ENCRYPT_MODE\n- AES/GCM/NoPadding"]
        D["Auto-generate IV - 12 bytes"]
        E["Encrypt Data\nProduces Ciphertext + Auth Tag"]
        F["Combine IV + Ciphertext"]
        G["Encode Combined Data to Base64"]
    end

    %% Decryption Process Subgraph
    subgraph Decryption_Process [Decryption Process]
        H["Encrypted Data in Base64"]
        I["Decode Base64 to retrieve Combined Data"]
        J["Extract IV - first 12 bytes"]
        K["Extract Ciphertext + Auth Tag"]
        L["Get Secret Key from Keystore"]
        M["Initialize Cipher in DECRYPT_MODE\nwith GCMParameterSpec"]
        N["Decrypt Ciphertext to recover Plaintext"]
    end

    %% Connections within and between subgraphs
    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    F --> G

    H --> I
    I --> J
    I --> K
    J --> M
    K --> M
    L --> M
    M --> N

    %% Apply custom styling for encryption and decryption nodes
    class A,B,C,D,E,F,G encryption;
    class H,I,J,K,L,M,N decryption;

    classDef encryption fill:#DDF,stroke:#333,stroke-width:1.5px,stroke-dasharray: 5 5, color:#000;
    classDef decryption fill:#FDD,stroke:#333,stroke-width:1.5px,stroke-dasharray: 5 5, color:#000;



```