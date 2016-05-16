package com.erigir.wrench.ape.service;

import com.erigir.wrench.QuietObjectMapper;
import com.erigir.wrench.ape.model.CustomerToken;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by cweiss1271 on 5/15/16.
 */
public class TestTokenService {

    @Test
    public void testCreateToken()
            throws Exception {

        TokenService tokenService1 = new TokenService();
        tokenService1.setEncryptionKeys(Arrays.asList("0123456789012345"));
        tokenService1.setObjectMapper(new QuietObjectMapper());

        TokenService tokenService2 = new TokenService();
        tokenService2.setEncryptionKeys(Arrays.asList("5432109876543210", "0123456789012345"));
        tokenService2.setObjectMapper(new QuietObjectMapper());

        String key = "TEST";
        Long expires = System.currentTimeMillis() + (1000 * 60 * 60); // 1 hour
        Map<String, Object> other = Collections.singletonMap("Testkey", (Object) "Testvalue");

        String token = tokenService1.createToken(key, expires, other);
        CustomerToken testToken = tokenService2.extractAndValidateToken(token);

        assertEquals(key, testToken.getKey());
        assertEquals(expires, testToken.getExpires());
        assertEquals(other.get("Testkey"), testToken.getOtherData().get("Testkey"));

        //tokenService.encryptTest();

    }
}
