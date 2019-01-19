package com.example.stby.miniweather;

import android.os.Handler;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.stby.adapter.GuideAdapter;
import com.example.stby.bean.TodayWeather;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.example.stby.util.NetUtil;


public class MainActivity extends Activity implements View.OnClickListener{
    private static final int UPDATE_TODAY_WEATHER = 1;

    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private ImageView mTitleLocation;

    //定义相关控件
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
                     temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    //六天天⽓气信息展示
    //显示两个展示⻚页
    private ViewPager viewPage;
    private List<View> views;
    private GuideAdapter guideAdapter;
    //为引导⻚页增加⼩小圆点
    private  TextView[] day0=new TextView[4],day1=new TextView[4],
            day2=new TextView[4],day3=new TextView[4],day4=new TextView[4],day5=new TextView[4];
    private  ImageView[] sixDayImage=new ImageView[6];


    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    public LocationClient mLocationClient = null;
    private MyLocationListener mLocationListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);//将weather_info.xml作为当前activity的布局

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);//获取布局中的刷新控件
        mUpdateBtn.setOnClickListener(this);//监听刷新的单击事件
        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);//获取布局中的城市控件
        mCitySelect.setOnClickListener(this);//监听城市管理的单击事件
        mTitleLocation = (ImageView) findViewById(R.id.title_location);//获取布局中的定位控件
        mTitleLocation.setOnClickListener(this);//监听定位的单击事件

        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(mLocationListener);//注册监听函数
        initLocation();

        //检查网络状态
        if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
            Log.d("myWeather","网络OK");
            Toast.makeText(MainActivity.this, "网络OK!", Toast.LENGTH_SHORT).show();
            //消息模式Toast显示网络信息，在MainActivity上下文中
        }else
        {
            Log.d("myWeather","网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了",Toast.LENGTH_LONG).show();
        }

        initView();


        //初始化界⾯面控件
        initView();
    }



    //配置定位SDK参数
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //设置定位模式：低功耗
        option.setCoorType("bd09ll");
        //设置返回经纬度坐标类型：百度经纬度坐标
        option.setScanSpan(0);
        option.setIsNeedAddress(true);
        //设置发起定位请求的间隔，int类型，单位ms
        option.setOpenGps(true);
        //设置是否使用gps
        option.setLocationNotify(true);
        //设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        option.SetIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false
        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        mLocationClient.setLocOption(option);
        mLocationClient.start();
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
    }
    void initView(){
        //初始化
        //获取相应控件，将初始值设为N/A
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality );
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature );
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

        viewPage=(ViewPager) findViewById(R.id.sixday);
        views = new ArrayList<View>();
        views.add(LayoutInflater.from(this).inflate(R.layout.threeday1, null));
        views.add(LayoutInflater.from(this).inflate(R.layout.threeday2, null));
        guideAdapter=new GuideAdapter(this, views);
        viewPage.setAdapter(guideAdapter);

        day0[0]=(TextView) views.get(0).findViewById(R.id.day1);
        day0[1]=(TextView) views.get(0).findViewById(R.id.temperature1);
        day0[2]=(TextView) views.get(0).findViewById(R.id.climate1);
        day0[3]=(TextView) views.get(0).findViewById(R.id.wind1);
        sixDayImage[0]=(ImageView) views.get(0).findViewById(R.id.img1);
        day1[0]=(TextView) views.get(0).findViewById(R.id.day2);
        day1[1]=(TextView) views.get(0).findViewById(R.id.temperature2);
        day1[2]=(TextView) views.get(0).findViewById(R.id.climate2);
        day1[3]=(TextView) views.get(0).findViewById(R.id.wind2);
        sixDayImage[1]=(ImageView) views.get(0).findViewById(R.id.img2);
        day2[0]=(TextView) views.get(0).findViewById(R.id.day3);
        day2[1]=(TextView) views.get(0).findViewById(R.id.temperature3);
        day2[2]=(TextView) views.get(0).findViewById(R.id.climate3);
        day2[3]=(TextView) views.get(0).findViewById(R.id.wind3);
        sixDayImage[2]=(ImageView) views.get(0).findViewById(R.id.img3);
        day3[0]=(TextView) views.get(1).findViewById(R.id.day4);
        day3[1]=(TextView) views.get(1).findViewById(R.id.temperature4);
        day3[2]=(TextView) views.get(1).findViewById(R.id.climate4);
        day3[3]=(TextView) views.get(1).findViewById(R.id.wind4);
        sixDayImage[3]=(ImageView) views.get(1).findViewById(R.id.img4);
        day4[0]=(TextView) views.get(1).findViewById(R.id.day5);
        day4[1]=(TextView) views.get(1).findViewById(R.id.temperature5);
        day4[2]=(TextView) views.get(1).findViewById(R.id.climate5);
        day4[3]=(TextView) views.get(1).findViewById(R.id.wind5);
        sixDayImage[4]=(ImageView) views.get(1).findViewById(R.id.img5);
        day5[0]=(TextView) views.get(1).findViewById(R.id.day6);
        day5[1]=(TextView) views.get(1).findViewById(R.id.temperature6);
        day5[2]=(TextView) views.get(1).findViewById(R.id.climate6);
        day5[3]=(TextView) views.get(1).findViewById(R.id.wind6);
        sixDayImage[5]=(ImageView) views.get(1).findViewById(R.id.img6);
    }

    //更新六天天气
    void updateSixWeather(TextView[] days,int i,TodayWeather todayWeather) {
        days[0].setText(todayWeather.sixWeather[i].getDate());
        days[1].setText(todayWeather.sixWeather[i].getLow()+"~"+todayWeather.sixWeather[i].getHigh());
        days[2].setText(todayWeather.sixWeather[i].getType());
        days[3].setText(todayWeather.sixWeather[i].getFengxiang());

        if(days[2].getText().toString().contains("暴雪")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_baoxue);
        }else if(days[2].getText().toString().contains("暴雨")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_baoyu);
        }else if(days[2].getText().toString().contains("大雨")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_dayu);
        }else if(days[2].getText().toString().contains("大暴雨")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
        }else if(days[2].getText().toString().contains("大雪")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_daxue);
        }else if(days[2].getText().toString().contains("多云")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_duoyun);
        }else if(days[2].getText().toString().contains("雷阵雨")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
        }else if(days[2].getText().toString().contains("雷阵雨冰雹")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
        }else if(days[2].getText().toString().contains("晴")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_qing);
        }else if(days[2].getText().toString().contains("沙尘暴")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_shachenbao);
        }else if(days[2].getText().toString().contains("特大暴雨")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
        }else if(days[2].getText().toString().contains("小雪")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
        }else if(days[2].getText().toString().contains("小雨")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
        }else if(days[2].getText().toString().contains("阴")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_yin);
        }else if(days[2].getText().toString().contains("雾")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_wu);
        }else if(days[2].getText().toString().contains("雨夹雪")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
        }else if(days[2].getText().toString().contains("阵雪")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_zhenxue);
        }else if(days[2].getText().toString().contains("阵雨")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_zhenyu);
        }else if(days[2].getText().toString().contains("中雪")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_zhongxue);
        }else if(days[2].getText().toString().contains("中雨")){
            sixDayImage[i].setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_city_manager){
            Intent i = new Intent(this,SelectCity.class);
            String cityName = cityTv.getText().toString();
            //将当前城市名称传给城市选择页面，标出当前城市信息
            i.putExtra("getName",cityName);
            Log.d("cityTV",cityName);
            startActivityForResult(i,1);
            //打开SelectCity，结束后返回相关信息
        }
        if (view.getId() == R.id.title_update_btn) {
            // 创建一个SharedPreferences类的对象用于存储城市代码信息，数据以键值对(key-value)保存，保存在config.xml文件中
            // MODE_PRIVATE:默认操作模式，表示xml文件是私有的，只能在创建文件的应用中访问
            // 使用getString()获取"main_city_code"对应的value,缺省值"101010100"
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather", cityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络OK");
                queryWeatherCode(cityCode);
            }else
            {
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了",Toast.LENGTH_LONG).show();
            }
        }
        if (view.getId() == R.id.title_location) {
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                if(mLocationClient.isStarted()){
                    mLocationClient.stop();
                }
                mLocationClient.start();
                //queryWeatherCode(mLocationListener.cityCode);
            }


        }
    }

    //SelectCity通过intent返回城市代码，进行信息更新
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1 && resultCode == RESULT_OK){
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather","选择的城市代码为"+newCityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络OK");
                queryWeatherCode(newCityCode);
            }else {
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
        try{
            //创建生产XML的pull解析器的工厂
            //创建一个pull解析器xmlPullParser
            //使用解析器读取xml文件
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));//StringReader是用于处理String类型的Reader类，更有效率
            int eventType = xmlPullParser.getEventType();//获取当前事件的状态
            Log.d("myWeather","parseXML");
            //解析过程：当前事件！=XmlPullParser.END_DOCUMENT就循环下去，每一个事件的开头和结尾分别是START_TAG和END_TAG，
            //使用getName()方法获得当前节点的名字，在相应的条件判断中用getText()获得事件的内容，并保存在todayWeather类中
            //使用next()移动到下一个节点，数个Count变量用于保证一些信息只被读取一次
            while (eventType != XmlPullParser.END_DOCUMENT){
                switch(eventType){
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp" )){
                            todayWeather= new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date_1")&&dateCount<6) {
                                eventType = xmlPullParser.next();
                                Log.d("myWeather", dateCount+"date_1:    "+xmlPullParser.getText());
                                todayWeather.sixWeather[dateCount].setDate(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("high_1")&&highCount<6) {
                                eventType = xmlPullParser.next();
                                Log.d("myWeather", "high_1:    "+xmlPullParser.getText());
                                todayWeather.sixWeather[highCount].setHigh(xmlPullParser.getText().split(" ")[1]);
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low_1")&&lowCount<6) {
                                eventType = xmlPullParser.next();
                                Log.d("myWeather", "low_1:    "+xmlPullParser.getText().split(" ")[1]);
                                todayWeather.sixWeather[lowCount].setLow(xmlPullParser.getText().split(" ")[1]);
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("type_1")&&typeCount<6) {
                                eventType = xmlPullParser.next();
                                Log.d("myWeather", "type_1:    "+xmlPullParser.getText());
                                todayWeather.sixWeather[typeCount].setType(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("fengxiang")&&fengxiangCount<6&&fengxiangCount==0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                Log.d("myWeather", "fengxiang"+fengxiangCount+xmlPullParser.getText());
                            }else if (xmlPullParser.getName().equals("fx_1")&&fengxiangCount<6) {
                                eventType = xmlPullParser.next();
                                Log.d("myWeather", "fx_1:    "+xmlPullParser.getText());
                                todayWeather.sixWeather[fengxiangCount].setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            }else if (xmlPullParser.getName().equals("fengxiang")&&fengxiangCount<6&&fengxiangCount!=0) {
                                eventType = xmlPullParser.next();
                                todayWeather.sixWeather[fengxiangCount].setFengxiang(xmlPullParser.getText());
                                Log.d("myWeather", "fengxiang"+fengxiangCount+xmlPullParser.getText());
                                fengxiangCount++;
                            }else if (xmlPullParser.getName().equals("date")&&dateCount<6) {
                                eventType = xmlPullParser.next();
                                if (dateCount==1) {
                                    todayWeather.setDate(xmlPullParser.getText());
                                }
                                Log.d("myWeather", "dateCount"+dateCount+xmlPullParser.getText());
                                todayWeather.sixWeather[dateCount].setDate(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("high")&&highCount<6) {
                                eventType = xmlPullParser.next();
                                if(highCount==1) {
                                    todayWeather.setHigh(xmlPullParser.getText().split(" ")[1]);
                                }
                                todayWeather.sixWeather[highCount].setHigh(xmlPullParser.getText().split(" ")[1]);
                                Log.d("myWeather", "high:    "+ highCount+highCount+xmlPullParser.getText());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low")&&lowCount<6) {
                                eventType = xmlPullParser.next();
                                if(lowCount==1) {
                                    todayWeather.setLow(xmlPullParser.getText().split(" ")[1]);
                                }
                                Log.d("myWeather", "low:    "+lowCount+xmlPullParser.getText());
                                todayWeather.sixWeather[lowCount].setLow(xmlPullParser.getText().split(" ")[1]);
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("type")&&typeCount<6) {
                                eventType = xmlPullParser.next();
                                if(typeCount==1) {
                                    todayWeather.setType(xmlPullParser.getText());
                                    Log.d("myWeather", "todayWeather.setType(xmlPullParser.getText());"+ typeCount+xmlPullParser.getText());
                                }
                                Log.d("myWeather", "type:    "+ typeCount+xmlPullParser.getText());
                                todayWeather.sixWeather[typeCount].setType(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("night")||xmlPullParser.getName().equals("night_1")) {
                                for (int i = 0; i < 9; i++) {
                                    eventType = xmlPullParser.next();
                                }
                            }
                        }
                        break;
                    //判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }

    void updateTodayWeather(TodayWeather todayWeather){
        //六天的天气更新
        updateSixWeather(day0,0,todayWeather);
        updateSixWeather(day1,1,todayWeather);
        updateSixWeather(day2,2,todayWeather);
        updateSixWeather(day3,3,todayWeather);
        updateSixWeather(day4,4,todayWeather);
        updateSixWeather(day5,5,todayWeather);

        day0[0].setText(todayWeather.sixWeather[0].getDate());
        day0[1].setText(todayWeather.sixWeather[0].getLow()+"~"+todayWeather.sixWeather[0].getHigh());
        day0[2].setText(todayWeather.sixWeather[0].getType());
        day0[3].setText(todayWeather.sixWeather[0].getDate());

        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();

        //更新pm指数对应图标
        if(todayWeather.getPm25()!= null){
            int pm2_5 = Integer.parseInt(todayWeather.getPm25());
            if(pm2_5 <= 50)pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            if(pm2_5>50 && pm2_5<=100)pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            if(pm2_5>100 && pm2_5<=150)pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            if(pm2_5>150 && pm2_5<=200)pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            if(pm2_5>200 && pm2_5<=300)pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            if(pm2_5>300)pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
        }

        String climate = todayWeather.getType();
        //更新天气类型对应图标

        if(climate.equals("暴雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
        if(climate.equals("暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
        if(climate.equals("大暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
        if(climate.equals("大雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
        if(climate.equals("大雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
        if(climate.equals("多云"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
        if(climate.equals("雷阵雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
        if(climate.equals("雷阵雨冰雹"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
        if(climate.equals("晴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        if(climate.equals("沙尘暴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
        if(climate.equals("特大暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
        if(climate.equals("雾"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
        if(climate.equals("小雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
        if(climate.equals("小雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
        if(climate.equals("阴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
        if(climate.equals("雨夹雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
        if(climate.equals("阵雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
        if(climate.equals("阵雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
        if(climate.equals("中雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
        if(climate.equals("中雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_LONG).show();

    }
    /**
     *
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather",address);
        //创建一个线程用于获取网络数据，不占用主线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try{
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection(); //调用url.openConnection()来获得一个新的HttpURLConnection对象
                    con.setRequestMethod("GET"); //设置请求方式为"GET"，从服务器上获取数据，将数据放在url中
                    con.setConnectTimeout(8000); //设置连接主机超时ms
                    con.setReadTimeout(8000);  //设置从主机读取数据超时ms
                    InputStream in = con.getInputStream();  //将封装好的HTTP请求电文以字节流的方式读取出来
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in)); //将文本保存到缓冲区，减少读取开销
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str = reader.readLine()) != null){  //将缓冲区的内容逐行读取，直到全部读完
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);

                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null){//获得今日消息后，传递给主线程，主线程用updateTodayWeather更新数据到UI上
                        Log.d("myWeather",todayWeather.toString());
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }
}
