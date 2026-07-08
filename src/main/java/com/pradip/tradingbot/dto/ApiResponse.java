package com.pradip.tradingbot.dto;

public class ApiResponse<T> {

    private String status;
    private T data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}
}