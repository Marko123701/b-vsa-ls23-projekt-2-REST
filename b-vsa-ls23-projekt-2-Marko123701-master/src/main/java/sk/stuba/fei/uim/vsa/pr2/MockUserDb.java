package sk.stuba.fei.uim.vsa.pr2;

import sk.stuba.fei.uim.vsa.pr2.User.User;
import sk.stuba.fei.uim.vsa.pr2.auth.Role;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MockUserDb {

    private List<User> users;

    public MockUserDb(){
        User user = new User("admin", BCryptService.hash("password"));
        user.setRole(Role.STUDENT);
        users = Arrays.asList(user);
    }

    public Optional<User> getUserByUsername(String username){
        return users.stream().filter(u -> Objects.equals(username, u.getName())).findFirst();
    }
}
