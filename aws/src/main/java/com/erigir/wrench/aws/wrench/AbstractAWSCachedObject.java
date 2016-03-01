package com.erigir.wrench.aws.wrench;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.erigir.wrench.ExtendedQuietObjectMapper;
import com.erigir.wrench.aws.wrench.AWSCachedObject;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Base class for implementing AWSCachedObject
 *
 * Created by cweiss1271 on 2/29/16.
 */
public abstract class AbstractAWSCachedObject<T> implements AWSCachedObject<T>{
    private static final ExtendedQuietObjectMapper OBJECT_MAPPER = new ExtendedQuietObjectMapper();
    private final Class<T> clazz;
    private final TypeReference<T> typeReference;
    private T cache;

    public AbstractAWSCachedObject(Class<T> clazz) {
        super();
        Objects.requireNonNull(clazz);
        this.clazz = clazz;
        this.typeReference = null;
    }

    public AbstractAWSCachedObject(TypeReference<T> typeReference) {
        super();
        Objects.requireNonNull(typeReference);

        this.typeReference = typeReference;
        this.clazz = null;
    }

    protected T deserialize(String json)
    {
        return (T)((clazz==null)?OBJECT_MAPPER.readValue(json, typeReference):OBJECT_MAPPER.readValue(json,clazz));
    }

    protected abstract void saveCacheToStore(String jsonValue);
    protected abstract T loadObjectFromStore();

    public final void value(T value)
    {
        try {
            cache = value;
            saveCacheToStore((value == null) ? null : OBJECT_MAPPER.writeValueAsString(cache));
        }
        catch (Exception e)
        {
            LoggerFactory.getLogger(getClass()).warn("Error while trying to write object to cache : {}",value,e);
        }
    }

    public final T value()
    {
        return (T)((clazz==null)?OBJECT_MAPPER.safeDuplicate(cache, typeReference):OBJECT_MAPPER.safeDuplicate(cache,clazz));
    }

    public void forceCacheReload()
    {
        try
        {
            cache = loadObjectFromStore();
        }
        catch (Exception e)
        {
            LoggerFactory.getLogger(getClass()).warn("Error while trying to read object from cache",e);
        }

    }

}
