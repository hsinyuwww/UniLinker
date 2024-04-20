# Mobile App development Project - Group 4

- Dheekksha Rajesh Babu
- Vinoothna Gangadasu
- Hsin-Yu Wen
- Bini Chandra

## A Campus Social Media App: UniLinker
“Connect, Collaborate, and Create Your Campus Community” —From UniLinker

## Description: 

UniLinker is your go-to-app for making the most of campus life. Imagine finding the perfect group for study sessions, weekend games, workouts, or just relaxing coffee talks - that’s what we’re here for. Setting up your profile is easy, and in no time, you’ll be discovering folks on campus who are into the same things as you are.You'll always know what’s happening. So ready to dive into campus life?
Let’s make the connections happen!


## Features:
Updated features list: added features regarding communities and calendar, added messaging, profile special features etc after taking feedback from Prof after P1 submission)

* User auth:
Register, login, logout etc

* Home screen / feed: 
User sees options to navigate to messaging, logout on top.
Users can view posts that are posted. This app is for campus connect- so all posts posted are by students at Northeastern University and are visible to everyone using the app.
The horizontal menu on the bottom has: Home, My events, +, communities, profile.

* Event:
Displays all extended information about the event.
Clicking on location mention opens it in google maps.
You can comment on the event too, see all previous comments.
You can see all attendees, attend.

* Adding events:
On clicking +: user can create an event and community, and they can fill in the required details like title, community, description, location, time, date, community of event. Users can upload a picture for each event/community (stored in firebase storage).

* Edit event:
If you have created the event, you have the option, you can navigate to my events and edit all details of the event too.

* Profile:
Displays the profile pic, about, major, email.
Displays all the events you are attending, and all the communities you have joined.
Clicking on the event here shows full details of Event.
You can upload a picture too. (from camera and from gallery)
Implemented edit profile to change any details/pics.
 
* Communities:
Can view all the communities and their details.
Can join the community, see all posts posted in it and see participants of the community.

* Messaging:
Can search for users and chat with them.
Can view the users profile also from the chat (by clicking on view profile).

* Adding events to google calendar
When you “attend” an event, you can add it to you google account’s calendar. It saves at the correct time/date. 


**Special items:**
Messaging (as recommended by the professor)
Camera for profile picture


## Contributions: 

### **Hsin-Yu Wen:**
* wen.hsi@northeastern.edu
* Github: hsinyuwww
Messages & chat activity
- searching users 
- showing the chat list
- entering a chat, sending a message, and the real-time update of chat view.

### **Bini Chandra:**
* chandra.bi@northeastern.edu
* Github: Bini-C
- Register, login, and logout activities/functionalities (Firebase User Authentication)
- Profile activity, update profile data, and delete profile functionality.
- Upload profile picture functionality -camera, gallery, remove picture (Firebase Storage)
- Added recycler viewer to display myCommunities & myevents on profile page
- Added other users' profile viewing functionality

### **Vinoothna Gangadasu:**
* gangadasu.v@northeastern.edu
* Github: Vinoothna99
- Top and Bottom Navigation Bars in a Base Activity 
- ⁠Added App Logo
- ⁠Layout design -Recycler views for Events, Communities, Attendees and Comments
- ⁠Layout design -Create and Edit events, Create Communities, View a Community
- ⁠Implemented calendar intent functionality to add events to google calendar app

### **Dheekksha Rajesh Babu:** 
* rajeshbabu.d@northeastern.edu
* Github: dheekkshaR
- DB schema design for events, comments, communities.
- Implemented add event with image from firebase storage, edit event, delete event, attend event (add attendee), add comment, view comments, view attendees, create community, filter posts in community, filter posts for author.
- View event details and google maps intent for location.
[* My commits are on branch dheekksha, code used by teammates and merged to main]

## Testing credentials:
test@northeastern.edu
123456
user1@northeastern.edu
123456
user2@northeastern.edu
123456
We have Register so you can create your own account as well.
