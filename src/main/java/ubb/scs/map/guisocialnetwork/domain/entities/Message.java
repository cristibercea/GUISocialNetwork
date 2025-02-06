package ubb.scs.map.guisocialnetwork.domain.entities;

import java.time.LocalDateTime;

public class Message {
    private Long id = 0L;
    private String message;
    private LocalDateTime date;
    private Utilizator from, to;

    public Message(String message, Utilizator from, Utilizator to) {
        this.message = message;
        this.from = from;
        this.to = to;
        this.date = LocalDateTime.now();
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getMessage() {return message;}

    public void setMessage(String message) {this.message = message; this.date = LocalDateTime.now();}

    public LocalDateTime getDate() {return date;}

    public void setDate(LocalDateTime date) {this.date = date;}

    public Utilizator getFrom() {return from;}

    public void setFrom(Utilizator from) {this.from = from;}

    public Utilizator getTo() {return to;}

    public void setTo(Utilizator to) {this.to = to;}

    @Override
    public String toString() {
        //return text + " (" + date.getHour() + ":" + date.getMinute() + ":" + date.getSecond()+")";
        return message;
    }
}
