import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseCreator {

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public DatabaseCreator() {
        this.mongoClient = MongoClients.create(System.getProperty("mongodb.uri"));
        this.database = mongoClient.getDatabase("final_project");
    }

    public void addPeople(Map<String, PersonModel> people) {
        database.getCollection("people").drop();
        database.createCollection("people");
        MongoCollection<Document> peopleCollection = database.getCollection("people");
        peopleCollection.createIndex(Indexes.ascending("name"), new IndexOptions().unique(true));
        List<Document> peopleList = people.values().stream().map(PersonModel::getDocument).toList();
        peopleCollection.insertMany(peopleList);
    }

    public void addMovies(Set<MovieModel> movies) {
        database.getCollection("movies").drop();
        database.createCollection("movies");
        MongoCollection<Document> moviesCollection = database.getCollection("movies");
        moviesCollection.createIndex(Indexes.hashed("genre"), new IndexOptions().unique(false));
        List<Document> moviesList = movies.stream().map(MovieModel::getMovieDocument).toList();
        moviesCollection.insertMany(moviesList);
    }

    public void addGenres(Set<GenreModel> genres) {
        database.getCollection("genres").drop();
        database.createCollection("genres");
        MongoCollection<Document> genresCollection = database.getCollection("genres");
        genresCollection.createIndex(Indexes.ascending("name"), new IndexOptions().unique(true));
        List<Document> genresList = genres.stream().map(GenreModel::getGenreDocument).toList();
        genresCollection.insertMany(genresList);
    }
}
