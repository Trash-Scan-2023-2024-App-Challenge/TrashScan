package com.trashscan.trashscan;

public class Post {
    private String parkNameTextView;
    private String parkDescriptionTextView;
    private String parkImageView;



    public Post(String parkNameTextView, String parkDescriptionTextView, String parkImageView) {
        this.parkNameTextView = parkNameTextView;
        this.parkDescriptionTextView = parkDescriptionTextView;
        this.parkImageView = parkImageView;
    }

    public String getParkNameTextView() {
        return parkNameTextView;
    }

    public void setParkNameTextView(String parkNameTextView) {
        this.parkNameTextView = parkNameTextView;
    }

    public String getParkDescriptionTextView() {
        return parkDescriptionTextView;
    }

    public void setParkDescriptionTextView(String parkDescriptionTextView) {
        this.parkDescriptionTextView = parkDescriptionTextView;
    }

    public String getParkImageView() {
        return parkImageView;
    }

    public void setParkImageView(String parkImageView) {
        this.parkImageView = parkImageView;
    }
}
