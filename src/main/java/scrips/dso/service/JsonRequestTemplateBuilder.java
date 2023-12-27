package scrips.dso.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import scrips.dso.pojo.SearchRequest;

public class JsonRequestTemplateBuilder {
    public static String toJsonString(Object generic) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(generic);
    }
}
