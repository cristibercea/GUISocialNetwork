package ubb.scs.map.guisocialnetwork.domain.validators;

import ubb.scs.map.guisocialnetwork.domain.entities.Message;

public class MessageValidator implements Validator<Message> {
    @Override
    public void validate(Message entity) throws ValidationException {
        if(entity.getId()<0) throw new ValidationException("Message id is negative!");
        UtilizatorValidator utilizatorValidator = new UtilizatorValidator();
        utilizatorValidator.validate(entity.getFrom());
        utilizatorValidator.validate(entity.getTo());
    }
}
