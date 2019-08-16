package com.jin.env.garbage.dto.user;

public class UserVillageDto {
    private Integer id;
    private String villageName;

    public UserVillageDto() {

    }

    public UserVillageDto(Integer id, String villageName) {
        this.id = id;
        this.villageName = villageName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }
}
