package br.com.lamassu;

public class Message {

    private final String id;
    private String content;

    public Message(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void updateContent(String content) {
        this.content = content;
    }
    
}
