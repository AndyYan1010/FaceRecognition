package com.botian.recognition.utils.netUtils;

import java.util.HashMap;

/**
 * @创建者 AndyYan
 * @创建时间 2018/4/27 15:46
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */

public class RequestParamsFM extends HashMap {
    private boolean isToJson = false;

    public void setUseJsonStreamer(boolean toJson) {
        this.isToJson = toJson;
    }

    public boolean getIsUseJsonStreamer() {
        return isToJson;
    }
}
