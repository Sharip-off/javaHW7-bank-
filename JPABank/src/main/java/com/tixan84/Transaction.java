package com.tixan84;

import javax.persistence.*;
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String transactionName;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    public Transaction() {
    }

    public Transaction(String transactionName) {
        this.transactionName = transactionName;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
