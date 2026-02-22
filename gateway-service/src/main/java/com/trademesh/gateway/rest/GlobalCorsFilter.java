package com.trademesh.gateway.rest;

import io.quarkus.vertx.http.runtime.filters.Filters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class GlobalCorsFilter {

    public void filters(@Observes Filters filters) {
        filters.register(rc -> {
            rc.response().putHeader("Access-Control-Allow-Origin", "*");
            rc.response().putHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            rc.response().putHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, x-requested-with");
            rc.response().putHeader("Access-Control-Allow-Credentials", "true");
            
            if (rc.request().method().name().equals("OPTIONS")) {
                rc.response().setStatusCode(200).end();
            } else {
                rc.next();
            }
        }, 100);
    }
}
