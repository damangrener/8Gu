package cn.ac.iscas.config.elasticsearch;

/**
 * @author WTF
 */

public enum ElasticsearchConnectMode {
    //
    HTTP,
    //NO.1
    HTTP_SUPERUSER,
    //
    HTTP_PASSWD,
    HTTPS_PASSWD,
    HTTPS_APIKEY,
    HTTPS_PASSWD_CRT,
    HTTPS_APIKEY_CRT

}
