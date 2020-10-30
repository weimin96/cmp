package com.wiblog.cmp.server.log;

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

public class GrokUtil {

    public static final GrokCompiler grokCompiler = GrokCompiler.newInstance();
    public static Grok grok = null;

    public static Map<String, Object> toMap(String pattern, String message){
        grokCompiler.registerPatternFromClasspath("/patterns.txt");
        grok = grokCompiler.compile(pattern);
        if(grok!=null){
            Match match = grok.match(message);
            return match.capture();
        } else {
            return new HashMap<>();
        }
    }

    public static Map<String, Object> toLogMap(String message){
        return toMap("^\\[%{TIMESTAMP_ISO8601:timestamp}] \\s*%{LOGLEVEL:level} %{NUMBER:pid} \\[\\s*%{GREEDYDATA:thread}] %{JAVACLASS:class}\\s*\\[\\s*%{NUMBER:line}\\]\\s*:%{GREEDYDATA:msg}",message);
    }

}
