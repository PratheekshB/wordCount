package com.demo.wordcount.exception;

import java.io.IOException;

public class UnsupportedFileTypeException extends IOException {

    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}