package com.wtf.atfutil.easyexcel;

import cn.ac.iscas.common.model.common.FormExport;
import cn.ac.iscas.common.model.common.FormHeader;
import cn.ac.iscas.common.model.common.TimestampConverter;
import cn.ac.iscas.common.service.common.ExcelService;
import cn.ac.iscas.common.util.DateUtil;
import cn.ac.iscas.pdm.common.mybatis.table.service.TableDefinitionService;
import cn.ac.iscas.pdm.templet.exception.ValidDataException;
import cn.ac.iscas.pdm.templet.view.table.TableResponse;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author WTF
 * @date 2023/3/9 15:06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExcelServiceImpl implements ExcelService {

    @Value("${pdm.download.filepath}")
    private String filePath;

    private final TableDefinitionService tableDefinitionService;


    @Override
    public String export(Class<?> clazz, List<?> list, String name) {

        // 写法1 JDK8+
        // since: 3.0.0-beta1
        String fileName = filePath + "/" + name + ".xlsx";
        File file = FileUtil.mkdir(filePath);
        if (!file.exists()) {
            FileUtil.createTempFile(file);
        }
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        // 如果这里想使用03 则 传入excelType参数即可
        EasyExcel.write(fileName, clazz)
                .sheet("sheet")
                .doWrite(() -> {
                    // 分页查询数据
                    return list;
                });

        return fileName;
    }

    @Override
    public <T> Map<Integer, String> excelSave(InputStream inputStream, Class<T> tClass, IService<T> service, String updateByField) {

        // 创建异常行号列表
        Map<Integer, String> exceptionRows = new HashMap<>();

        // 创建数据读取监听器
        AnalysisEventListener<T> listener = new AnalysisEventListener<>() {
            @Override
            public void onException(Exception exception, AnalysisContext context) {
                // 处理数据转换异常
                if (exception instanceof ExcelDataConvertException) {
                    ExcelDataConvertException convertException = (ExcelDataConvertException) exception;
                    int rowNumber = convertException.getRowIndex() + 1; // Excel行号从0开始，所以需要加1
                    exceptionRows.put(rowNumber, exception.getMessage());
                }
            }

            @Override
            public void invoke(T data, AnalysisContext context) {
                // 在这里可以处理每一行的数据
                // 比如将数据插入到数据库或进行其他操作
                int currentRow = context.readRowHolder().getRowIndex();
                try {
                    QueryWrapper<T> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq(updateByField, ReflectUtil.getFieldValue(data, updateByField));
                    service.saveOrUpdate(data, queryWrapper);
                } catch (Exception e) {
                    exceptionRows.put(currentRow + 1, "数据异常"); // Excel行号从0开始，所以需要加1
                    log.error("excel 数据异常，行号={}，", currentRow + 1, e);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 数据读取完成后的操作
                // 比如打印异常行号
                log.error("异常行号：{}", exceptionRows);
            }
        };

        // 开始读取Excel文件
        EasyExcel.read(inputStream).sheet(0).head(tClass).registerReadListener(listener).doRead();

        return exceptionRows;
    }

    @Override
    public <T> List<T> excelRead(InputStream inputStream, Class<T> tClass) {

        List<T> list = new ArrayList<>();

        // 创建异常行号列表
        Map<Integer, String> exceptionRows = new HashMap<>();

        // 创建数据读取监听器
        AnalysisEventListener<T> listener = new AnalysisEventListener<>() {
            @Override
            public void onException(Exception exception, AnalysisContext context) {
                // 处理数据转换异常
                if (exception instanceof ExcelDataConvertException) {
                    ExcelDataConvertException convertException = (ExcelDataConvertException) exception;
                    int rowNumber = convertException.getRowIndex() + 1; // Excel行号从0开始，所以需要加1
                    exceptionRows.put(rowNumber, exception.getMessage());
                }
            }

            @Override
            public void invoke(T data, AnalysisContext context) {
                // 在这里可以处理每一行的数据
                // 比如将数据插入到数据库或进行其他操作
                int currentRow = context.readRowHolder().getRowIndex();
                try {
                    ReflectUtil.setFieldValue(data, "rowNum", currentRow + 1);
                    list.add(data);
                } catch (Exception e) {
                    exceptionRows.put(currentRow + 1, "数据异常"); // Excel行号从0开始，所以需要加1
                    log.error("excel 数据异常，行号={}，", currentRow + 1, e);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 数据读取完成后的操作
                // 比如打印异常行号
                log.error("异常行号：{}", exceptionRows);
            }
        };

        // 开始读取Excel文件
        EasyExcel.read(inputStream).sheet(0).head(tClass).registerReadListener(listener).doRead();
        if (MapUtils.isNotEmpty(exceptionRows)) {
            log.error("导入异常条数[{}]", exceptionRows.size());
            exceptionRows.forEach((k, v) -> {
                log.error("行号：" + k + "," + v);
            });
        }
        return list;
    }

    @Override
    public Map<Class<?>, List<?>> excelRead(InputStream inputStream, List<Class<?>> tClazzList) {

        // 创建异常行号列表
        Map<Integer, String> exceptionRows = new HashMap<>();

        Map<Class<?>, List<?>> map = new HashMap<>();
        ReadSheet[] sheets = new ReadSheet[tClazzList.size()];

        try (ExcelReader excelReader = EasyExcel.read(inputStream).build()) {
            for (int i = 0; i < tClazzList.size(); i++) {
                List datas = new ArrayList<>();
                ReadSheet readSheet =
                        EasyExcel.readSheet(i).head(tClazzList.get(i)).registerReadListener(new AnalysisEventListener<>() {
                            @Override
                            public void invoke(Object data, AnalysisContext context) {
                                int currentRow = context.readRowHolder().getRowIndex();
                                try {
                                    ReflectUtil.setFieldValue(data, "rowNum", currentRow + 1);
                                    datas.add(data);
                                } catch (Exception e) {
                                    exceptionRows.put(currentRow + 1, "数据异常"); // Excel行号从0开始，所以需要加1
                                    log.error("excel 数据异常，行号={}，", currentRow + 1, e);
                                }
                            }

                            @Override
                            public void onException(Exception exception, AnalysisContext context) {
                                // 处理数据转换异常
                                if (exception instanceof ExcelDataConvertException) {
                                    ExcelDataConvertException convertException = (ExcelDataConvertException) exception;
                                    int rowNumber = convertException.getRowIndex() + 1; // Excel行号从0开始，所以需要加1
                                    exceptionRows.put(rowNumber, exception.getMessage());
                                }
                            }

                            @Override
                            public void doAfterAllAnalysed(AnalysisContext context) {
                                // 数据读取完成后的操作
                                // 比如打印异常行号
                                log.error("异常行号：{}", exceptionRows);
                            }
                        }).build();

                sheets[i] = readSheet;
                map.put(tClazzList.get(i), datas);
            }

            // 这里注意 一定要把sheet1 sheet2 一起传进去，不然有个问题就是03版的excel 会读取多次，浪费性能
            excelReader.read(sheets);
        }

        return map;
    }

    @Override
    public Map<Class<?>, List<?>> excelRead(InputStream inputStream, Map<String, Class<?>> classMap) {

        // 创建异常行号列表
        Map<Integer, String> exceptionRows = new HashMap<>();

        Map<Class<?>, List<?>> map = new HashMap<>();
        ReadSheet[] sheets = new ReadSheet[classMap.size()];

        try (ExcelReader excelReader = EasyExcel.read(inputStream).build()) {
            AtomicInteger i = new AtomicInteger();
            classMap.forEach((k, v) -> {
                List datas = new ArrayList<>();
                ReadSheet readSheet =
                        EasyExcel.readSheet(k).head(v).registerReadListener(new AnalysisEventListener<>() {
                            @Override
                            public void invoke(Object data, AnalysisContext context) {
                                int currentRow = context.readRowHolder().getRowIndex();
                                try {
                                    ReflectUtil.setFieldValue(data, "rowNum", currentRow + 1);
                                    datas.add(data);
                                } catch (Exception e) {
                                    exceptionRows.put(currentRow + 1, "数据异常"); // Excel行号从0开始，所以需要加1
                                    log.error("excel 数据异常，行号={}，", currentRow + 1, e);
                                }
                            }

                            @Override
                            public void onException(Exception exception, AnalysisContext context) {
                                // 处理数据转换异常
                                if (exception instanceof ExcelDataConvertException) {
                                    ExcelDataConvertException convertException = (ExcelDataConvertException) exception;
                                    int rowNumber = convertException.getRowIndex() + 1; // Excel行号从0开始，所以需要加1
                                    exceptionRows.put(rowNumber, exception.getMessage());
                                }
                            }

                            @Override
                            public void doAfterAllAnalysed(AnalysisContext context) {
                                // 数据读取完成后的操作
                                // 比如打印异常行号
                                log.error("异常行号：{}", exceptionRows);
                            }
                        }).build();

                sheets[i.get()] = readSheet;
                i.getAndIncrement();
                map.put(v, datas);
            });

            // 这里注意 一定要把sheet1 sheet2 一起传进去，不然有个问题就是03版的excel 会读取多次，浪费性能
            excelReader.read(sheets);
        }

        return map;
    }

    @SneakyThrows
    @Override
    public <T> List<T> excelRead(String filePath, Class<T> tClass) {

        return excelRead(new FileInputStream(filePath), tClass);
    }

    @Override
    public String dynamicHeadWrite(List<Object> list, String name, List<String> excelHeaders, List<String> excelFields, List<Map<String, String>> refValueList) {

        String fileName = filePath + "/" + name + ".xlsx";

        EasyExcel.write(fileName)
                // 这里放入动态头
                .head(dynamicHead(excelHeaders))
                .sheet("模板")
                // 当然这里数据也可以用 List<List<String>> 去传入
                .doWrite(dynamicData(list, excelFields, refValueList));

        return fileName;
    }

    /**
     * k"标题" v字段名
     *
     * @return
     */
    private List<List<String>> dynamicHead(List<String> excelHeaders) {

        List<List<String>> list = new ArrayList<>();
        excelHeaders.forEach(x -> {
            List<String> head = new ArrayList<>();
            head.add(x);
            list.add(head);
        });

        return list;
    }

    /**
     * @return
     */
    private List<List<Object>> dynamicData(List<Object> datas, List<String> excelFields, List<Map<String, String>> refValueList) {

        if (CollectionUtils.isNotEmpty(datas) && CollectionUtils.isNotEmpty(refValueList)) {
            datas.forEach(x -> {

            });
        }

        List<List<Object>> dataList = new ArrayList<>();
        datas.forEach(x -> {
            List<Object> row = new ArrayList<>();
            excelFields.forEach(y -> {

                Object fieldValue = ReflectUtil.getFieldValue(x, y);
                Object data;
                if (ObjectUtils.isEmpty(fieldValue)) {
                    data = "";
                } else if (fieldValue instanceof Data) {
                    data = DateUtil.dateToString((Date) fieldValue);
                } else {
                    data = fieldValue.toString();
                    if (CollectionUtils.isNotEmpty(refValueList)) {
                        for (Map<String, String> map : refValueList) {
                            if (Objects.equals(map.get("FIELD"), y)) {

                                Map<String, Object> refvalue = JSON.parseObject(map.get("REFVALUE"), Map.class);

                                data = refvalue.get(data);
                            }
                        }
                    }
                }
                row.add(data);
            });
            dataList.add(row);
        });

        return dataList;
    }

    /**
     * @param triples left sheet
     *                middle clazz
     *                right data
     * @param name
     * @return
     */
    @Override
    public String export(List<Triple<String, Class<?>, List<?>>> triples, String name) {

        // 写法1 JDK8+
        // since: 3.0.0-beta1
        String fileName = filePath + "/" + name + ".xlsx";

        try (ExcelWriter excelWriter = EasyExcel.write(fileName).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build()) {
            for (int i = 0; i < triples.size(); i++) {
                WriteSheet writeSheet = EasyExcel.writerSheet(i, triples.get(i).getLeft()).head(triples.get(i).getMiddle()).build();
                excelWriter.write(triples.get(i).getRight()
                        , writeSheet);
            }
        }

        return fileName;
    }

    @Override
    public String export(String name, List<FormHeader> headers, List<Map<String, String>> datas) {

        List<List<String>> heads = new ArrayList<>();

        List<String> realHead = new ArrayList<>();

        headers.forEach(x -> {
            List<String> first = new ArrayList<>();
            first.add(x.getHeader());
            if (CollectionUtils.isNotEmpty(x.getChildren())) {
                x.getChildren().forEach(y -> {
                    List<String> second = new ArrayList<>();
                    second.add(x.getHeader());
                    second.add(y.getHeader());
                    heads.add(second);

                    realHead.add(y.getField());
                });
            } else {
                heads.add(first);
                realHead.add(x.getField());
            }
        });

        List<List<Object>> list = new ArrayList<>();

        for (Map<String, String> data : datas) {

            List<Object> newData = new ArrayList<>();
            for (String head : realHead) {
                newData.add(data.get(head));
            }
            list.add(newData);
        }

        String fileName = filePath + "/" + name + System.currentTimeMillis() + ".xlsx";

        EasyExcel.write(fileName)
                .head(heads)
                .sheet()
                .registerConverter(new TimestampConverter())
                .doWrite(list);

        return fileName;
    }

    @Override
    public String export(FormExport formExport) {
        if (formExport.getDataMode() == 2) {
            try {
                formExport.getRequest().setPageSize(10000);
                TableResponse response = tableDefinitionService.getData(formExport.getRealTableName(), formExport.getRequest(), null);

                if (ObjectUtils.isNotEmpty(response) && ObjectUtils.isNotEmpty(response.getValue()) && ObjectUtils.isNotEmpty(response.getValue().getData())) {
                    List<Map<String, String>> datas = (List<Map<String, String>>) response.getValue().getData();
                    return this.export(formExport.getName(), formExport.getHeaders(), datas);
                }
            } catch (ValidDataException e) {
                log.error("excel export error! ", e);
                throw new RuntimeException(e);
            }
        } else if (formExport.getDataMode() == 1) {
            return this.export(formExport.getName(), formExport.getHeaders(), formExport.getDatas());
        }
        return null;
    }

}
