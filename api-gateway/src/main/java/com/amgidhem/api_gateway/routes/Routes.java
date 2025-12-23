package com.amgidhem.api_gateway.routes;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.*;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.*;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class Routes {

    @Bean
    public RouterFunction<ServerResponse> customRoutes() {
        return route("product_service")
                .route(path("/api/product/**"), http())
                .before(uri("http://localhost:8080"))
                .build()
                .and(route("order_service")
                        .route(path("/api/order/**"), http())
                        .before(uri("http://localhost:8081"))
                        .build()
                        .and(route("inventory_service")
                                .route(path("/api/inventory/**"), http())
                                .before(uri("http://localhost:8082"))
                                .build()));
    }
}