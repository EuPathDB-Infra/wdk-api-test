package test.support.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.gusdb.wdk.model.api.FilterValueSpec;
import org.gusdb.wdk.model.api.SearchConfig;
import org.gusdb.wdk.model.api.StepRequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class StepUtil {
  private static StepUtil instance;

  private StepUtil() {}

  public static final String BASE_PATH = UserUtil.BY_ID_PATH + "/steps";
  public static final String BY_ID_PATH = BASE_PATH + "/{stepId}";

  public static StepUtil getInstance() {
    if (instance == null) {
      instance = new StepUtil();
    }
    return instance;
  }
  
  public Response createValidStepResponse(RequestFactory requestFactory, String cookieId, SearchConfig searchConfig, String searchUrlSegment)
      throws JsonProcessingException {

    StepRequestBody step = new StepRequestBody(searchConfig, searchUrlSegment);

    return requestFactory.jsonPayloadRequest(step, HttpStatus.SC_OK, ContentType.JSON).request().cookie("JSESSIONID", cookieId).when().post(BASE_PATH,
        "current");
  }

  public SearchConfig createSearchConfigWithStepFilter(String filterName)
      throws JsonProcessingException {
    SearchConfig searchConfig = ReportUtil.createValidExonCountSearchConfig();

    // add filter to searchConfig
    FilterValueSpec filterSpec = new FilterValueSpec();
    filterSpec.setName(filterName);
    Map<String, Object> value = new HashMap<String, Object>();
    String[] matches = { "Y", "N" };
    value.put("values", matches);
    filterSpec.setValue(value);
    List<FilterValueSpec> filterSpecs = new ArrayList<FilterValueSpec>();
    filterSpecs.add(filterSpec);
    searchConfig.setFilters(filterSpecs);
    return searchConfig;
  }
  
  // this is a transform.  not allowed to have non-null step param
  public SearchConfig createValidOrthologsSearchConfig(RequestFactory requestFactory) throws JsonProcessingException {
    Map<String, String> paramsMap = new HashMap<String, String>();
    paramsMap.put("organism", "Plasmodium adleri G01");
    SearchConfig searchConfig = new SearchConfig();
    searchConfig.setParameters(paramsMap);
    return searchConfig;
  }

  // this is a transform.  not allowed to have non-null step param
  public SearchConfig createInvalidOrthologsSearchConfig(RequestFactory requestFactory, Long leafStepId) throws JsonProcessingException {
    SearchConfig searchConfig = createValidOrthologsSearchConfig(requestFactory);
    searchConfig.getParameters().put("gene_result", leafStepId.toString()); // naughty naughty
    return searchConfig;
  }

  public SearchConfig createValidBooleanSearchConfig(RequestFactory requestFactory) throws JsonProcessingException {
    Map<String, String> paramsMap = new HashMap<String, String>();
    paramsMap.put("Operator", "INTERSECT");
    SearchConfig searchConfig = new SearchConfig();
    searchConfig.setParameters(paramsMap);
    return searchConfig;
  }

  public Response getStep(long stepId, RequestFactory requestFactory, String cookieId, int expectedStatus)
      throws JsonProcessingException {
    return requestFactory.emptyRequest().cookie("JSESSIONID", cookieId).expect().statusCode(
        expectedStatus).when().get(BY_ID_PATH, "current", stepId);
  }

  public void deleteStep(long stepId, RequestFactory requestFactory, String cookieId, int expectedStatus)
      throws JsonProcessingException {

    requestFactory.emptyRequest().cookie("JSESSIONID", cookieId).expect().statusCode(
        expectedStatus).when().delete(BY_ID_PATH, "current", stepId);
  }  

}
