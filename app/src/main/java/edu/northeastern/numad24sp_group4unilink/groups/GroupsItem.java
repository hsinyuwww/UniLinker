package edu.northeastern.numad24sp_group4unilink.groups;

public class GroupsItem implements GroupsInterface{

    private String title;
    private String image;

    private String groupID;

    public GroupsItem(String title, String image, String groupID ) {
        this.groupID = groupID;
        this.title = title;
        this.image = image;


    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getGroupID() {
        return groupID;
    }

    @Override
    public void onJoinClick(int position) {

    }

    @Override
    public void onCommunityClick(int position) {

    }
}
