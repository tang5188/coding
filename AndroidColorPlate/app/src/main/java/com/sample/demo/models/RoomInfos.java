package com.sample.demo.models;


import java.util.List;

public class RoomInfos {
    public List<RoomInfo> infos;

    //房间信息
    public class RoomInfo {
        public String id;
        public String icon;
        public String name;
        public List<String> images;
    }
}
