package com.fxbank.cap.ykwm.netty.ykwm;

import com.fxbank.cip.base.log.MyLog;
import com.fxbank.cip.base.netty.NettySyncHandler;
import com.fxbank.cip.base.netty.NettySyncSlot;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class YkwmInitializer<T> extends ChannelInitializer<SocketChannel> {

    private MyLog myLog;
    private NettySyncSlot<T> slot;
    private Object reqData;
    private Class<T> clazz;

    public YkwmInitializer(MyLog myLog, Object reqData, Class<T> clazz) {
        this.myLog = myLog;
        this.reqData = reqData;
        this.clazz = clazz;
    }
    
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new YkwmLenghtDecoder<T>(this.myLog,this.slot));
        p.addLast(new YkwmLengthEncoder(this.myLog));
        p.addLast(new YkwmPackConvInHandler<T>(this.myLog,this.slot,clazz));
        p.addLast(new YkwmPackConvOutHandler(this.myLog));
        p.addLast(new NettySyncHandler<T>(this.myLog,this.reqData));
    }

}