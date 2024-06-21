package com.wtf.atfutil.easyexcel;

import cn.ac.iscas.common.model.common.FormExport;
import cn.ac.iscas.common.model.common.FormHeader;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.commons.lang3.tuple.Triple;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author WTF
 * @date 2023/3/9 15:05
 */
public interface ExcelService {

    String export(Class<?> clazz, List<?> list, String name);

    <T> Map<Integer, String> excelSave(InputStream inputStream, Class<T> tClass, IService<T> service, String updateByField);

    <T> List<T> excelRead(InputStream inputStream, Class<T> tClass);

    Map<Class<?>,List<?>> excelRead(InputStream inputStream, List<Class<?>> tClazzList);

    /**
     * 按照sheet name读取excel
     * @param inputStream
     * @param classMap
     * @return
     */
    Map<Class<?>, List<?>> excelRead(InputStream inputStream, Map<String, Class<?>> classMap);

    <T> List<T> excelRead(String filePath, Class<T> tClass);

    String dynamicHeadWrite(List<Object> list, String name, List<String> excelHeaders, List<String> excelFields, List<Map<String, String>> refValueList);

    /**
     * 导出多sheet
     * @param triples
     * @param name
     * @return
     */
    String export(List<Triple<String, Class<?>, List<?>>> triples, String name);

    /**
     * 按照前端传来的表单数据导出，表单数据固定
     * @param name
     * @param headers
     * @param datas
     * @return
     */
    String export(String name, List<FormHeader> headers, List<Map<String, String>> datas);

    String export(FormExport formExport);
}
