package tk.leoforney.spotifydownloader;

/**
 * Created by Dynamic Signals on 11/28/2016.
 */

public class VideoItem {
    private String title;
    private String description;
    private String thumbnailURL;
    private String id;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public String getTitle() {
        return title;
    }
}
