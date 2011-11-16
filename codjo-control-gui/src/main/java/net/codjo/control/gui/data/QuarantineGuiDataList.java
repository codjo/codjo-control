/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.data;
import java.util.ArrayList;
import java.util.Collection;
/**
 */
public class QuarantineGuiDataList {
    private Collection<QuarantineGuiData> dataList;
    private String requestTopic;


    public Collection<QuarantineGuiData> getDataList() {
        return dataList;
    }


    public void addData(QuarantineGuiData data) {
        if (dataList == null) {
            dataList = new ArrayList<QuarantineGuiData>();
        }
        dataList.add(data);
    }


    public void setDataList(Collection<QuarantineGuiData> dataList) {
        this.dataList = dataList;
    }


    public String getRequestTopic() {
        return requestTopic;
    }


    public void setRequestTopic(String requestTopic) {
        this.requestTopic = requestTopic;
    }


    public void compileDataList() {
        for (QuarantineGuiData data : dataList) {
            data.getWindow().compile();
            data.setMyList(this);
        }
    }
}
