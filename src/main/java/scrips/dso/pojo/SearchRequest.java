package scrips.dso.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "jql",
        "startAt",
        "maxResults",
        "fields"
})
public class SearchRequest {

    @JsonProperty("jql")
    private String jql;

    @JsonProperty("startAt")
    private Integer startAt;

    @JsonProperty("maxResults")
    private Integer maxResults;

    @JsonProperty("fields")
    private List<String> fields;

    public String getJql() {
        return jql;
    }

    public void setJql(String jql) {
        this.jql = jql;
    }

    public Integer getStartAt() {
        return startAt;
    }

    public void setStartAt(Integer startAt) {
        this.startAt = startAt;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}
