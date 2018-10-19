package test.wdk;

import static test.support.Conf.SERVICE_PATH;

import org.apache.http.HttpStatus;
import org.gusdb.wdk.model.api.AnswerSpec;
import org.gusdb.wdk.model.api.DefaultAnswerRequestBody;
import org.gusdb.wdk.model.api.DefaultJsonAnswerFormatConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.restassured.http.ContentType;
import test.support.Category;
import test.support.util.AnswerUtil;
import test.support.util.GuestRequestFactory;

public class AnswersTest extends TestBase {

  public static final String BASE_PATH = SERVICE_PATH + "/answer";

  public final GuestRequestFactory _guestRequestFactory;

  public AnswersTest(GuestRequestFactory req) {
    this._guestRequestFactory = req;
  }

  @Test
  @Tag(Category.PLASMO_TEST)
  @DisplayName("Test answer GET (by POST)")
  void testBlastReporterSuccess() throws JsonProcessingException {
    AnswerSpec answerSpec = AnswerUtil.createExonCountAnswerSpec(_guestRequestFactory);
    DefaultJsonAnswerFormatConfig formatConfig = AnswerUtil.getDefaultFormatConfigOneRecord();
    DefaultAnswerRequestBody requestBody = new DefaultAnswerRequestBody(answerSpec);
    requestBody.setFormatConfig(formatConfig);
    _guestRequestFactory.jsonPayloadRequest(requestBody, HttpStatus.SC_OK,
        ContentType.JSON).when().post(BASE_PATH);
  }

}