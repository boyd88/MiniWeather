package com.example.stby.miniweather;

import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.example.stby.app.MyApplication;
import com.example.stby.bean.City;

import java.util.List;

public class MyLocationListener extends BDAbstractLocationListener {
    public String cityCode;
    @Override
    public void onReceiveLocation(BDLocation location){
        //此处的 BDLocation 为定位结果信息类，通过它的各种 get 方法可获取定位相关的全部结果
        //以下只列举部分获取经纬度相关（常用）的结果信息
        //更多结果信息获取说明，请参照类参考中 BDLocation 类中的说明

        Log.d("where","1");
        Log.d("where","2");
        String getCity=location.getCity();//获取城市
        String getDistrict = location.getDistrict();
        String city=getCity.replace("市","");
        Log.d("where",city);
        //打印出当前城市
        List<City> mCitylist;
        MyApplication myApplication;
        myApplication=MyApplication.getInstance();

        mCitylist=myApplication.getCityList();
        for(City city1:mCitylist){
            Log.d("compare",city1+" "+city);
            if(city1.getCity().equals(city))
            {
                cityCode=city1.getNumber();
                Log.d("Locate",cityCode);
            }
        }
        // int errorCode = location.getLocType();
        Log.d("TAGlocation", "location.getLocType()=" + location.getLocType());
        //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation 类中的说明
    }
}
