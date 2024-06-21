package cn.ac.iscas.config.elasticsearch;

import cn.ac.iscas.util.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * @Description ES HTTP拦截器
 * @Author WTF
 * @Date 2022/5/18 14:35
 */
@Slf4j
@Component
public class ElasticsearchInterceptor implements HttpRequestInterceptor {

    @Value("${elasticsearch.logType}")
    private String logType;

    @Override
    public void process(HttpRequest request, HttpContext context) throws IOException {

        try {

            if (StringUtils.isNotBlank(logType) && !logType.equals("none")) {
                StringBuilder sb = new StringBuilder();

                sb.append(request.getRequestLine().getMethod());
                sb.append(" ");
                sb.append(request.getRequestLine().getUri());

                HttpEntity entity = (HttpEntity) ReflectUtils.getFieldValue(request, "entity");

                if (null != entity && entity.getContent().available() > 0) {
                    sb.append("\n");
                    String s = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining(System.lineSeparator()));
                    sb.append(s);
                }

                String[] split = logType.split(",");
                for (String s : split) {
                    if (sb.toString().contains(s)) {
                        log.debug("ES DSL = {}", sb);
                    }
                }
            }
        }catch (Exception e){

        }

    }

}
