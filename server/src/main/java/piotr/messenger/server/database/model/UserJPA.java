package piotr.messenger.server.database.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import piotr.messenger.library.Constants;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Data
public class UserJPA implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = Constants.RECORD_LENGTH, nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime registeredAt;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime lastloggedAt;

    private int active;
}
