package wooteco.subway.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import wooteco.subway.dao.LineJdbcDao;
import wooteco.subway.dao.SectionJdbcDao;
import wooteco.subway.dao.StationJdbcDao;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Sections;
import wooteco.subway.dto.LineResponse;
import wooteco.subway.dto.SectionRequest;
import wooteco.subway.dto.SectionsResponse;

@Service
public class SectionService {

    private final SectionJdbcDao sectionJdbcDao;
    private final LineJdbcDao lineDao;
    private final StationJdbcDao stationJdbcDao;

    public SectionService(SectionJdbcDao sectionJdbcDao, LineJdbcDao lineDao, StationJdbcDao stationJdbcDao) {
        this.sectionJdbcDao = sectionJdbcDao;
        this.lineDao = lineDao;
        this.stationJdbcDao = stationJdbcDao;
    }

    public void save(Long id, SectionRequest request, SectionsResponse response, LineResponse line) {
        lineDao.findAll().validateExist(id);
        Sections sections = new Sections(response.getSections());
        sections.validateUpAndDownSameStation(request);
        sections.validateSaveCondition(request, line);

        if (sections.isAddSectionMiddle(request)) {
            addMiddleSection(id, request, sections);
            return;
        }
        sectionJdbcDao.save(id, new Section(id, request.getUpStationId(), request.getDownStationId(), request.getDistance()));
    }

    private void addMiddleSection(Long id, SectionRequest request, Sections sections) {
        Optional<Section> targetSection = sections.getSections()
                .stream()
                .filter(section -> section.hasSameStationId(request))
                .findAny();

        targetSection.ifPresent(section -> addSectionBySameStationId(id, request, section));
    }

    private void addSectionBySameStationId(Long id, SectionRequest request, Section section) {
        sectionJdbcDao.save(id, section.createBySameStationId(id, request));
        sectionJdbcDao.delete(id, section);

        section.updateSameStationId(request);
        sectionJdbcDao.save(id, section);
    }

    public void delete(Long id, Long stationId) {
        lineDao.findAll().validateExist(id);
        stationJdbcDao.findAll().validateExist(id);
        sectionJdbcDao.findById(id).validateDeleteCondition();

        Sections sections = sectionJdbcDao.findById(id);
        if (sections.isMiddleSection(stationId)) {
            deleteMiddleSection(id, sections.findDownSection(stationId).get(), sections.findDownSection(stationId).get());
            return;
        }
        deleteEndSection(id, sections.findDownSection(stationId), sections.findDownSection(stationId));
    }

    private void deleteMiddleSection(Long id, Section downSection, Section upSection) {
        sectionJdbcDao.save(id, new Section(id, upSection.getUpStationId(), downSection.getDownStationId(),
                upSection.getDistance() + downSection.getDistance()));
        sectionJdbcDao.delete(id, downSection);
        sectionJdbcDao.delete(id, upSection);
    }

    private void deleteEndSection(Long id, Optional<Section> downSection, Optional<Section> upSection) {
        if (upSection.isEmpty()) {
            sectionJdbcDao.delete(id, downSection.get());
        }
        if (downSection.isEmpty()) {
            sectionJdbcDao.delete(id, upSection.get());
        }
    }
}
