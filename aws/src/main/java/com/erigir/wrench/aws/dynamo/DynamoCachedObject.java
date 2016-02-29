package com.erigir.wrench.aws.dynamo;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.erigir.wrench.ExtendedQuietObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * This object assumes the existence of a Table in DynamoDB with
 * hash key "applicationId" and range key "objectId".  It
 * will store objects in there in a field called "document".
 *
 * Basically, it offers "get" and "put" functionality, where the
 * "put" call is auto-backed by writing the state to the dynamodb
 * table, and the "get" call is auto-backed by reading from
 * dynamodb at startup.  Get will return null only if the
 * system has never been used before, and therefor there is no
 * row in the system (or if "reset" is called - typically only
 * needed if the schema changes so drastically that you need to
 * nuke the Dynamo row and start over)
 *
 * Note that this always serves a COPY of the object, not the
 * cache itself.  Deep copying is done via ExtendedQuietObjectMapper's
 * cloning capability.
 *
 * Created by cweiss1271 on 2/29/16.
 */
public class DynamoCachedObject<T> {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoCachedObject.class);
    private static final ExtendedQuietObjectMapper OBJECT_MAPPER = new ExtendedQuietObjectMapper();
    private final Class<T> clazz;
    private final TypeReference<T> typeReference;
    private Table table;
    private String applicationId;
    private String objectId;
    private T cache;

    public DynamoCachedObject(Table table, String applicationId, String objectId, Class<T> clazz) {
        super();
        Objects.requireNonNull(table);
        Objects.requireNonNull(applicationId);
        Objects.requireNonNull(objectId);
        Objects.requireNonNull(clazz);

        this.table = table;
        this.applicationId = applicationId;
        this.objectId = objectId;
        this.clazz = clazz;
        this.typeReference = null;

        loadCacheFromTable();
    }

    public DynamoCachedObject(Table table, String applicationId, String objectId, TypeReference<T> typeReference) {
        super();
        Objects.requireNonNull(table);
        Objects.requireNonNull(applicationId);
        Objects.requireNonNull(objectId);
        Objects.requireNonNull(typeReference);

        this.table = table;
        this.applicationId = applicationId;
        this.objectId = objectId;
        this.typeReference = typeReference;
        this.clazz = null;

        loadCacheFromTable();
    }

    private T deserialize(String json)
    {
        return (T)((clazz==null)?OBJECT_MAPPER.readValue(json, typeReference):OBJECT_MAPPER.readValue(json,clazz));
    }

    /**
     * Called by the system on construction, but can also be called by other objects who want
     * to force a cache re-read
     */
    public final void loadCacheFromTable()
    {
        LOG.debug("Force-reading cache from table");
        Item item = table.getItem("applicationId",applicationId,"objectId",objectId);
        if (item==null)
        {
            LOG.warn("Couldnt find entry in table for {}/{} - get requests will be null");
        }
        else
        {
            cache = deserialize(item.getJSON("document"));
        }
    }

    public final void saveCacheToTable()
    {
        if (cache==null)
        {
            LOG.warn("Removing value from table");
            table.deleteItem("applicationId",applicationId, "objectId",objectId);
            LOG.info("Deleted object for {}/{}",applicationId,objectId);
        }
        else
        {
            String json = OBJECT_MAPPER.writeValueAsString(cache);
            Item item = new Item().withPrimaryKey("applicationId",applicationId, "objectId",objectId)
                    .withJSON("document",json);
            table.putItem(item);
            LOG.info("Updated object for {}/{}",applicationId,objectId);
        }
    }

    public final void value(T value)
    {
        cache = value;
        saveCacheToTable();
    }

    public final T value()
    {
        return OBJECT_MAPPER.safeDuplicate(cache, clazz);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getObjectId() {
        return objectId;
    }

}
