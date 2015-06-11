package com.erigir.wrench;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

/**
 * A simple extension of the jackson object mapper that converts all checked exceptions to runtime
 * Created by chrweiss on 6/6/15.
 */
public class QuietObjectMapper extends ObjectMapper {

    @Override
    public <T> T readValue(JsonParser jsonParser, Class<T> aClass) {
        try {
            return super.readValue(jsonParser, aClass);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(JsonParser jsonParser, TypeReference<?> typeReference) {
        try {
            return super.readValue(jsonParser, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(JsonParser jsonParser, JavaType javaType) {
        try {
            return super.readValue(jsonParser, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T extends TreeNode> T readTree(JsonParser jsonParser) {
        try {
            return super.readTree(jsonParser);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> MappingIterator<T> readValues(JsonParser jsonParser, ResolvedType resolvedType) {
        try {
            return super.readValues(jsonParser, resolvedType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> MappingIterator<T> readValues(JsonParser jsonParser, JavaType javaType) {
        try {
            return super.readValues(jsonParser, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> MappingIterator<T> readValues(JsonParser jsonParser, Class<T> aClass) {
        try {
            return super.readValues(jsonParser, aClass);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> MappingIterator<T> readValues(JsonParser jsonParser, TypeReference<?> typeReference) {
        try {
            return super.readValues(jsonParser, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public JsonNode readTree(InputStream inputStream) {
        try {
            return super.readTree(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public JsonNode readTree(Reader reader) {
        try {
            return super.readTree(reader);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public JsonNode readTree(String s) {
        try {
            return super.readTree(s);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public JsonNode readTree(byte[] bytes) {
        try {
            return super.readTree(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public JsonNode readTree(File file) {
        try {
            return super.readTree(file);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public JsonNode readTree(URL url) {
        try {
            return super.readTree(url);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public void writeValue(JsonGenerator jsonGenerator, Object o) {
        try {
            super.writeValue(jsonGenerator, o);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public void writeTree(JsonGenerator jsonGenerator, TreeNode treeNode) {
        try {
            super.writeTree(jsonGenerator, treeNode);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public void writeTree(JsonGenerator jsonGenerator, JsonNode jsonNode) {
        try {
            super.writeTree(jsonGenerator, jsonNode);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T treeToValue(TreeNode treeNode, Class<T> aClass) {
        try {
            return super.treeToValue(treeNode, aClass);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }


    @Override
    public <T> T readValue(File file, Class<T> aClass) {
        try {
            return super.readValue(file, aClass);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(File file, TypeReference typeReference) {
        try {
            return super.readValue(file, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(File file, JavaType javaType) {
        try {
            return super.readValue(file, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(URL url, Class<T> aClass) {
        try {
            return super.readValue(url, aClass);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(URL url, TypeReference typeReference) {
        try {
            return super.readValue(url, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(URL url, JavaType javaType) {
        try {
            return super.readValue(url, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(String s, Class<T> aClass) {
        try {
            return super.readValue(s, aClass);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(String s, TypeReference typeReference) {
        try {
            return super.readValue(s, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(String s, JavaType javaType) {
        try {
            return super.readValue(s, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(Reader reader, Class<T> aClass) {
        try {
            return super.readValue(reader, aClass);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(Reader reader, TypeReference typeReference) {
        try {
            return super.readValue(reader, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(Reader reader, JavaType javaType) {
        try {
            return super.readValue(reader, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(InputStream inputStream, Class<T> aClass) {
        try {
            return super.readValue(inputStream, aClass);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(InputStream inputStream, TypeReference typeReference) {
        try {
            return super.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(InputStream inputStream, JavaType javaType) {
        try {
            return super.readValue(inputStream, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(byte[] bytes, Class<T> aClass) {
        try {
            return super.readValue(bytes, aClass);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(byte[] bytes, int i, int i1, Class<T> aClass) {
        try {
            return super.readValue(bytes, i, i1, aClass);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(byte[] bytes, TypeReference typeReference) {
        try {
            return super.readValue(bytes, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(byte[] bytes, int i, int i1, TypeReference typeReference) {
        try {
            return super.readValue(bytes, i, i1, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(byte[] bytes, JavaType javaType) {
        try {
            return super.readValue(bytes, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public <T> T readValue(byte[] bytes, int i, int i1, JavaType javaType) {
        try {
            return super.readValue(bytes, i, i1, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public void writeValue(File file, Object o) {
        try {
            super.writeValue(file, o);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public void writeValue(OutputStream outputStream, Object o) {
        try {
            super.writeValue(outputStream, o);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public void writeValue(Writer writer, Object o) {
        try {
            super.writeValue(writer, o);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public String writeValueAsString(Object o) {
        try {
            return super.writeValueAsString(o);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public byte[] writeValueAsBytes(Object o) {
        try {
            return super.writeValueAsBytes(o);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public JsonSchema generateJsonSchema(Class<?> aClass) {
        try {
            return super.generateJsonSchema(aClass);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public void acceptJsonFormatVisitor(Class<?> aClass, JsonFormatVisitorWrapper jsonFormatVisitorWrapper) {
        try {
            super.acceptJsonFormatVisitor(aClass, jsonFormatVisitorWrapper);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    public void acceptJsonFormatVisitor(JavaType javaType, JsonFormatVisitorWrapper jsonFormatVisitorWrapper) {
        try {
            super.acceptJsonFormatVisitor(javaType, jsonFormatVisitorWrapper);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }


    @Override
    protected Object _readValue(DeserializationConfig deserializationConfig, JsonParser jsonParser, JavaType javaType) {
        try {
            return super._readValue(deserializationConfig, jsonParser, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    protected Object _readMapAndClose(JsonParser jsonParser, JavaType javaType) {
        try {
            return super._readMapAndClose(jsonParser, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    protected JsonToken _initForReading(JsonParser jsonParser) {
        try {
            return super._initForReading(jsonParser);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }

    @Override
    protected JsonDeserializer<Object> _findRootDeserializer(DeserializationContext deserializationContext, JavaType javaType) {
        try {
            return super._findRootDeserializer(deserializationContext, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Quieted Jackson Exception", e);
        }
    }


}
