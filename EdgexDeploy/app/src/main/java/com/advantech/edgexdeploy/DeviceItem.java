package com.advantech.edgexdeploy;


public class DeviceItem {
	private String macAddr;
	private String ipAddr;
	private String status;
	private String result;

	//无参构造方法
	DeviceItem(String mac, String ip, String status, String result) {
		this.macAddr = mac;
		this.ipAddr = ip;
		this.status = status;
		this.result = result;


	}

	public String getMacAddr() {
		return macAddr;
	}
	public String getIpAddr(){
		return ipAddr;
	}
	public String getStatus () {
		return status;
	}
	public String getResult(){
		return result;
	}

	public void setMacAddr(String mac) {
		this.macAddr = mac;
	}
	public void setIpAddr(String ip) {
		this.ipAddr = ip;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public void setResult(String result) {
		this.result = result;
	}
}
