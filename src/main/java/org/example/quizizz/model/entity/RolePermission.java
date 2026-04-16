package org.example.quizizz.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "role_permissions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_role_permissions_role_permission", columnNames = {"role_id", "permission_id"})
    },
    indexes = {
        @Index(name = "idx_role_permissions_role_id", columnList = "role_id"),
        @Index(name = "idx_role_permissions_permission_id", columnList = "permission_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "role_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_role_permissions_role_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Role role;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "permission_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_role_permissions_permission_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Permission permission;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
