package org.tkit.onecx.help.test;

import java.util.List;

import org.tkit.quarkus.security.test.AbstractSecurityTest;
import org.tkit.quarkus.security.test.SecurityTestConfig;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SecurityTest extends AbstractSecurityTest {
    @Override
    public SecurityTestConfig getConfig() {
        SecurityTestConfig config = new SecurityTestConfig();
        config.addConfig("read", "/internal/helps/id", 404, List.of("ocx-hp:read"), "get");
        config.addConfig("write", "/internal/helps", 400, List.of("ocx-hp:write"), "post");
        return config;
    }
}
