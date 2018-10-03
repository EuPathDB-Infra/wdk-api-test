package test.wdk.users;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.gusdb.wdk.model.api.AnswerSpec;
import org.gusdb.wdk.model.api.Step;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import test.support.Category;
import test.support.util.AuthUtil;
import test.support.util.AuthenticatedRequestFactory;
import test.support.util.GuestRequestFactory;
import test.support.util.RequestFactory;
import test.wdk.UsersTest;

@DisplayName("Steps")
public class StepsTest extends UsersTest {
  public static final String BASE_PATH = UsersTest.BY_ID_PATH + "/steps";
  public static final String BY_ID_PATH = BASE_PATH + "/{stepId}";

  protected final AuthUtil _authUtil;
  private GuestRequestFactory _guestRequestFactory;

  public StepsTest(AuthUtil auth, AuthenticatedRequestFactory authReqFactory, GuestRequestFactory guestReqFactory ) {
    super(authReqFactory);
    this._authUtil = auth;
    _guestRequestFactory = guestReqFactory;
  }
  
  @Test
  @Tag (Category.PLASMO_TEST)
  @DisplayName("Create and delete a guest step")
  void createAndDeleteGuestStep() throws JsonProcessingException {
    
    Response stepResponse = createExonCountStep(_guestRequestFactory);
    long stepId = stepResponse.body().jsonPath().getLong("ID"); // TODO: use JsonKeys
    String cookieId = stepResponse.getCookie("JSESSIONID");

    // delete the step
    deleteStep(stepId, _guestRequestFactory, cookieId, HttpStatus.SC_NO_CONTENT);

    // deleting again should get a not found
    deleteStep(stepId, _guestRequestFactory, cookieId, HttpStatus.SC_NOT_FOUND);
  }
  
  private Response createExonCountStep(RequestFactory requestFactory) throws JsonProcessingException {
    AnswerSpec answerSpec = new AnswerSpec("GeneQuestions.GenesByExonCount");
    Map<String, String> paramsMap = new HashMap<String, String>();
    paramsMap.put("organism", "Plasmodium adleri G01");
    paramsMap.put("num_exons_gte", "6");
    paramsMap.put("num_exons_lte", "7");
    answerSpec.setParameters(paramsMap);
    Step step = new Step(answerSpec);
    
    return requestFactory.jsonPayloadRequest(step, HttpStatus.SC_OK, ContentType.JSON)
        .when()
        .post(BASE_PATH, "current");    
  }
  
  private void deleteStep(long stepId, RequestFactory requestFactory, String cookieId, int expectedStatus) throws JsonProcessingException {

    requestFactory.emptyRequest()
    .cookie("JSESSIONID", cookieId)
    .expect()
    .statusCode(expectedStatus)
    .when()
    .delete(BY_ID_PATH, stepId); 
  }
}
