package com.trademesh;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

/**
 * Simple GraphQL API boilerplate provided by Quarkus.
 */
@GraphQLApi
public class HelloGraphQLResource {

    /**
     * Responds with a hello greeting via GraphQL query.
     * @param name Name to greet.
     * @return Greeting text.
     */
    @Query
    @Description("Say hello")
    public String sayHello(@DefaultValue("World") String name) {
        return "Hello " + name;
    }
}