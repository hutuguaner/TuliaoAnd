package com.lzq.tuliaoand.presenter;

import com.blankj.utilcode.util.SPUtils;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.common.Const;
import com.lzq.tuliaoand.common.SPKey;
import com.lzq.tuliaoand.model.ConversationModel;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class ConversationPresenter {

    private ConversationModel conversationModel;

    public ConversationPresenter(ConversationModel conversationModel) {
        this.conversationModel = conversationModel;
    }

    public void sendMessage(Message message) {
        JSONObject params = new JSONObject();
        try {
            params.put("from", message.getFrom().getEmail());
            params.put("to", message.getTo().getEmail());
            params.put("content", message.getContent());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkGo.<String>post(Const.UPLOAD_MSG).upJson(params).execute(new StringCallback() {
            @Override
            public void onStart(Request<String, ? extends Request> request) {
                super.onStart(request);
                conversationModel.sendMsgStart();
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                conversationModel.sendMsgError(response.body());
            }

            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JSONObject result = new JSONObject(response.body());
                    int code = result.getInt("code");
                    if (code == 0) {
                        conversationModel.sendMsgSuccess();
                    } else {
                        String msg = result.getString("message");
                        conversationModel.sendMsgError(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    conversationModel.sendMsgError(e.getMessage());
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                conversationModel.sendMsgFinish();
            }
        });

    }

}
