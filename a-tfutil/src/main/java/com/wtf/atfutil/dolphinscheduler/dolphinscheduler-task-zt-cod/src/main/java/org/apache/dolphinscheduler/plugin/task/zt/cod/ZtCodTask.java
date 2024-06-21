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

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.InnerHitsResult;
import co.elastic.clients.json.JsonData;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.dolphinscheduler.common.constants.zt.TleSource;
import org.apache.dolphinscheduler.common.enums.zt.ParamMode;
import org.apache.dolphinscheduler.common.enums.zt.ParamType;
import org.apache.dolphinscheduler.common.enums.zt.ZtTaskStatus;
import org.apache.dolphinscheduler.common.model.zt.*;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.wtf.IndexUtils;
import org.apache.dolphinscheduler.common.utils.wtf.ObjAndStrConverter;
import org.apache.dolphinscheduler.dao.utils.BeanContext;
import org.apache.dolphinscheduler.plugin.task.api.*;
import org.apache.dolphinscheduler.common.enums.zt.ExecuteMode;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskUtils;
import org.apache.dolphinscheduler.zt.service.es.ElasticsearchService;
import org.apache.dolphinscheduler.zt.service.service.TaskFileInfoService;
import org.apache.dolphinscheduler.zt.service.service.TaskLogService;
import org.apache.dolphinscheduler.zt.service.service.TleService;
import org.apache.dolphinscheduler.zt.service.service.impl.TaskLogServiceImpl;
import org.springframework.scheduling.annotation.Async;

import javax.swing.text.StringContent;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * shell task
 */
public class ZtCodTask extends AbstractTask {

    /**
     * shell parameters
     */
    private ZtCodParameters parameters;

    /**
     * shell command executor
     */
    private ZtShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    private TaskLogService taskLogService;

    private TaskFileInfoService taskFileInfoService;

    private ElasticsearchService elasticsearchService;

    private ElasticsearchClient elasticsearchClient;
    private TleService tleService;


//    @Lazy
//    private ResourcesService resourcesService;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public ZtCodTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ZtShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                logger);

