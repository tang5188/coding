package com.sample.demo.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sample.demo.R;
import com.sample.demo.common.GetResourcesUtils;
import com.sample.demo.models.RoomInfos;

import java.util.ArrayList;
import java.util.List;

public class DefaultDrawables {

    //连续空闲时间会进入屏保页面（单位：秒）
    public static int SleepSeconds = 300;
    //房间设定
    private static String RoomSetting = "{'infos':[{'id':'room1','name':'厨房','icon':'room1','images':['eel_mask1','eel_mask2','eel_mask3','eel_mask4','eel_mask5','eel_mask6','eel_mask7']},{'id':'room2','name':'客厅','icon':'room2','images':['crab_mask1','crab_mask2','crab_mask3','crab_mask4','crab_mask5','crab_mask6','crab_mask7','crab_mask8']},{'id':'room3','name':'衣帽间','icon':'room3','images':[]}]}";

    //结构化后的房间信息
    private static RoomInfos roomInfos;

    //获取房间设定
    public static RoomInfos GetRoomSetting() {
        if (roomInfos == null) {
            Gson gson = new Gson();
            roomInfos = gson.fromJson(RoomSetting, RoomInfos.class);
        }
        return roomInfos;
    }

    //获取房间信息
    public static RoomInfos.RoomInfo GetRoomInfo(String roomId) {
        if (TextUtils.isEmpty(roomId) ||
                roomInfos == null ||
                roomInfos.infos == null ||
                roomInfos.infos.size() == 0) return null;

        for (int i = 0; i < roomInfos.infos.size(); i++) {
            if (!roomId.equals(roomInfos.infos.get(i).id)) continue;

            RoomInfos.RoomInfo roomInfo = roomInfos.infos.get(i);
            return roomInfo;
        }
        return null;
    }

    //获取指定房间的图层设定
    public static List<Integer> GetDrawableIds(Context context, String roomId) {
        RoomInfos.RoomInfo roomInfo = GetRoomInfo(roomId);
        if (roomInfo == null ||
                roomInfo.images == null) return null;

        List<String> images = roomInfo.images;
        ArrayList<Integer> rets = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            int drawableId = GetResourcesUtils.getDrawableId(context, images.get(i));
            if (drawableId <= 0) continue;
            rets.add(drawableId);
        }
        return rets;
    }
}
