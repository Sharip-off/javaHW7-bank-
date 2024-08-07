package com.tixan84;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Account> accounts = new HashSet<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Transaction> transactions = new HashSet<>();

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    // Additional user control method
    public void addAccount(Account account) {
        if (!accounts.contains(account)) {
            accounts.add(account);
            account.setUser(this);
        }
    }

    public Account getAccountByName(String name) {
        for (Account account : accounts) {
            if (account.getName().equals(name)) {
                return account;
            }
        }
        return null;
    }

    //  For transaction
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setUser(this);
    }

    //    Standard getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions;
    }
}
