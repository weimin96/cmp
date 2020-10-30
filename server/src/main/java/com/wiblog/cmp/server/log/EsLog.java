package com.wiblog.cmp.server.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

/**
 * @author panweimin
 */
@Document(indexName = "cmp_log",type = "cmp_log")
public class EsLog implements Serializable {
    private static final long serialVersionUID = -7577471814974481136L;

    @Id
    private String id;

    private String msg;

    private String level;

    private String clientId;

    private String appName;

    @Field(type = FieldType.Long)
    private Long line;

    @Field(type = FieldType.Date, format = DateFormat.custom,pattern ="yyyy-MM-dd HH:mm:ss.SSS")
    @JsonFormat(shape =JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss.SSS",timezone ="GMT+8")
    private Date timestamp;

    public EsLog() {
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getLine() {
        return line;
    }

    public void setLine(Long line) {
        this.line = line;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public EsLog id(String id) {
        this.id = id;
        return this;
    }

    public EsLog msg(String msg) {
        this.msg = msg;
        return this;
    }

    public EsLog level(String level) {
        this.level = level;
        return this;
    }

    public EsLog clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public EsLog appName(String appName) {
        this.appName = appName;
        return this;
    }

    public EsLog line(Long line) {
        this.line = line;
        return this;
    }

    public EsLog timestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return "EsLog{" +
                "id='" + id + '\'' +
                ", msg='" + msg + '\'' +
                ", level='" + level + '\'' +
                ", clientId='" + clientId + '\'' +
                ", appName='" + appName + '\'' +
                ", line=" + line +
                ", timestamp=" + timestamp +
                '}';
    }
}
