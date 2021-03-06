package com.fxbank.cap.ceba.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fxbank.cap.ceba.util.CebaXmlUtil;
import com.fxbank.cip.base.log.MyLog;

/** 
* @ClassName: REP_BJCEBQCRRes 
* @Description: 缴费单销账应答
* @作者 杜振铎
* @date 2019年5月10日 下午2:06:27 
*  
*/
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "out")
public class REP_BJCEBBCRes extends MODEL_BASE {

	private static final long serialVersionUID = -5970071351047001526L;

	private Tout tout = new Tout();

	public Tout getTout() {
		return tout;
	}

	public void setTout(Tout tout) {
		this.tout = tout;
	}

	public REP_BJCEBBCRes() {
		super(null, 0, 0, 0);
	}

	public REP_BJCEBBCRes(MyLog mylog, Integer sysDate, Integer sysTime, Integer sysTraceno) {
		super(mylog, sysDate, sysTime, sysTraceno);
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(propOrder = { "billKey", "companyId", "billNo", "payDate", "payAmount", "bankBillNo",
			"receiptNo", "acctDate" })
	public static class Tout implements Serializable {
		private static final long serialVersionUID = -581103924009687799L;
		private String billKey = null;
		private String companyId = null;
		private String billNo = null;
		private String payDate = null;
		private BigDecimal payAmount = null;
		private String bankBillNo = null;
		private String receiptNo = "";
		private String acctDate = null;
		public String getBillKey() {
			return billKey;
		}
		public void setBillKey(String billKey) {
			this.billKey = billKey;
		}
		public String getCompanyId() {
			return companyId;
		}
		public void setCompanyId(String companyId) {
			this.companyId = companyId;
		}
		public String getBillNo() {
			return billNo;
		}
		public void setBillNo(String billNo) {
			this.billNo = billNo;
		}
		public String getPayDate() {
			return payDate;
		}
		public void setPayDate(String payDate) {
			this.payDate = payDate;
		}
		public BigDecimal getPayAmount() {
			return payAmount;
		}
		public void setPayAmount(BigDecimal payAmount) {
			this.payAmount = payAmount;
		}
		public String getBankBillNo() {
			return bankBillNo;
		}
		public void setBankBillNo(String bankBillNo) {
			this.bankBillNo = bankBillNo;
		}
		public String getReceiptNo() {
			return receiptNo;
		}
		public void setReceiptNo(String receiptNo) {
			this.receiptNo = receiptNo;
		}
		public String getAcctDate() {
			return acctDate;
		}
		public void setAcctDate(String acctDate) {
			this.acctDate = acctDate;
		}

	}

	@Override
	public void chanFixPack(String pack) {
		REP_BJCEBBCRes res = (REP_BJCEBBCRes) CebaXmlUtil.xmlToObject(this.getClass(), pack);
		this.setHead(res.getHead());
		if (res.getTout().getPayAmount() != null) {
			res.getTout()
					.setPayAmount(res.getTout().getPayAmount().movePointLeft(2).setScale(2, BigDecimal.ROUND_HALF_UP));
		}
		this.setTout(res.getTout());
	}

	@Override
	public String creaFixPack() {
		if (this.getTout().getPayAmount() != null) {
			this.getTout().setPayAmount(
					this.getTout().getPayAmount().movePointRight(2).setScale(2, BigDecimal.ROUND_HALF_UP));
		}
		return CebaXmlUtil.objectToXml(this);
	}

}
