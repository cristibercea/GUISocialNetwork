package ubb.scs.map.guisocialnetwork.repository.dbrepo;

import ubb.scs.map.guisocialnetwork.domain.entities.*;
import ubb.scs.map.guisocialnetwork.utils.Pair;
import ubb.scs.map.guisocialnetwork.domain.validators.Validator;
import ubb.scs.map.guisocialnetwork.repository.Repository;
import ubb.scs.map.guisocialnetwork.repository.RepositoryException;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class ReteaDB implements Repository<Tuple<Utilizator,Utilizator>, Friendship> {
    private final String url;
    private final String username;
    private final String password;
    private final Validator<Friendship> validator;
    private final UtilizatorDbRepository utilizatorDbRepository;

    public ReteaDB(String url, String username, String password, Validator<Friendship> validator, UtilizatorDbRepository udbRepository) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
        this.utilizatorDbRepository = udbRepository;
    }

    @Override
    public Optional<Friendship> findOne(Tuple<Utilizator, Utilizator> utilizatorUtilizatorTuple) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from \"Friendships\" where uid1=? and uid2=?");
        ) {
            statement.setLong(1, utilizatorUtilizatorTuple.getE1().getId());
            statement.setLong(2, utilizatorUtilizatorTuple.getE2().getId());
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                statement.setLong(2, utilizatorUtilizatorTuple.getE1().getId());
                statement.setLong(1, utilizatorUtilizatorTuple.getE2().getId());
                resultSet = statement.executeQuery();
                if (!resultSet.next()) return Optional.empty();
                Friendship f = new Friendship();
                f.setId(new Tuple<>(utilizatorUtilizatorTuple.getE2(),utilizatorUtilizatorTuple.getE1()));
                f.setDate(LocalDate.parse(resultSet.getString("fdate")));
                return Optional.of(f);
            }
            Friendship f = new Friendship();
            f.setId(utilizatorUtilizatorTuple);
            f.setDate(LocalDate.parse(resultSet.getString("fdate")));
            return Optional.of(f);
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }

    @Override
    public Iterable<Friendship> findAll() {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from \"Friendships\"");) {
            return getResultsFromStatement(statement);
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }

    public Iterable<Friendship> filterFriendshipsForUserId(Long userId) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from \"Friendships\" where status='accepted' and (uid1=? or uid2=?)");
        ) {
            statement.setLong(1, userId);
            statement.setLong(2, userId);
            return getResultsFromStatement(statement);
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }

    public Page<Friendship> filterFriendshipsForUserIdPaged(Long userId, Pageable pageable) throws RepositoryException {
        return new Page<>(findAllOnPage(pageable,userId),numberOfFriends(userId).orElse(-1L));
    }

    public Iterable<Friendship> findAllOnPage(Pageable pageable, Long userId) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from \"Friendships\" where status='accepted' and (uid1=? or uid2=?) limit ? offset ?");
        ) {
            statement.setLong(1, userId);
            statement.setLong(2, userId);
            statement.setLong(3, pageable.getSize());
            statement.setInt(4, pageable.getPage()*pageable.getSize());
            return getResultsFromStatement(statement);
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }

    public Optional<Long> numberOfFriends(Long userId) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT count(*) as nr from \"Friendships\" where status='accepted' and (uid1=? or uid2=?)");
        ) {
            statement.setLong(1, userId);
            statement.setLong(2, userId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) return Optional.of(resultSet.getLong("nr"));
            return Optional.empty();
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }

    public Iterable<Friendship> filterReceivedFriendshipsForUserId(Long userId) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from \"Friendships\" where status='pending' and uid2=?");
        ) {
            statement.setLong(1, userId);
            return getResultsFromStatement(statement);
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }

    public Iterable<Friendship> filterSentFriendshipsForUserId(Long userId) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from \"Friendships\" where status='pending' and uid1=?");
        ) {
            statement.setLong(1, userId);
            return getResultsFromStatement(statement);
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }

    private Iterable<Friendship> getResultsFromStatement(PreparedStatement statement) throws SQLException {
        ResultSet resultSet = statement.executeQuery();
        Set<Friendship> friendships = new HashSet<>();
        while(resultSet.next()) {
            Long id = resultSet.getLong("uid1");
            Long id2 = resultSet.getLong("uid2");
            Optional<Utilizator> u1 = utilizatorDbRepository.findOne(id);
            Optional<Utilizator> u2 = utilizatorDbRepository.findOne(id2);
            Tuple<Utilizator, Utilizator> tuple = new Tuple<>(u1.orElse(null), u2.orElse(null));
            Friendship f = new Friendship();
            f.setId(tuple);
            f.setDate(LocalDate.parse(resultSet.getString("fdate")));
            f.setStatus(resultSet.getString("status"));
            friendships.add(f);
        }
        return friendships;
    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        validator.validate(entity);
        if(findOne(entity.getId()).isPresent()) throw new RepositoryException("Prietenia exista deja!");
        int rez = -1;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO \"Friendships\" (uid1, uid2, fdate, status) VALUES (?, ?, ?, ?)");
        ) {
            statement.setLong(1, entity.getId().getE1().getId());
            statement.setLong(2, entity.getId().getE2().getId());
            statement.setDate(3, Date.valueOf(entity.getDate()));
            statement.setString(4, entity.getStatus());
            rez = statement.executeUpdate();
        } catch (SQLException e) {throw new RepositoryException("Database error");}
        if (rez > 0) return Optional.empty();
        else return Optional.of(entity);
    }

    @Override
    public Optional<Friendship> delete(Tuple<Utilizator, Utilizator> utilizatorUtilizatorTuple) {
        if (utilizatorUtilizatorTuple==null) throw new IllegalArgumentException("Invalid user ID");
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("Delete from \"Friendships\" where uid1=? and uid2=?");
        ) {
            Optional<Friendship> friendship = findOne(utilizatorUtilizatorTuple);
            if (friendship.isEmpty()) return Optional.empty();
            statement.setLong(1, friendship.get().getId().getE1().getId());
            statement.setLong(2, friendship.get().getId().getE2().getId());
            statement.executeUpdate();
            return friendship;
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("update \"Friendships\" set status=?, fdate=? where uid1=? and uid2=?");
        ) {
            statement.setString(1, entity.getStatus());
            statement.setDate(2, Date.valueOf(LocalDate.now()));
            statement.setLong(3, entity.getId().getE1().getId());
            statement.setLong(4, entity.getId().getE2().getId());
            if(statement.executeUpdate()>0) return Optional.empty();
            return Optional.of(entity);
        } catch (SQLException ignored) {throw new RepositoryException("Database error");}
    }

    public UtilizatorDbRepository getUtilizatorRepository() {return utilizatorDbRepository;}

    public Map<Utilizator, List<Utilizator>> getReteaGraph(ArrayList<Pair<Utilizator,Boolean>> visited){
        Map<Utilizator, List<Utilizator>> graph = new HashMap<>();
        findAll().forEach(friendship->{
            insertInFriendshipGraph(graph, friendship.getId().getE1(), visited);
            insertInFriendshipGraph(graph, friendship.getId().getE2(), visited);

            if(graph.containsKey(friendship.getId().getE1())){
                graph.get(friendship.getId().getE1()).add(friendship.getId().getE2());
            }

            if(graph.containsKey(friendship.getId().getE2())){
                graph.get(friendship.getId().getE2()).add(friendship.getId().getE1());
            }
        });
        return graph;
    }

    private void insertInFriendshipGraph(Map<Utilizator, List<Utilizator>> graph, Utilizator utilizator, ArrayList<Pair<Utilizator,Boolean>> visited) {
        if(graph.putIfAbsent(utilizator, new ArrayList<>())==null)
            visited.add(new Pair<>(utilizator, false));
    }
}
