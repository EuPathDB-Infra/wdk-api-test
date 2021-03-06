package test.wdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.gusdb.wdk.model.api.AnswerSpec;
import org.gusdb.wdk.model.api.DefaultAnswerRequestBody;
import org.gusdb.wdk.model.api.DefaultJsonAnswerFormatConfig;
import org.gusdb.wdk.model.api.RecordInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.support.Category;
import test.support.util.AnswerUtil;
import test.support.util.GuestRequestFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.support.Conf.SERVICE_PATH;

public class AnswersTest extends TestBase {

  public static final String BASE_PATH = SERVICE_PATH + "/record-types/{recordType}/searches/{search}/reports/standard";

  public final GuestRequestFactory _guestRequestFactory;

  public AnswersTest(GuestRequestFactory req) {
    this._guestRequestFactory = req;
  }

  @Test
  @Tag(Category.PLASMO_TEST)
  @DisplayName("Test answer GET (by POST)")
  void testSingleRecordAnswer() throws JsonProcessingException {
    AnswerSpec answerSpec = AnswerUtil.createExonCountAnswerSpec(_guestRequestFactory);
    DefaultJsonAnswerFormatConfig formatConfig = AnswerUtil.getDefaultFormatConfigOneRecord();
    DefaultAnswerRequestBody requestBody = new DefaultAnswerRequestBody(answerSpec);
    requestBody.setFormatConfig(formatConfig);
    Response response = _guestRequestFactory.jsonPayloadRequest(requestBody, HttpStatus.SC_OK,
        ContentType.JSON).when().post(BASE_PATH, "transcript", "GenesByExonCount");

    // minimally, confirm we got exactly one record
    List<RecordInstance> records = response.body().jsonPath().getList("records", RecordInstance.class);
    assertEquals(1, records.size(), "Expected exactly one record, but got " + records.size());

  }

}
