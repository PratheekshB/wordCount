package com.demo.wordcount.exception;

import java.io.IOException;

public class FileSizeLimitExceededException extends IOException {

    public FileSizeLimitExceededException(String message) {
        super(message);
    }
}