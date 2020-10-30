package com.wiblog.cmp.server.log;

import java.io.Serializable;

/**
 * @author panweimin
 * @create 2020-10-29 15:09
 */
public class LogMessage implements Serializable {

    private static final long serialVersionUID = -9154095008255676966L;

    private String str;

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public LogMessage str(String str) {
        this.str = str;
        return this;
    }

    public static Builder builder(){
        return new Builder();
    }


    public static final class Builder {
        private String str;

        private Builder() {
        }

        public static Builder aLogMessage() {
            return new Builder();
        }

        public Builder str(String str) {
            this.str = str;
            return this;
        }

        public LogMessage build() {
            LogMessage logMessage = new LogMessage();
            logMessage.str = this.str;
            return logMessage;
        }
    }

    @Override
    public String toString() {
        return "LogMessage{" +
                "str='" + str + '\'' +
                '}';
    }
}
