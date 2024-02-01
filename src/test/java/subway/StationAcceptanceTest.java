package subway;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import subway.controller.dto.StationResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;
import static subway.StationFixture.GANGNAM_STATION;
import static subway.StationFixture.SEOLLEUNG_STATION;

@DisplayName("지하철역 관련 기능")
public class StationAcceptanceTest extends AcceptanceTest {

    private static final String STATION_NAME_GANGNAM = "강남역";
    private static final String STATION_NAME_SEOLLEUNG = "선릉역";

    /**
     * When 지하철역을 생성하면
     * Then 지하철역이 생성된다
     * Then 지하철역 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {
        // when
        createStation(GANGNAM_STATION.toCreateRequest(), CREATED.value());

        ExtractableResponse<Response> findResponse = findStation(OK.value());
        List<String> stationsNames = findResponse.jsonPath().getList("name", String.class);
        assertThat(stationsNames).hasSize(1)
                .containsExactly(STATION_NAME_GANGNAM);
    }

    /**
     * Given 2개의 지하철역을 생성하고
     * When 지하철역 목록을 조회하면
     * Then 2개의 지하철역을 응답 받는다
     */
    @DisplayName("지하철역 목록을 조회한다.")
    @Test
    void selectStation() {
        createStation(GANGNAM_STATION.toCreateRequest(), CREATED.value());
        createStation(SEOLLEUNG_STATION.toCreateRequest(), CREATED.value());

        // when
        ExtractableResponse<Response> findResponse = findStation(OK.value());
        List<String> stationsNames = findResponse.jsonPath().getList("name", String.class);

        // then
        assertThat(stationsNames).hasSize(2)
                .containsExactly("강남역", STATION_NAME_SEOLLEUNG);
    }

    /**
     * Given 지하철역을 생성하고
     * When 그 지하철역을 삭제하면
     * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
     */
    @DisplayName("지하철역을 삭제한다.")
    @Test
    void removeStation() {
        // given
        ExtractableResponse<Response> createResponse = createStation(GANGNAM_STATION.toCreateRequest(), CREATED.value());
        StationResponse stationResponse = createResponse.as(StationResponse.class);

        // when
        deleteStation(stationResponse.getId(), NO_CONTENT.value());

        // then
        ExtractableResponse<Response> findResponse = findStation(OK.value());
        List<String> stationsNames = findResponse.jsonPath().getList("name", String.class);
        assertThat(stationsNames).isEmpty();
    }

    private ExtractableResponse<Response> findStation(int statusCode) {
        return get("/stations/all", statusCode);
    }

    private ExtractableResponse<Response> deleteStation(Long id, int statusCode) {
        return delete("/stations/{id}", statusCode, id);
    }

}
