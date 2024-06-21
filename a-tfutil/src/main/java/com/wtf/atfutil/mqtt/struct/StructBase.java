package cn.ac.iscas.util.struct;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * @author WTF
 * @date 2022/8/10 15:40
 */
public class StructBase {

//    protected byte[] headSourceByteArray;
//    protected byte[] sourceByteArray;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    protected Date createTime;

//    @JsonProperty("headSourceByteArray")
//    public byte[] headSourceByteArray() {
//        return headSourceByteArray;
//    }
//
//    public StructBase setHeadSourceByteArray(byte[] headSourceByteArray) {
//        this.headSourceByteArray = headSourceByteArray;
//        return this;
//    }
//
//    @JsonProperty("sourceByteArray")
//    public byte[] sourceByteArray() {
//        return sourceByteArray;
//    }
//
//    public StructBase setSourceByteArray(byte[] sourceByteArray) {
//        this.sourceByteArray = sourceByteArray;
//        return this;
//    }
//

    @JsonProperty("createTime")
    public Date createTime() {
        return createTime;
    }

    public StructBase setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }
}
