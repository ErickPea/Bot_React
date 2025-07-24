package botinteractions.models;

public class Profile {
    private String name;
    private String profileId;

    public Profile(String name, String profileId) {
        this.name = name;
        this.profileId = profileId;
    }

    public String getName() {
        return name;
    }

    public String getProfileId() {
        return profileId;
    }

    @Override
    public String toString() {
        return "Profile{name='" + name + "', profileId='" + profileId + "'}";
    }
}
