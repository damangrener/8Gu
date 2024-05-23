package com.wtf.atfutil.page;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author WTF
 * @date 2023/3/2 18:11
 */
@Data
public class Page<T> {

    private long rows;

    private List<T> data;

    private Integer pageNum;
    private Integer pageSize;

    public Page(List<T> list, Integer pageNum, Integer pageSize) {
        this.rows = list.size();
        this.data = list.stream().skip((long) (pageNum - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
    }

    public Page(List<T> list, long rows) {
        this.data = list;
        this.rows = rows;
    }
}
