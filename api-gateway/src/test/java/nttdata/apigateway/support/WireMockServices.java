package nttdata.apigateway.support;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public final class WireMockServices {

    public static final WireMockServer USER = new WireMockServer(wireMockConfig().dynamicPort());
    public static final WireMockServer MENU = new WireMockServer(wireMockConfig().dynamicPort());
    public static final WireMockServer ORDER = new WireMockServer(wireMockConfig().dynamicPort());

    static {
        USER.start();
        MENU.start();
        ORDER.start();
    }

    private WireMockServices() {
    }

    public static void resetAll() {
        USER.resetAll();
        MENU.resetAll();
        ORDER.resetAll();
    }

    public static void stopAll() {
        USER.stop();
        MENU.stop();
        ORDER.stop();
    }
}
