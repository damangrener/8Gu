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

package org.apache.dolphinscheduler.plugin.task.zt.cod;

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
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.zt.service.config.common.YmlReader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * //    private Property COD;
 * //    private Property ELE;
 * //    private Property FIX;
 * //    private Property OPT;
 * //    private Property RES;
 * //    private Property RPT;
 * //    private Property TLE;
 * //    private Property XYZ;
 */
@Data
public class ZtCodParameters extends AbstractParameters {

    //执行模式
    private ExecuteMode executeMode;
    //参数模式
    private ParamMode paramMode;

    private Property satProperty;

    private Property iniProperty;

    private Property coeProperty;

    private Property obsProperty;

    private Property siteProperty;

    private Property fapkPdProperty;

    private Property finalSdProperty;

    private Property noradCodeProperty;

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
    private ResourceInfo folderResource;

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

    public boolean checkFinalParameters() {
        StringBuffer sb = new StringBuffer();
        if (null == getSatProperty() || StringUtils.isBlank(getSatProperty().getValue())) {
            sb.append("INI must contains 'SAT' ! \n");
        }
        getIniRows().stream().filter(x -> Objects.equals(ParamType.FILE_IN, x.getType())).forEach(x -> {
            if (StringUtils.isBlank(x.getValue())) {
                sb.append("param [")
                        .append(x.getCodeStr())
                        .append("] cannot be null \n");
            } else if (!Files.exists(Paths.get(x.getValue()))) {
                sb.append("param [")
                        .append(x.getCodeStr())
                        .append("] file ")
                        .append(x.getValue())
                        .append(" is not exist \n");
            }
        });
        if (StringUtils.isNotEmpty(sb.toString())) {
            throw new TaskException("checkParameters error,{}" + sb);
        }
        return true;
    }

    public List<IniRow> initIni() {

        List<IniRow> inis = new ArrayList<>(16);

        IniRow SAT = new IniRow("SAT", ParamType.PARAM_IN);
        IniRow INI = new IniRow("INI", ParamType.FILE_IN);
        IniRow COE = new IniRow("COE", ParamType.FILE_IN);
        IniRow OBS = new IniRow("OBS", ParamType.FILE_IN);
        IniRow COD = new IniRow("COD", ParamType.FILE_OUT);
        IniRow ELE = new IniRow("ELE", ParamType.FILE_OUT);
        IniRow FIX = new IniRow("FIX", ParamType.FILE_OUT);
        IniRow OPT = new IniRow("OPT", ParamType.FILE_OUT);
        IniRow RES = new IniRow("RES", ParamType.FILE_OUT);
        IniRow RPT = new IniRow("RPT", ParamType.FILE_OUT);
        IniRow TLE = new IniRow("TLE", ParamType.FILE_OUT);
        IniRow XYZ = new IniRow("XYZ", ParamType.FILE_OUT);
        IniRow SITE = new IniRow("SITE", YmlReader.getZtValue("SITE"), ParamType.STATIC_FILE_IN);
        IniRow FAPKPD = new IniRow("FAPKPD", YmlReader.getZtValue("FAPKPD"), ParamType.STATIC_FILE_IN);
        IniRow FINALSD = new IniRow("FINALSD", YmlReader.getZtValue("FINALSD"), ParamType.STATIC_FILE_IN);
        IniRow NORADCODE = new IniRow("NORADCODE", YmlReader.getZtValue("NORADCODE"), ParamType.STATIC_FILE_IN);

        inis.add(SAT);
        inis.add(INI);
        inis.add(COE);
        inis.add(OBS);
        inis.add(COD);
        inis.add(ELE);
        inis.add(FIX);
        inis.add(OPT);
        inis.add(RES);
        inis.add(RPT);
        inis.add(TLE);
        inis.add(XYZ);
        inis.add(SITE);
        inis.add(FAPKPD);
        inis.add(FINALSD);
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
    public ZtCodParameters fillIniValue(Map<String, String> paramMap) {
        if (MapUtils.isEmpty(paramMap)) {
            throw new RuntimeException("参数不能为空");
        }

        List<IniRow> iniRowList = this.getIniRows();

        for (IniRow iniRow : iniRowList) {

            if (paramMap.containsKey(iniRow.getCodeStr())) {
                iniRow.setValue(paramMap.get(iniRow.getCodeStr()));
            }
        }
        this.setIniRows(iniRowList);

        //没有指定输出文件地址的,设置默认输出地址
        iniRowList.stream().filter(x -> Objects.equals(ParamType.FILE_OUT, x.getType()) && StringUtils.isBlank(x.getValue())).forEach(x -> {
            x.setValue(getWorkPath().getValue() + File.separator + getSatProperty().getValue() + "." + x.getCodeStr());
        });

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
    public ZtCodParameters fillTaskFiles(List<IniRow> iniRows, String taskId) {

        List<IniRow> iniRows1 = iniRows.stream().filter(x -> x.getType().name().contains("FILE")).collect(Collectors.toList());

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
            if (!Objects.equals(iniRow.getType(), ParamType.STATIC_FILE_IN)) {
                fileInfo.setFileContent(FileUtils.readFile(fileInfo.getFilePath()));
            }
            fileInfo.setInOut(iniRow.getType().name());
            taskFileInfos1.add(fileInfo);
        }

        this.setTaskFileInfos(taskFileInfos1);
        return this;
    }


}
