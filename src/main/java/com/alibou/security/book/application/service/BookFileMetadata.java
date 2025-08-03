package com.alibou.security.book.application.service;

import java.util.List;

/**
 * Extracted metadata from book file
 */
public class BookFileMetadata {
    private final String title;
    private final String author;
    private final String language;
    private final Integer pageCount;
    private final String isbn;
    private final String publisher;
    private final Integer publicationYear;
    private final String description;
    private final List<String> subjects;
    
    public BookFileMetadata(String title, String author, String language, Integer pageCount,
                           String isbn, String publisher, Integer publicationYear, 
                           String description, List<String> subjects) {
        this.title = title;
        this.author = author;
        this.language = language;
        this.pageCount = pageCount;
        this.isbn = isbn;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.description = description;
        this.subjects = subjects;
    }
    
    // Getters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getLanguage() { return language; }
    public Integer getPageCount() { return pageCount; }
    public String getIsbn() { return isbn; }
    public String getPublisher() { return publisher; }
    public Integer getPublicationYear() { return publicationYear; }
    public String getDescription() { return description; }
    public List<String> getSubjects() { return subjects; }
}
