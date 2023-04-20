package irc.tula.tg.db.entity;

import lombok.Data;
/*
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.*;
*/

@Data
/*
@Entity
@Table(name = "USERS")
*/
//@Table(name = "USERS", schema = "APP")
public class User {
    /*
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic
    @Column(name = "nick")
    private String nick;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "status")
    private Integer status;

    @Basic
    @CreationTimestamp
    @Column(name = "last_seen", updatable = false)
    private Timestamp lastSeen;
*/
}
