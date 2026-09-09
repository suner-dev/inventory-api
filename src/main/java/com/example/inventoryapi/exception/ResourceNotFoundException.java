package com.example.inventoryapi.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Class<?> resourceType, Long id) {
        super(resourceType.getSimpleName() + " with id " + id + " not found");
    }
}
