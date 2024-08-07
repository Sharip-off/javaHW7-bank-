package com.tixan84;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {

    static EntityManagerFactory emf;
    static EntityManager em;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            // Create connection
            emf = Persistence.createEntityManagerFactory("JPABank");
            em = emf.createEntityManager();
            try {
                while (true) {
                    System.out.println("1: Create User");
                    System.out.println("2: Deposit");
                    System.out.println("3: Transfer money");
                    System.out.println("4: Convert");
                    System.out.println("5: Create/update currency rate");
                    System.out.println("6: View currency rates");
                    System.out.println("7: Get total amount in UAH");

                    System.out.println("Press enter to exit");
                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            createUser(sc);
                            break;
                        case "2":
                            deposit(sc);
                            break;
                        case "3":
                            transferMoney(sc);
                            break;
                        case "4":
                            convertMoney(sc);
                            break;
                        case "5":
                            currencyRate(sc);
                            break;
                        case "6":
                            viewRate();
                            break;
                        case "7":
                            getAllMoney(sc);
                            break;
                        default:
                            return;
                    }
                }
            } finally {
                em.close();
                emf.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void createUser(Scanner sc) {
        System.out.println("Input user name ->");
        String name = sc.nextLine();
        try {
            em.getTransaction().begin();
            User user = new User(name);
            Account uah = new Account("UAH");
            Account usd = new Account("USD");
            Account eur = new Account("EUR");
            user.addAccount(uah);
            user.addAccount(usd);
            user.addAccount(eur);

            em.persist(user);
            em.getTransaction().commit();

        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }

    public static void deposit(Scanner sc) {
        System.out.println("Input user name");
        String name = sc.nextLine();
        System.out.println("Input account's name(UAH,USD,EUR)");
        String accountName = sc.nextLine().toUpperCase();
        System.out.println("Input amount");
        String sAmount = sc.nextLine();
        double amount = Double.parseDouble(sAmount);

        try {
            em.getTransaction().begin();

//            Use jpql to get user by name
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.name = :name", User.class);
            query.setParameter("name", name);
            User user = query.getSingleResult();
//            Create and flush transaction
            Transaction tr = new Transaction("Deposit in " + accountName);
            user.addTransaction(tr);

//          Create a new account objet and set or update balance
            Account temp = user.getAccountByName(accountName);
            if (temp.getBalance() == null) {
                temp.setBalance(amount);
            } else {
                temp.setBalance(temp.getBalance() + amount);
            }

//          Update the new account in the database
            em.merge(user);
            em.merge(temp);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }

    public static void transferMoney(Scanner sc) {
        System.out.println("Input sender name");
        String from = sc.nextLine();
        System.out.println("Input recipient name");
        String to = sc.nextLine();
        System.out.println("Currency(UAH,USD,EUR)");
        String currency = sc.nextLine().toUpperCase();
        System.out.println("Input amount");
        String aAmount = sc.nextLine();
        double amount = Double.parseDouble(aAmount);
        try {
            em.getTransaction().begin();
//            Finding users to transfer money
            TypedQuery<User> queryFrom = em.createQuery("SELECT u FROM User u WHERE u.name=:name", User.class);
            queryFrom.setParameter("name", from);
            User fromUser = queryFrom.getSingleResult();
            TypedQuery<User> queryTo = em.createQuery("SELECT u FROM User u WHERE u.name=:name", User.class);
            queryTo.setParameter("name", to);
            User toUser = queryTo.getSingleResult();
//            Create transaction
            Transaction trSend = new Transaction("send " + currency + " to " + toUser.getName());
            Transaction trReceive = new Transaction("receive " + currency + " from " + fromUser.getName());
            fromUser.addTransaction(trSend);
            toUser.addTransaction(trReceive);
//            Finding users accounts
            Account fromAccount = fromUser.getAccountByName(currency);
            Account toAccount = toUser.getAccountByName(currency);
//            Transferring
            if (fromAccount.getBalance() < amount) {
                System.out.println("Money is not enough.");
            } else {
                fromAccount.setBalance(fromAccount.getBalance() - amount);
                if (toAccount.getBalance() == null) {
                    toAccount.setBalance(amount);
                } else {
                    toAccount.setBalance(toAccount.getBalance() + amount);
                }
            }
            em.merge(fromUser);
            em.merge(toUser);
            em.merge(fromAccount);
            em.merge(toAccount);
            em.getTransaction().commit();

        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }

    public static void convertMoney(Scanner sc) {
        System.out.println("Input user name");
        String name = sc.nextLine();
        System.out.println("What would you like to buy(USD, EUR)?");
        String currency = sc.nextLine().toUpperCase();
        System.out.println("How much UAH would you like convert to " + currency);
        double amount = Double.parseDouble(sc.nextLine());

        try {
            em.getTransaction().begin();

            TypedQuery<User> uQuery = em.createQuery("select u from User u where name=:name", User.class);
            uQuery.setParameter("name", name);
            User user = uQuery.getSingleResult();

            TypedQuery<Rate> query = em.createQuery("SELECT r FROM Rate r", Rate.class);
            List<Rate> rates = query.getResultList();


            for (Rate rate : rates) {
                if (rate.getName().equals(currency)) {
                    if (user.getAccountByName("UAH").getBalance() < amount || user.getAccountByName("UAH").getBalance() == null) {
                        System.out.println("Not enough money.");
                        return;
                    }
                    user.getAccountByName("UAH").setBalance(user.getAccountByName("UAH").getBalance() - amount);
                    if (user.getAccountByName(currency).getBalance() == null) {
                        user.getAccountByName(currency).setBalance(amount / rate.getRate());
                    } else {
                        user.getAccountByName(currency).setBalance(user.getAccountByName(currency).getBalance() + amount / rate.getRate());
                    }
                }
            }
            Transaction tr = new Transaction("Convert UAH to " + currency);
            user.addTransaction(tr);

            em.flush();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }


    public static void currencyRate(Scanner sc) {
        System.out.println("Enter the rate of USD");
        double usd = Double.parseDouble(sc.nextLine());
        System.out.println("Enter the rate of EUR");
        double eur = Double.parseDouble(sc.nextLine());
        Rate rateUSD;
        Rate rateEUR;

        try {
            em.getTransaction().begin();

            TypedQuery<Rate> query = em.createQuery("SELECT r FROM Rate r", Rate.class);
            List<Rate> rates = query.getResultList();

            if (rates.isEmpty()) {
                rateUSD = new Rate("USD", usd);
                rateEUR = new Rate("EUR", eur);
                em.merge(rateUSD);
                em.merge(rateEUR);
            }
            if (!rates.isEmpty()) {
                for (Rate r : rates) {
                    if (r.getName().equals("USD")) r.setRate(usd);
                    if (r.getName().equals("EUR")) r.setRate(eur);
                }
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }

    public static void viewRate() {
        TypedQuery<Rate> query = em.createQuery("SELECT r FROM Rate r", Rate.class);
        List<Rate> rates = query.getResultList();
        for (Rate r : rates) {
            System.out.println(r);
        }
    }

    public static void getAllMoney(Scanner sc) {
        System.out.println("Input the user name.");
        String name = sc.nextLine();

        TypedQuery<User> uQuery = em.createQuery("SELECT u FROM User u WHERE name=:name", User.class);
        uQuery.setParameter("name", name);
        User user = uQuery.getSingleResult();

        TypedQuery<Rate> rQuery = em.createQuery("SELECT r FROM Rate r", Rate.class);
        List<Rate> rates = rQuery.getResultList();

        double sum = user.getAccountByName("UAH").getBalance();

        Set<Account> accounts = user.getAccounts();
        for (Account a : accounts) {
            for (Rate r : rates) {
                if (a.getName().equals(r.getName())) {
                    sum = sum + a.getBalance() * r.getRate();
                }
            }
        }

        System.out.println(user.getName()+" total amount in UAH equals " + sum);

    }
}
