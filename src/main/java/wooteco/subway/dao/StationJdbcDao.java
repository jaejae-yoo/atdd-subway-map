package wooteco.subway.dao;

import java.sql.PreparedStatement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import wooteco.subway.domain.Station;
import wooteco.subway.dto.StationRequest;

@Repository
public class StationJdbcDao implements StationDao {

    private final JdbcTemplate jdbcTemplate;

    public StationJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Station save(StationRequest stationRequest) {
        final String sql = "insert into station (name) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, stationRequest.getName());
            return ps;
        }, keyHolder);
        return new Station(keyHolder.getKey().longValue(), stationRequest.getName());
    }

    @Override
    public List<Station> findAll() {
        final String sql = "select * from station";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Station(rs.getLong("id"), rs.getString("name")));
    }

    @Override
    public int deleteStation(long id) {
        final String sql = "delete from station where id = (?)";
        return jdbcTemplate.update(sql, id);
    }
}
