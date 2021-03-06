package com.fxbank.cap.ykwm.netty.ykwm;

import com.fxbank.cap.ykwm.model.REP_BASE;
import com.fxbank.cap.ykwm.model.REP_ERROR;
import com.fxbank.cip.base.log.MyLog;
import com.fxbank.cip.base.netty.NettySyncSlot;
import com.fxbank.cip.base.pkg.fixed.FixedUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/** 
* @ClassName: YkwmPackConvInHandler 
* @Description: 客户端应答拆包
* @作者 杜振铎
* @date 2019年4月29日 下午2:21:20 
* 
* @param <T> 
*/
public class YkwmPackConvInHandler<T> extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(YkwmPackConvInHandler.class);

	private MyLog myLog;

	private NettySyncSlot<T> slot;

	private Class<T> clazz;
	
	private static final String SUCCESS_CODE = "0";

	public YkwmPackConvInHandler(MyLog myLog, NettySyncSlot<T> slot, Class<T> clazz) {
		this.myLog = myLog;
		this.slot = slot;
		this.clazz = clazz;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			StringBuffer pack = new StringBuffer((String) msg);
			String fixPack = pack.substring(0, pack.length());
			if(!fixPack.startsWith(SUCCESS_CODE)) {
				REP_ERROR repBase = new REP_ERROR(null,0,0,0);
				repBase = new FixedUtil(fixPack, "\\|",YkwmClient.CODING).toBean(repBase.getClass()); 
				ctx.fireChannelRead(repBase);
			}else {
				REP_BASE repBase = (REP_BASE) this.clazz.newInstance();
				repBase = new FixedUtil(fixPack, "\\|",YkwmClient.CODING).toBean(repBase.getClass()); 
				ctx.fireChannelRead(repBase);
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		this.myLog.info(logger, "解析服务端应答异常", cause);
		this.slot.setResponse(null);
	}

}
