package com.erigir.wrench.ape.model;

/**
 * Wraps up a response from the API to add some general data
 * Created by chrweiss on 6/28/14.
 */
public class ApeResponse<T> {
    private T data;
    private int code;
    private String notes;

    public ApeResponse() {
    }

    public ApeResponse(T data, int code) {
        this.data = data;
        this.code = code;
    }

    public ApeResponse(T data, int code, String notes) {
        this.data = data;
        this.code = code;
        this.notes = notes;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
