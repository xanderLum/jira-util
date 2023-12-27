package scrips.dso.controller;


//import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import scrips.dso.service.JiraService;

@RestController
@RequestMapping("/jira")
public class JiraController {
    private final JiraService jiraService;
    public static final Logger log = LoggerFactory.getLogger(JiraController.class);

    public JiraController(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @PostMapping("/update-issue-status/{issueKey}/{newStatus}")
    public void updateIssueStatus(@PathVariable String issueKey, @PathVariable String newStatus) {
        System.out.println("IssueKey: " + issueKey);
        System.out.println("NewStatus: " + newStatus);

        log.info("[Service Invoke START] /update-issue-status/{"+issueKey+"}/{"+newStatus+"}");
        jiraService.updateIssueStatus(issueKey, newStatus);
        log.info("[Service Invoke END] /update-issue-status/{"+issueKey+"}/{"+newStatus+"}");
    }

    @GetMapping("/project")
    public void getAllProjects() {
        System.out.println("Getting all projects...");
        log.info("[Service Invoke START] /project");

        try {
            jiraService.getAllProjects();
        } catch (JsonProcessingException e) {
            log.trace(""+ new RuntimeException(e));
            throw new RuntimeException(e);
        }
        log.info("[Service Invoke END] /project");

    }

    @GetMapping("/getIssue/{issueKey}")
    public void getIssue(@PathVariable String issueKey) {
        System.out.println("Getting issue...");
        log.info("[Service Invoke START] /getIssue/{"+issueKey+"}");

        try {
            jiraService.getIssue(issueKey);
        } catch (JsonProcessingException e) {
            log.trace(""+ new RuntimeException(e));
            throw new RuntimeException(e);
        }
        log.info("[Service Invoke END] /getIssue/{"+issueKey+"}");
    }

    @GetMapping("/findJiraIDBy/{testCaseId}/{testPhase}/{releaseName}/{env}")
    public void findJiraIDByTestCaseIdNTestPhaseNReleaseNEnv(@PathVariable String testCaseId, @PathVariable String testPhase, @PathVariable String releaseName, @PathVariable String env) {
        log.info("[Service Invoke START] /findJiraIDBy/{"+testCaseId+"}/{"+testPhase+"}/{"+releaseName+"}/{"+env+"}");

        try {
            jiraService.findJiraIDByTestCaseIdNTestPhaseNReleaseNEnv(testCaseId, testPhase, releaseName, env);
        } catch (JsonProcessingException e) {
            log.trace(""+ new RuntimeException(e));
            throw new RuntimeException(e);
        }
        log.info("[Service Invoke END] /findJiraIDBy/{"+testCaseId+"}/{"+testPhase+"}/{"+releaseName+"}/{"+env+"}");
    }

    @GetMapping("/findTestCases")
    public void findTestCases() {
        log.info("[Service Invoke START] /findTestCases");

        try {
            jiraService.findTestCases();
        } catch (Exception e) {
            log.trace(""+ new RuntimeException(e));
            throw new RuntimeException(e);
        }
        log.info("[Service Invoke END] /findTestCases");
    }
}

