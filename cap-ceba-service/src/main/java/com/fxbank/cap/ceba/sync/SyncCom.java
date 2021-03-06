package com.fxbank.cap.ceba.sync;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import com.fxbank.cap.ceba.exception.CebaTradeExecuteException;
import com.fxbank.cap.ceba.netty.ceba.CebaClient;
import com.fxbank.cip.base.common.MyJedis;
import com.fxbank.cip.base.exception.SysTradeExecuteException;
import com.fxbank.cip.base.log.MyLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

@Component
public class SyncCom {

    private static Logger logger = LoggerFactory.getLogger(SyncCom.class);

    @Resource
	private MyJedis myJedis;

    public byte[] get(MyLog myLog, String channel,long timeout, TimeUnit unit) throws SysTradeExecuteException{
		SyncSlot<byte[]> slot = new SyncSlot<byte[]>();
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try (Jedis jedis = myJedis.connect()) {
					jedis.subscribe(new BinaryJedisPubSub() {
						@Override
						public void onMessage(byte[] channel, byte[] message) {
							String sChannel = new String(channel);
							myLog.info(logger, "通道[" + sChannel + "]收到消息[" + message + "]");
							slot.setResponse(message);
							super.unsubscribe(channel);
						}

						@Override
						public void onSubscribe(byte[] channel, int subscribedChannels) {
							String sChannel = new String(channel);
							myLog.info(logger, "订阅通道成功[" + sChannel + "][" + subscribedChannels + "]");
						}

						@Override
						public void onUnsubscribe(byte[] channel, int subscribedChannels) {
							String sChannel = new String(channel);
							myLog.info(logger, "取消订阅通道成功[" + sChannel + "][" + subscribedChannels + "]");
						}
					}, channel.getBytes());
				}
				myLog.info(logger, "订阅子进程退出");
			}
		});

		//T t = null;
		byte[] repData = null;
		try {
			myLog.info(logger, "实时等待应答["+channel+"]...");
			repData = slot.get(timeout, unit);
			//t = (T) SerializeUtil.unserialize(repData);
			myLog.info(logger, "收到实时应答["+channel+"]["+new String(repData,CebaClient.CODING)+"]...");
		} catch (InterruptedException e) {
			myLog.error(logger, "等待服务端应答被中断", e);
			CebaTradeExecuteException e1 = new CebaTradeExecuteException(CebaTradeExecuteException.CEBA_E_10009);
			throw e1;
		} catch (Exception e) {
			myLog.error(logger, "等待服务端应答超时", e);
			CebaTradeExecuteException e1 = new CebaTradeExecuteException(CebaTradeExecuteException.CEBA_E_10007);
			throw e1;
		}finally {
			try (Jedis jedis = myJedis.connect()) {
				myLog.info(logger, "确保订阅线程退出["+channel+"]");
				jedis.publish(channel.getBytes(), "QUIT".getBytes());
			}
		}
		if (!slot.isDone()) {
			myLog.error(logger, "等待服务端应答超时");
			CebaTradeExecuteException e1 = new CebaTradeExecuteException(CebaTradeExecuteException.CEBA_E_10007);
			throw e1;
		}
		return repData;
	}

}