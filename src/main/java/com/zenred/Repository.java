package com.zenred;

import java.io.Serializable;
import java.util.List;

import com.zenred.RepositoryException;

/**
 * Repository interface for type-safe CRUD operations
 * @param <PK>
 * @param <T>
 * 
 * @category interface
 * 
 */
public interface Repository <PK extends Serializable, T> {

	public void create(T obj) throws RepositoryException;
	
	public T read(PK id) throws RepositoryException;
	
	public void update(T obj) throws RepositoryException;
	
	public void delete(T obj) throws RepositoryException;
	
	public List<T> findAll() throws RepositoryException;
	
	
	
}
