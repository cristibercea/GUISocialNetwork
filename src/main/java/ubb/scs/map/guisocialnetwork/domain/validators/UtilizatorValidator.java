package ubb.scs.map.guisocialnetwork.domain.validators;

import ubb.scs.map.guisocialnetwork.domain.entities.Utilizator;

public class UtilizatorValidator implements Validator<Utilizator> {
    private final EmailValidator emailValidator = new EmailValidator();
    @Override
    public void validate(Utilizator entity) throws ValidationException {
        if(entity.getFirstName().isEmpty())
            throw new ValidationException("Numele utilizatorului nu este valid!");
        if(entity.getLastName().isEmpty())
            throw new ValidationException("Prenumele utilizatorului nu este valid!");
        emailValidator.validate(entity.getEmail());
        if(Character.isLowerCase(entity.getFirstName().toCharArray()[0]))
            throw new ValidationException("Numele utilizatorului trebuie sa inceapa cu litera mare!");
        if(Character.isLowerCase(entity.getLastName().toCharArray()[0]))
            throw new ValidationException("Prenumele utilizatorului trebuie sa inceapa cu litera mare!");
    }
}
