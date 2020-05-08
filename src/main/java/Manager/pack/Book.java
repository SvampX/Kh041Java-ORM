package Manager.pack;

import annotations.*;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "EntityName")
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue
    @Column(name = "id")
    int bookId;

    @Column(name = "book_name")
    String name;

    @Column(name = "author")
    String author;

    @ManyToMany(tableName = "user_book", inverseJoinColumnsName = "book_id", inverseJoinColumnsReferencedName = "id")
    Set<User> users = new HashSet<>();

    public Book() {
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
