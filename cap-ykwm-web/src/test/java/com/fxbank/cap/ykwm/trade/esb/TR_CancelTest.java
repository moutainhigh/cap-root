package com.fxbank.cap.ykwm.trade.esb;

import static org.junit.Assert.assertEquals;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.annotation.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.alibaba.fastjson.JSON;
import com.fxbank.cap.ykwm.dto.esb.REP_30064000201;
import com.fxbank.cap.ykwm.dto.esb.REQ_30064000201;
import com.fxbank.cip.base.common.LogPool;
import com.fxbank.cip.base.dto.REQ_SYS_HEAD;
import com.fxbank.cip.base.util.JsonUtil;


/** 
* @ClassName: TR_CancelTest 
* @Description: 柜面冲正模拟
* @作者 杜振铎
* @date 2019年4月29日 下午2:14:18 
*  
*/
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc	
public class TR_CancelTest {
	
	private static Logger logger = LoggerFactory.getLogger(TR_CancelTest.class);
	
     private static final String URL="http://127.0.0.1:7006/esb/ykwm.do";

	@Autowired
	private MockMvc mockMvc;
	
	@Resource
	private LogPool logPool;
	
	private REQ_30064000201 req ;
	private REQ_SYS_HEAD reqSysHead;
	private REQ_30064000201.REQ_BODY reqBody ;
	
	@Before
	public void init(){
		req = new REQ_30064000201();
		reqSysHead = new REQ_SYS_HEAD();
		reqSysHead.setServiceId("300640002");
		reqSysHead.setSceneId("01");
		reqSysHead.setSystemId("301907");
		reqSysHead.setTranMode("ONLINE");
		//网联
		reqSysHead.setSourceType("BH");	
		reqSysHead.setBranchId("01002");
		reqSysHead.setUserId("001068");
		reqSysHead.setTranDate(String.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date())));
		reqSysHead.setTranTimestamp(String.valueOf(new SimpleDateFormat("HHmmss").format(new Date())));		
		reqSysHead.setUserLang("CHINESE");
		reqSysHead.setSeqNo(String.valueOf(Math.abs(new Random().nextInt())));
		//网联
		reqSysHead.setSystemId("301907");	
		reqSysHead.setCompany("COMPANY");
		reqSysHead.setSourceBranchNo("SOURCE_BRANCH_NO");
		reqSysHead.setDestBranchNo("DEST_BRANCH_NO");
		reqSysHead.setFilePath("FILE_PATH");
		reqSysHead.setGloabalSeqNo(reqSysHead.getSeqNo());
		reqSysHead.setAuthUserId("999");
		reqBody = req.new REQ_BODY(); 
		req.setReqSysHead(reqSysHead);
		req.setReqBody(reqBody);
	}
	
	@Test
	public void payOk() throws Exception {
		logger.info("冲正测试");
		
		reqBody.setChannelDate("20190530");
		reqBody.setChannelSeqNo("7860");
		reqBody.setRevokeReason("重复缴费");
		
		String reqContent = JSON.toJSONString(req);
		logger.info("冲正测试请求");
		RequestBuilder request = MockMvcRequestBuilders.post(URL)
				.contentType(MediaType.APPLICATION_JSON_UTF8).content(reqContent);
		MvcResult mvcResult = mockMvc.perform(request).andReturn();
		logger.info("冲正测试请求完毕");
		int status = mvcResult.getResponse().getStatus();
		assertEquals(status, 200);
		String repContent = mvcResult.getResponse().getContentAsString();
		REP_30064000201 rep = JsonUtil.toBean(repContent, REP_30064000201.class);
	}

}
