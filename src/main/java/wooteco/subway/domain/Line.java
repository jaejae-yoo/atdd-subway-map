package wooteco.subway.domain;

public class Line {

    private final long id;
    private final String name;
    private final String color;

    public Line(long id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}
