import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);
        long start = System.nanoTime();
        MovieParser mp = new MovieParser();
        DatabaseCreator dc = new DatabaseCreator();
        Map<String, PersonModel> people = mp.readActors();
        Map<String, GenreModel> genres = Stream.of(new Object[][] {
                {GenreModel.Genre.ActionAndAdventure.toString(), new GenreModel(GenreModel.Genre.ActionAndAdventure.toString())},
                {GenreModel.Genre.Comedy.toString(), new GenreModel(GenreModel.Genre.Comedy.toString())},
                {GenreModel.Genre.Drama.toString(), new GenreModel(GenreModel.Genre.Drama.toString())},
                {GenreModel.Genre.Horror.toString(), new GenreModel(GenreModel.Genre.Horror.toString())},
                {GenreModel.Genre.ScienceFiction.toString(), new GenreModel(GenreModel.Genre.ScienceFiction.toString())},
                {GenreModel.Genre.Suspense.toString(), new GenreModel(GenreModel.Genre.Suspense.toString())},
        }).collect(Collectors.toMap(genre -> (String) genre[0], genre -> (GenreModel) genre[1]));
        Set<MovieModel> movies = mp.readMovies(people, genres);
        long parseTextTime = System.nanoTime();
        dc.addPeople(people);
        dc.addMovies(movies);
        dc.addGenres(new HashSet<>(genres.values()));
        long dbCreationTime = System.nanoTime();
        DatabaseReader dr = new DatabaseReader();
        long drCreationTime = System.nanoTime();
        q3(dr.getGenreAmounts());
        long q3Time = System.nanoTime();
        q4(dr.getInStockGenreAmounts());
        long q4Time = System.nanoTime();
        q5(dr.getActorsAndGenres(), 10);
        long q5Time = System.nanoTime();
        q6(dr.getMultiGenreActors(), 10);
        long q6Time = System.nanoTime();
        q7(dr.getNoComedyActors(), 10);
        long q7Time = System.nanoTime();
        q8(dr.getComedyAndActionActors());
        long q8Time = System.nanoTime();
        q9(dr.getDirectorCameos());
        long q9Time = System.nanoTime();
        System.out.println("Total Time Taken: " + ((q9Time - start)) / 1000000000.0 + "s");
        System.out.println("Text Parsing Time Taken: " + ((parseTextTime - start) / 1000000000.0) + "s");
        System.out.println("DB Creation Time Take: " + ((dbCreationTime - parseTextTime) / 1000000000.0) + "s");
        System.out.println("DB Connection Time Taken: " + ((drCreationTime - dbCreationTime) / 1000000000.0) + "s");
        System.out.println("Q3 Time Taken: " + ((q3Time - drCreationTime) / 1000000000.0) + "s");
        System.out.println("Q4 Time Taken: " + ((q4Time - q3Time) / 1000000000.0) + "s");
        System.out.println("Q5 Time Taken: " + ((q5Time - q4Time) / 1000000000.0) + "s");
        System.out.println("Q6 Time Taken: " + ((q6Time - q5Time) / 1000000000.0) + "s");
        System.out.println("Q7 Time Taken: " + ((q7Time - q6Time) / 1000000000.0) + "s");
        System.out.println("Q8 Time Taken: " + ((q8Time - q7Time) / 1000000000.0) + "s");
        System.out.println("Q9 Time Taken: " + ((q9Time - q8Time) / 1000000000.0) + "s");
    }

    private static void debugGenres(Map<Integer, MovieModel> movies) {
        System.out.println("Movie Model Documents Look Like: ");
        for(MovieModel m : movies.values()) {
            System.out.println(m.getMovieDocument().toJson());
        }
        System.out.println();
        System.out.println();
    }

    private static void q3(Map<GenreModel.Genre, Integer> genreAmounts) {
        System.out.println("Q3: Number of Videos For Each Genre");
        System.out.println();
        for(GenreModel.Genre genre : genreAmounts.keySet()) {
            System.out.println(genre.toString() + ": " + genreAmounts.get(genre));
        }
        System.out.println();
        System.out.println();
    }

    private static void q4(Map<GenreModel.Genre, Integer> genreAmounts) {
        System.out.println("Q4: Number of In Stock Videos For Each Genre");
        System.out.println();
        for (GenreModel.Genre genre : genreAmounts.keySet()) {
            System.out.println(genre.toString() + ": " + genreAmounts.get(genre));
        }
        System.out.println();
        System.out.println();
    }

    private static void q5(Map<String, Set<GenreModel.Genre>> actorGenres, int limit) {
        System.out.println("Q5: " + actorGenres.keySet().size() + " Genres Each Actor Has Appeared In");
        System.out.println();
        if(limit == 0) {
            for (String actor : actorGenres.keySet()) {
                StringBuilder actorGenreString = new StringBuilder(actor + " has appeared in:");
                for (GenreModel.Genre genre : actorGenres.get(actor)) {
                    actorGenreString.append(", ");
                    actorGenreString.append(genre.toString());
                }
                actorGenreString.deleteCharAt(actorGenreString.indexOf(","));
                System.out.println(actorGenreString.toString());
            }
        } else {
            int i = 0;
            for (String actor : actorGenres.keySet()) {
                if (i == limit) {
                    break;
                }
                StringBuilder actorGenreString = new StringBuilder(actor + " has appeared in:");
                for (GenreModel.Genre genre : actorGenres.get(actor)) {
                    actorGenreString.append(", ");
                    actorGenreString.append(genre.toString());
                }
                actorGenreString.deleteCharAt(actorGenreString.indexOf(","));
                System.out.println(actorGenreString.toString());
                i++;
            }
        }
        System.out.println();
        System.out.println();
    }

    private static void q6(Map<String, Set<GenreModel.Genre>> actorGenres, int limit) {
        System.out.println("Q6: " + actorGenres.keySet().size() + " Actors Who Have Appeared In More Than One Genre");
        System.out.println();
        if(limit == 0) {
            for(String actor : actorGenres.keySet()) {
                StringBuilder actorGenreString = new StringBuilder(actor + " has appeared in:");
                for(GenreModel.Genre genre : actorGenres.get(actor)) {
                    actorGenreString.append(", ");
                    actorGenreString.append(genre.toString());
                }
                actorGenreString.deleteCharAt(actorGenreString.indexOf(","));
                System.out.println(actorGenreString.toString());
            }
        } else {
            int i = 0;
            for(String actor : actorGenres.keySet()) {
                if (i == limit) {
                    break;
                }
                StringBuilder actorGenreString = new StringBuilder(actor + " has appeared in:");
                for(GenreModel.Genre genre : actorGenres.get(actor)) {
                    actorGenreString.append(", ");
                    actorGenreString.append(genre.toString());
                }
                actorGenreString.deleteCharAt(actorGenreString.indexOf(","));
                System.out.println(actorGenreString.toString());
                i++;
            }
        }
        System.out.println();
        System.out.println();
    }

    private static void q7(Map<String, Set<GenreModel.Genre>> actorGenres, int limit) {
        System.out.println("Q7: " + actorGenres.keySet().size() + " Actors Who Have Not Appeared In A Comedy");
        System.out.println();
        if(limit == 0) {
            for(String actor : actorGenres.keySet()) {
                StringBuilder actorGenreString = new StringBuilder(actor + " has appeared in:");
                for(GenreModel.Genre genre : actorGenres.get(actor)) {
                    actorGenreString.append(", ");
                    actorGenreString.append(genre.toString());
                }
                actorGenreString.deleteCharAt(actorGenreString.indexOf(","));
                System.out.println(actorGenreString.toString());
            }
        } else {
            int i = 0;
            for (String actor : actorGenres.keySet()) {
                if (i == limit) {
                    break;
                }
                StringBuilder actorGenreString = new StringBuilder(actor + " has appeared in:");
                for (GenreModel.Genre genre : actorGenres.get(actor)) {
                    actorGenreString.append(", ");
                    actorGenreString.append(genre.toString());
                }
                actorGenreString.deleteCharAt(actorGenreString.indexOf(","));
                System.out.println(actorGenreString.toString());
                i++;
            }
        }
        System.out.println();
        System.out.println();
    }

    private static void q8(Set<String> actors) {
        System.out.println("Q8: " + actors.size() + " Actors Who Have Appeared in Both a Comedy and Action & Adventure");
        System.out.println();
        for(String actor : actors) {
            System.out.println(actor);
        }
        System.out.println();
        System.out.println();
    }

    private static void q9(Set<String> directors) {
        System.out.println("Q9: " + directors.size() + " Directors Who Have Cameoed in Their Own Movies");
        System.out.println();
        for(String director : directors) {
            System.out.println(director);
        }
        System.out.println();
        System.out.println();
    }
}
