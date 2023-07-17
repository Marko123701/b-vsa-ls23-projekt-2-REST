package sk.stuba.fei.uim.vsa.pr2.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@NamedNativeQueries({
        @NamedNativeQuery(name = Student.FIND_ALL, query = "select * from STUDENT", resultClass = Student.class)
})
@NamedQuery( name = Student.FIND_BY_ID, query = "select p from Student p where p.id=:id")
public class Student implements Serializable {

    public static final String FIND_ALL = "Student.findAll";
    public static final String FIND_BY_ID = "Student.findById";

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
    private int year;

    @Column(nullable = false)
    private int term;

    @Column(nullable = false)
    private String programme;

    @OneToOne(mappedBy = "student")
    @JoinColumn(unique = true)
    private FinalThesis finalThesis;
}
