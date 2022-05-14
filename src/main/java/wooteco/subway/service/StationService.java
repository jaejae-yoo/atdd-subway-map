package wooteco.subway.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.TransactionSystemException;
import wooteco.subway.dao.StationJdbcDao;
import wooteco.subway.domain.Station;
import wooteco.subway.dto.StationRequest;
import wooteco.subway.dto.StationResponse;
import wooteco.subway.exception.ClientException;

@Service
public class StationService {

    private final StationJdbcDao stationDao;

    public StationService(final StationJdbcDao stationDao) {
        this.stationDao = stationDao;
    }

    public StationResponse save(StationRequest request) {
        try {
            Station station = stationDao.save(new Station(request.getName()));
            return new StationResponse(station.getId(), station.getName());
        } catch (DataAccessException exception) {
            throw new ClientException("이미 등록된 지하철역입니다.");
        }
    }

    public List<StationResponse> findAll() {
        List<Station> stations = stationDao.findAll();
        return stations.stream()
                .map(it -> new StationResponse(it.getId(), it.getName()))
                .collect(Collectors.toList());
    }

    public int delete(long id) {
        try {
            return stationDao.delete(id);
        } catch (TransactionSystemException exception) {
            throw new ClientException("구간에 등록되어 있는 역은 제거할 수 없습니다.");
        }
    }
}