        this.taskLogService = BeanContext.getBean(TaskLogServiceImpl.class);
        this.taskFileInfoService = BeanContext.getBean(TaskFileInfoService.class);
        this.tleService = BeanContext.getBean(TleService.class);
        this.elasticsearchService = BeanContext.getBean(ElasticsearchService.class);
        this.elasticsearchClient = BeanContext.getBean(ElasticsearchClient.class);

    }

    @Override
    public void init() {
        logger.info("shell task params {}", taskExecutionContext.getTaskParams());

        parameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ZtCodParameters.class);

        this.parameters.setProgramPath(new Property(TaskUtils.getProgramPath("COD")));

        if (!parameters.checkParameters()) {
            throw new RuntimeException("shell task params is not valid");
        }

        parameters.initIni();
        Map<String, String> map = new HashMap<>();
        parameters.setParamMap(map);
        parameters.setWorkPath(new Property(taskExecutionContext.getExecutePath() + File.separator));
        parameters.setOutPath(parameters.getWorkPath());

        //INI模式
        if (Objects.equals(parameters.getParamMode(), ParamMode.INI)) {
            //文本模式
            if (Objects.nonNull(parameters.getIniProperty()) && StringUtils.isNotBlank(parameters.getIniProperty().getValue())) {
                String value = parameters.getIniProperty().getValue();
                String[] split = value.split("\n");
                for (String line : split) {
                    String[] lineStr = line.split("\\s+");
                    map.put(lineStr.length > 0 ? lineStr[0] : null, lineStr.length > 1 ? lineStr[1] : "");
                }
                parameters.setParamMap(map);
            }
        }

    }

    private void handleParamMode() {
        if (Objects.equals(parameters.getParamMode(), ParamMode.PARAM)) {
            handleProperty(parameters.getSatProperty());
            handleProperty(parameters.getIniProperty());
            handleProperty(parameters.getCoeProperty());
            handleProperty(parameters.getObsProperty());
            handleProperty(parameters.getSiteProperty());
            handleProperty(parameters.getFapkPdProperty());
            handleProperty(parameters.getFinalSdProperty());
            handleProperty(parameters.getNoradCodeProperty());
        }
    }

    private void handleProperty(Property property) {
        if (Objects.nonNull(property) && StringUtils.isNotBlank(property.getValue())) {
            if (Objects.nonNull(property.getType()) && property.getType().name().equals("CONTENT")) {
                String path = parameters.getWorkPath().getValue() + File.separator + "temp." + property.getProp();
                org.apache.dolphinscheduler.common.utils.wtf.FileUtils.createFile(path, property.getValue());
                parameters.getParamMap().put(property.getProp(), path);
            } else {
                parameters.getParamMap().put(property.getProp(), property.getValue());
            }
        }
    }


    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            List<String> analyseTaskIds = new ArrayList<>();
            //手动模式
            if (Objects.equals(parameters.getExecuteMode(), ExecuteMode.MANUAL)) {

                ZtCodLog ztCodLog = new ZtCodLog();
                analyseTaskIds.add(ztCodLog.getTaskId());
                ztCodLog.setExecuteMode(ExecuteMode.MANUAL);
                parameters.setWorkPath(new Property(taskExecutionContext.getExecutePath() + File.separator + ztCodLog.getTaskId()));

                org.apache.dolphinscheduler.common.utils.wtf.FileUtils.mkdirs(parameters.getWorkPath().getValue());
                this.handleParamMode();
                if (!parameters.getParamMap().containsKey("INI")) {
                    String iniPath = parameters.getWorkPath().getValue() + File.separator + "COD.INI";
                    parameters.getParamMap().put("INI", iniPath);
                    parameters.setIniPath(new Property(iniPath));
                    parameters.fillIniValue(parameters.getParamMap());
                    this.writeIni();
                }
                parameters.checkFinalParameters();

                this.singleHandle(ztCodLog);
            }
            //自动模式,数据来源指定目录，需要生成参数文件
            else if (Objects.equals(parameters.getExecuteMode(), ExecuteMode.AUTO_FROM_FOLDER)) {

                if (Objects.isNull(parameters.getFolderProperty()) || StringUtils.isBlank(parameters.getFolderProperty().getValue())) {
                    throw new TaskException("AUTO_FROM_FOLDER mode, folder path cannot be null !");
                }

                Path path = Paths.get(parameters.getFolderProperty().getValue());
                if (!Files.exists(path) && !Files.isDirectory(path)) {
                    throw new TaskException("Folder is not exist : " + parameters.getFolderProperty().getValue());
                }

                File file = path.toFile();
                File[] files = file.listFiles(x -> x.getName().endsWith(".INI"));

                for (File file1 : files) {
                    ZtCodLog ztCodLog = new ZtCodLog();
                    analyseTaskIds.add(ztCodLog.getTaskId());

                    ztCodLog.setExecuteMode(ExecuteMode.AUTO_FROM_FOLDER);

                    parameters.setWorkPath(new Property(taskExecutionContext.getExecutePath() + File.separator + ztCodLog.getTaskId()));
                    FileUtils.createWorkDirIfAbsent(parameters.getWorkPath().getValue());
                    this.handleParamMode();

                    String content = FileUtils.readFile2Str(new FileInputStream(file1));
                    // 先按换行符分隔字符串
                    String[] lines = content.split("\n");

                    // 遍历每行并按多个空格分隔
                    for (String line : lines) {
                        String[] lineStr = line.split("\\s+");
                        parameters.getParamMap().put(lineStr.length > 0 ? lineStr[0] : null, lineStr.length > 1 ? lineStr[1] : "");
                    }


                    String sat = parameters.getParamMap().get("SAT");
                    parameters.setSatProperty(new Property(sat));

                    String ini = parameters.getParamMap().get("INI");
                    parameters.setIniPath(new Property(file1.getAbsolutePath()));

                    parameters.fillIniValue(parameters.getParamMap());
                    parameters.checkFinalParameters();

                    this.singleHandle(ztCodLog);
                }
            }
            //自动,数据来源物联网服务器
            else {
                while (true) {

                    // 1 获取匹配成功的GTW
                    String lastIndexName = IndexUtils.ALIAS_TASK_LOG_ZT_GTW;
                    List<ZtGtwLog> taskLogs = (List<ZtGtwLog>) taskLogService.listByStatusAndMatchStatus(ZtGtwLog.class,
                            lastIndexName, Lists.newArrayList(ZtTaskStatus.GtwStatus.GTW_OVER.name()),
                            Lists.newArrayList(ZtTaskStatus.MatchStatus.MATCH_SUCCESS.name()),
                            null,
                            null,
                            500);

                    if (CollectionUtils.isEmpty(taskLogs)) {
                        noDataForceSuccess();
                        break;
                    }

                    String lastFileIndexName = IndexUtils.ALIAS_FILE_LOG_ZT_GTW;
                    List<TaskFileInfo> taskFileInfos = taskFileInfoService.listTaskFileInfos(lastFileIndexName, taskLogs.stream().map(TaskLog::getTaskId).collect(Collectors.toList()), Lists.newArrayList("GTW"), 500);

                    if (CollectionUtils.isEmpty(taskFileInfos)) {
                        noDataForceSuccess();
                        break;
                    }

                    List<String> ids = taskLogs.stream().map(ZtGtwLog::getSatId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());

                    for (String id : ids) {

                        //重置输出路径
                        parameters.getIniRows().stream().filter(x -> Objects.equals(ParamType.FILE_OUT, x.getType())).forEach(x -> x.setValue(null));

                        ZtCodLog ztCodLog = new ZtCodLog();
                        analyseTaskIds.add(ztCodLog.getTaskId());
                        ztCodLog.setExecuteMode(ExecuteMode.AUTO_FROM_IOT);

                        parameters.setWorkPath(new Property(taskExecutionContext.getExecutePath() + File.separator + ztCodLog.getTaskId()));
                        FileUtils.createWorkDirIfAbsent(parameters.getWorkPath().getValue());
                        this.handleParamMode();

                        ztCodLog.setSatId(id);
                        // 准备输入文件
                        String listPath = parameters.getWorkPath().getValue() + File.separator + id + ".LIST";
                        List<String> taskIds = taskLogs.stream().filter(x -> Objects.equals(x.getSatId(), id)).map(ZtGtwLog::getTaskId).collect(Collectors.toList());
                        List<TaskFileInfo> thisFiles = taskFileInfos.stream().filter(x -> taskIds.contains(x.getTaskId())).collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(thisFiles)) {
                            continue;
                        }
                        this.writeList(listPath, thisFiles);

                        parameters.setSatProperty(new Property(id));

                        parameters.getParamMap().put("SAT", id);
                        parameters.getParamMap().put("OBS", listPath);
                        if (!(MapUtils.isNotEmpty(parameters.getParamMap()) && parameters.getParamMap().containsKey("COE") && StringUtils.isNotBlank(parameters.getParamMap().get("COE")))) {
                            String coePath = parameters.getWorkPath().getValue() + File.separator + id + ".COE";
                            this.writeCoe(id, parameters.getWorkPath().getValue() + File.separator + id + ".COE");
                            parameters.getParamMap().put("COE", coePath);
                        }
                        String iniPath = parameters.getWorkPath().getValue() + File.separator + "COD.INI";
                        parameters.getParamMap().put("INI", iniPath);
                        parameters.setIniPath(new Property(iniPath));

                        parameters.fillIniValue(parameters.getParamMap());
                        this.writeIni();

                        parameters.checkFinalParameters();

                        this.singleHandle(ztCodLog);
                    }
                    taskLogService.updateStatusByTaskId(lastIndexName,
                            taskLogs.stream().map(TaskLog::getTaskId).collect(Collectors.toList()),
                            ZtTaskStatus.GtwStatus.COD_OVER.name(),
                            null
                    );

                }
            }
            try {
                this.analyseResult(analyseTaskIds);
            } catch (Exception e) {
                logger.error("cod analyseResult error, {}", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("The current Shell task has been interrupted", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("The current Shell task has been interrupted", e);
        } catch (Exception e) {
            logger.error("shell task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("Execute shell task error", e);
        }
    }

    @Override
    public void cancel() throws TaskException {
        // cancel process
        try {
            shellCommandExecutor.cancelApplication();
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

    /**
     * create command
     *
     * @return file name
     * @throws Exception exception
     */
    private String buildCommand() throws Exception {
        // generate scripts
        String fileName = String.format("%s/%s_node.%s",
                parameters.getWorkPath().getValue(),
                taskExecutionContext.getTaskAppId(), SystemUtils.IS_OS_WINDOWS ? "bat" : "sh");

        File file = new File(fileName);
        Path path = file.toPath();

        if (Files.exists(path)) {
            // this shouldn't happen
            logger.warn("The command file: {} is already exist", path);
            return fileName;
        }

        //${programPath} ${iniPath}
        String script = parameters.getRawScript()
                .replaceAll("\\r\\n", "\n")
                .replace("${programPath}", parameters.getProgramPath().getValue())
                .replace("${iniPath}", parameters.getIniPath().getValue());
        script = parseScript(script);
        parameters.setRawScript(script);


        logger.info("raw script : {}", parameters.getRawScript());
        logger.info("task execute path : {}", taskExecutionContext.getExecutePath());

        org.apache.dolphinscheduler.plugin.task.api.utils.FileUtils.createFileWith775(path);
        Files.write(path, parameters.getRawScript().getBytes(), StandardOpenOption.APPEND);

        return fileName;
    }

    @Override
    public AbstractParameters getParameters() {
        return parameters;
    }

    private String parseScript(String script) {
        // combining local and global parameters
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        return ParameterUtils.convertParameterPlaceholders(script, ParamUtils.convert(paramsMap));
    }

    /**
     * 单次处理
     *
     * @param ztCodLog
     * @throws Exception
     */
    private void singleHandle(ZtCodLog ztCodLog) throws Exception {
        // construct process
        String command = buildCommand();
        try {
            ztCodLog.setBeginTime(new Date());

            TaskResponse commandExecuteResult = shellCommandExecutor.run(command, ztCodLog.getTaskId());
            ztCodLog.setEndTime(new Date());
            ztCodLog.setExecuteTime(ztCodLog.getEndTime().getTime() - ztCodLog.getBeginTime().getTime());
            ztCodLog.setExecuteStatus(String.valueOf(commandExecuteResult.getExitStatusCode()));
            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setProcessId(commandExecuteResult.getProcessId());
            parameters.dealOutParam(shellCommandExecutor.getVarPool());

            ztCodLog.setProcessId(commandExecuteResult.getProcessId());
            ztCodLog.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
            ztCodLog.setExecuteStatement(parameters.getExecuteStatement());
            ztCodLog.setExecuteStatus(String.valueOf(commandExecuteResult.getExitStatusCode()));

            parameters.fillTaskFiles(parameters.getIniRows(), ztCodLog.getTaskId());
        } catch (Exception e) {
            ztCodLog.setExecuteStatus("-1");
            logger.error("cod exec error,{}", e);
        } finally {
            shellCommandExecutor.pkill("COD");
        }

        ztCodLog.setStatus(ZtTaskStatus.CodStatus.COD_OVER.name());

        taskLogService.logInsert(parameters.getTaskFileInfos(), ztCodLog);

    }

    private void writeCoe(String id, String filePath) {
        TleFromOut tleFromOut = tleService.getCoe(id);//todo
//        String content = "1 41744U          20180.18333333 0.00000018  00000+0  10000-2 0    02\n" +
//                "2 41744   0.5833  96.5431 0000983  10.0007 286.2296  1.00334564    08";
        String content = "";
        if (StringUtils.isNotBlank(tleFromOut.getOrbitRoots())) {
            content = tleFromOut.getOrbitRoots();
        } else {
            content = StringUtils.isNotBlank(tleFromOut.getTle()) ? tleFromOut.getTle() : tleFromOut.getCod();
        }
        org.apache.dolphinscheduler.common.utils.wtf.FileUtils.createFile(filePath, content);
//        org.apache.dolphinscheduler.common.utils.wtf.FileUtils.createFile(filePath, content);

    }

    private void writeList(String filePath, List<TaskFileInfo> files) {

        String listContent = files.stream()
                .map(x -> x.getFilePath())
                .collect(Collectors.joining(System.lineSeparator()));

        org.apache.dolphinscheduler.common.utils.wtf.FileUtils.createFile(filePath, listContent);
    }

    private void writeIni() {
        String content = parameters.getIniRows().stream().map(IniRow::toString).collect(Collectors.joining(System.lineSeparator()));
        org.apache.dolphinscheduler.common.utils.wtf.FileUtils.createFile(parameters.getParamMap().get("INI"), content);
    }

    private void noDataForceSuccess() {
        setExitStatusCode(0);
        setProcessId(taskExecutionContext.getProcessId());
    }

    @SneakyThrows
    public List<Tle> analyseResult(List<String> codTaskIds) {
        logger.error("分析cod结果");
        if (CollectionUtils.isEmpty(codTaskIds)) {
            return new ArrayList<>(0);
        }

        if (!elasticsearchService.exist(IndexUtils.ALIAS_FILE_LOG_ZT_COD)) {
            logger.error("index not exist:[{}]", IndexUtils.ALIAS_FILE_LOG_ZT_COD);
            return new ArrayList<>(0);
        }

        SearchResponse<TaskFileInfo> tleSearchResponse = elasticsearchClient.search(s -> s
                        .index(IndexUtils.ALIAS_FILE_LOG_ZT_COD)
                        .size(10000)
                        .query(q -> q.bool(b -> b
                                        .must(m -> m
                                                .term(t -> t.field("fileKind.keyword").value("TLE"))
                                        ).must(m -> m
                                                .terms(t -> t
                                                        .field("taskId")
                                                        .terms(tt -> tt.
                                                                value(codTaskIds.stream().map(x -> FieldValue.of(x)).collect(Collectors.toList()))
                                                        )
                                                )
                                        )
                                )
                        )
                , TaskFileInfo.class);

        List<Hit<TaskFileInfo>> tleHits = tleSearchResponse.hits().hits();

        List<Tle> tles = new ArrayList<>(tleHits.size());

        for (Hit<TaskFileInfo> outerHit : tleHits) {
            Tle tle = new Tle();
            TaskFileInfo fileInfo = outerHit.source();
            tle.setCodTime(fileInfo.getCreateTime());
            tle.setSource(TleSource.PMO);
            tle.setProgram(IndexUtils.ZT_COD);
            tle.setObjectFlag(Tle.ObjectFlag.OLD);
            tle.setTaskId(fileInfo.getTaskId());
            if (StringUtils.isNotBlank(fileInfo.getFileName()) && fileInfo.getFileName().contains(".")) {
                tle.setSatId(fileInfo.getFileName().substring(0, fileInfo.getFileName().indexOf(".")));
            }
            tles.add(tle);

            tle.setTle(fileInfo.getFileContent());

        }

        SearchResponse<TaskFileInfo> codSearchResponse = elasticsearchClient.search(s -> s
                        .index(IndexUtils.ALIAS_FILE_LOG_ZT_COD)
                        .size(10000)
                        .query(q -> q.bool(b -> b
                                        .must(m -> m
                                                .term(t -> t.field("fileKind.keyword").value("COD"))
                                        ).must(m -> m
                                                .terms(t -> t
                                                        .field("taskId")
                                                        .terms(tt -> tt.
                                                                value(codTaskIds.stream().map(x -> FieldValue.of(x)).collect(Collectors.toList()))
                                                        )
                                                )
                                        )
                                )
                        )
                , TaskFileInfo.class);

        List<Hit<TaskFileInfo>> codHits = codSearchResponse.hits().hits();
        for (Hit<TaskFileInfo> outerHit : codHits) {
            for (Tle tle : tles) {
                TaskFileInfo fileInfo = outerHit.source();
                if (tle.getTaskId().equals(fileInfo.getTaskId())) {
                    tle.setCod(fileInfo.getFileContent());
                }
            }
        }

        elasticsearchService.insert(IndexUtils.INDEX_TLE_TOTAL, tles);

        //分析dg报告,rpt文件入库
        SearchResponse<TaskFileInfo> rptSearchResponse = elasticsearchClient.search(s -> s
                        .index(IndexUtils.ALIAS_FILE_LOG_ZT_COD)
                        .size(10000)
                        .query(q -> q.bool(b -> b
                                        .must(m -> m
                                                .term(t -> t.field("fileKind.keyword").value("RPT"))
                                        ).must(m -> m
                                                .terms(t -> t
                                                        .field("taskId")
                                                        .terms(tt -> tt.
                                                                value(codTaskIds.stream().map(x -> FieldValue.of(x)).collect(Collectors.toList()))
                                                        )
                                                )
                                        )
                                )
                        )
                , TaskFileInfo.class);

        List<Hit<TaskFileInfo>> rptHits = rptSearchResponse.hits().hits();
        List<CodRpt> save = new ArrayList<>();
        for (Hit<TaskFileInfo> outerHit : rptHits) {
            TaskFileInfo fileInfo = outerHit.source();
            String fileContent = fileInfo.getFileContent();
            List<String> collect = Arrays.stream(fileContent.split("\n")).filter(x -> x.contains(".GTW")).collect(Collectors.toList());
            for (String s : collect) {
                if (s.length() < 156) {
                    s = s + Strings.repeat(" ", 156 - s.length());
                }
                CodRpt codRpt = ObjAndStrConverter.str2Obj(s, CodRpt.class);
                codRpt.setTaskId(fileInfo.getTaskId());
                codRpt.setFileId(fileInfo.getFileId());
                save.add(codRpt);
            }
        }
        elasticsearchService.insert(IndexUtils.INDEX_COD_RPT, save);

        return tles;
    }


}
