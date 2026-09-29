import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MovieParser {

    public Map<String, PersonModel> readActors() {
        Map<String, PersonModel> people = new HashMap<>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(Main.class.getResource("cleaned_video_actors.txt").getPath()));
            String line = br.readLine();
            while (line != null) {
                String[] actorAttributes = line.split("\t");
                PersonModel actor = people.get(actorAttributes[1]);
                if (actor == null) {
                    actor = new PersonModel(actorAttributes[1]);
                }
                actor.addOldActingCredit(Integer.parseInt(actorAttributes[2]));
                people.put(actor.getName(), actor);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return people;
    }

    public Set<MovieModel> readMovies(Map<String, PersonModel> people, Map<String, GenreModel> genres) {
        Set<MovieModel> movies = new HashSet<>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(Main.class.getResource("cleaned_video_recordings.txt").getPath()));
            String line = br.readLine();
            while (line != null) {
                String[] movieAttributes = line.split("\t");
                MovieModel movie = new MovieModel(movieAttributes);
                movies.add(movie);
                for (PersonModel actor : people.values()) {
                    for(int oldId : actor.getOldActingCredits()) {
                        if(oldId == movie.getOldId()) {
                            actor.addNewActingCredit(movie.getNewId());
                        }
                    }
                }
                PersonModel director = people.get(movieAttributes[1]);
                if (director == null) {
                    director = new PersonModel(movieAttributes[1]);
                    people.put(director.getName(), director);
                }
                director.addDirectingCredit(movie.getNewId());

                for (GenreModel genre : genres.values()) {
                    if(genre.getGenreString().equals(movieAttributes[3])) {
                        genre.addMovie(movie.getNewId());
                    }
                }

                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return movies;
    }
}
