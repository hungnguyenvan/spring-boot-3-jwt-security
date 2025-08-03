package com.alibou.security.core.domain.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base entity for all domain entities
 * Provides common audit fields and behaviors
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(nullable = false)
    protected LocalDateTime lastModifiedDate;
    
    @CreatedBy
    @Column(nullable = false, updatable = false)
    protected Integer createdBy;
    
    @LastModifiedBy
    @Column(nullable = false)
    protected Integer lastModifiedBy;
    
    @Column(nullable = false)
    protected Boolean active = true;
    
    // Domain events support
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    public Integer getId() {
        return id;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    public Integer getCreatedBy() {
        return createdBy;
    }
    
    public Integer getLastModifiedBy() {
        return lastModifiedBy;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // Common behaviors
    public void activate() {
        this.active = true;
        addDomainEvent(new EntityActivatedEvent(this));
    }
    
    public void deactivate() {
        this.active = false;
        addDomainEvent(new EntityDeactivatedEvent(this));
    }
    
    public boolean isActive() {
        return this.active;
    }
}

// Domain Event interfaces and implementations
interface DomainEvent {
    BaseEntity getSource();
    LocalDateTime getOccurredOn();
}

class EntityActivatedEvent implements DomainEvent {
    private final BaseEntity source;
    private final LocalDateTime occurredOn;
    
    public EntityActivatedEvent(BaseEntity source) {
        this.source = source;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public BaseEntity getSource() {
        return source;
    }
    
    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}

class EntityDeactivatedEvent implements DomainEvent {
    private final BaseEntity source;
    private final LocalDateTime occurredOn;
    
    public EntityDeactivatedEvent(BaseEntity source) {
        this.source = source;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public BaseEntity getSource() {
        return source;
    }
    
    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
