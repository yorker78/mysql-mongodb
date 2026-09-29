import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Set;

public class PersonModel {
    private final String name;
    private final Set<Integer> oldActingCredits;
    private final Set<ObjectId> actingCredits;
    private final Set<ObjectId> directingCredits;

    public PersonModel(String name) {
        this.name = name;
        this.oldActingCredits = new HashSet<>();
        this.actingCredits = new HashSet<>();
        this.directingCredits = new HashSet<>();
    }

    public String getName(){
        return this.name;
    }

    public void addOldActingCredit(int credit) {
        this.oldActingCredits.add(credit);
    }

    public Set<Integer> getOldActingCredits() {
        return this.oldActingCredits;
    }

    public void addNewActingCredit(ObjectId newCredit) {
        this.actingCredits.add(newCredit);
    }

    public void addDirectingCredit(ObjectId credit) {
        directingCredits.add(credit);
    }

    public Document getDocument() {
        Document personDocument = new Document();
        personDocument.append("name", name);
        if(!this.actingCredits.isEmpty()) {
            personDocument.append("actingCredits", this.actingCredits);
        }
        if(!this.directingCredits.isEmpty()) {
            personDocument.append("directingCredits", this.directingCredits);
        }
        return personDocument;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof PersonModel) {
            return ((PersonModel) o).name.equals(this.name);
        }
        return false;
    }
}
