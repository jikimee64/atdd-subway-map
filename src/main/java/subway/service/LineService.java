package subway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.controller.dto.LineRequest;
import subway.controller.dto.LineResponse;
import subway.domain.Line;
import subway.domain.Station;
import subway.repository.LineRepository;
import subway.repository.StationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LineService {
    private LineRepository lineRepository;
    private StationRepository stationRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public LineResponse saveLine(LineRequest lineRequest) {
        Line line = lineRepository.save(new Line(
                lineRequest.getName(),
                lineRequest.getColor(),
                lineRequest.getUpStationId(),
                lineRequest.getDownStationId(),
                lineRequest.getDistance()
        ));
        return LineResponse.of(line, findStationsBy(line.stationIds()));
    }

    public List<LineResponse> findLines() {
        List<Line> lines = lineRepository.findAll();
        List<Station> stations = stations(lines);
        return LineResponse.listOf(lines, stations);
    }

    private List<Station> stations(List<Line> lines) {
        List<Long> stationIds = lines.stream()
                .map(Line::stationIds)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return findStationsBy(stationIds);
    }

    private List<Station> findStationsBy(List<Long> ids) {
        return stationRepository.findAll().stream()
                .filter(station -> ids.contains(station.getId()))
                .collect(Collectors.toList());
    }
}