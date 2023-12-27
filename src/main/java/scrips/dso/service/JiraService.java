package scrips.dso.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import scrips.dso.config.Variables;
import scrips.dso.controller.JiraController;
import scrips.dso.pojo.SearchRequest;

import javax.management.ObjectName;
import java.nio.charset.StandardCharsets;
//import java.util.Base64;

@Service
public class JiraService {
    @Value("${jira.url}")
    private String jiraUrl;

    @Value("${jira.api.basepath}")
    private String jiraApiBasePath;
    @Value("${jira.api.version}")
    private String jiraApiVersion;

    @Value("${jira.username}")
    private String jiraUsername;

    @Value("${jira.api-token}")
    private String jiraApiToken;

    @Value("${jira.project.tc.master}")
    private String masterProject;
    @Value("${jira.project.tc.exec}")
    private String tcExecProject;

    private final String STR_PROJECT = "project";
    private final String OPR_AND = "AND";
    private final String STR_ISSUETYPE = "issueType";
    private final String STR_TESTCASE = "Test Case";
    private final String STR_TESTCASEID = "Test Case ID";
    private final String STR_RELVER = "Release Version";
    private final String STR_TESTENV = "Test Environment";

    private final RestTemplate restTemplate;

    public static final Logger log = LoggerFactory.getLogger(JiraController.class);

    public JiraService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    /*public JiraService(RestTemplate restTemplate, RequestCallback requestCallback) {
        this.restTemplate = restTemplate;
        this.requestCallback = requestCallback;
    }*/

    /*private String getJiraBasicAuth(){
        // Encode the credentials
        System.out.println("Jira Username: "+jiraUsername);
        System.out.println("Jira API token: "+jiraApiToken);
        String credentials = jiraUsername + ":" + jiraApiToken;
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        System.out.println("Base64creds: "+base64Credentials);
        return base64Credentials;
    }*/

    public void updateIssueStatus(String issueKey, String newStatus) {
        String apiUrl = jiraUrl + "/issue/" + issueKey + "/transitions";
        HttpHeaders headers = createHeaders();

        // Define the transition JSON payload
        String transitionPayload = "{\"transition\": {\"id\": \"" + newStatus + "\"}}";

        HttpEntity<String> requestEntity = new HttpEntity<>(transitionPayload, headers);
        invokeJira(apiUrl, HttpMethod.POST, requestEntity);

        /*ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            System.out.println("Issue status updated successfully.");
        } else {
            System.err.println("Error updating issue status: " + responseEntity.getBody());
        }*/
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(jiraUsername, jiraApiToken);
        headers.set("Content-Type", "application/json");
//        headers.set(HttpHeaders.AUTHORIZATION, "Basic" + getJiraBasicAuth());
        return headers;
    }

    private String buildJIRAApiURL() {
        return jiraUrl + jiraApiBasePath + jiraApiVersion;
    }

    private ResponseEntity invokeJira(String apiUrl, HttpMethod method, HttpEntity requestEntity) {
        log.info("[API Invoke START] "+ apiUrl);

        System.out.println("JIRA API URL invoked: " + apiUrl);

        RestOperations restOperations = rest(new RestTemplateBuilder());
        ResponseEntity<String> responseEntity = restOperations.exchange(apiUrl, method, requestEntity, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("[API Response SUCCESS] "+ responseEntity.getBody());
            System.out.println("Projects retrieved: " + responseEntity.getBody());
        } else {
            log.error("[API Response FAILED] "+ responseEntity.getBody());
            System.err.println("[API Response] " + responseEntity.getBody());
        }

        log.info("[API Invoke END] "+ apiUrl);

        return responseEntity;
    }

    private RestOperations rest(RestTemplateBuilder restTemplateBuilder) {
        log.info("[REST Template builder] " + "Username: " + jiraUsername + "\t Token: " + jiraApiToken);
        return restTemplateBuilder.basicAuthentication(jiraUsername, jiraApiToken).build();
    }

    /*private ResponseEntity invokeGETJira(String apiUrl, HttpEntity requestEntity) throws JsonProcessingException {
//        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, method, requestEntity, String.class);
        System.out.println("JIRA API URL invoked: " + apiUrl);
        RestOperations restOperations = rest(new RestTemplateBuilder());
        ResponseEntity<String> responseEntity = restOperations.getForEntity(apiUrl, String.class);

//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl, String.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            //Parse the JSON response using Jackson
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                // Extract information from the JSON response
                String issueKey = jsonNode.get("key").asText();
                String summary = jsonNode.get("fields").get("summary").asText();

                // Print the extracted information
                System.out.println("Issue Key: " + issueKey);
                System.out.println("Summary: " + summary);
            } catch (Exception e) {
                e.printStackTrace();
                //            System.out.println(""+e.printStackTrace());
            }


            System.out.println("Projects retrieved: " + responseEntity.getBody());
            System.out.printf("Response body: " + responseEntity.toString());
        } else {
            System.err.println("Error updating issue status: " + responseEntity.getBody());
        }

        return responseEntity;
    }*/

