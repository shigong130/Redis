package com.neu.manager;

import com.google.gson.Gson;
import com.neu.dao.ResortDao;
import com.neu.dao.ResortSeasonDao;
import com.neu.pojo.Resort;
import com.neu.pojo.ResortSeason;

import java.util.List;

public class ResortManager {
    ResortDao resortDao;
    ResortSeasonDao resortSeasonDao;

    public ResortManager() {
        resortDao = new ResortDao();
        resortSeasonDao = new ResortSeasonDao();
    }

    public String getAllResort(){
        List<Resort> resortList = resortDao.getAllResorts();

        Gson gson = new Gson();
        String jsonList = gson.toJson(resortList);
        String template = "{\"resorts\": %s }";
        String res = String.format(template, jsonList);
        return res;
    }

    public String getSeasonsByResortId(int resortId){
        List<Integer> seasonList = resortSeasonDao.getSeasonsByResortId(resortId);

        Gson gson = new Gson();
        String jsonList = gson.toJson(seasonList);
        String template = "{\"seasons\": %s }";
        String res = String.format(template, jsonList);
        return res;
    }

    public boolean addResortSeason(int resortId, int season) {
        ResortSeason rs = new ResortSeason();
        rs.setResort(resortId);
        rs.setSeason(season);
        boolean result = resortSeasonDao.insertResortSeason(rs);
        return result;
    }

    public static void main(String[] args) {
        ResortManager rm = new ResortManager();

        System.out.println(rm.getAllResort());

        System.out.println(rm.getSeasonsByResortId(1));
        System.out.println(rm.getSeasonsByResortId(2));

        //System.out.println(rm.addResortSeason(1, 2020));
        //System.out.println(rm.getSeasonsByResortId(1));

        System.out.println(rm.addResortSeason(5, 2020));
    }


}
