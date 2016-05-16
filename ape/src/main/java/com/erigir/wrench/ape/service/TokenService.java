package com.erigir.wrench.ape.service;

import com.erigir.wrench.CrockfordBase32;
import com.erigir.wrench.QuietObjectMapper;
import com.erigir.wrench.ZipUtils;
import com.erigir.wrench.ape.exception.InvalidTokenException;
import com.erigir.wrench.ape.model.CustomerToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * As of version 1.2.0, you may specify more than one key for the token service - it accepts a list
 * If you do so, any new tokens are encrypted with the first key, but the other keys are checked for
 * decrypting tokens.  This is to simplify the process of key rotation - just add a new key to the start
 * of the list and restart the server.  After the expiration time has passed any old keys can
 * be removed from the list as they will be invalid anyway.
 * Created by cweiss on 7/18/15.
 */
public class TokenService {
    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);
    private static final String CIPHER = "AES/ECB/PKCS5Padding";
    private List<String> encryptionKeys;
    private QuietObjectMapper objectMapper;
    private CrockfordBase32 base32 = new CrockfordBase32();

    public String createToken(String keyValue, Long expires, Map<String, Object> otherData) {
        LOG.debug("Creating token {} / {} / {}", keyValue, expires, otherData);
        Objects.requireNonNull(keyValue);
        Objects.requireNonNull(expires);

        // We always encrypt with the first key
        String encryptionKey = encryptionKeys.get(0);
        CustomerToken token = new CustomerToken(keyValue, expires, otherData);
        String raw = objectMapper.writeValueAsString(token);
        byte[] compressed = ZipUtils.zipData(raw.getBytes());

        try {
            byte[] encrypted = cipher(Cipher.ENCRYPT_MODE, encryptionKey).doFinal(compressed);
            String rval = base32.encodeToString(encrypted);
            return rval;
        } catch (Exception e) {
            throw new IllegalStateException("Shouldnt happen, error on encode: (key size was " + encryptionKey.length() + " and msg size was " + raw.length() + ") " + e, e);
        }
    }

    public void validateToken(CustomerToken token)
            throws InvalidTokenException {
        if (token == null) {
            throw new InvalidTokenException("Supplied string was not a valid token");
        }
        if (token.getExpires() == null || System.currentTimeMillis() > token.getExpires()) {
            throw new InvalidTokenException("Token expired at " + token.getExpires() + " and it is " + System.currentTimeMillis());
        }
    }

    private Cipher cipher(int mode, String key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), CIPHER.substring(0, 3));
            Cipher nCipher = Cipher.getInstance(CIPHER);
            //IvParameterSpec iv = new IvParameterSpec("0000000000000000".getBytes());
            nCipher.init(mode, keySpec);//,iv);
            return nCipher;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            // Like these would be handled at runtime... right?
            throw new RuntimeException("Error creating cipher", e);
        }
    }

    public CustomerToken extractAndValidateToken(String tokenString)
            throws InvalidTokenException {
        CustomerToken token = extractToken(tokenString);
        validateToken(token);
        return token;
    }

    public CustomerToken extractToken(String tokenString) {
        CustomerToken rval = null;
        for (int i = 0; i < encryptionKeys.size() && rval == null; i++) {
            rval = innerExtractToken(tokenString, encryptionKeys.get(i));
        }
        return rval;
    }

    private CustomerToken innerExtractToken(String tokenString, String key) {
        try {
            byte[] zippedData;
            byte[] encryptedText = base32.decode(tokenString.getBytes());
            zippedData = cipher(Cipher.DECRYPT_MODE, key).doFinal(encryptedText);
            byte[] unzippedData = ZipUtils.unzipData(zippedData);
            CustomerToken output = objectMapper.readValue(unzippedData, CustomerToken.class);
            return output;
        } catch (Exception e) {
            LOG.trace("Unable to decrypt token with this key");
            return null;
        }
    }

    public void setEncryptionKeys(List<String> encryptionKeys) {
        this.encryptionKeys = encryptionKeys;
    }

    /**
     * Here for backwards compatibility with previous versions
     *
     * @param encryptionKey A single key that tokenservice will decrypt with
     */
    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKeys = (encryptionKey == null) ? null : Collections.singletonList(encryptionKey);
    }

    public void setObjectMapper(QuietObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
