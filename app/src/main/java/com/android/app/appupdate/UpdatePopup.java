package com.android.app.appupdate;

import android.content.Context;
import android.view.View;

import razerdp.basepopup.BasePopupWindow;

/**
 * @author fanhenghao
 * @time 2019/6/24 4:11 PM
 * @class describe：这里弹窗继承BasePopupWindow使用，也可以直接用popupwindow（注意兼容性）
 */
public class UpdatePopup extends BasePopupWindow {

    public UpdatePopup(Context context) {
        super(context);
    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.popup_update);
    }
}
