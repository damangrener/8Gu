/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.task.zt.pas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.common.enums.zt.ParamMode;
import org.apache.dolphinscheduler.common.enums.zt.ParamType;
import org.apache.dolphinscheduler.common.model.zt.IniRow;
import org.apache.dolphinscheduler.common.model.zt.TaskFileInfo;
import org.apache.dolphinscheduler.common.model.zt.TaskLog;
import org.apache.dolphinscheduler.common.utils.wtf.FileUtils;
import org.apache.dolphinscheduler.common.enums.zt.ExecuteMode;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.zt.service.config.common.YmlReader;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class ZtPasParameters extends AbstractParameters {

    //执行模式
    private ExecuteMode executeMode;
    //参数模式
    private ParamMode paramMode;

    private Property dataProperty;

    private Property tleProperty;

    private Property resultProperty;

    private Property iniProperty;

    private Property programProperty;

    private Property workPath;

    private Property programPath;

    private Property outPath;

    private String executeStatement;

    private Property iniPath;

    @JsonIgnore
    private List<IniRow> iniRows;

    @JsonIgnore
    private List<TaskFileInfo> taskFileInfos;

    private Property folderProperty;

    private Property beginDateTime;

    private Property endDateTime;

    /**
     * 参数map,临时使用,为了生成ini
     */
    private Map<String, String> paramMap;


    /**
     * shell script
     */
    private String rawScript = "${programPath} ${iniPath}";

    /**
     * resource list
     */
//    private List<ResourceInfo> resourceList;
//
    public String getRawScript() {
        return rawScript;
    }

    public void setRawScript(String rawScript) {
        this.rawScript = rawScript;
    }
//
//    public List<ResourceInfo> getResourceList() {
//        return resourceList;
//    }
//
//    public void setResourceList(List<ResourceInfo> resourceList) {
//        this.resourceList = resourceList;
//    }

    public void setExecuteMode(ExecuteMode executeMode) {
        this.executeMode = executeMode;
    }

//    @Override
//    public boolean checkParameters() {
//        return rawScript != null && !rawScript.isEmpty();
//    }

//    @Override
//    public List<ResourceInfo> getResourceFilesList() {
//        return resourceList;
//    }

    public void setOutPath(Property outPath) {
        this.outPath = outPath;
        FileUtils.mkdirs(outPath.getValue());
    }


    @Override
    public boolean checkParameters() {
        return true;
    }

    public List<IniRow> initIni() {

        List<IniRow> inis = Collections.synchronizedList(new ArrayList<>());

        IniRow INI = new IniRow("INI", ParamType.FILE_IN);
        IniRow DATA = new IniRow("DATA", ParamType.FILE_IN);
        IniRow TLE = new IniRow("TLE", ParamType.FILE_IN);
        IniRow RESULT = new IniRow("RESULT", ParamType.FILE_OUT);
        IniRow SITE = new IniRow("SITE", YmlReader.getZtValue("SITE"), ParamType.STATIC_FILE_IN);
//        IniRow STATION = new IniRow("STATION", YmlReader.getZtValue("STATION"), ParamType.STATIC_FILE_IN);
        //NORADCODE参数需要小写,不然result会出现'     0'的情况
//        IniRow NORADCODE = new IniRow("NORADCODE", YmlReader.getZtValue("NORADCODE1"), ParamType.STATIC_FILE_IN);
        //2024-06的版本有用大写了
        IniRow NORADCODE = new IniRow("NORADCODE", YmlReader.getZtValue("NORADCODE"), ParamType.STATIC_FILE_IN);

        inis.add(INI);
        inis.add(DATA);
        inis.add(TLE);
        inis.add(RESULT);
        inis.add(SITE);
//        inis.add(STATION);
        inis.add(NORADCODE);

        this.setIniRows(inis);

        return inis;
    }

    /**
     * 填充参数信息
     *
     * @param paramMap
     * @return
     */
    public ZtPasParameters fillIniValue(ZtPasParameters currentParameters) {
        Map<String, String> paramMap=currentParameters.getParamMap();
        if (MapUtils.isEmpty(paramMap)) {
            throw new RuntimeException("参数不能为空");
        }

        for (IniRow iniRow : currentParameters.getIniRows()) {

            if (paramMap.containsKey(iniRow.getCodeStr())) {
                iniRow.setValue(paramMap.get(iniRow.getCodeStr()));
            }
        }

        //提前将输入文件复制到临时文件目录
        currentParameters.getIniRows().stream().filter(x -> Objects.equals(ParamType.FILE_IN, x.getType()) && StringUtils.isNotBlank(x.getValue())).forEach(x -> {
            FileUtils.cp(x.getValue(), currentParameters.getWorkPath().getValue());
        });

        //执行pas需要的一些静态文件,目前只有pas需要
        FileUtils.cp(YmlReader.getZtValue("SITE"), currentParameters.getWorkPath().getValue());
        FileUtils.cp(YmlReader.getZtValue("NORADCODE"), currentParameters.getWorkPath().getValue());

        //提前创建输出文件
        currentParameters.getIniRows().stream().filter(x -> Objects.equals(ParamType.FILE_OUT, x.getType()) && StringUtils.isNotBlank(x.getValue())).forEach(x -> {
            FileUtils.createFile(x.getValue());
        });
        return currentParameters;
    }

    /**
     * 填充参数信息
     *
     * @param paramMap
     * @return
     */
    public ZtPasParameters fillIniValue(ZtPasParameters currentParameters,Map<String, String> paramMap) {
        if (MapUtils.isEmpty(paramMap)) {
            throw new RuntimeException("参数不能为空");
        }

        List<IniRow> iniRowList = this.getIniRows();

        for (IniRow iniRow : iniRowList) {

            if (paramMap.containsKey(iniRow.getCodeStr())) {
                iniRow.setValue(paramMap.get(iniRow.getCodeStr()));
            }
        }
        currentParameters.setIniRows(iniRowList);

        //提前将输入文件复制到临时文件目录
        iniRowList.stream().filter(x -> Objects.equals(ParamType.FILE_IN, x.getType()) && StringUtils.isNotBlank(x.getValue())).forEach(x -> {
            FileUtils.cp(x.getValue(), currentParameters.getWorkPath().getValue());
//            if (Files.exists(Paths.get(x.getValue()))){
//                x.setValue(FileUtils.cp(x.getValue(), getWorkPath().getValue()));
//            }
        });

        //执行pas需要的一些静态文件,目前只有pas需要
        FileUtils.cp(YmlReader.getZtValue("SITE"), currentParameters.getWorkPath().getValue());
//        FileUtils.cp(YmlReader.getZtValue("STATION"), getWorkPath().getValue());
        FileUtils.cp(YmlReader.getZtValue("NORADCODE1"), currentParameters.getWorkPath().getValue());

        //提前创建输出文件
        iniRowList.stream().filter(x -> Objects.equals(ParamType.FILE_OUT, x.getType()) && StringUtils.isNotBlank(x.getValue())).forEach(x -> {
            FileUtils.createFile(x.getValue());
        });
        return this;
    }

    /**
     * 填充日志文件信息
     *
     * @param iniRows
     * @return
     */
    public ZtPasParameters fillTaskFiles(ZtPasParameters currentParameters, String taskId) {

        List<IniRow> iniRows1 = currentParameters.getIniRows().stream().filter(x -> x.getType().name().contains("FILE")).collect(Collectors.toList());

        List<TaskFileInfo> taskFileInfos1 = new ArrayList<>();

        for (IniRow iniRow : iniRows1) {
            TaskFileInfo fileInfo = new TaskFileInfo();
            fileInfo.setTaskId(taskId);
            fileInfo.setFileId(String.valueOf(TaskLog.idWorker.nextId()));
            if (StringUtils.isNotBlank(iniRow.getValue())) {
                fileInfo.setFileName(Paths.get(iniRow.getValue()).toFile().getName());
            }
            fileInfo.setFilePath(iniRow.getValue());
            fileInfo.setFileKind(iniRow.getCodeStr());
//            if (!Objects.equals(iniRow.getType(), ParamType.STATIC_FILE_IN)) {
//                if (!Objects.equals(iniRow.getType(), ParamType.STATIC_FILE_IN)) {
//                    fileInfo.setFileContent(FileUtils.readFile(fileInfo.getFilePath()));
//                }
//            }
            fileInfo.setInOut(iniRow.getType().name());
            taskFileInfos1.add(fileInfo);
        }

        currentParameters.setTaskFileInfos(taskFileInfos1);
        return this;
    }


}
