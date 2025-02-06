package ubb.scs.map.guisocialnetwork.repository.dbrepo;

import ubb.scs.map.guisocialnetwork.domain.entities.Friendship;
import ubb.scs.map.guisocialnetwork.domain.entities.Message;
import ubb.scs.map.guisocialnetwork.domain.entities.Utilizator;
import ubb.scs.map.guisocialnetwork.domain.validators.Validator;
import ubb.scs.map.guisocialnetwork.repository.RepositoryException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MessageDBRepository {
    private final String url;
    private final String username;
    private final String password;
    private final Validator<Message> validator;

    public MessageDBRepository(String url, String username, String password, Validator<Message> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    public Optional<Message> create(Message message, boolean reversed) {
        validator.validate(message);
        int rez = -1;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO \"Messages\" (sid, did, message, date, reversed) VALUES (?, ?, ?, ?, ?)");
        ) {
            statement.setLong(1, message.getFrom().getId());
            statement.setLong(2, message.getTo().getId());
            statement.setString(3, message.getMessage());
            statement.setString(4, message.getDate().toString());
            statement.setBoolean(5, reversed);
            rez = statement.executeUpdate();
        } catch (SQLException e) {throw new RepositoryException("Database error");}
        if (rez > 0) return Optional.empty();
        else return Optional.of(message);
    }

    public Iterable<Message> findAllByFriendship(Friendship friendship) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from \"Messages\" where (sid=? and did=?) or (sid=? and did=?) order by date asc");) {
            statement.setLong(1,friendship.getId().getE1().getId());
            statement.setLong(2,friendship.getId().getE2().getId());
            statement.setLong(3, friendship.getId().getE2().getId());
            statement.setLong(4, friendship.getId().getE1().getId());
            ResultSet resultSet = statement.executeQuery();

            Set<Message> messages = new HashSet<>();
            while(resultSet.next()) {
                Utilizator u1,u2;
                if(!resultSet.getBoolean("reversed")) {
                    u1 = friendship.getId().getE1();
                    u2 = friendship.getId().getE2();
                } else {
                    u1 = friendship.getId().getE2();
                    u2 = friendship.getId().getE1();
                }
                String text = resultSet.getString("message");
                LocalDateTime timestamp = LocalDateTime.parse(resultSet.getString("date"));
                Message message = new Message(text,u1,u2);
                message.setId(resultSet.getLong("id"));
                message.setDate(timestamp);
                messages.add(message);
            }
            return messages;
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }
}
