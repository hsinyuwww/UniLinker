package edu.northeastern.numad24sp_group4unilink.events;


public class EventItem implements EventInterface{


    private String location;
    private String date;
    private String time;
    private String title;
    private String image;
    private String community;
    private String description;
    private String eventID;



    public EventItem(String title, String description, String date, String time, String image, String location, String community, String eventID) {
        this.eventID = eventID;
        this.title = title;
        this.description=description;
        this.date=date;
        this.time=time;
        this.image = image;
        this.location = location;
        this.community = community;


    }
    @Override
    public void onLocationClick(int position) {

    }

    @Override
    public void onAttendeesClick(int position) {

    }

    @Override
    public void onAttendClick(int position) {

    }

    @Override
    public void onCommentClick(int position) {

    }

    @Override
    public void onEditClick(int position) {

    }

    @Override
    public void onDeleteClick(int position) {

    }


    public String getEventID() {
        return eventID;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getCommunity() {
        return community;
    }

    public String getDescription() {
        return description;
    }
}
