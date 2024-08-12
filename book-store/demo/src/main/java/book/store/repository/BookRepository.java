package book.store.repository;

import book.store.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
    @Repository
    public interface BookRepository extends JpaRepository<Book, Long> {
    }


