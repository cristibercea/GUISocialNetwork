package ubb.scs.map.guisocialnetwork.services;

import ubb.scs.map.guisocialnetwork.domain.entities.*;
import ubb.scs.map.guisocialnetwork.repository.dbrepo.MessageDBRepository;
import ubb.scs.map.guisocialnetwork.utils.IterativeDFS;
import ubb.scs.map.guisocialnetwork.utils.Pair;
import ubb.scs.map.guisocialnetwork.repository.RepositoryException;
import ubb.scs.map.guisocialnetwork.repository.dbrepo.ReteaDB;
import ubb.scs.map.guisocialnetwork.utils.events.ChangeEventType;
import ubb.scs.map.guisocialnetwork.utils.events.UtilizatorEntityChangeEvent;
import ubb.scs.map.guisocialnetwork.utils.observer.Observable;
import ubb.scs.map.guisocialnetwork.utils.observer.Observer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public class Service implements Observable<UtilizatorEntityChangeEvent> {
    private final ReteaDB retea;
    private final MessageDBRepository messageDBRepository;
    private final List<Observer<UtilizatorEntityChangeEvent>> observers=new ArrayList<>();

    public Service(ReteaDB reteaRepo, MessageDBRepository messageDBRepository) {this.retea = reteaRepo; this.messageDBRepository = messageDBRepository;}

    @Override
    public void addObserver(Observer<UtilizatorEntityChangeEvent> e) {observers.add(e);}

    @Override
    public void removeObserver(Observer<UtilizatorEntityChangeEvent> e) {observers.remove(e);}

    @Override
    public void notifyObservers(UtilizatorEntityChangeEvent t) {observers.stream().forEach(x->x.update(t));}

    public Optional<Utilizator> getUtilizator(Long id) {return retea.getUtilizatorRepository().findOne(id);}

    public Iterable<Utilizator> getAllForCurrentUser(Utilizator loggedIn) {
        List<Utilizator> currentUserFriends = new ArrayList<>();
        retea.filterFriendshipsForUserId(loggedIn.getId()).forEach(x->{
            if(Objects.equals(x.getId().getE1().getId(), loggedIn.getId())) currentUserFriends.add(x.getId().getE2());
            else currentUserFriends.add(x.getId().getE1());
        });
        return currentUserFriends;
    }

    public Page<Utilizator> getAllForCurrentUserPaged(Utilizator loggedIn, Pageable pageable) {
        List<Utilizator> currentUserFriends = new ArrayList<>();
        retea.filterFriendshipsForUserIdPaged(loggedIn.getId(),pageable).getObjects().forEach(x->{
            if(Objects.equals(x.getId().getE1().getId(), loggedIn.getId())) currentUserFriends.add(x.getId().getE2());
            else currentUserFriends.add(x.getId().getE1());
        });
        return new Page<>(currentUserFriends, retea.numberOfFriends(loggedIn.getId()).orElse(-1L));
    }

    public long getNumberOfFriends(Utilizator loggedIn) {
        return retea.numberOfFriends(loggedIn.getId()).orElse(-1L);
    }

    public Iterable<Utilizator> getAllSentForCurrentUser(Utilizator loggedIn) {
        List<Utilizator> currentUserFriends = new ArrayList<>();
        retea.filterSentFriendshipsForUserId(loggedIn.getId()).forEach(x->currentUserFriends.add(x.getId().getE2()));
        return currentUserFriends;
    }

    public Iterable<Utilizator> getAllReceivedForCurrentUser(Utilizator loggedIn) {
        List<Utilizator> currentUserFriends = new ArrayList<>();
        retea.filterReceivedFriendshipsForUserId(loggedIn.getId()).forEach(x->currentUserFriends.add(x.getId().getE1()));
        return currentUserFriends;
    }

    public Iterable<Utilizator> getAllNonFriends(Utilizator loggedIn) {
        Iterable<Utilizator> friends = getAllForCurrentUser(loggedIn);
        Iterable<Utilizator> sent = getAllSentForCurrentUser(loggedIn);
        Iterable<Utilizator> received = getAllReceivedForCurrentUser(loggedIn);
        HashSet<Utilizator> nonFriends = (HashSet<Utilizator>) retea.getUtilizatorRepository().findAll();
        friends.forEach(nonFriends::remove);
        sent.forEach(nonFriends::remove);
        received.forEach(nonFriends::remove);
        nonFriends.remove(loggedIn);
        return nonFriends;
    }

    public Utilizator addUtilizator(Utilizator entity) throws ServiceException {
        try {
            if(retea.getUtilizatorRepository().save(entity).isEmpty()){
                UtilizatorEntityChangeEvent event = new UtilizatorEntityChangeEvent(ChangeEventType.ADD, entity);
                notifyObservers(event);
                return null;
            }
            return entity;
        }
        catch (RepositoryException e) {throw new ServiceException(e);}
    }

    public Utilizator deleteUtilizator(Long id) throws ServiceException {
        try {
            Optional<Utilizator> user=retea.getUtilizatorRepository().delete(id);
            if (user.isPresent()) {
                notifyObservers(new UtilizatorEntityChangeEvent(ChangeEventType.DELETE, user.get()));
                return user.get();
            }
            return null;
        } catch (RepositoryException e) {throw new ServiceException(e);}
    }

    public Utilizator updateUtilizator(Utilizator entity) throws ServiceException {
        try{
            Optional<Utilizator> oldUser=getUtilizator(entity.getId());
            if(oldUser.isPresent()) {
                Optional<Utilizator> newUser=retea.getUtilizatorRepository().update(entity);
                if (newUser.isEmpty())
                    notifyObservers(new UtilizatorEntityChangeEvent(ChangeEventType.UPDATE, entity, oldUser.get()));
                return newUser.orElse(null);
            }
            return null;
        } catch (RepositoryException e) {throw new ServiceException(e);}
    }

    public String getPassword(String email) throws ServiceException {
        try { return retea.getUtilizatorRepository().getPassword(email).orElse(null);}
        catch (RepositoryException e) {throw new ServiceException(e);}
    }

    public void setPassword(String pass, Long userId) throws ServiceException {
        try { retea.getUtilizatorRepository().setPassword(pass, userId);}
        catch (RepositoryException e) {throw new ServiceException(e);}
    }

    public Utilizator getUserByEmail(String email) throws ServiceException {
        try { return retea.getUtilizatorRepository().findByEmail(email).orElse(null);}
        catch (RepositoryException e) {throw new ServiceException(e);}
    }

    public Friendship addFriendship(Friendship entity, Utilizator loggedIn) throws ServiceException {
        try {
            if(retea.save(entity).isEmpty()) {
                Utilizator friend = entity.getId().getE1();
                if(entity.getId().getE1().equals(loggedIn)) friend = entity.getId().getE2();
                notifyObservers(new UtilizatorEntityChangeEvent(ChangeEventType.ADD, friend));
                return null;
            }
            return entity;
        }
        catch (RepositoryException e) {throw new ServiceException(e);}
    }

    public void updateFriendship(Friendship entity) throws ServiceException {
        try {
            if(retea.update(entity).isEmpty())
                notifyObservers(new UtilizatorEntityChangeEvent(ChangeEventType.UPDATE, entity.getId().getE1()));
        }
        catch (RepositoryException e) {throw new ServiceException(e);}
    }

    public Friendship removeFriendship(Friendship friendship, Utilizator loggedIn) throws ServiceException {
        try {
            Optional<Friendship> deleted = retea.delete(friendship.getId());
            if(deleted.isEmpty() && Objects.equals(friendship.getStatus(), "accepted"))
                deleted = retea.delete(new Tuple<>(friendship.getId().getE2(), loggedIn));
            if(deleted.isPresent()) {
                Utilizator unfriended = deleted.get().getId().getE1();
                if(deleted.get().getId().getE1().equals(loggedIn)) unfriended = deleted.get().getId().getE2();
                notifyObservers(new UtilizatorEntityChangeEvent(ChangeEventType.DELETE, unfriended));
                return deleted.get();
            }
            return null;
        }
        catch (RepositoryException e) {throw new ServiceException(e);}
    }

    public void saveMessage(String text, Tuple<Utilizator,Utilizator> friendshipId) throws ServiceException {
        Optional<Friendship> chatOwners = retea.findOne(friendshipId);
        if(chatOwners.isEmpty()) {throw new ServiceException("There was an error in identifying current session!");}
        Message message = new Message(text,chatOwners.get().getId().getE1(),chatOwners.get().getId().getE2());
        Predicate<Friendship> reversed = x -> !friendshipId.getE1().equals(x.getId().getE1());
        try {
            if(messageDBRepository.create(message, reversed.test(chatOwners.get())).isEmpty())
                notifyObservers(new UtilizatorEntityChangeEvent(ChangeEventType.ADD, null));
        } catch (RepositoryException e) {throw new ServiceException(e);}
    }

    public Iterable<MessageDTO> getAllMessagesForFriendship(Tuple<Utilizator,Utilizator> friendshipId, double prefWidth) throws ServiceException {
        Optional<Friendship> chatOwners = retea.findOne(friendshipId);
        if(chatOwners.isEmpty()) {throw new ServiceException("There was an error in identifying current session!");}
        Iterable<Message> messages = StreamSupport.stream(messageDBRepository.findAllByFriendship(chatOwners.get()).spliterator(), false)
                .sorted(Comparator.comparing(Message::getDate)).toList();
        List<MessageDTO> result = new ArrayList<>();
        messages.forEach(x-> {
            int length = (int) prefWidth - 133;
            int count = 0;
            StringBuilder builder = new StringBuilder();
            if (x.getMessage().length() > length){
                while (count < x.getMessage().length()) {
                    builder.append(x.getMessage(), count, Math.min(length, x.getMessage().length())).append('\n');
                    count = length + 1;
                    length += (int) prefWidth - 133;
                }
                builder.append('\n');
            }else builder.append(x.getMessage());
            MessageDTO dto = getMessageDTO(friendshipId, x, builder.toString());
            result.add(dto);
        });
        return result;
    }

    private MessageDTO getMessageDTO(Tuple<Utilizator, Utilizator> friendshipId, Message x, String message) {
        MessageDTO dto = new MessageDTO();
        String[] datetime = x.getDate().toString().split("T");
        String[] time = datetime[1].split("\\.");
        if(x.getFrom().equals(friendshipId.getE1())) {
            dto.setMessageLeft("<Sent: "+ datetime[0]+">     \n      <"+time[0]+">     ");
            dto.setMessageRight(message);
        }else{
            dto.setMessageLeft(message);
            dto.setMessageRight("     <Received: "+ datetime[0]+">\n           <"+time[0]+">");
        }
        return dto;
    }


    public ArrayList<ArrayList<Utilizator>> conexComponents(){
        ArrayList<ArrayList<Utilizator>> components = new ArrayList<>();
        ArrayList<Pair<Utilizator,Boolean>> visited = new ArrayList<>();
        Map<Utilizator, List<Utilizator>> graph = retea.getReteaGraph(visited);
        visited.forEach(el->{
            if(!el.getY()) {
                ArrayList<Utilizator> found = IterativeDFS.conexComponent(graph, el.getX()).getY();
                components.add(found);
                visited.forEach(pair->{if(found.contains(pair.getX())) pair.setY(true);});
            }
        });
        return components;
    }

    public ArrayList<Utilizator> biggestConexComponent(){
        AtomicReference<ArrayList<Utilizator>> longest = new AtomicReference<>(new ArrayList<>());
        AtomicInteger maxLength= new AtomicInteger(-1);
        ArrayList<Pair<Utilizator,Boolean>> visited = new ArrayList<>();
        Map<Utilizator, List<Utilizator>> graph = retea.getReteaGraph(visited);
        visited.forEach(el->{
            if(!el.getY()) {
                Pair<Integer, ArrayList<Utilizator>> found = IterativeDFS.conexComponent(graph, el.getX());
                if (maxLength.get() < found.getX()) {
                    maxLength.set(found.getX());
                    longest.set(found.getY());
                }
                visited.forEach(pair -> {if (found.getY().contains(pair.getX())) pair.setY(true);});
            }
        });
        return longest.get();
    }
}
