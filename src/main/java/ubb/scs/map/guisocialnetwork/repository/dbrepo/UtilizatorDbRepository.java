package ubb.scs.map.guisocialnetwork.repository.dbrepo;

import ubb.scs.map.guisocialnetwork.domain.entities.Utilizator;
import ubb.scs.map.guisocialnetwork.domain.validators.Validator;
import ubb.scs.map.guisocialnetwork.repository.Repository;
import ubb.scs.map.guisocialnetwork.repository.RepositoryException;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UtilizatorDbRepository implements Repository<Long, Utilizator> {
    private String url;
    private String username;
    private String password;
    private Validator<Utilizator> validator;

    public UtilizatorDbRepository(String url, String username, String password, Validator<Utilizator> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    private Utilizator createUserFromResultSet(ResultSet resultSet) {
        try {
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            String email = resultSet.getString("email");
            Long idd = resultSet.getLong("id");
            Utilizator user = new Utilizator(firstName, lastName, email);
            user.setId(idd);
            return user;
        } catch (SQLException e) {return null;}
    }

    @Override
    public Optional<Utilizator> findOne(Long id) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from \"Users\" U where U.id= '%d'", id))) {
            if(resultSet.next()){
                Utilizator user = createUserFromResultSet(resultSet);
                return Optional.ofNullable(user);
            }
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
        return Optional.empty();
    }

    @Override
    public Iterable<Utilizator> findAll() {
        Set<Utilizator> users = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from \"Users\"");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");

                Utilizator utilizator = new Utilizator(firstName, lastName, email);
                utilizator.setId(id);
                users.add(utilizator);
            }
            return users;
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }

    @Override
    public Optional<Utilizator> save(Utilizator entity) {
        int rez=-1;
        String sql = "insert into \"Users\" (first_name, last_name, email, password) values (?, ?, ?, ?)";
        validator.validate(entity);
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getFirstName());
            ps.setString(2, entity.getLastName());
            ps.setString(3, entity.getEmail());
            ps.setString(4, "to-be-set");
            rez = ps.executeUpdate();
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
        if (rez > 0) return Optional.empty();
        else return Optional.of(entity);
    }

    @Override
    public Optional<Utilizator> delete(Long id) {
        String sql = "delete from \"Users\" where id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            Optional<Utilizator> user = findOne(id);
            if (user.isEmpty()) return Optional.empty();
            ps.setLong(1, id);
            ps.executeUpdate();
            return user;
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }

    @Override
    public Optional<Utilizator> update(Utilizator user) {
        if(user == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(user);
        String sql = "update \"Users\" set first_name = ?, last_name = ?, email = ? where id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setLong(4, user.getId());
            if( ps.executeUpdate() > 0 ) return Optional.empty();
            return Optional.of(user);
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }


    public Optional<String> getPassword(String email) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement()
                    .executeQuery(String.format("select U.password from \"Users\" U where U.email= '%s'", email))) {
            if(resultSet.next()) return Optional.ofNullable(resultSet.getString("password"));
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
        return Optional.empty();
    }

    public Optional<Utilizator> findByEmail(String email) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement()
                    .executeQuery(String.format("select * from \"Users\" U where U.email= '%s'", email))) {
            if(resultSet.next()) return Optional.ofNullable(createUserFromResultSet(resultSet));
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
        return Optional.empty();
    }

    public void setPassword(String pass, Long userId){
        String sql = "update \"Users\" set password = ? where id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pass);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }
}
