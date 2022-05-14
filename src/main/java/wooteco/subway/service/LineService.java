package wooteco.subway.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wooteco.subway.dao.LineJdbcDao;
import wooteco.subway.dao.SectionJdbcDao;
import wooteco.subway.dao.StationJdbcDao;
import wooteco.subway.domain.*;
import wooteco.subway.dto.LineRequest;
import wooteco.subway.dto.LineResponse;
import wooteco.subway.dto.SectionsResponse;

@Service
public class LineService {

    private final LineJdbcDao lineDao;
    private final StationJdbcDao stationDao;
    private final SectionJdbcDao sectionJdbcDao;

    public LineService(LineJdbcDao lineDao, StationJdbcDao stationDao, SectionJdbcDao sectionJdbcDao) {
        this.lineDao = lineDao;
        this.stationDao = stationDao;
        this.sectionJdbcDao = sectionJdbcDao;
    }

    @Transactional
    public LineResponse save(LineRequest request) {
        Lines lines = lineDao.findAll();
        lines.add(new Line(request.getName(), request.getColor()));

        Line line = lineDao.save(new Line(request.getName(), request.getColor()));
        sectionJdbcDao.save(line.getId(), new Section(line.getId(), request.getUpStationId(), request.getDownStationId(), request.getDistance()));

        Station upsStation = stationDao.findById(request.getUpStationId());
        Station downStation = stationDao.findById(request.getDownStationId());
        return new LineResponse(line.getId(), line.getName(), line.getColor(), Set.of(upsStation, downStation));
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findAll() {
        List<LineResponse> responses = new ArrayList<>();
        for (Line line : lineDao.findAll().getLines()) {
            responses.add(makeLineResponseWithLinkedStations(line, sectionJdbcDao.findById(line.getId())));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public LineResponse findById(Long id) {
        lineDao.findAll().validateExist(id);

        Line line = lineDao.findById(id);
        Sections sections = sectionJdbcDao.findById(line.getId());
        return makeLineResponseWithLinkedStations(line, sections);
    }

    private LineResponse makeLineResponseWithLinkedStations(Line line, Sections sections) {
        Set<Station> stations = new LinkedHashSet<>();
        for (Section section : sections.linkSections()) {
            stations.add(toMapStations().get(section.getUpStationId()));
            stations.add(toMapStations().get(section.getDownStationId()));
        }
        return new LineResponse(line.getId(), line.getName(), line.getColor(), stations);
    }

    @Transactional(readOnly = true)
    public SectionsResponse findSections(Long id) {
        lineDao.findAll().validateExist(id);

        Sections sections = sectionJdbcDao.findById(id);
        return new SectionsResponse(sections.linkSections());
    }

    private Map<Long, Station> toMapStations() {
        return stationDao.findAll()
                .getStations()
                .stream()
                .collect(Collectors.toMap(Station::getId, station -> station));
    }

    @Transactional
    public int update(Long id, LineRequest request) {
        Lines lines = lineDao.findAll();
        lines.validateExist(id);
        Line line = new Line(request.getName(), request.getColor());
        lines.add(line);
        return lineDao.update(id, new Line(line.getName(), line.getColor()));
    }

    @Transactional
    public int delete(Long id) {
        lineDao.findAll().validateExist(id);
        return lineDao.delete(id);
    }
}
