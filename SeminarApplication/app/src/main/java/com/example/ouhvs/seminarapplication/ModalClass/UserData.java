package com.example.ouhvs.seminarapplication.ModalClass;

public class UserData {

    String name,password,mobileno,fcmId;

    public void UserData(){}

    public UserData(String username, String password, String mobileno, String regId) {
        this.name=username;
        this.password=password;
        this.mobileno=mobileno;
        this.fcmId=regId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setMobileno(String mobileno) {
        this.mobileno = mobileno;
    }

    public String getMobileno() {
        return mobileno;
    }

    public void setFcmId(String fcmId) {
        this.fcmId = fcmId;
    }

    public String getFcmId() {
        return fcmId;
    }
}
