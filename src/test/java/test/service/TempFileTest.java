package test.service;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static test.support.Conf.SERVICE_PATH;

@DisplayName("Temporary File")
public class TempFileTest extends TestBase {
  public static final String BASE_PATH = SERVICE_PATH + "/temporary-file";
  public static final String DELETE_PATH = BASE_PATH + "/{ID}";

  @Test
  @DisplayName("Create and delete temp file")
  void createAndDeleteTempFile() {

    // Test temp file create.  also grab cookie and ID
    Response response = testTempFileCreate();
    String cookieId = response.getCookie("JSESSIONID");
    String tempFileId = response.getHeader("ID");

    // test DELETE with irrelevant cookie. should get NOT FOUND
    testTempFileDelete(tempFileId, getIrrelevantCookieId(), HttpStatus.SC_NOT_FOUND);

    // test DELETE with silly ID. should get NOT FOUND
    testTempFileDelete("silly-id", cookieId, HttpStatus.SC_NOT_FOUND);

    // test real DELETE
    testTempFileDelete(tempFileId, cookieId, HttpStatus.SC_NO_CONTENT);
  }
  
  private Response testTempFileCreate() {
    return
    RestAssured
    .given().multiPart("file", "this is the contents")
    .expect()
    .statusCode(HttpStatus.SC_NO_CONTENT)
    .when()
    .post(BASE_PATH);
  }
  
  private void testTempFileDelete(String fileId, String cookieId, int expectedStatus) {
    RestAssured
    .given()
    .cookie("JSESSIONID", cookieId)
    .expect()
    .statusCode(expectedStatus)
    .when().delete(DELETE_PATH, fileId);    
  }
  
  private String getIrrelevantCookieId() {
    return RestAssured
        .expect()
        .statusCode(HttpStatus.SC_OK)
        .when()
        .get(SERVICE_PATH)
        .getCookie("JSESSIONID");
  }

}
