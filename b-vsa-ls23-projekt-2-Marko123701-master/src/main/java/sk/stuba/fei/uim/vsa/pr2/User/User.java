package sk.stuba.fei.uim.vsa.pr2.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.stuba.fei.uim.vsa.pr2.auth.Role;

import javax.persistence.*;
import java.io.Serializable;
import java.security.Principal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vsa_user")
public class User implements Principal, Serializable {
    private static final long serialVersionUID = 1546587565214595625L;

    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String email, String hash, Role role) {
        this.email = email;
        this.password = hash;
        this.role = role;
    }

    public String getName() {
        return email;
    }

    public void addRole(Role role){
            this.role = role;
    }
}
