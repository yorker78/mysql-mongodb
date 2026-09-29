import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Rating {
    PG("PG"),
    PG13("PG-13"),
    R("R"),
    NR("NR");

    public static final Map<String, Rating> ratings = Stream.of(new Object[][] {
            {"PG", Rating.PG},
            {"PG-13", Rating.PG13},
            {"R", Rating.R},
            {"NR", Rating.NR}
    }).collect(Collectors.toMap(rating -> (String) rating[0], rating -> (Rating) rating[1]));

    private final String rating;

    Rating(final String rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return rating;
    }
}