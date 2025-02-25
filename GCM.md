
<img src="https://github.com/user-attachments/assets/94a5a034-e1bf-4efc-8b24-ad99bf4a5273" style="background-color: white; padding: 20px; border: 1px solid #ddd; border-radius: 5px;" alt="GCM Protocol Diagram">


## Explanation of the Diagram

This diagram provides a visual overview of the Galois/Counter Mode (GCM) protocol, illustrating how encryption and authentication are combined:

- **Initialization Vector (IV) Integration:**  
  The IV is used to derive the initial counter block. When the IV is 96 bits (the recommended length), it is concatenated with a fixed suffix (typically 31 zero bits followed by a 1) to form the starting counter value.

- **Counter Mode Encryption (CTR):**  
  The diagram shows how successive counter values are generated (using an increment function) and then encrypted with the block cipher (e.g., AES). The resulting keystream is XORed with the plaintext to produce the ciphertext.

### 3. GHASH Authentication

- **Purpose:**  
  While encryption hides the content, GHASH ensures that the ciphertext and any additional authenticated data (AAD) have not been altered. In other words, it detects even the smallest changes made to the data during transmission.

- **How It Works in Detail:**

    1. **Data Preparation:**
        - **Ciphertext and AAD Block Division:**  
          After the plaintext is encrypted using counter mode (CTR), the resulting ciphertext is divided into 128-bit blocks. Similarly, any AAD (which might include header information or metadata) is also split into 128-bit blocks. If the last block of either the ciphertext or the AAD is shorter than 128 bits, it is padded with zeros.

    2. **Generation of the Hash Subkey (H):**
        - **Derivation:**  
          A special value called the hash subkey `H` is produced by encrypting an all-zero 128-bit block with the encryption key using the block cipher (typically AES).
          ```
          H = AES(K, 0^128)
          ```  
          This subkey is fixed for the duration of the encryption and is used in all subsequent GHASH computations.

    3. **Iterative Processing Using a Recurrence Relation:**
        - **Sequential Computation:**  
          The GHASH function processes each 128-bit block in a sequential manner using the following recurrence:
          ```
          Y0 = 0
          Yi = (Yi-1 ⊕ Si) · H
          ```
          Here:
            - `Si` represents the i-th 128-bit block from the combined data (first the AAD blocks, then the ciphertext blocks, and finally a block that encodes the bit lengths of both the AAD and ciphertext).
            - `⊕` denotes the bitwise XOR operation.
            - `·` denotes multiplication in the finite field GF(2^128).

        - **Finite Field Multiplication (GF(2^128)):**  
          In GF(2^128), the addition operation is performed using XOR, and multiplication is carried out modulo a fixed irreducible polynomial (commonly `x^128 + x^7 + x^2 + x + 1`). This means that the multiplication “mixes” the bits in a way that any change in input produces a completely different product.

    4. **Combining into a Single Authentication Value:**
        - **Final GHASH Output:**  
          After processing all blocks, the final computed value (`Ym`) is a single 128-bit number that summarizes all the data. This value is highly sensitive to every bit of input, so even a minor alteration in the ciphertext or AAD will change the outcome.

    5. **Tag Generation:**
        - **Final Step:**  
          The final GHASH output is then combined (typically using XOR) with an encrypted version of a counter block (derived from the IV). The result is truncated (to 128, 120, 112, 104, or 96 bits, based on the implementation) to produce the final authentication tag `T`.
        - **Purpose of the Tag:**  
          This tag is sent along with the ciphertext. During decryption, the recipient re-computes the GHASH value using the received ciphertext and AAD. If the recomputed tag matches the transmitted tag, it confirms both the integrity and authenticity of the data.

- **Key Takeaways:**
    - **Data Integrity Assurance:**  
      Any tampering with the ciphertext or AAD will result in a different GHASH output, which in turn produces a mismatched authentication tag.
    - **Efficient Parallel Processing:**  
      The operations involved in GHASH (XOR and finite field multiplication) are highly parallelizable, making it suitable for high-throughput applications.
    - **Unified Security Mechanism:**  
      By integrating GHASH with counter mode encryption, GCM provides both confidentiality (through encryption) and integrity/authentication (through GHASH) in one cohesive framework.
- **Output Combination:**  
  The final output of GCM consists of the ciphertext along with the authentication tag, ensuring both confidentiality and integrity of the transmitted data.

This integrated design allows GCM to provide authenticated encryption in a single pass over the data, supporting parallel processing for high throughput.

---

*The remainder of the document details the inner workings of GCM, including its mathematical basis, encryption process, and a Mermaid diagram illustrating the flow of operations.*
