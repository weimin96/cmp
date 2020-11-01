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
        return toMap("^\\[%{TIMESTAMP_ISO8601:timestamp}] \\s*%{LOGLEVEL:level} %{NUMBER:pid} \\[\\s*%{GREEDYDATA:thread}] %{JAVACLASS:class}\\s*\\[\\s*%{NUMBER:line}\\]\\s*:%{MSG:msg}",message);
    }

    public static void main(String[] args) {
        String a = "[2020-11-01 16:46:45.750] ERROR 15836 [tbeatExecutor-0] com.wiblog.cmp.client.CmpClient          [ 153]   :心跳异常\n" +
                "org.springframework.web.client.ResourceAccessException: I/O error on POST request for \"http://127.0.0.1:6060/cmp/renew\": Read timed out; nested exception is java.net.SocketTimeoutException: Read timed out\n" +
                "\tat org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:744)\n" +
                "\tat org.springframework.web.client.RestTemplate.execute(RestTemplate.java:670)\n" +
                "\tat org.springframework.web.client.RestTemplate.postForObject(RestTemplate.java:414)\n" +
                "\tat com.wiblog.cmp.client.CmpClient.renew(CmpClient.java:143)\n" +
                "\tat com.wiblog.cmp.client.CmpClient$HeartbeatThread.run(CmpClient.java:104)\n" +
                "\tat java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)\n" +
                "\tat java.util.concurrent.FutureTask.run$$$capture(FutureTask.java:266)\n" +
                "\tat java.util.concurrent.FutureTask.run(FutureTask.java)\n" +
                "\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)\n" +
                "\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)\n" +
                "\tat java.lang.Thread.run(Thread.java:748)\n" +
                "Caused by: java.net.SocketTimeoutException: Read timed out\n" +
                "\tat java.net.SocketInputStream.socketRead0(Native Method)\n" +
                "\tat java.net.SocketInputStream.socketRead(SocketInputStream.java:116)\n" +
                "\tat java.net.SocketInputStream.read(SocketInputStream.java:171)\n" +
                "\tat java.net.SocketInputStream.read(SocketInputStream.java:141)\n" +
                "\tat org.apache.http.impl.io.SessionInputBufferImpl.streamRead(SessionInputBufferImpl.java:137)\n" +
                "\tat org.apache.http.impl.io.SessionInputBufferImpl.fillBuffer(SessionInputBufferImpl.java:153)\n" +
                "\tat org.apache.http.impl.io.SessionInputBufferImpl.readLine(SessionInputBufferImpl.java:280)\n" +
                "\tat org.apache.http.impl.conn.DefaultHttpResponseParser.parseHead(DefaultHttpResponseParser.java:138)\n" +
                "\tat org.apache.http.impl.conn.DefaultHttpResponseParser.parseHead(DefaultHttpResponseParser.java:56)\n" +
                "\tat org.apache.http.impl.io.AbstractMessageParser.parse(AbstractMessageParser.java:259)\n" +
                "\tat org.apache.http.impl.DefaultBHttpClientConnection.receiveResponseHeader(DefaultBHttpClientConnection.java:163)\n" +
                "\tat org.apache.http.impl.conn.CPoolProxy.receiveResponseHeader(CPoolProxy.java:157)\n" +
                "\tat org.apache.http.protocol.HttpRequestExecutor.doReceiveResponse(HttpRequestExecutor.java:273)\n" +
                "\tat org.apache.http.protocol.HttpRequestExecutor.execute(HttpRequestExecutor.java:125)\n" +
                "\tat org.apache.http.impl.execchain.MainClientExec.execute(MainClientExec.java:272)\n" +
                "\tat org.apache.http.impl.execchain.ProtocolExec.execute(ProtocolExec.java:186)\n" +
                "\tat org.apache.http.impl.execchain.RetryExec.execute(RetryExec.java:89)\n" +
                "\tat org.apache.http.impl.execchain.RedirectExec.execute(RedirectExec.java:110)\n" +
                "\tat org.apache.http.impl.client.InternalHttpClient.doExecute(InternalHttpClient.java:185)\n" +
                "\tat org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:83)\n" +
                "\tat org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:56)\n" +
                "\tat org.springframework.http.client.HttpComponentsClientHttpRequest.executeInternal(HttpComponentsClientHttpRequest.java:87)\n" +
                "\tat org.springframework.http.client.AbstractBufferingClientHttpRequest.executeInternal(AbstractBufferingClientHttpRequest.java:48)\n" +
                "\tat org.springframework.http.client.AbstractClientHttpRequest.execute(AbstractClientHttpRequest.java:53)\n" +
                "\tat org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:735)\n" +
                "\t... 10 common frames omitted";
        System.out.println(toMap("^\\[%{TIMESTAMP_ISO8601:timestamp}] \\s*%{LOGLEVEL:level} %{NUMBER:pid} \\[\\s*%{GREEDYDATA:thread}] %{JAVACLASS:class}\\s*\\[\\s*%{NUMBER:line}\\]\\s*:%{MSG:msg}",a));
    }

}
