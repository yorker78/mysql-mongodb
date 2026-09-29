# MySQL vs. MongoDB

A comparison of how a relational database and a document database handle the same data and queries, using a small video store dataset of movies, actors, directors, and genres. This repository holds both halves: the MySQL schema and queries in [`mysql/videos.sql`](mysql/videos.sql), and the MongoDB implementation in Java.

More context is on my portfolio at [billyyork.com](https://billyyork.com).

## Data model

**MySQL.** The raw data is loaded as-is, then normalized into three tables: `people`, `recordings`, and `worked_on`, which records each person's role on a recording as either an actor or a director. Treating actors and directors as the same kind of entity handles the case where a director also acts in a movie, and a single role column avoids keeping two tables of the same relationship.

**MongoDB.** Each person keeps lists of their acting and directing credits, rather than each movie keeping lists of its actors and directors, since a director can also be an actor. A separate genres collection keeps a list of the movies in each genre. Both choices make the queries much easier to express with MongoDB's aggregation pipeline.

```
person: { name, actingCredits: [movie ids], directingCredits: [movie ids] }
movie:  { title, img, duration, rating, yearReleased, price, stockCount }
genre:  { name, movies: [movie ids] }
```

## Queries

Both implementations answer the same questions and return the same results. MongoDB timings:

| Query | Time |
| --- | --- |
| Number of movies in each genre | 0.425s |
| Number of in-stock movies in each genre | 0.056s |
| Genres each actor has appeared in | 0.220s |
| Actors who have appeared in more than one genre | 0.103s |
| Actors who have never appeared in a comedy | 0.115s |
| Actors who have appeared in both a comedy and an action & adventure movie | 0.107s |
| Directors who had cameos in their own movies | 0.083s |

My first working version took more than five minutes per query. Rewriting the queries around the aggregation pipeline's `match()`, a filter, and `lookup()`, which builds a virtual collection much like an SQL join, brought every query under half a second. For comparison, the simpler MySQL queries finished in under a hundredth of a second, which is what you'd expect from a relational database on a dataset this small.

## Running it

**MySQL:** run `mysql/videos.sql` from the repository root with `local_infile` enabled, since it loads the data files from `src/main/resources`.

**MongoDB:** the program needs Java 21, Maven, and a running MongoDB instance. It reads the connection string from the `mongodb.uri` system property, loads the data into a `final_project` database, and prints each query's results and timing:

```bash
mvn compile exec:java -Dexec.mainClass=Main -Dmongodb.uri=mongodb://localhost:27017
```

## Credits

The video store dataset in `src/main/resources` was provided with the coursework.

## License

MIT for the code.
