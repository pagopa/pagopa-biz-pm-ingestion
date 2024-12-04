package it.gov.pagopa.bizpmingestion.entity.pm;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "PP_USER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPUser {
    @Id
    @Column(name = "ID_USER", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FISCAL_CODE")
    private String fiscalCode;
    @Column(name = "SURNAME")
    private String surname;
    @Column(name = "NAME")
    private String name;
    @Column(name = "NOTIFICATION_EMAIL", nullable = false)
    private String notificationEmail;

    @Builder.Default
    @OneToMany(targetEntity = PPTransaction.class, fetch = FetchType.LAZY, mappedBy = "ppUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PPTransaction> ppTransaction = new ArrayList<>();

}
