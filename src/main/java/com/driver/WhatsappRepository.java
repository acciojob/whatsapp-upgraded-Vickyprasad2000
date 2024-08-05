package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most once group
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        //your code here
        if(userMobile.contains(mobile)) {
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group customGroupCount". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // If group is successfully created, return group.
        //your code here
        Group group;
        if (users.size() == 2) {
            group = new Group(users.get(1).getName(), 2);
        } else {
            customGroupCount++;
            group = new Group("Group " + customGroupCount, users.size());
        }
        groupUserMap.put(group, users);
        groupMessageMap.put(group, new ArrayList<>());
        adminMap.put(group, users.get(0));
        return group;
    }

    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        //your code here
        messageId++;
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        //your code here
        if (!groupUserMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }
        if (!groupUserMap.get(group).contains(sender)) {
            throw new Exception("You are not allowed to send message");
        }
        groupMessageMap.get(group).add(message);
        senderMap.put(message, sender);
        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS".

        //your code here
        if (!groupUserMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }
        if (!adminMap.get(group).equals(approver)) {
            throw new Exception("Approver does not have rights");
        }
        if (!groupUserMap.get(group).contains(user)) {
            throw new Exception("User is not a participant");
        }
        adminMap.put(group, user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        //your code here
        Group groupToRemoveUser = null;
        for (Map.Entry<Group, List<User>> entry : groupUserMap.entrySet()) {
            if (entry.getValue().contains(user)) {
                groupToRemoveUser = entry.getKey();
                break;
            }
        }
        if (groupToRemoveUser == null) {
            throw new Exception("User not found");
        }
        if (adminMap.get(groupToRemoveUser).equals(user)) {
            throw new Exception("Cannot remove admin");
        }
        groupUserMap.get(groupToRemoveUser).remove(user);
        List<Message> messages = groupMessageMap.get(groupToRemoveUser);
        for (Message message : messages) {
            senderMap.remove(message);
        }
        groupMessageMap.remove(groupToRemoveUser);
        groupUserMap.remove(groupToRemoveUser);
        adminMap.remove(groupToRemoveUser);
        return groupUserMap.get(groupToRemoveUser).size() + messages.size() + messageId;
    }

    public String findMessage(Date start, Date end, int K) throws Exception{
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        //your code here
        List<Message> allMessages = new ArrayList<>();
        for (List<Message> messages : groupMessageMap.values()) {
            allMessages.addAll(messages);
        }
        allMessages.sort((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()));

        int count = 0;
        for (Message message : allMessages) {
            if (message.getTimestamp().after(start) && message.getTimestamp().before(end)) {
                count++;
                if (count == K) {
                    return message.getContent();
                }
            }
        }
        throw new Exception("K is greater than the number of messages");
    }
}