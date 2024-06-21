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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.dolphinscheduler.common.enums.zt.ParamMode;
import org.apache.dolphinscheduler.common.enums.zt.ZtTaskStatus;
import org.apache.dolphinscheduler.common.model.zt.*;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.wtf.IndexUtils;
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
import org.apache.dolphinscheduler.zt.service.service.PlanService;
import org.apache.dolphinscheduler.zt.service.service.SingleObjectDataService;
import org.apache.dolphinscheduler.zt.service.service.TaskLogService;
import org.apache.dolphinscheduler.zt.service.service.impl.TaskLogServiceImpl;
import org.springframework.beans.BeanUtils;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * shell task
 */
public class ZtPasTask extends AbstractTask {

    /**
     * shell parameters
     */
    private ZtPasParameters parameters;

    private static ThreadLocal<Tuple3<ZtPasParameters, ZtShellCommandExecutor, ZtPasLog>> threadLocalParams = ThreadLocal.withInitial(() -> null);
    private static ExecutorService executorService = Executors.newFixedThreadPool(16);

    /**
     * shell command executor
     */
    private ZtShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    private TaskLogService taskLogService;

    private SingleObjectDataService singleObjectDataService;

    private PlanService planService;


//    @Lazy
//    private ResourcesService resourcesService;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public ZtPasTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ZtShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                logger);

