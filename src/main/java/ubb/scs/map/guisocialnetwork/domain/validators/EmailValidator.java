package ubb.scs.map.guisocialnetwork.domain.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator implements Validator<String> {
    @Override
    public void validate(String email) throws ValidationException {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        Matcher matcher = pattern.matcher(email);
        if(email.isEmpty() || !matcher.matches())
            throw new ValidationException("Emailul nu este valid!");
    }
}