    public void getAllProjects() throws JsonProcessingException {
        String apiUrl = buildJIRAApiURL() + "/project";
//        https://kidgenius.atlassian.net/rest/api/3
//        String apiUrl = "https://kidgenius.atlassian.net/rest/api/3/project";
        HttpHeaders headers = createHeaders();

        // Define the transition JSON payload
//        String transitionPayload = "{\"transition\": {\"id\": \"" + newStatus + "\"}}";

        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        invokeJira(apiUrl, HttpMethod.GET, requestEntity);
//        invokeGETJira(apiUrl, requestEntity);
    }

    public void getIssue(String issueKey) throws JsonProcessingException {
        String apiUrl = buildJIRAApiURL() + "/issue/" + issueKey;
        HttpHeaders headers = createHeaders();
        System.out.println("Getting Issue key details: " + issueKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        System.out.println("RequestEntity: [ "+requestEntity.toString()+ " ]");
        invokeJira(apiUrl, HttpMethod.GET, requestEntity);
    }

    public void findJiraIDByTestCaseIdNTestPhaseNReleaseNEnv(String testCaseId, String testPhase, String releaseVersion, String testEnv) throws JsonProcessingException {
        log.info("[Method Invoke START] findJiraIDByTestCaseIdNTestPhaseNReleaseNEnv");

        String apiUrl = buildJIRAApiURL() + "/search";
        SearchRequest sr = buildSearchRequest(testCaseId, testPhase, releaseVersion, testEnv);


        HttpEntity<String> requestEntity = new HttpEntity<>(sr.toString(), createHeaders());
        System.out.println("RequestEntity: [ "+requestEntity.toString()+ " ]");
        invokeJira(apiUrl, HttpMethod.POST, requestEntity);
//        invokeGETJira(apiUrl, requestEntity);
    }

    private SearchRequest buildSearchRequest(String testCaseId, String testPhase, String releaseVersion, String testEnv){
        //String jql = "project = \"%s\" AND issuetype = \"%s\" And \"Test Case ID\" ~  \"OSP-US-11-TS-008-TC-024\"  AND \"Release Version\"  ~ AND \"Test Environment\" ~";
        String jqlTemplate = "%s = \"%s\" AND issuetype = \"%s\" AND \"%s\" ~ \"%s\" AND \"%s\" AND \"%s\" ~ AND \"%s\" ~ \"%s\"";

        String jql = String.format(jqlTemplate, STR_PROJECT, tcExecProject, STR_TESTCASE, STR_TESTCASEID, testCaseId, STR_RELVER, releaseVersion, STR_TESTENV, testEnv);
        log.info("JQL Query: [%s]", jql);
        SearchRequest sr = new SearchRequest();
        sr.setJql(jql);
        return sr;
    }

    private SearchRequest testCopy(){
        String jql = "project = SCRIPS AND issuetype = Test Case AND status = No Run AND Test Case ID is not EMPTY";
        String jqlTemplate = "project = '%s' AND issuetype = '%s' AND status = '%s' AND 'Test Case ID' is not EMPTY";

        String finalStr = String.format(jqlTemplate, "SCRIPS", "Test Case", "No Run");
        SearchRequest sr = new SearchRequest();
        sr.setJql(finalStr);
        sr.setMaxResults(5);
        return sr;
    }

    public void findTestCases() {
        log.info("[Method Invoke START] findTestCases");

        String apiUrl = buildJIRAApiURL() + "/search";
        SearchRequest sr = testCopy();
        try {
            log.info(("[REQUEST JSON]: "+ JsonRequestTemplateBuilder.toJsonString(sr)));
            HttpEntity<String> requestEntity = new HttpEntity<>(JsonRequestTemplateBuilder.toJsonString(sr), createHeaders());
            ResponseEntity response = invokeJira(apiUrl, HttpMethod.POST, requestEntity);
            log.info("[RESPONSE JSON] "+ JsonRequestTemplateBuilder.toJsonString(response.getBody()));

            //Parse the JSON String
            ObjectMapper obj = new ObjectMapper();
            JsonNode rootNode = obj.readTree(JsonRequestTemplateBuilder.toJsonString(response.getBody()));

            //Identify the parent node ("issue)
            ObjectNode issueNode = (ObjectNode) rootNode.path("issue");

            //Inject New Fields into the parent node
            issueNode.put(Variables.TEST_PHASE, "SampleTestPhase");
            issueNode.put(Variables.TEST_ENVIRONMENT, "SIT");
            issueNode.put(Variables.RELEASE_VERSION, "SampleReleaseVersion");

            String updatedJsonResponse = obj.writeValueAsString(issueNode);
            log.info("[REQUEST JSON to CREATE BULK ISSUES] "+updatedJsonResponse);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException: "+e.getMessage());
        }catch (Exception e){
            log.error("General Exception Found: "+e.getMessage());
        }


    }
}
