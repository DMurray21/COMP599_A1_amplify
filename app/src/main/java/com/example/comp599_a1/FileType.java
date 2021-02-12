package com.example.comp599_a1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.net.URI;

public class FileType extends File {
    private StorageType type;

    public FileType(@NonNull String pathname, StorageType type) {
        super(pathname);
        this.type = type;
    }

    public FileType(@Nullable String parent, @NonNull String child, StorageType type) {
        super(parent, child);
        this.type = type;
    }

    public FileType(@Nullable File parent, @NonNull String child, StorageType type) {
        super(parent, child);
        this.type = type;
    }

    public FileType(@NonNull URI uri, StorageType type) {
        super(uri);
        this.type = type;
    }

    public StorageType getStorageType(){
        return this.getStorageType();
    }

    public void setStorageType(StorageType type){
        this.type = type;
    }
}

