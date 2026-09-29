import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Aggregates.*;
import static java.util.Arrays.asList;

public class DatabaseReader {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> genreCollection;
    private final MongoCollection<Document> moviesCollection;
    private final MongoCollection<Document> peopleCollection;

    public DatabaseReader() {
        this.mongoClient = MongoClients.create(System.getProperty("mongodb.uri"));
        this.database = this.mongoClient.getDatabase("final_project");
        this.genreCollection = this.database.getCollection("genres");
        this.moviesCollection = this.database.getCollection("movies");
        this.peopleCollection = this.database.getCollection("people");
    }

    // Question 3: List The Number of Videos For Each Genre
    public Map<GenreModel.Genre, Integer> getGenreAmounts() {
        Map<GenreModel.Genre, Integer> genreAmounts = new HashMap<>();
        FindIterable<Document> genreIterable = this.genreCollection.find();
        for (Document genre : genreIterable) {
            genreAmounts.put(GenreModel.getGenreFromString(genre.getString("name")), genre.getList("movies", ObjectId.class).size());
        }
        return genreAmounts;
    }

    // Question 4: List The Number of Videos For Each Genre Where the Inventory is Non-Zero
    public Map<GenreModel.Genre, Integer> getInStockGenreAmounts() {
        Map<GenreModel.Genre, Integer> inStockGenreAmounts = new HashMap<>();
        Bson inStock = match(ne("stockCount", 0));
        Bson idMatch = match(eq("_id", "$$movies"));
        Bson lookupStage = lookup("movies", List.of(inStock,idMatch), "movies_info");
        AggregateIterable<Document> genreIterator = genreCollection.aggregate(List.of(lookupStage));
        for (Document genre : genreIterator) {
            inStockGenreAmounts.put(GenreModel.Genre.genres.get(genre.getString("name")), genre.getList("movies", ObjectId.class).size());
        }
        return inStockGenreAmounts;
    }

    // Question 5: For Each Actor, List the Genres That Actor Has Appeared In
    public Map<String, Set<GenreModel.Genre>> getActorsAndGenres() {
        Map<String, Set<GenreModel.Genre>> actorGenres = new HashMap<>();
        Bson matchStage = match(exists("actingCredits"));
        Bson lookupStage = lookup("genres", "actingCredits", "movies", "genres");
        AggregateIterable<Document> actors = peopleCollection.aggregate(asList(matchStage, lookupStage));
        for (Document actor : actors) {
            Set<GenreModel.Genre> actorGenreSet = new HashSet<>();
            List<Document> genres = actor.getList("genres", Document.class);
            for(Document genre : genres) {
                actorGenreSet.add(GenreModel.getGenreFromString(genre.getString("name")));
            }
            actorGenres.put(actor.getString("name"), actorGenreSet);
        }
        return actorGenres;
    }

    // Question 6: Which Actors Have Appeared in Movies in Different Genres?
    public Map<String, Set<GenreModel.Genre>> getMultiGenreActors() {
        Map<String, Set<GenreModel.Genre>> actorGenres = new HashMap<>();
        Bson matchStage = match(exists("actingCredits"));
        Bson lookupStage = lookup("genres", "actingCredits", "movies", "genres");
        AggregateIterable<Document> actors = peopleCollection.aggregate(asList(matchStage, lookupStage));
        for (Document actor : actors) {
            Set<GenreModel.Genre> actorGenreSet = new HashSet<>();
            List<Document> genres = actor.getList("genres", Document.class);
            for(Document genre : genres) {
                actorGenreSet.add(GenreModel.getGenreFromString(genre.getString("name")));
            }
            if(actorGenreSet.size() > 1) {
                actorGenres.put(actor.getString("name"), actorGenreSet);
            }
        }
        return actorGenres;
    }

    // Question 7: Which Actors Have Not Appeared in a Comedy?
    public Map<String, Set<GenreModel.Genre>> getNoComedyActors() {
        Map<String, Set<GenreModel.Genre>> actorGenres = new HashMap<>();
        Bson matchStage = match(exists("actingCredits"));
        Bson lookupStage = lookup("genres", "actingCredits", "movies", "genres");
        AggregateIterable<Document> actors = peopleCollection.aggregate(asList(matchStage, lookupStage));
        for (Document actor : actors) {
            Set<GenreModel.Genre> actorGenreSet = new HashSet<>();
            boolean inComedy = false;
            List<Document> genres = actor.getList("genres", Document.class);
            for(Document genre : genres) {
                if(GenreModel.Genre.Comedy.toString().equals(genre.getString("name"))) {
                    inComedy = true;
                } else {
                    actorGenreSet.add(GenreModel.Genre.genres.get(genre.getString("name")));
                }
            }
            if(!inComedy) {
                actorGenres.put(actor.getString("name"), actorGenreSet);
            }
        }
        return actorGenres;
    }

    // Question 8: Which Actors Have Appeared in Both a Comedy and an Action & Adventure Movie?
    public Set<String> getComedyAndActionActors() {
        Set<String> comedyAndActionActors = new HashSet<>();
        Bson matchStage = match(exists("actingCredits"));
        Bson lookupStage = lookup("genres", "actingCredits", "movies", "genres");
        AggregateIterable<Document> actors = peopleCollection.aggregate(asList(matchStage, lookupStage));
        for(Document actor: actors) {
            List<Document> genres = actor.getList("genres", Document.class);
            boolean inComedy = false;
            boolean inAction = false;
            for(Document genre : genres) {
                if(GenreModel.Genre.Comedy.toString().equals(genre.getString("name"))) {
                    inComedy = true;
                }
                if(GenreModel.Genre.ActionAndAdventure.toString().equals(genre.getString("name"))) {
                    inAction = true;
                }
            }
            if(inComedy && inAction) {
                comedyAndActionActors.add(actor.getString("name"));
            }
        }
        return comedyAndActionActors;
    }

    // Question 9: How Many Director Cameos Are in The Data Set?
    public Set<String> getDirectorCameos() {
        Set<String> directorCameos = new HashSet<>();
        for(Document person: peopleCollection.find(new Document())) {
            if(person.containsKey("actingCredits") && person.containsKey("directingCredits")) {
                if(person.getList("actingCredits", ObjectId.class).removeAll(person.getList("directingCredits", ObjectId.class))) {
                    directorCameos.add(person.getString("name"));
                }
            }
        }
        return directorCameos;
    }
}
