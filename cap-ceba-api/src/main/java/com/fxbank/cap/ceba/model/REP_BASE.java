package com.fxbank.cap.ceba.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import com.fxbank.cip.base.log.MyLog;
import com.fxbank.cip.base.model.FIXP_SERIAL;
import com.fxbank.cip.base.model.ModelBase;

/** 
* @ClassName: REP_BASE 
* @Description: 应答报文基类 
* @作者 杜振铎
* @date 2019年5月7日 下午5:00:34 
*  
*/
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class REP_BASE extends ModelBase implements Serializable, FIXP_SERIAL {

	private static final long serialVersionUID = -6652288226005628489L;

	private BASE_HEAD head = new BASE_HEAD();

	public REP_BASE(MyLog mylog, Integer sysDate, Integer sysTime, Integer sysTraceno) {
		super(mylog, sysDate, sysTime, sysTraceno);
	}

	public BASE_HEAD getHead() {
		return head;
	}

	public void setHead(BASE_HEAD head) {
		this.head = head;
	}

}