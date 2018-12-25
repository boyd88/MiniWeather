package com.example.stby.miniweather;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stby.app.MyApplication;
import com.example.stby.bean.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectCity extends Activity implements View.OnClickListener{

    //    监听按钮
    private ImageView mBackBtn;
    //private TextView mSelectBtn;

    // 搜索栏和 ListView
    private TextView citySelected = null;//声名 TextView 对象，用于绑定 select_city 布局文件中顶部栏的显示内容
    private ListView listView = null;//声名 listView 对象，用于绑定 select_city 布局文件中的 ListView

//获取城市列表
    private List<City> ListCity = MyApplication.getInstance().getCityList();
    private int listSize = ListCity.size();
    private String[] city = new String[listSize];

    private ArrayList<String> mSearchResult = new ArrayList<>();//搜索结果
    private Map<String,String> nameToCode = new HashMap<>();//城市名到代码
    private Map<String,String> nameToPinyin = new HashMap<>();//城市名到拼音

    private EditText mSearch;//关联搜索框

    private String returnCode;
    private String cityName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        //获取布局中的返回控件
        //为返回按钮设置单击事件监听
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        //获取布局中当前城市控件
        //为当前城市控件设置单击事件监听
        //mSelectBtn = (TextView) findViewById(R.id.title_name);
        //mSelectBtn.setOnClickListener(this);

        Intent intent = getIntent();
        cityName = intent.getStringExtra("getName");
        citySelected = (TextView) findViewById(R.id.title_name);
        citySelected.setText("当前城市：" + cityName);
        Log.i("City",ListCity.get(1).getCity());
        for(int i = 0;i<listSize;i++){
            city[i] = ListCity.get(i).getCity();
            Log.d("City",city[i]);
        }
        //建立映射
        String strName;
        String strNamePinyin;
        String strCode;

        for(City city:ListCity){
            strCode = city.getNumber();
            strName = city.getCity();
            strNamePinyin = city.getFirstPY();
            nameToCode.put(strName,strCode);
            nameToPinyin.put(strName,strNamePinyin);
            mSearchResult.add(strName);
        }

        //为ListView设置适配器
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, mSearchResult);
        listView = findViewById(R.id.list_view);
        listView.setAdapter(arrayAdapter);//设置适配器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                //选中列表中的城市，得到选中城市的代码，并直接返回主页面，获得该城市的信息
                String returnCityName = mSearchResult.get(pos);
                Toast.makeText(SelectCity.this, "你已选择：" + returnCityName, Toast.LENGTH_SHORT).show();
                returnCode = nameToCode.get(returnCityName);
                Log.d("returncode",returnCode);
                citySelected.setText("当前城市：" + returnCityName);
                Intent i= new Intent();
                i.putExtra("cityCode",returnCode);
                setResult(RESULT_OK, i);
                finish();
            }
        });
        mSearch = (EditText)findViewById(R.id.search_edit);
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                arrayAdapter.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                //点击返回按钮，没有选择新的城市更新信息，延续之前的城市信息
                Log.d("myWeather", "我点击了返回界面");
                String select_cityCode = nameToCode.get(cityName);
                Intent i = new Intent();
                i.putExtra("cityCode",select_cityCode);
                setResult(RESULT_OK,i);
                finish();
                break;
            case R.id.title_city_name:
                Log.d("myWeather","我是北京");
            default:
                break;
        }
    }
}
