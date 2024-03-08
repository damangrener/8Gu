
import okhttp3.*;
import org.json.JSONObject;

import java.io.*;


/**
 * 需要添加依赖
 * <!-- https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp -->
 * <dependency>
 *     <groupId>com.squareup.okhttp3</groupId>
 *     <artifactId>okhttp</artifactId>
 *     <version>4.12.0</version>
 * </dependency>
 */

class App {
    public static final String API_KEY = "ArrmOaysYA6nSd9O2HbqH8j7";
    public static final String SECRET_KEY = "UZggSkmiI5ihSkG3cb6RtwKTThto5L6E";

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public static void main(String []args) throws IOException{
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"messages\":[{\"role\":\"user\",\"content\":\"12345\"},{\"role\":\"assistant\",\"content\":\"12345 是一个数字序列，通常用于表示一个数列或者是一个账号、密码等需要数字组合的场合。这个数字组合可以被看作是一个字符串，也可以被看作是一个数值。\\n\\n如果将 12345 作为一个数值来考虑，它是一个自然数，可以参与各种数学运算。例如，它可以被加、减、乘、除，也可以用来表示其他数学表达式。\\n\\n如果将 12345 作为一个字符串来考虑，它可以被用来作为用户名、密码、车牌号、产品序列号、门牌号等需要数字组合的场合。在这种情况下，每个数字可能代表特定的含义或者用于识别特定的信息。\\n\\n在不同的上下文中，12345 可能有不同的含义。例如，在金融交易中，它可能是一个账户号码；在教育系统中，它可能是一个学号；在电子商务中，它可能是一个订单号码；在安全领域，它可能是一个密码。\\n\\n总之，12345 是一个数字组合，可以有多种用途，具体取决于它出现的上下文。\"}]}");
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/yi_34b_chat?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        System.out.println(response.body().string());

    }


    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    static String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }

}