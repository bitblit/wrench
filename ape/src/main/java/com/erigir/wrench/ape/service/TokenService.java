package com.erigir.wrench.ape.service;

import com.erigir.wrench.QuietObjectMapper;
import com.erigir.wrench.ZipUtils;
import com.erigir.wrench.ape.exception.InvalidTokenException;
import com.erigir.wrench.ape.model.CustomerToken;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;

import java.util.Map;
import java.util.Objects;

/**
 * Created by cweiss on 7/18/15.
 */
public class TokenService {
    private String encryptionKey;
    private QuietObjectMapper objectMapper;
    private Base32 base32 = new Base32();
    private static final String CIPHER = "AES";

    public String createToken(String keyValue, Long expires, Map<String,Object> otherData)
    {
        Objects.requireNonNull(keyValue);
        Objects.requireNonNull(expires);

        CustomerToken token = new CustomerToken(keyValue, expires, otherData);

        String raw = objectMapper.writeValueAsString(token);
        byte[] compressed = ZipUtils.zipData(raw.getBytes());

        try {
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), CIPHER);
            Cipher nCipher = Cipher.getInstance(CIPHER);
            nCipher.init(Cipher.ENCRYPT_MODE, keySpec);

            return base32.encodeAsString(nCipher.doFinal(compressed));
        } catch (Exception e) {
            throw new IllegalStateException("Shouldnt happen, error on encode: (key size was " + encryptionKey.length() + " and msg size was " + raw.length() + ") " + e, e);
        }
    }

    public void validateToken(CustomerToken token)
            throws InvalidTokenException
    {
        Objects.requireNonNull(token);
        if (token.getExpires()==null || System.currentTimeMillis()>token.getExpires())
        {
            throw new InvalidTokenException("Token expired at "+token.getExpires()+" and it is "+System.currentTimeMillis());
        }
    }

    public CustomerToken extractAndValidateToken(String token)
            throws InvalidTokenException
    {
        byte[] zippedData;
        try
        {
            byte[] encryptedText = base32.decode(token.getBytes());
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), CIPHER);
            Cipher nCipher = Cipher.getInstance(CIPHER);
            nCipher.init(Cipher.DECRYPT_MODE, keySpec);

            zippedData = nCipher.doFinal(encryptedText);
        }
        catch (Exception e)
        {
            throw new InvalidTokenException("Failed to decrypt/unzip token",e);
        }
        byte[] unzippedData = ZipUtils.unzipData(zippedData);
        CustomerToken output = objectMapper.readValue(unzippedData, CustomerToken.class);
        validateToken(output);

        return output;
    }


    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public void setObjectMapper(QuietObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
