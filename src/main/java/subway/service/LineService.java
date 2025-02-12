package subway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.controller.dto.LineCreateRequest;
import subway.controller.dto.LineResponse;
import subway.controller.dto.LineUpdateRequest;
import subway.domain.Line;
import subway.domain.Section;
import subway.domain.Station;
import subway.domain.Stations;
import subway.repository.LineRepository;
import subway.repository.SectionRepository;
import subway.repository.StationRepository;

import java.util.List;

@Service
public class LineService {
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final SectionRepository sectionRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository, SectionRepository sectionRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional
    public LineResponse saveLine(LineCreateRequest request) {
        Line line = lineRepository.save(new Line(
                request.getName(),
                request.getColor()
        ));
        Stations stations = new Stations(stationRepository.findByIdIn(request.stationIds()));

        Station upStation = stations.findBy(request.getUpStationId());
        Station downStation = stations.findBy(request.getDownStationId());

        sectionRepository.save(new Section(
                line,
                upStation,
                downStation,
                request.getDistance()
        ));
        return LineResponse.ofWithStations(line, List.of(upStation, downStation));
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findLines() {
        List<Line> lines = lineRepository.findAll();
        List<Section> sections = sectionRepository.findAllByLineIn(lines);
        return LineResponse.listOf(lines, sections);
    }

    @Transactional(readOnly = true)
    public LineResponse findLine(Long id) {
        Line line = findBy(id);
        List<Section> sections = sectionRepository.findByLine(line);
        return LineResponse.ofWithSections(line, sections);
    }

    @Transactional
    public void updateLine(Long id, LineUpdateRequest request) {
        Line line = findBy(id);
        line.update(request.getName(), request.getColor());
    }

    private Line findBy(Long id) {
        return lineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 노선입니다."));
    }

    @Transactional
    public void deleteLine(Long id) {
        sectionRepository.deleteByLine(new Line(id));
        lineRepository.deleteById(id);
    }
}
