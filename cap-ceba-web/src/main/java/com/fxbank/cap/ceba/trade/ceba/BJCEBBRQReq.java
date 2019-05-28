package com.fxbank.cap.ceba.trade.ceba;

import java.math.BigDecimal;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.dubbo.config.annotation.Reference;
import com.fxbank.cap.ceba.dto.ceba.REP_BJCEBBRQRes;
import com.fxbank.cap.ceba.dto.ceba.REP_ERROR;
import com.fxbank.cap.ceba.dto.ceba.REQ_BJCEBBRQReq;
import com.fxbank.cap.esb.service.IForwardToESBService;
import com.fxbank.cip.base.common.LogPool;
import com.fxbank.cip.base.common.MyJedis;
import com.fxbank.cip.base.dto.DataTransObject;
import com.fxbank.cip.base.exception.SysTradeExecuteException;
import com.fxbank.cip.base.log.MyLog;
import com.fxbank.cip.base.route.trade.TradeExecutionStrategy;



/** 
* @ClassName: BJCEBBRQReq 
* @Description: 模拟光大银行销账结果查询
* @作者 杜振铎
* @date 2019年5月10日 下午4:05:48 
*  
*/
@Service("BJCEBBRQReq")
public class BJCEBBRQReq implements TradeExecutionStrategy {

	private static Logger logger = LoggerFactory.getLogger(BJCEBBRQReq.class);

	@Reference(version = "1.0.0")
	private IForwardToESBService forwardToESBService;

	@Resource
	private LogPool logPool;
	
	@Resource
	private MyJedis myJedis;
	
	private final static String COMMON_PREFIX = "ceba.";
	
	private static final String ERR_BILL_NO = "12345";

	@Override
	public DataTransObject execute(DataTransObject dto) throws SysTradeExecuteException {
		MyLog myLog = logPool.get();
		REQ_BJCEBBRQReq req = (REQ_BJCEBBRQReq) dto;
		
		if(ERR_BILL_NO.equals(req.getTin().getBillNo())) {
			REP_ERROR repError = new REP_ERROR();
			repError.getHead().setInstId(req.getHead().getInstId());
			repError.getHead().setAnsTranCode("Error");
			repError.getHead().setTrmSeqNum(req.getHead().getTrmSeqNum());
			repError.getTout().setErrorCode("DEF0002");
			return repError;
		}
		REP_BJCEBBRQRes rep = new REP_BJCEBBRQRes();
		rep.getHead().setInstId(req.getHead().getInstId());
		rep.getHead().setAnsTranCode("BJCEBBRQRes");
		rep.getHead().setTrmSeqNum(req.getHead().getTrmSeqNum());
		rep.getTout().setBillKey("123456");
		rep.getTout().setBillNo(req.getTin().getBillNo());
		rep.getTout().setPayDate(req.getTin().getPayDate());
		rep.getTout().setBankBillNo("123237202");
		rep.getTout().setPayAmount(new BigDecimal(5555));
		rep.getTout().setPayState("2");
		
		return rep;
	}
	
	
}