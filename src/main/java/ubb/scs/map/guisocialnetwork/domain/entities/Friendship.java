package ubb.scs.map.guisocialnetwork.domain.entities;

import java.time.LocalDate;
import java.util.Objects;

public class Friendship extends Entity <Tuple<Utilizator, Utilizator>> {
    private LocalDate date = LocalDate.now();
    private String status;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date=date;
    }

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status=status;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return this.getId().equals(that.getId())&&status.equals(that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, status, getId());
    }
}
