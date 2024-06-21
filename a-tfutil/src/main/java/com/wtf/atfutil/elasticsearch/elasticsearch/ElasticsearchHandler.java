package cn.ac.iscas.config.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;



/**
 *
 * @author WTF
 * 链接es集群
 */
@Component
public class ElasticsearchHandler {

    @Value("${elasticsearch.ip}")
    private String ip;
    @Value("${elasticsearch.port}")
    private Integer port;
    @Value("${elasticsearch.connect_mode}")
    private String connectMode;
    @Value("${elasticsearch.superuser.name}")
    private String superUserName;
    @Value("${elasticsearch.superuser.password}")
    private String superUserPassword;

    private ElasticsearchClient client;

    @Autowired
    private ElasticsearchInterceptor elasticsearchInterceptor;

    /**
     * @resource https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/8.1/_basic_authentication.html
     */
//    @PostConstruct
    @Bean
    public ElasticsearchClient init() {
        try {

            RestClientBuilder restClientBuilder = RestClient
                    .builder(new HttpHost(ip, port));
            // Create the low-level client
            RestClient restClient = null;
            switch (ElasticsearchConnectMode.valueOf(connectMode)) {
                case HTTP:
                    restClient = connectByHttp(restClientBuilder);
                    break;
                case HTTP_SUPERUSER:
                    restClient = this.connectByHttpSuperuser(restClientBuilder);
                    break;
                default:
                    break;
            }
            // Create the transport with a Jackson mapper
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());
            // And create the API client
            client = new ElasticsearchClient(transport);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }

    private RestClient connectByHttpSuperuser(RestClientBuilder builder) {

        //用户名密码认证
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(superUserName, superUserPassword));

        return builder
                .setDefaultHeaders(new Header[]{new BasicHeader("X-Elastic-Product", "Elasticsearch")})
                .setHttpClientConfigCallback(x -> x
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .addInterceptorLast(elasticsearchInterceptor)
                        )
                .build();
    }

    private RestClient connectByHttp(RestClientBuilder builder) {

        return builder
                .setDefaultHeaders(new Header[]{new BasicHeader("X-Elastic-Product", "Elasticsearch")})
                .setHttpClientConfigCallback(x -> x
                        .addInterceptorLast(elasticsearchInterceptor))
                .build();
    }

    public boolean close() {
        if (client != null) {
//            client.close();
        }
        return true;
    }

    public ElasticsearchClient getClient() {
        return client;
    }

}
