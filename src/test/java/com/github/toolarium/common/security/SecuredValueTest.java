/*
 * SecuredValueTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;


/**
 * Test {@link SecuredValue}.
 *  
 * @author patrick
 */
public class SecuredValueTest {

    
    /**
     * Test {@link ISecuredValue}
     */
    @Test
    public void ss() {
        assertEquals("...", new SecuredValue<String>("secureValue1").toString());
        assertEquals("*", new SecuredValue<String>("secureValue2", "*").getSecuredValue());
        assertEquals("secureValue3", new SecuredValue<String>("secureValue3").getValue());
        assertEquals(5, new SecuredValue<>(5, "...").getValue());
        
        assertEquals(new SecuredValue<String>("secureValue"), new SecuredValue<String>("secureValue"));
        assertEquals(new SecuredValue<String>("secureValue", "...."), new SecuredValue<String>("secureValue", "...."));
        assertNotEquals(new SecuredValue<String>("secureValue", "*"), new SecuredValue<String>("secureValue2", "*"));
    }


    /**
     * Test that the value field is transient and not serialized
     *
     * @throws Exception in case of error
     */
    @Test
    public void testSerializationExcludesValue() throws Exception {
        SecuredValue<String> original = new SecuredValue<>("mySecret", "***");
        assertEquals("mySecret", original.getValue());
        assertEquals("***", original.getSecuredValue());

        // serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }

        // deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        SecuredValue<?> deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            deserialized = (SecuredValue<?>) ois.readObject();
        }

        // value should be null (transient), securedValue should survive
        assertNull(deserialized.getValue());
        assertEquals("***", deserialized.getSecuredValue());
    }
}
