package de.ph1b.audiobook.persistence

import de.ph1b.audiobook.Book
import de.ph1b.audiobook.persistence.internals.InternalBookRegister
import rx.Observable
import rx.subjects.PublishSubject
import v
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Provides access to all books.

 * @author Paul Woitaschek
 */
@Singleton
class BookChest
@Inject
constructor(private val register: InternalBookRegister) {

    private val active: MutableList<Book> by lazy { register.activeBooks().toMutableList() }
    private val orphaned: MutableList<Book> by lazy { register.orphanedBooks().toMutableList() }

    private val added = PublishSubject.create<Book>()
    private val removed = PublishSubject.create<Book>()
    private val updated = PublishSubject.create<Book>()

    @Synchronized fun removedObservable(): Observable<Book> = removed.asObservable()

    @Synchronized fun addedObservable(): Observable<Book> = added.asObservable()

    @Synchronized fun updateObservable(): Observable<Book> = updated.asObservable()

    @Synchronized fun addBook(book: Book) {
        v { "addBook=${book.name}" }

        val bookWithId = register.addBook(book)
        active.add(bookWithId)
        added.onNext(bookWithId)
    }

    /**
     * All active books. We
     */
    val activeBooks: List<Book>
        get() = synchronized(this) { ArrayList(active) }

    fun bookById(id: Long) = activeBooks.firstOrNull { it.id == id }

    @Synchronized fun getOrphanedBooks(): List<Book> = ArrayList(orphaned)

    @Synchronized fun updateBook(book: Book, chaptersChanged: Boolean = false) {
        v { "updateBook=${book.name} with time ${book.time}" }

        val bookIterator = active.listIterator()
        while (bookIterator.hasNext()) {
            val next = bookIterator.next()
            if (book.id == next.id) {
                bookIterator.set(book)
                register.updateBook(book, chaptersChanged)
                break
            }
        }

        updated.onNext(book)
    }

    @Synchronized fun hideBook(book: Book) {
        v { "hideBook=${book.name}" }

        val iterator = active.listIterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.id == book.id) {
                iterator.remove()
                register.hideBook(book.id)
                break
            }
        }
        orphaned.add(book)
        removed.onNext(book)
    }

    @Synchronized fun revealBook(book: Book) {
        v { "Called revealBook=$book" }

        val orphanedBookIterator = orphaned.iterator()
        while (orphanedBookIterator.hasNext()) {
            if (orphanedBookIterator.next().id == book.id) {
                orphanedBookIterator.remove()
                register.revealBook(book.id)
                break
            }
        }
        active.add(book)
        added.onNext(book)
    }
}
