package wooteco.subway.dao;

import java.util.List;

import wooteco.subway.domain.Line;

public interface LineDao {

    Line save(Line line);

    List<Line> findAll();

    Line find(Long id);

    int update(Long id, Line line);

    int delete(Long id);
}
