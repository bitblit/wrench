package com.erigir.wrench.aws.dynamo;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.erigir.wrench.aws.wrench.AbstractAWSCachedObject;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * An implementation of cached object that uses Dynamo as its
 * backing object.
 *
 * Note that this is constrained by dynamo so it should
 * only be used for objects small enough to fit inside
 * dynamo's scope
 *
 * Created by cweiss1271 on 2/29/16.
 */
public class DynamoCachedObject<T> extends AbstractAWSCachedObject<T>{
    private static final Logger LOG = LoggerFactory.getLogger(DynamoCachedObject.class);
    private Table table;
    private String applicationId;
    private String objectId;

    public DynamoCachedObject(Table table, String applicationId, String objectId, Class<T> clazz) {
        super(clazz);
        Objects.requireNonNull(table);
        Objects.requireNonNull(applicationId);
        Objects.requireNonNull(objectId);

        this.table = table;
        this.applicationId = applicationId;
        this.objectId = objectId;

        forceCacheReload();
    }

    public DynamoCachedObject(Table table, String applicationId, String objectId, TypeReference<T> typeReference) {
        super(typeReference);
        Objects.requireNonNull(table);
        Objects.requireNonNull(applicationId);
        Objects.requireNonNull(objectId);

        this.table = table;
        this.applicationId = applicationId;
        this.objectId = objectId;

        forceCacheReload();
    }

    @Override
    protected T loadObjectFromStore() {
        T rval = null;
        LOG.debug("Force-reading cache from table");
        Item item = table.getItem("applicationId",applicationId,"objectId",objectId);
        if (item==null)
        {
            LOG.warn("Couldnt find entry in table for {}/{} - get requests will be null");
        }
        else
        {
            rval = deserialize(item.getJSON("document"));
        }
        return rval;
    }

    @Override
    protected final void saveCacheToStore(String jsonValue) {
        if (jsonValue==null)
        {
            LOG.warn("Removing value from table");
            table.deleteItem("applicationId",applicationId, "objectId",objectId);
            LOG.info("Deleted object for {}/{}",applicationId,objectId);
        }
        else
        {
            Item item = new Item().withPrimaryKey("applicationId",applicationId, "objectId",objectId)
                    .withJSON("document",jsonValue);
            table.putItem(item);
            LOG.info("Updated object for {}/{}",applicationId,objectId);
        }
    }

    public Table getTable() {
        return table;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getObjectId() {
        return objectId;
    }

}
