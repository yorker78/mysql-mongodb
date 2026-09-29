import org.bson.Document;
import org.bson.types.ObjectId;

public class MovieModel {
    private final ObjectId id;
    private final int oldId;
    private final String title;
    private final String img;
    private final double duration;
    private final Rating rating;
    private final int yearReleased;
    private final double price;
    private final int stockCount;

    public MovieModel(String[] movieAttributes) {
        this.id = new ObjectId();
        this.oldId = Integer.parseInt(movieAttributes[0]);
        this.title = movieAttributes[2];
        this.img = movieAttributes[4];
        this.duration = Double.parseDouble(movieAttributes[5]);
        this.rating = Rating.ratings.get(movieAttributes[6]);
        this.yearReleased = Integer.parseInt(movieAttributes[7]);
        this.price = Double.parseDouble(movieAttributes[8]);
        this.stockCount = Integer.parseInt(movieAttributes[9]);
    }

    public int getOldId() {
        return this.oldId;
    }

    public ObjectId getNewId() {
        return this.id;
    }

    public Document getMovieDocument() {
        Document movieDocument = new Document("_id", this.id);
        movieDocument.append("title", this.title);
        movieDocument.append("img", this.img);
        movieDocument.append("duration", this.duration);
        movieDocument.append("rating", this.rating.toString());
        movieDocument.append("yearReleased", this.yearReleased);
        movieDocument.append("price", this.price);
        movieDocument.append("stockCount", this.stockCount);
        return movieDocument;
    }
}