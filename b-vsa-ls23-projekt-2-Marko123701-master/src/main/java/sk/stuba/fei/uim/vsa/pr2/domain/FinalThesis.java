package sk.stuba.fei.uim.vsa.pr2.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@NamedNativeQueries({
        @NamedNativeQuery(name = FinalThesis.FIND_ALL, query = "select * from FINALTHESIS", resultClass = FinalThesis.class)
})
@NamedQuery(name = FinalThesis.FIND_BY_TEACHER_ID, query = "SELECT t FROM FinalThesis t WHERE t.educator.id = :id")
public class FinalThesis implements Serializable {

    public static final String FIND_ALL = "FinalThesis.findAll";
    public static final String FIND_BY_ID = "FinalThesis.findById";
    public static final String FIND_BY_TEACHER_ID = "FinalThesis.findByTeacherId";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column()
    private String institute;

    @Column()
    private String department;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date publicationDate;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date submissionDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne()
    private Educator educator;

    @OneToOne
    private Student student;

    public String getRegistrationNumber() {
        return "FEI-" + this.getId();
    }
    }

