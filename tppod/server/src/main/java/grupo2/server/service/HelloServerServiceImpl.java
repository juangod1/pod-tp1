package grupo2.server.service;

import grupo2.api.HelloServerService;
import grupo2.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloServerServiceImpl implements HelloServerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    @Override
    public String hello(String name) {
        LOGGER.info("{} said hi!", name);
        return "Hello, " + name + "!";
    }
}
