package com.jin.env.garbage.dto.user;

import java.util.List;

public class VillageUserInfo {
    private String username;
    private String phone;
    private List<String> eNos;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> geteNos() {
        return eNos;
    }

    public void seteNos(List<String> eNos) {
        this.eNos = eNos;
    }
}