        this.taskLogService = BeanContext.getBean(TaskLogServiceImpl.class);
        this.singleObjectDataService = BeanContext.getBean(SingleObjectDataService.class);
        this.planService = BeanContext.getBean(PlanService.class);

    }

    @Override
    public void init() {
        logger.info("shell task params {}", taskExecutionContext.getTaskParams());

        parameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ZtPasParameters.class);

        this.parameters.setProgramPath(new Property(TaskUtils.getProgramPath("PAS")));

        if (!parameters.checkParameters()) {
            throw new RuntimeException("shell task params is not valid");
        }

        parameters.initIni();
        Map<String, String> map = new HashMap<>();
        parameters.setParamMap(map);
        parameters.setWorkPath(new Property(taskExecutionContext.getExecutePath()));
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
            }
        } else if (Objects.equals(parameters.getParamMode(), ParamMode.PARAM)) {
            handleProperty(parameters.getDataProperty());
            handleProperty(parameters.getTleProperty());
            handleProperty(parameters.getResultProperty());
        }
    }

    private void handleParamMode() {
        if (Objects.equals(threadLocalParams.get().getT1().getParamMode(), ParamMode.PARAM)) {
            handleProperty(threadLocalParams.get().getT1().getDataProperty());
            handleProperty(threadLocalParams.get().getT1().getTleProperty());
            handleProperty(threadLocalParams.get().getT1().getResultProperty());
        }
    }

    private void handleProperty(Property property) {
        if (Objects.nonNull(property) && StringUtils.isNotBlank(property.getValue())) {
            if (Objects.nonNull(property.getType()) && property.getType().name().equals("CONTENT")) {
                //todo mk file
                String path = threadLocalParams.get().getT1().getWorkPath().getValue() + File.separator + "temp." + property.getProp();
                org.apache.dolphinscheduler.common.utils.wtf.FileUtils.createFile(path, property.getValue());
                threadLocalParams.get().getT1().getParamMap().put(property.getProp(), path);
            } else {
                threadLocalParams.get().getT1().getParamMap().put(property.getProp(), property.getValue());
            }
        }
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {

            List<Future<?>> futures = new CopyOnWriteArrayList<>();
            List<TaskResponse> responses = new CopyOnWriteArrayList<>();

            List<ZtPasLog> logs = Collections.synchronizedList(new ArrayList<>());
            if (!parameters.getParamMap().containsKey("TLE") || ObjectUtils.isEmpty(parameters.getTleProperty()) || StringUtils.isBlank(parameters.getTleProperty().getValue())) {
                String tlePath = parameters.getWorkPath().getValue() + File.separator + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".tle";
//                tleService.writeTle(tlePath);
//                parameters.getParamMap().put("TLE", tlePath);
                //todo 暂时使用,自己写的tle执行全是match fail
                parameters.getParamMap().put("TLE", "/home/wtf/project/pmo/RJSTEST/zauxdata/20200701.tle");

            }
            List<String> successIds = Collections.synchronizedList(new ArrayList<>());

            //手动模式
            if (Objects.equals(parameters.getExecuteMode(), ExecuteMode.MANUAL)) {
                ZtPasParameters currentParameters = new ZtPasParameters(); // 每个线程使用独立的参数实例
                BeanUtils.copyProperties(parameters, currentParameters);
                ZtShellCommandExecutor executor = new ZtShellCommandExecutor(this::logHandle, taskExecutionContext, logger);
                ZtPasLog log = new ZtPasLog();

                Tuple3<ZtPasParameters, ZtShellCommandExecutor, ZtPasLog> tuple3 = Tuples.of(currentParameters, executor, log);
                threadLocalParams.set(tuple3);
                log.setExecuteMode(ExecuteMode.MANUAL);
                currentParameters.setWorkPath(new Property(taskExecutionContext.getExecutePath() + File.separator + log.getTaskId()));

                org.apache.dolphinscheduler.common.utils.wtf.FileUtils.mkdirs(currentParameters.getWorkPath().getValue());
                this.handleParamMode();

                this.singleHandle(tuple3);
                logs.add(log);
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
                File[] files = file.listFiles(x -> x.getName().endsWith(".PAS"));

                final ZtPasParameters temp = new ZtPasParameters();
                BeanUtils.copyProperties(parameters, temp);

                for (File file1 : files) {
                    Future<?> future = executorService.submit(() -> {
                        ZtPasParameters currentParameters = new ZtPasParameters(); // 每个线程使用独立的参数实例
                        BeanUtils.copyProperties(temp, currentParameters);
                        ZtShellCommandExecutor executor = new ZtShellCommandExecutor(this::logHandle, taskExecutionContext, logger);
                        ZtPasLog log = new ZtPasLog();

                        Tuple3<ZtPasParameters, ZtShellCommandExecutor, ZtPasLog> tuple3 = Tuples.of(currentParameters, executor, log);
                        threadLocalParams.set(tuple3);

                        log.setExecuteMode(ExecuteMode.AUTO_FROM_FOLDER);
                        log.setPasName(file1.getName());

                        String workPath = taskExecutionContext.getExecutePath() + File.separator + log.getTaskId();
                        currentParameters.setWorkPath(new Property(workPath));
                        currentParameters.setOutPath(new Property(workPath));

                        try {
                            FileUtils.createWorkDirIfAbsent(currentParameters.getWorkPath().getValue());
                        } catch (IOException e) {
                            logger.error("FileUtils.createWorkDirIfAbsent error", e);
                            throw new RuntimeException(e);
                        }

                        this.handleParamMode();

                        currentParameters.getParamMap().put("DATA", file1.getAbsolutePath());
                        currentParameters.getParamMap().put("RESULT", currentParameters.getWorkPath().getValue() + File.separator + "match.result");

                        // 每个线程独立初始化 List<IniRow>
                        List<IniRow> iniRows = currentParameters.initIni();
                        currentParameters.setIniRows(iniRows);

                        currentParameters.fillIniValue(currentParameters);

                        try {
                            responses.add(this.singleHandle(tuple3));
                        } catch (Exception e) {
                            logger.error("executorService.submit error", e);
                            throw new RuntimeException(e);
                        } finally {
                            logs.add(log);
                            threadLocalParams.remove();
                        }
                    });
                    futures.add(future);
                }
            }
            //自动,数据来源物联网服务器
            else if (Objects.equals(parameters.getExecuteMode(), ExecuteMode.AUTO_FROM_IOT)) {
//                while (true) {
                Integer handleSize = 500;
                if (MapUtils.isNotEmpty(parameters.getLocalParametersMap())) {
                    handleSize = Integer.valueOf(parameters.getLocalParametersMap().getOrDefault("size", new Property("10")).getValue());
                }
                List<SingleObjectData> singleObjectDatas = singleObjectDataService.selectByStatus(handleSize, SingleObjectData.Status.INIT);
                if (CollectionUtils.isEmpty(singleObjectDatas)) {
                    noDataForceSuccess();
//                        break;
                }

                final ZtPasParameters temp = new ZtPasParameters();
                BeanUtils.copyProperties(parameters, temp);

                for (SingleObjectData singleObjectData : singleObjectDatas) {
                    Future<?> future = executorService.submit(() -> {
                        ZtPasParameters currentParameters = new ZtPasParameters(); // 每个线程使用独立的参数实例
                        BeanUtils.copyProperties(temp, currentParameters);
                        ZtShellCommandExecutor executor = new ZtShellCommandExecutor(this::logHandle, taskExecutionContext, logger);
                        ZtPasLog log = new ZtPasLog();

                        Tuple3<ZtPasParameters, ZtShellCommandExecutor, ZtPasLog> tuple3 = Tuples.of(currentParameters, executor, log);
                        threadLocalParams.set(tuple3);

                        BeanUtils.copyProperties(singleObjectData, log);
                        log.setExecuteMode(ExecuteMode.AUTO_FROM_IOT);
                        log.setLastTaskId(singleObjectData.esId());
                        log.setPasName(singleObjectData.fileName());
                        log.setMeasureTime(singleObjectData.measureTime());
                        log.setMeasureBeginTime(singleObjectData.beginTime());
                        log.setMeasureEndTime(singleObjectData.endTime());
                        log.setTimeDifference(singleObjectData.timeDifference());

                        String workPath = taskExecutionContext.getExecutePath() + File.separator + log.getTaskId();
                        currentParameters.setWorkPath(new Property(workPath));
                        currentParameters.setOutPath(new Property(workPath));

                        try {
                            FileUtils.createWorkDirIfAbsent(currentParameters.getWorkPath().getValue());
                        } catch (IOException e) {
                            logger.error("FileUtils.createWorkDirIfAbsent error", e);
                            throw new RuntimeException(e);
                        }

                        this.handleParamMode();

                        currentParameters.getParamMap().put("DATA", singleObjectData.filePath());
                        currentParameters.getParamMap().put("RESULT", currentParameters.getWorkPath().getValue() + File.separator + "match.result");

                        // 每个线程独立初始化 List<IniRow>
                        List<IniRow> iniRows = currentParameters.initIni();
                        currentParameters.setIniRows(iniRows);

                        currentParameters.fillIniValue(currentParameters);

                        try {
                            responses.add(this.singleHandle(tuple3));
                        } catch (Exception e) {
                            logger.error("executorService.submit error", e);
                            throw new RuntimeException(e);
                        } finally {
                            logs.add(log);
                            if (log.getExecuteStatus().equals("0")) {
                                successIds.add(singleObjectData.esId());
                            }
                            threadLocalParams.remove();
                        }
                    });
                    futures.add(future);
                }

            }
            try {
                for (Future<?> future : futures) {
                    future.get();  // Wait for each submitted task to complete
                }

                if (CollectionUtils.isNotEmpty(successIds)) {
                    singleObjectDataService.updateStatusByIds(IndexUtils.INDEX_SINGLE_OBJECT_DATA, successIds, SingleObjectData.Status.PAS_OVER.name());
                }
                TaskExecutionContextCacheManager.removeByTaskInstanceId(taskRequest.getTaskInstanceId());
                ProcessUtils.kill(taskRequest);
                Runtime.getRuntime().exec("pkill PAS");

                analyseJh(logs);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error waiting for task completion", e);
                Thread.currentThread().interrupt();
                logger.error("The current Shell task has been interrupted", e);
                setExitStatusCode(EXIT_CODE_FAILURE);
                throw new TaskException("The current Shell task has been interrupted", e);
            } catch (Exception e) {
                logger.error("pas analyseJh error, {}", e);
            }
        } catch (Exception e) {
            logger.error("shell task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("Execute shell task error", e);
        } finally {
            //            result.setExitStatusCode(EXIT_CODE_KILL);

        }
    }

    private void writeIni() {
        String content = parameters.getIniRows().stream().map(IniRow::toString).collect(Collectors.joining(System.lineSeparator()));
        org.apache.dolphinscheduler.common.utils.wtf.FileUtils.createFile(parameters.getParamMap().get("INI"), content);
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
    private String buildCommand(ZtPasParameters currentParameters) throws Exception {
        // generate scripts
        String fileName = String.format("%s/%s_node.%s",
                currentParameters.getWorkPath().getValue(),
                taskExecutionContext.getTaskAppId(), SystemUtils.IS_OS_WINDOWS ? "bat" : "sh");

        File file = new File(fileName);
        Path path = file.toPath();

        if (Files.exists(path)) {
            // this shouldn't happen
            logger.warn("The command file: {} is already exist", path);
            return fileName;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(currentParameters.getProgramPath().getValue())
                .append(" ")
                .append(currentParameters.getIniRows().stream().filter(x -> Objects.equals(x.getCodeStr(), "DATA")).findFirst().get().getValue())
                .append(" ");
        sb.append(currentParameters.getIniRows().stream().filter(x -> Objects.equals(x.getCodeStr(), "TLE")).findFirst().get().getValue())
                .append(" ");
        sb.append(currentParameters.getIniRows().stream().filter(x -> Objects.equals(x.getCodeStr(), "RESULT")).findFirst().get().getValue());
        currentParameters.setExecuteStatement(sb.toString());
        currentParameters.setRawScript(sb.toString());

        logger.info("raw script : {}", currentParameters.getRawScript());
        logger.info("task execute path : {}", taskExecutionContext.getExecutePath());

        org.apache.dolphinscheduler.plugin.task.api.utils.FileUtils.createFileWith775(path);
        Files.write(path, currentParameters.getRawScript().getBytes(), StandardOpenOption.APPEND);

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
     * @throws Exception
     */
    private TaskResponse singleHandle(Tuple3<ZtPasParameters, ZtShellCommandExecutor, ZtPasLog> tuple3) throws Exception {
        // construct process
        String command = buildCommand(tuple3.getT1());

        tuple3.getT3().setBeginTime(new Date());
        TaskResponse commandExecuteResult = null;
        try {
            commandExecuteResult = tuple3.getT2().runByExecPath(command, tuple3.getT1().getWorkPath().getValue());
            tuple3.getT3().setEndTime(new Date());
            tuple3.getT3().setExecuteTime(tuple3.getT3().getEndTime().getTime() - tuple3.getT3().getBeginTime().getTime());
            tuple3.getT3().setExecuteStatus(String.valueOf(commandExecuteResult.getExitStatusCode()));
            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setProcessId(commandExecuteResult.getProcessId());
            tuple3.getT1().dealOutParam(tuple3.getT2().getVarPool());

            tuple3.getT3().setProcessId(commandExecuteResult.getProcessId());
            tuple3.getT3().setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
            tuple3.getT3().setExecuteStatement(tuple3.getT1().getExecuteStatement());
            tuple3.getT3().setExecuteStatus(String.valueOf(commandExecuteResult.getExitStatusCode()));
        } catch (Exception e) {
            logger.error("runByExecPath error,ztPasLogId={}", tuple3.getT3().getTaskId());
        }

        tuple3.getT1().fillTaskFiles(tuple3.getT1(), tuple3.getT3().getTaskId());
        if (tuple3.getT1().getTaskFileInfos().stream().anyMatch(x -> Objects.equals("DATA", x.getFileKind()))) {
            tuple3.getT3().setPasName(tuple3.getT1().getTaskFileInfos().stream().filter(x -> Objects.equals("DATA", x.getFileKind())).findFirst().get().getFileName());
            String[] fileNameArr = tuple3.getT3().getPasName().split("_");
            try {
                Date parseDate = DateUtils.parseDate(fileNameArr[0], "yyyyMMddHHmmss", "yyyyMMdd");
                tuple3.getT3().setMeasureTime(parseDate);
                tuple3.getT3().setBeginTime(parseDate);
                tuple3.getT3().setStaId(fileNameArr[1]);
                tuple3.getT3().setSatId(Integer.parseInt(fileNameArr[2]));
                tuple3.getT3().setSerialNum(fileNameArr[3].replace(".PAS", ""));
            } catch (ParseException e) {
                logger.error("set ztPasLog error,ztPasLogId={}", tuple3.getT3().getTaskId());
            }

        }
        if (tuple3.getT1().getTaskFileInfos().stream().anyMatch(x -> Objects.equals("RESULT", x.getFileKind()))) {
            String resultPath = tuple3.getT1().getTaskFileInfos().stream().filter(x -> x.getFileKind().equals("RESULT")).findFirst().get().getFilePath();
            tuple3.getT3().setMatchStatus(analyseMatchResult(resultPath));
            tuple3.getT3().setStatus(ZtTaskStatus.PasStatus.PAS_OVER.name());
        }

        taskLogService.logInsert(threadLocalParams.get().getT1().getTaskFileInfos(), tuple3.getT3());
        return commandExecuteResult;
    }

    /**
     * 分析Match.result,更新
     */
    private String analyseMatchResult(String path) {
        String content = org.apache.dolphinscheduler.common.utils.wtf.FileUtils.readFile(path);
        if (StringUtils.isBlank(content)) {
            return "MATCH_FAIL";
        }
        // /home/wtf/nanjing/RJSTEST/TEST/PAS/1002/20200701144900_1002_108376_0000.PAS      108375      342.77      167.59
        if (content.contains("match fail") || content.contains("     0")) {
            return "MATCH_FAIL";
        } else {
            return "MATCH_SUCCESS";
        }

    }

    //计划中目标数量
    //非计划中目标数量

    /**
     * 和计划比对,标记数据是否是计划中的
     *
     * @param logs
     */
    private void analyseJh(List<ZtPasLog> logs) {
        if (CollectionUtils.isEmpty(logs) || logs.stream().noneMatch(x -> Objects.equals(x.getMatchStatus(), ZtTaskStatus.MatchStatus.MATCH_SUCCESS.name()))) {
            return;
        }
        List<ZtPasLog> successes = logs.stream().filter(x -> Objects.equals(x.getMatchStatus(), ZtTaskStatus.MatchStatus.MATCH_SUCCESS.name())).collect(Collectors.toList());
        List<String> satIds = successes.stream().map(ZtPasLog::getSatId).map(String::valueOf).collect(Collectors.toList());
        List<String> staIds = successes.stream().map(ZtPasLog::getStaId).map(String::valueOf).collect(Collectors.toList());

        Date measureTime = successes.stream().filter(x -> ObjectUtils.isNotEmpty(x.getMeasureTime())).max(Comparator.comparingLong(x -> x.getMeasureTime().getTime())).get().getMeasureTime();
        LocalDateTime beginTime = measureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
        LocalDateTime endTime = measureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(LocalTime.MAX);

        Page<JhjContent> page = planService.listJhj(1, 10000, beginTime, endTime, satIds, staIds);
        if (null != page && CollectionUtils.isNotEmpty(page.getRecords())) {
            for (JhjContent jhjContent : page.getRecords()) {
                for (ZtPasLog success : successes) {
                    if (success.getMeasureTime().getTime() > jhjContent.getBeginTime().getTime()
                            && success.getMeasureTime().getTime() > jhjContent.getEndTime().getTime()
                            && success.getSatId().equals(jhjContent.getSatId())
                            && success.getStaId().equals(jhjContent.getStaId())) {
                        success.setIsJh(1);
                        success.setJhjId(jhjContent.getId());
                    } else {
                        success.setIsJh(0);
                    }
                }
            }
        }
    }

    private void noDataForceSuccess() {
        setExitStatusCode(0);
        setProcessId(taskExecutionContext.getProcessId());
    }

}
