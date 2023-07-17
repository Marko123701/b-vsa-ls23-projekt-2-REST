package sk.stuba.fei.uim.vsa.pr2.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@NamedNativeQueries({
        @NamedNativeQuery(name = Educator.FIND_ALL, query = "select * from EDUCATOR", resultClass = Educator.class)
})
@NamedQuery( name = Educator.FIND_BY_ID, query = "select p from Educator p where p.id=:id")
public class Educator implements Serializable {

    public static final String FIND_ALL = "Educator.findAll";
    public static final String FIND_BY_ID = "Educator.findById";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long aisId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String institute;

    @Column(nullable = false)
    private String department;

    @OneToMany(mappedBy = "educator",orphanRemoval = true)
    private List<FinalThesis> finalTheses = new ArrayList<>();

    public void addFinalThesis(FinalThesis thesis) {
        this.finalTheses.add(thesis);
    }
}
