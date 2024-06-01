package fr.jeremy.mas.communication;

import fr.jeremy.mas.utils.TileWorldService;

import java.util.ArrayList;
import java.util.List;

public class MessageBox {

    public String name;
    public TileWorldService tileWorldService;
    public String owner;
    public List<Message> messageList = new ArrayList<Message>();
    public int expectedMessages;

    public MessageBox(String owner, int expectedMessages, TileWorldService tileWorldService){
        this.name = owner;
        this.owner = owner;
        this.expectedMessages = expectedMessages;
        this.tileWorldService = tileWorldService;
    }

    public String getOwner() {
        return owner;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public int getExpectedMessages() {
        return expectedMessages;
    }

    public synchronized void checkMessageList(String threadName) {

        try{
            if(messageList.size() < expectedMessages){
                System.out.println(this.name + ": checkMessageList(): Wait: messageList.size()=" + messageList.size() + ". expectedMessages=" + expectedMessages + ")");
                wait();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public synchronized void checkNegotiationMessageList(String threadName){
        try{
            if(messageList.size() < expectedMessages){
                System.out.println(this.name + ": checkMessageList(): Wait: messageList.size()=" + messageList.size() + ". expectedMessages=" + expectedMessages + ")");
                wait();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public synchronized void isMessageListProcessed(){
        try{
            System.out.println("isMessageListProcessed(): Wait: messageList.size()=" + messageList.size() + ")");
            if(!messageList.isEmpty()) {
                wait();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public synchronized void emptyMessageList(String threadName) {
        this.messageList.clear();
        System.out.println(this.name + ": emptyMessageList(): messageList.size()=" + messageList.size() + ")");
        notifyAll();
    }

    public synchronized void addStatusMessage(Message message) {
        messageList.add(message);
        System.out.println("addStatusMessage(): messageList.size()=" + messageList.size() + ")");
        notifyAll();
    }

    public synchronized void addNegotiationMessage(Message message) {
        messageList.add(message);
        System.out.println("addNegotiationMessage(): messageList.size()=${messageList.size()}; expectedMessages=${expectedMessages -1}");
        if(messageList.size() == (expectedMessages - 1)) {
            notifyAll();
        }
    }

    public synchronized void addMessage(Message message) {
        messageList.add(message);
        System.out.println("addMessage(): owner="+ this.owner + ": messageList.size()=" + messageList.size() + "expectedMessages=" + expectedMessages + ")");
        if(messageList.size() == expectedMessages) {
            notifyAll();
        }
    }

}
