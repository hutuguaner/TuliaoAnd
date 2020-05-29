package com.lzq.tuliaoand.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.adapter.AdapterCommunicationList;
import com.lzq.tuliaoand.bean.BeanCommunication;

import java.util.ArrayList;
import java.util.List;

public class CommunicationListActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout rlBack;
    private ListView lv;
    private List<BeanCommunication> beanCommunications;
    private AdapterCommunicationList adapterCommunicationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_list);
        initView();
    }

    private void initView() {
        rlBack = findViewById(R.id.rl_communication_list_back);
        rlBack.setOnClickListener(this);
        lv = findViewById(R.id.lv_communication_list);
        if (beanCommunications == null) beanCommunications = new ArrayList<>();
        if (adapterCommunicationList == null)
            adapterCommunicationList = new AdapterCommunicationList(this, beanCommunications);
        lv.setAdapter(adapterCommunicationList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_communication_list_back:
                this.finish();
                break;
        }
    }
}
