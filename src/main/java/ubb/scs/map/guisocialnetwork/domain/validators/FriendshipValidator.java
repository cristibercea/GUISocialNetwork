package ubb.scs.map.guisocialnetwork.domain.validators;

import ubb.scs.map.guisocialnetwork.domain.entities.Friendship;

public class FriendshipValidator implements Validator<Friendship> {
    @Override
    public void validate(Friendship entity) throws ValidationException {
        if(entity.getId().getE1()==null || entity.getId().getE2()==null)
            throw new ValidationException("Prietenia trebuie facuta intre doua entitati nenule");
        UtilizatorValidator utilizatorValidator = new UtilizatorValidator();
        utilizatorValidator.validate(entity.getId().getE1());
        utilizatorValidator.validate(entity.getId().getE2());
    }
}
