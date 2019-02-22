package com.alibaba.pelican.nacos;

import com.alibaba.pelican.chaos.client.impl.RemoteCmdClient;
import com.alibaba.pelican.chaos.client.utils.NetAccessUtils;
import com.alibaba.pelican.deployment.junit.AbstractJUnit4PelicanTests;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * @author moyun@middleware
 */

@Slf4j
public class TestNacosNetwork extends AbstractJUnit4PelicanTests {

    private String ip;

    private String userName;

    private String password;

    {
        Map<String, String> params = this.getTestProject().getVariables();
        ip = params.get("ip");
        userName = params.get("userName");
        password = params.get("password");
    }

    @Test
    public void test() throws Exception {

        Response response = given().put(String.format("http://%s:8848/nacos/v1/ns/instance?serviceName=nacos.naming.serviceName&ip=20.18.7.10&port=8080", ip));
        response.print();

        response = given()
                .get(String.format("http://%s:8848/nacos/v1/ns/instances?serviceName=nacos.naming.serviceName", ip));
        response.print();

        RemoteCmdClient client = new RemoteCmdClient(ip, userName, password);
        NetAccessUtils.blockPortProtocol(client, "8848", "TCP", 20);

        try {
            response = given()
                    .get(String.format("http://%s:8848/nacos/v1/ns/instances?serviceName=nacos.naming.serviceName", ip));
            response.print();
        } catch (Exception e) {
            String message = ExceptionUtils.getMessage(e);
            if (message.contains("Operation timed out (Connection timed out)")) {
                log.info("Operation timed out (Connection timed out)");
            }
        } finally {
            NetAccessUtils.clearAll(client);
        }

        response = given()
                .get(String.format("http://%s:8848/nacos/v1/ns/instances?serviceName=nacos.naming.serviceName", ip));
        response.print();
    }

}
