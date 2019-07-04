package com.fxbank.cap.ceba.trade.esb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fxbank.cap.ceba.dto.ceba.DTO_BASE;
import com.fxbank.cap.ceba.dto.ceba.REP_BJCEBQBIRes;
import com.fxbank.cap.ceba.dto.ceba.REP_ERROR;
import com.fxbank.cap.ceba.dto.ceba.REP_BJCEBQBIRes.Tout.Data;
import com.fxbank.cap.ceba.dto.ceba.REP_BJCEBRWKRes;
import com.fxbank.cap.ceba.dto.esb.REP_30063001404;
import com.fxbank.cap.ceba.dto.esb.REP_30063001404.DataInfo;
import com.fxbank.cap.ceba.dto.esb.REQ_30063001404;
import com.fxbank.cap.ceba.model.CityInfo;
import com.fxbank.cap.ceba.model.ErrorInfo;
import com.fxbank.cap.ceba.model.REQ_BJCEBQBIReq;
import com.fxbank.cap.ceba.model.REQ_BJCEBRWKReq;
import com.fxbank.cap.ceba.service.IForwardToCebaService;
import com.fxbank.cap.ceba.sync.SyncCom;
import com.fxbank.cip.base.common.LogPool;
import com.fxbank.cip.base.common.MyJedis;
import com.fxbank.cip.base.dto.DataTransObject;
import com.fxbank.cip.base.exception.SysTradeExecuteException;
import com.fxbank.cip.base.log.MyLog;
import com.fxbank.cip.base.route.trade.TradeExecutionStrategy;
import com.fxbank.cip.base.util.JsonUtil;
import com.fxbank.cip.pub.service.IPublicService;

import redis.clients.jedis.Jedis;



/** 
* @ClassName: QR_CityInfo 
* @Description:缴费城市查询 
* @作者 杜振铎
* @date 2019年5月16日 下午2:37:08 
*  
*/
@Service("REQ_30063001404")
public class QR_CityInfo extends TradeBase implements TradeExecutionStrategy {
	private static Logger logger = LoggerFactory.getLogger(QR_CityInfo.class);

	@Resource
	private LogPool logPool;
	
	@Resource
	private MyJedis myJedis;
	
	private final static String COMMON_PREFIX = "ceba.";
	
	@Reference(version = "1.0.0")
	private IForwardToCebaService forwardToCebaService;
	
	@Resource
	private SyncCom syncCom;
	
	@Reference(version = "1.0.0")
	private IPublicService publicService;

	@Override
	public DataTransObject execute(DataTransObject dto) throws SysTradeExecuteException {
		MyLog myLog = logPool.get();
		REQ_30063001404 reqDto = (REQ_30063001404) dto;
		REP_30063001404 rep = new REP_30063001404();	
		REP_30063001404.REP_BODY repBody = rep.getRepBody();
		
		REQ_BJCEBRWKReq reqW = new REQ_BJCEBRWKReq(myLog, reqDto.getSysDate(), reqDto.getSysTime(),
				reqDto.getSysTraceno()); 
		String instld = null;
		try (Jedis jedis = myJedis.connect()) {
			instld = jedis.get(COMMON_PREFIX + "ceba_instld");
		}
		reqW.getHead().setInstId(instld);
		reqW.getHead().setAnsTranCode("BJCEBRWKReq");
		reqW.getHead().setTrmSeqNum(publicService.getSysDate("CIP").toString() + publicService.getSysTraceno());
		reqW.getTin().setPartnerCode("746");
		reqW.getTin().setOperationDate(reqDto.getSysDate().toString());
		reqW = (REQ_BJCEBRWKReq) forwardToCebaService.sendToCeba(reqW);
		String channel = reqW.getHead().getTrmSeqNum();
		myLog.info(logger, "查询工作密钥报文发送通道编号=[" + channel);
		DTO_BASE dtoBase = syncCom.get(myLog, channel, 55, TimeUnit.SECONDS);
		if (dtoBase.getHead().getAnsTranCode().equals("Error")) {
			REP_ERROR repError = (REP_ERROR) dtoBase;
			String errorCode = repError.getTout().getErrorCode();
			String jsonStr = null;
			try (Jedis jedis = myJedis.connect()) {
				jsonStr = jedis.get(COMMON_PREFIX + "ceba_error_list");
			}
			if (jsonStr == null || jsonStr.length() == 0) {
				logger.error("渠道未配置[" + COMMON_PREFIX + "ceba_error_list" + "]");
				throw new RuntimeException("渠道未配置[" + COMMON_PREFIX + "ceba_error_list" + "]");
			}
			Map<String, ErrorInfo> map = (Map) JSONObject.parse(jsonStr);
			ErrorInfo errorInfo = JsonUtil.toBean(JSON.toJSONString(map.get(errorCode)), ErrorInfo.class);
			String errorMsg = null;
			errorMsg = errorInfo.getQrErrorMsg();
			SysTradeExecuteException e = new SysTradeExecuteException(errorCode, errorMsg);
			myLog.error(logger, e.getRspCode() + " | " + e.getRspMsg());
			throw e;
		} else if (dtoBase.getHead().getAnsTranCode().equals("BJCEBRWKRes")) {
			REP_BJCEBRWKRes res = (REP_BJCEBRWKRes) dtoBase;
			myLog.info(logger,"工作密钥"+res.getTout().getKeyName()+","+res.getTout().getKeyValue()+","+
			res.getTout().getVerifyValue()+","+res.getTout().getKeyName1()+","+res.getTout().getKeyValue1()+","+
			res.getTout().getVerifyValue1());
		}
		
		String jsonStr = null;
		try(Jedis jedis = myJedis.connect()){
			jsonStr = jedis.get(COMMON_PREFIX+"ceba_city_list");
        }
		if(jsonStr==null||jsonStr.length()==0){
			logger.error("渠道未配置["+COMMON_PREFIX + "ceba_city_list"+"]");
			throw new RuntimeException("渠道未配置["+COMMON_PREFIX + "ceba_city_list"+"]");
		}
		Map<String,CityInfo> map = (Map)JSONObject.parse(jsonStr);
		repBody.setCityNum(String.valueOf(map.size()));
		List<DataInfo> list = new ArrayList<DataInfo>();
		for (String key : map.keySet()) { 
			CityInfo cityInfo = JsonUtil.toBean(JSON.toJSONString(map.get(key)),CityInfo.class);
			DataInfo data = new DataInfo();
			data.setCityName(cityInfo.getCityName());
			data.setPyCityCode(key);
			data.setStateName(cityInfo.getParentName());
			list.add(data);
			} 
		repBody.setCityArray(list);
		myLog.info(logger, "查询缴费城市成功，渠道日期"+reqDto.getSysDate()+"渠道流水号"+reqDto.getSysTraceno());
		return rep;
	}
}
