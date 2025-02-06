package ubb.scs.map.guisocialnetwork.domain.entities;

import java.util.Objects;

public class FriendRequest extends Entity<Friendship> {
    private String status;

    public FriendRequest(String status) {this.status=status;}

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status=status;}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FriendRequest that = (FriendRequest) o;
        return Objects.equals(status, that.status) && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, getId());
    }
}
