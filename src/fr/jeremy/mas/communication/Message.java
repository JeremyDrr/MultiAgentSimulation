package fr.jeremy.mas.communication;

public class Message {

    public String sender;
    public String inReplyTo;
    public String replyWith;
    public Operation operation;
    public String successCode;

    public Message(String sender, String code) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", inReplyTo='" + inReplyTo + '\'' +
                ", replyWith='" + replyWith + '\'' +
                ", operation=" + operation +
                ", successCode='" + successCode + '\'' +
                '}';
    }

    public Operation getOperation() {
        return operation;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getInReplyTo() {
        return inReplyTo;
    }

    public void setInReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public String getReplyWith() {
        return replyWith;
    }

    public void setReplyWith(String replyWith) {
        this.replyWith = replyWith;
    }

    public String getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(String successCode) {
        this.successCode = successCode;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}
