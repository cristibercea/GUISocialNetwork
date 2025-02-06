package ubb.scs.map.guisocialnetwork.domain.validators;

public interface Validator<T> {
    void validate(T entity) throws ValidationException;
}