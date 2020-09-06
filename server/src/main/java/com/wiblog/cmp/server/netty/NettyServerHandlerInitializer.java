//package com.wiblog.cmp.server.netty;
//
//import com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase;
//import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
//import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
//
///**
// * @author pwm
// * @date 2020/4/26
// */
//public class NettyServerHandlerInitializer extends ChannelInitializer<Channel> {
//
//    @Override
//    protected void initChannel(Channel ch) throws Exception {
//        ch.pipeline()
//                //空闲检测
//                .addLast(new ServerIdleStateHandler())
//                .addLast(new ProtobufVarint32FrameDecoder())
//                .addLast(new ProtobufDecoder(MessageBase.Message.getDefaultInstance()))
//                .addLast(new ProtobufVarint32LengthFieldPrepender())
//                .addLast(new ProtobufEncoder())
//                .addLast(new NettyServerHandler());
//    }
//}