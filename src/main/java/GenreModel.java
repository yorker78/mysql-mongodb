import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenreModel {

    private final Genre genre;
    private final Set<ObjectId> moviesInGenre;

    public GenreModel(String genre) {
        this.genre = Genre.genres.get(genre);
        this.moviesInGenre = new HashSet<>();
    }

    public void addMovie(ObjectId id) {
        moviesInGenre.add(id);
    }

    public String getGenreString() {
        return this.genre.toString();
    }

    public Document getGenreDocument() {
        Document genreDocument = new Document();
        genreDocument.append("name", genre.toString());
        if(!this.moviesInGenre.isEmpty()) {
            genreDocument.append("movies", this.moviesInGenre);
        }
        return genreDocument;
    }

    public static Genre getGenreFromString(String genre) {
        return Genre.genres.get(genre);
    }

    public enum Genre {
        ActionAndAdventure("Action & Adventure"),
        Comedy("Comedy"),
        Drama("Drama"),
        Horror("Horror"),
        ScienceFiction("Science Fiction"),
        Suspense("Suspense");

        public static final Map<String, Genre> genres = Stream.of(new Object[][] {
                {"Action & Adventure", Genre.ActionAndAdventure},
                {"Comedy", Genre.Comedy},
                {"Drama", Genre.Drama},
                {"Horror", Genre.Horror},
                {"Science Fiction", Genre.ScienceFiction},
                {"Suspense", Genre.Suspense}
        }).collect(Collectors.toMap(genre -> (String) genre[0], genre -> (Genre) genre[1]));

        private final String genre;

        Genre(final String genre) {
            this.genre = genre;
        }

        @Override
        public String toString() {
            return genre;
        }
    }
}
