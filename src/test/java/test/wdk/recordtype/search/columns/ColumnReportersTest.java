package test.wdk.recordtype.search.columns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.wdk.recordtype.ReportersTest.REPORT_PATH;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.function.Function;

import org.gusdb.wdk.model.api.DefaultReportRequest;
import org.gusdb.wdk.model.api.FilterValueSpec;
import org.gusdb.wdk.model.api.SearchConfig;
import org.gusdb.wdk.model.api.StandardReportConfig;
import org.gusdb.wdk.model.api.record.search.column.reporter.Histogram;
import org.gusdb.wdk.model.api.record.search.reporter.JsonReporterResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import test.support.Category;
import test.support.util.ColumnUtil;
import test.support.util.GuestRequestFactory;
import test.support.util.ReportUtil;
import test.support.util.RequestFactory;
import test.wdk.TestBase;
import util.Json;

import java.util.ArrayList;
import java.util.HashMap;

public class ColumnReportersTest extends TestBase {

  private final static String RECORD = "transcript";
  private final static String SEARCH = "GenesByExonCount";

  private final static String COLUMN_REPORT_PATH = ColumnUtil.KEYED_URI + "/reports/byValue";

  private final RequestFactory rFac;

  public ColumnReportersTest(GuestRequestFactory rFac) {
    this.rFac = rFac;
  }
  
  // choose search config to try to get at least some genes w/ multiple transcripts
  private static SearchConfig getExonCountSearchConfig() {
    SearchConfig config =  new SearchConfig()
      .setParameters(new HashMap<>(){{
        put("organism", "Plasmodium falciparum 3D7");
        put("scope", "Transcript");
        put("num_exons_gte", "15");
        put("num_exons_lte", "20");
      }});
    return config;
  }
  
  private static SearchConfig getExonCountSearchConfigWithFilter() {
    FilterValueSpec oneGenePerTranFilter = new FilterValueSpec();
    oneGenePerTranFilter.setName("representativeTranscriptOnly");
    oneGenePerTranFilter.setValue(new HashMap<String,Object>());
    oneGenePerTranFilter.setDisabled(false);
    ArrayList<FilterValueSpec> filters = new ArrayList<FilterValueSpec>();
    filters.add(oneGenePerTranFilter);
    SearchConfig config =  getExonCountSearchConfig();
    config.setViewFilters(filters);
    return config;
  }

  @Nested
  public class SuperReporter {
    
    @Test
    @Tag(Category.PLASMO_TEST)
    @DisplayName("Test super column reporter on a number column")
    void numberColumn() {
      final var column = "exon_count";

      // filter standard report so we get count of genes, not transcripts, to match histogram
      JsonReporterResponse standardReport = rFac.jsonIoSuccessRequest(
        new DefaultReportRequest(getExonCountSearchConfigWithFilter(), new StandardReportConfig()
          .addAttribute(column))
      )
        .when()
        .post(REPORT_PATH, RECORD, SEARCH, "standard")
        .as(JsonReporterResponse.class);

      Histogram<BigDecimal> histogramReport = Json.MAPPER.convertValue(
        rFac.jsonIoSuccessRequest(
          new DefaultReportRequest(getExonCountSearchConfig(), new StandardReportConfig()))
          .when()
          .post(COLUMN_REPORT_PATH, RECORD, SEARCH, column)
          .as(JsonNode.class),
        new TypeReference<Histogram<BigDecimal>>(){}
      );

      compareHistogram(standardReport, histogramReport, column, BigDecimal::new);
    }

    @Test
    @Tag(Category.PLASMO_TEST)
    @DisplayName("Test super column filter on a string column a pattern")
    void stringColumn() {
      final var column = "organism";

      // filter standard report so we get count of genes, not transcripts, to match histogram
      JsonReporterResponse standardReport = rFac.jsonIoSuccessRequest(
        new DefaultReportRequest(getExonCountSearchConfigWithFilter(), new StandardReportConfig()
          .addAttribute(column))
      )
        .when()
        .post(REPORT_PATH, RECORD, SEARCH, "standard")
        .as(JsonReporterResponse.class);

      Histogram<String> histogramReport = Json.MAPPER.convertValue(
        rFac.jsonIoSuccessRequest(
          new DefaultReportRequest(getExonCountSearchConfig(), new StandardReportConfig()))
          .when()
          .post(COLUMN_REPORT_PATH, RECORD, SEARCH, column)
          .as(JsonNode.class),
        new TypeReference<Histogram<String>>(){}
      );

      compareHistogram(standardReport, histogramReport, column, Function.identity());
    }

    @Test
    @Tag(Category.CLINEPI_TEST)
    @DisplayName("Test super column filter on a date column with single values")
    void multiSingleValDate() {
      final var column = "EUPATH_0000091"; // Visit date
      final var conf1  = ReportUtil.prismObservationSearch();

      conf1.getParameters()
        .put("visit_date", Json.object()
          .put("min", "2013-07-01")
          .put("max", "2013-07-10")
          .toString());

      final var value = rFac.jsonIoSuccessRequest(
        new DefaultReportRequest(conf1, new StandardReportConfig()
          .addAttribute(column))
      )
        .when()
        .post(REPORT_PATH, "DS_0ad509829e_observation",
          "ClinicalVisitsByRelativeVisits_prism", "standard")
        .as(JsonReporterResponse.class);

      Histogram<LocalDateTime> report = Json.MAPPER.convertValue(
        rFac.jsonIoSuccessRequest(
          new DefaultReportRequest(conf1, new StandardReportConfig()))
          .when()
          .post(COLUMN_REPORT_PATH, "DS_0ad509829e_observation",
            "ClinicalVisitsByRelativeVisits_prism", column)
          .as(JsonNode.class),
        new TypeReference<Histogram<LocalDateTime>>(){}
      );

      compareHistogram(value, report, column, s -> LocalDateTime.parse(
        s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
      ));
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> void compareHistogram(
    final JsonReporterResponse std,
    final Histogram<T>         his,
    final String               col,
    final Function<String, T>  fn
  ) {
    assertEquals(std.getRecords().size(), his.getTotalValues());

    var nullCount = 0L;
    var uniques = new HashSet<T>();
    T min = null;
    T max = null;

    for (var rec : std.getRecords()) {
      var val = rec.getAttributes().get(col);

      if (val == null) {
        nullCount++;
        continue;
      }

      var dec = fn.apply(val);
      uniques.add(dec);

      if (his.hasMin() && dec instanceof Comparable)
        if (min == null || ((Comparable<T>) dec).compareTo(min) < 0)
          min = dec;

      if (his.hasMax() && dec instanceof Comparable)
        if (max == null || ((Comparable<T>) dec).compareTo(max) > 0)
          max = dec;
    }

    assertEquals(nullCount, his.getNullValues());
    assertEquals(uniques.size(), his.getUniqueValues());

    if (his.hasMin())
      assertEquals(min, his.getMinValue());
    if (his.hasMax())
      assertEquals(max, his.getMaxValue());
  }
}
