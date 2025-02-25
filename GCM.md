
<img src="https://upload.wikimedia.org/wikipedia/commons/2/25/GCM-Galois_Counter_Mode_with_IV.svg" alt="GCM Protocol Diagram" style="background-color: white;">


## Explanation of the Diagram

This diagram provides a visual overview of the Galois/Counter Mode (GCM) protocol, illustrating how encryption and authentication are combined:

- **Initialization Vector (IV) Integration:**  
  The IV is used to derive the initial counter block. When the IV is 96 bits (the recommended length), it is concatenated with a fixed suffix (typically 31 zero bits followed by a 1) to form the starting counter value.

- **Counter Mode Encryption (CTR):**  
  The diagram shows how successive counter values are generated (using an increment function) and then encrypted with the block cipher (e.g., AES). The resulting keystream is XORed with the plaintext to produce the ciphertext.

- **GHASH Authentication:**  
  In parallel with encryption, the ciphertext (and any additional authenticated data, or AAD) is processed through the GHASH function. This function performs multiplications in the finite field GF(2^128) using a hash subkey (derived by encrypting an all-zero block). The GHASH output, which serves as a cumulative authentication value, is later combined with an encrypted counter block to generate the final authentication tag.

- **Output Combination:**  
  The final output of GCM consists of the ciphertext along with the authentication tag, ensuring both confidentiality and integrity of the transmitted data.

This integrated design allows GCM to provide authenticated encryption in a single pass over the data, supporting parallel processing for high throughput.

---

*The remainder of the document details the inner workings of GCM, including its mathematical basis, encryption process, and a Mermaid diagram illustrating the flow of operations.*
