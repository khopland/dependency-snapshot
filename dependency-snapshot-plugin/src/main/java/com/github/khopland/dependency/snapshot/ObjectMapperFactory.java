package com.github.khopland.dependency.snapshot;


import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

final class ObjectMapperFactory {

    private ObjectMapperFactory() {
    }

    static ObjectMapper create() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }
}
